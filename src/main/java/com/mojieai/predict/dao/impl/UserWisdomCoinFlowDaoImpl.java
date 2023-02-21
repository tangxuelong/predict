package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserWisdomCoinFlowDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserWisdomCoinFlow;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserWisdomCoinFlowDaoImpl extends BaseDao implements UserWisdomCoinFlowDao {

    public PaginationList<UserWisdomCoinFlow> getUserWisdomCoinFlowsByPage(Long userId, Integer pageSize, Integer
            page) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        PaginationInfo paginationInfo = new PaginationInfo(page, pageSize);
        return selectPaginationList("UserWisdomCoinFlow.getUserWisdomCoinFlowsByPage", params, paginationInfo);
    }

    @Override
    public UserWisdomCoinFlow getUserWisdomCoinFlowByFlowId(String flowId, Long userId) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        params.put("flowId", flowId);
        return sqlSessionTemplate.selectOne("UserWisdomCoinFlow.getUserWisdomCoinFlowByFlowId", params);
    }

    @Override
    public Integer insert(UserWisdomCoinFlow userWisdomCoinFlow) {
        return sqlSessionTemplate.insert("UserWisdomCoinFlow.insert", userWisdomCoinFlow);
    }

    @Override
    public Integer updateUserWisdomFlowIsPay(String flowId, Long userId, Integer isPay) {
        Map params = new HashMap();
        params.put("flowId", flowId);
        params.put("userId", userId);
        params.put("isPay", isPay);
        return sqlSessionTemplate.update("UserWisdomCoinFlow.updateUserWisdomFlowIsPay", params);
    }

    @Override
    public Integer saveUserWisdomCoinAccountFlowId(String flowId, Long userId, String exchangeFlowId) {
        Map params = new HashMap();
        params.put("flowId", flowId);
        params.put("userId", userId);
        params.put("exchangeFlowId", exchangeFlowId);
        return sqlSessionTemplate.update("UserWisdomCoinFlow.saveUserWisdomCoinAccountFlowId", params);
    }

    @Override
    public Long getUserWisdomCoinFlowSumByStatusByOtter(Timestamp beginTime, Timestamp endTime, Integer exchangeType) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        param.put("exchangeType", exchangeType);
        return otterSqlSessionTemplate.selectOne("UserWisdomCoinFlow.getUserWisdomCoinFlowSumByStatusByOtter", param);
    }

}
