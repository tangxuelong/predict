package com.mojieai.predict.interceptor;

import com.mojieai.predict.dialect.Dialect;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.ReflectHelperUtil;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PaginationInterceptor implements Interceptor {
    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();
    private Dialect dialect = null;
    private String paginationSqlRegEx = "ByPage";//.*ByPage
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // this.debug("intercept");
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY,
                DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        // 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环可以分离出最原始的的目标类)
        while (metaStatementHandler.hasGetter("h")) {
            Object object = metaStatementHandler.getValue("h");
            metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY,
                    DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        }
        while (metaStatementHandler.hasGetter("target")) {
            Object object = metaStatementHandler.getValue("target");
            metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY,
                    DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        }
        if (metaStatementHandler.getOriginalObject() instanceof RoutingStatementHandler) {
            BaseStatementHandler delegate = (BaseStatementHandler) metaStatementHandler.getValue("delegate");
            MappedStatement mappedStatement = (MappedStatement) ReflectHelperUtil.getValueByFieldName(delegate,
                    "mappedStatement");

            if (mappedStatement.getId().endsWith(paginationSqlRegEx)) {
                BoundSql boundSql = delegate.getBoundSql();
                String sql = boundSql.getSql();
                Object parameterObject = boundSql.getParameterObject();
                if (parameterObject != null) {
                    int count = calcCount(invocation, mappedStatement, boundSql, parameterObject, sql);
                    processBoundSqlAndPaginationInfo(parameterObject, boundSql, sql, count);
                }
            }
        }
        return invocation.proceed();
    }

    private void processBoundSqlAndPaginationInfo(Object parameterObject, BoundSql boundSql, String sql, int count)
            throws NoSuchFieldException, IllegalAccessException {
        PaginationInfo paginationInfo;
        if (parameterObject instanceof Map<?, ?>) {
            paginationInfo = (PaginationInfo) ((Map<?, ?>) parameterObject).get("paginationInfo");
            if (paginationInfo == null) {
                paginationInfo = new PaginationInfo(1, count);
            }
        } else {
            Field pageField = ReflectHelperUtil.getFieldByFieldName(parameterObject, "paginationInfo");
            if (pageField != null) {
                paginationInfo = (PaginationInfo) ReflectHelperUtil.getValueByFieldName(parameterObject,
                        "paginationInfo");
                if (paginationInfo == null) {
                    paginationInfo = new PaginationInfo();
                }
                ReflectHelperUtil.setValueByFieldName(parameterObject, "paginationInfo", paginationInfo);
            } else {
                throw new NoSuchFieldException(parameterObject.getClass().getName()
                        + "不存在 PaginationInfo 属性");
            }
        }
        paginationInfo.setTotalRecord(count);
        paginationInfo.setTotalPage(((count - 1) / paginationInfo.getRecordPerPage()) + 1);
        String paginationSql = this.dialect.getLimitString(sql, paginationInfo.getOffset(),
                paginationInfo.getLimit());
        ReflectHelperUtil.setValueByFieldName(boundSql, "sql", paginationSql);
    }

    private int calcCount(Invocation invocation, MappedStatement mappedStatement, BoundSql boundSql, Object
            parameterObject, String sql) {
        int count = 0;
        Connection connection;
        PreparedStatement countStmt = null;
        ResultSet rs = null;
        try {
            connection = (Connection) invocation.getArgs()[0];
            String countSql = this.dialect.getCountString(sql);
            countStmt = connection.prepareStatement(countSql);
            BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(), countSql,
                    boundSql.getParameterMappings(), parameterObject);
            setParameters(countStmt, mappedStatement, countBS, parameterObject);
            rs = countStmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            throw new BusinessException(e);
        } finally {
            try {
                rs.close();
                countStmt.close();
            } catch (Exception e) {
                throw new BusinessException(e);
            }
        }
        return count;
    }

    private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql,
                               Object parameterObject) throws SQLException {
        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                    if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)
                            && boundSql.hasAdditionalParameter(prop.getName())) {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null) {
                            value = configuration.newMetaObject(value).getValue(
                                    propertyName.substring(prop.getName().length()));
                        }
                    } else {
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null) {
                        throw new ExecutorException("There was no TypeHandler found for parameter " + propertyName
                                + " of statement " + mappedStatement.getId());
                    }
                    typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
                }
            }
        }
    }

    @Override
    public Object plugin(Object arg0) {
        return Plugin.wrap(arg0, this);
    }

    @Override
    public void setProperties(Properties p) {
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public String getPaginationSqlRegEx() {
        return paginationSqlRegEx;
    }

    public void setPaginationSqlRegEx(String paginationSqlRegEx) {
        this.paginationSqlRegEx = paginationSqlRegEx;
    }
}
