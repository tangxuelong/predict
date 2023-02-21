package com.mojieai.predict.dao;

import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class BaseDao {

    @Autowired
    public SqlSessionTemplate sqlSessionTemplate;
    @Autowired
    public SqlSessionTemplate slaveSqlSessionTemplate;
    @Autowired
    public SqlSessionTemplate otterSqlSessionTemplate;

    @SuppressWarnings("unchecked")
    /*
     * parameter 如果是一个非map对象，里面必须包含PaginationInfo属性或者就是PaginationInfo对象
	 */
    public PaginationList selectPaginationList(String statement, Object parameter, PaginationInfo paginationInfo) {
        PaginationList paginationList = new PaginationList();

        if (parameter == null) {
            throw new RuntimeException("parameter can not be null");
        }
        if (parameter instanceof Map<?, ?>) {
            ((Map) parameter).put("paginationInfo", paginationInfo);
        }
        List result = sqlSessionTemplate.selectList(statement, parameter);

        paginationList.addAll(result);
        if (paginationInfo == null) {
            paginationInfo = new PaginationInfo(1, result.size(), 1, result.size());
        }
        paginationList.setPaginationInfo(paginationInfo);

        return paginationList;
    }

    public PaginationList selectPaginationList(String statement,
                                               Object parameter) {
        return selectPaginationList(statement, parameter, null);
    }

    public PaginationList selectPaginationList(String statement,
                                               PaginationInfo paginationInfo) {
        return selectPaginationList(statement, new HashMap(), paginationInfo);
    }
}
