package com.mojieai.predict.entity.bo;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.ReflectHelperUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.Field;
import java.util.Map;

public class AnalyzeActualSql {


    private MappedStatement mappedStatement;
    private Object parameterObject;
    private BoundSql boundSql;

    public AnalyzeActualSql(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        this.setMappedStatement(mappedStatement);
        this.setParameterObject(parameterObject);
        this.setBoundSql(boundSql);
    }

    public String getActualSql(String originalSql, TableShard tableShard) throws Exception {
        String newSql = null;
        originalSql = originalSql.toLowerCase();

        if (tableShard == null) {
            return newSql;
        }

        String tableName = tableShard != null ? tableShard.tableName().trim() : "";
        String shardType = tableShard != null ? tableShard.shardType().trim() : "";
        String shardBy = tableShard != null ? tableShard.shardBy().trim() : "";

        if (!shardType.startsWith(CommonConstant.PERCENT_SPLIT_STR)) {
            return newSql;
        }

        Long value;
        switch (shardBy) {
            case CommonConstant.SHARD_BY_GAME_ID://long
            case CommonConstant.SHARD_BY_USER_ID:
                value = getShardByLongValue(shardBy);
                break;
            //String
            case CommonConstant.SHARD_BY_USER_CODE:
            case CommonConstant.SHARD_BY_MOBILE:
            case CommonConstant.SHARD_BY_DEVICE_ID:
            case CommonConstant.SHARD_BY_TOKEN:
            case CommonConstant.SHARD_BY_OAUTH_ID:
            case CommonConstant.SHARD_BY_VIP_OPERATE_CODE:
            case CommonConstant.SHARD_BY_PERIOD_ID:
                String shardByStr = getShardByStrValue(shardBy);
                if (StringUtils.isNotBlank(shardByStr)) {
                    int shardByLength = shardByStr.length() - (shardType.length() - 2);
                    shardByLength = shardByLength <= 0 ? 0 : shardByLength;
                    String valueString = shardByStr.substring(shardByLength);
                    valueString = StringUtils.isBlank(valueString) ? "0" : valueString;
                    value = Long.valueOf(valueString);
                } else {
                    value = null;
                }
                break;
            default:
                throw new BusinessException("shardBy param is error");
        }

        // 分表100 第一个表为00
        String suffixStr = "";
        if (value != null) {
            long div = Long.parseLong(shardType.substring(1));
            long suffix = value % div;
            suffixStr = String.valueOf(suffix);
            if (suffixStr.length() < 2 && String.valueOf(div).length() > 2) {
                suffixStr = "0" + suffixStr;
            }
        }

        String[] tableNames = tableName.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMA_SPLIT_STR);
        for (String tabName : tableNames) {
            String newTableName = tabName;
            if (StringUtils.isNotBlank(suffixStr)) {
                newTableName = tabName + CommonConstant.COMMON_SPLIT_STR + suffixStr;
            }

            newSql = originalSql.replaceAll(tabName, newTableName);
        }
        return newSql;
    }

    public static String generateTableNameByPeriod(TableShard tableShard, String periodId) {
        String tableName = tableShard != null ? tableShard.tableName().trim() : "";
        String shardType = tableShard != null ? tableShard.shardType().trim() : "";
        long div = Long.parseLong(shardType.substring(1));
        long value = Long.parseLong(periodId);
        long suffix = value % div;
        String newTableName = tableName + CommonConstant.COMMON_SPLIT_STR + suffix;
        return newTableName;
    }

    private String getShardByStrValue(String shardBy) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Object parameterObject = boundSql.getParameterObject();
        String value = null;
        if (parameterObject != null) {
            if (parameterObject instanceof String) {
                value = (String) parameterObject;
            } else if (parameterObject instanceof Map<?, ?>) {
                value = (String) ((Map<?, ?>) parameterObject).get(shardBy);
            } else {
                Field valueField = ReflectHelperUtil.getFieldByFieldName(parameterObject, shardBy);
                if (valueField != null) {
                    value = (String) ReflectHelperUtil.getValueByFieldName(parameterObject, shardBy);
                }
            }
        }
        return value;
    }

    private Long getShardByLongValue(String shardBy) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Object parameterObject = boundSql.getParameterObject();
        Long value = null;
        if (parameterObject != null) {
            if (parameterObject instanceof Long) {
                value = (Long) parameterObject;
            } else if (parameterObject instanceof Map<?, ?>) {
                value = (Long) ((Map<?, ?>) parameterObject).get(shardBy);
            } else {
                Field valueField = ReflectHelperUtil.getFieldByFieldName(parameterObject, shardBy);
                if (valueField != null) {
                    value = (Long) ReflectHelperUtil.getValueByFieldName(parameterObject, shardBy);
                }
            }
        }
        return value;
    }

    /**
     * 取得shardBy字段的值
     * 注意：根据多个字段进行分库分表时，必须固定各个字段的顺序，这里的返回的取值也是按着shardBy本身的顺序
     * 这里按String类型
     */
    private String[] getShardByValues(String shardBy) throws Exception {
        String[] shardByArr = shardBy.split(",");
        String[] valueArr = new String[shardByArr.length];
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject != null) {
            for (int i = 0; i < shardByArr.length; i++) {
                String eachShardBy = shardByArr[i];
                if (parameterObject instanceof Map<?, ?>) {
                    valueArr[i] = (String) ((Map<?, ?>) parameterObject).get(eachShardBy);
                } else {
                    Field field = ReflectHelperUtil.getFieldByFieldName(parameterObject, eachShardBy);
                    if (field != null) {
                        valueArr[i] = (String) ReflectHelperUtil.getValueByFieldName(parameterObject, eachShardBy);
                    }
                }
            }
        }
        return valueArr;
    }

    public Object getParameterObject() {
        return parameterObject;
    }

    public void setParameterObject(Object parameterObject) {
        this.parameterObject = parameterObject;
    }

    public MappedStatement getMappedStatement() {
        return mappedStatement;
    }

    public void setMappedStatement(MappedStatement mappedStatement) {
        this.mappedStatement = mappedStatement;
    }

    public BoundSql getBoundSql() {
        return boundSql;
    }

    public void setBoundSql(BoundSql boundSql) {
        this.boundSql = boundSql;
    }
}
