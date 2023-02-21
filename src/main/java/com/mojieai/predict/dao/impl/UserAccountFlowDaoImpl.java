package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserAccountFlowDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserAccountFlow;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserAccountFlowDaoImpl extends BaseDao implements UserAccountFlowDao {

    @Override
    public UserAccountFlow getUserFlowByShardType(String flowId, Long userIdSuffix, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("flowId", flowId);
        params.put("userId", userIdSuffix);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserAccountFlow.getUserFlowByShardType", params);
    }

    @Override
    public UserAccountFlow getUserFlowCheck(String payId, Integer payType, Long userId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("payId", payId);
        params.put("payType", payType);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserAccountFlow.getUserFlowCheck", params);
    }

    @Override
    public List<Map> getSumAmountByChannelId(Long userId, Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        return sqlSessionTemplate.selectList("UserAccountFlow.getSumAmountByChannelId", params);
    }

    @Override
    public Integer countUserFlow(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return sqlSessionTemplate.selectOne("UserAccountFlow.countUserFlow", params);
    }

    @Override
    public Integer sumAllCashFlow() {
        return otterSqlSessionTemplate.selectOne("UserAccountFlow.sumAllCashFlow");
    }

    @Override
    public Integer maxDayCashFlow(Timestamp beginDate, Timestamp endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginDate", beginDate);
        params.put("endDate", endDate);
        return otterSqlSessionTemplate.selectOne("UserAccountFlow.maxDayCashFlow", params);
    }

    @Override
    public PaginationList<UserAccountFlow> getUserFlowListByPage(Long userId, Integer payType, Integer page, Integer
            pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("payType", payType);
        PaginationInfo paginationInfo = new PaginationInfo(page, pageSize);
        return selectPaginationList("UserAccountFlow.getUserFlowListByPage", params, paginationInfo);
    }

    @Override
    public void update(UserAccountFlow userAccountFlow) {
        sqlSessionTemplate.update("UserAccountFlow.update", userAccountFlow);
    }

    @Override
    public Integer updateFlowStatus(Long userId, String flowId, Integer setStatus, Integer oldStatus) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("flowId", flowId);
        param.put("setStatus", setStatus);
        param.put("oldStatus", oldStatus);
        return sqlSessionTemplate.update("UserAccountFlow.updateFlowStatus", param);
    }

    @Override
    public Integer insert(UserAccountFlow userAccountFlow) {
        return sqlSessionTemplate.insert("UserAccountFlow.insert", userAccountFlow);
    }

    @Override
    public Long getUserAllMoney(Long userId, Timestamp createTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("createTime", createTime);
        return sqlSessionTemplate.selectOne("UserAccountFlow.getUserAllMoney", params);
    }

    @Override
    public Long getTestUserMoney(Long userId, Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        return sqlSessionTemplate.selectOne("UserAccountFlow.getTestUserMoney", params);
    }

    @Override
    public Integer getUserRecentAccountFlow(Long userId, Integer payType, Integer payStatus) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("payType", payType);
        param.put("payStatus", payStatus);
        return sqlSessionTemplate.selectOne("UserAccountFlow.getUserRecentAccountFlow", param);
    }

    @Override
    public List<UserAccountFlow> getTestUserFlow(Long userId, Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        return sqlSessionTemplate.selectList("UserAccountFlow.getTestUserFlow", params);
    }

    @Override
    public List<UserAccountFlow> getCashUserFlowByStatus(Long userPrx, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userPrx);
        params.put("status", status);
        return sqlSessionTemplate.selectList("UserAccountFlow.getCashUserFlowByStatus", params);
    }

    @Override
    public List<UserAccountFlow> getUserFlowByDate(Long userPrx, Timestamp startTime, Timestamp endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userPrx);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        return sqlSessionTemplate.selectList("UserAccountFlow.getUserFlowByDate", params);
    }

    @Override
    public List<UserAccountFlow> getUserFlowByPayType(Long userIdSuffix, Integer payType, Integer status, Timestamp
            beginTime, Timestamp endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userIdSuffix);
        params.put("payType", payType);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("status", status);
        return sqlSessionTemplate.selectList("UserAccountFlow.getUserFlowByPayType", params);
    }

    @Override
    public Integer getUserFlowCountByUserIdAndTime(Long userId, Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        return sqlSessionTemplate.selectOne("UserAccountFlow.getUserFlowCountByUserIdAndTime", params);
    }

    @Override
    public List<UserAccountFlow> getAllCashFlowFromOtter(Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        return otterSqlSessionTemplate.selectList("UserAccountFlow.getAllCashFlowFromOtter", param);
    }

    @Override
    public Integer getPayPersonCountFromOtter(Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        return otterSqlSessionTemplate.selectOne("UserAccountFlow.getPayPersonCountFromOtter", param);
    }

    @Override
    public Long getRepurchaseAmountFromOtter(Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        return otterSqlSessionTemplate.selectOne("UserAccountFlow.getRepurchaseAmountFromOtter", param);
    }

    @Override
    public Map<String, Object> getOrderNumAndAmountFromOtter(Timestamp beginTime, Timestamp endTime, Integer payType) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        param.put("payType", payType);
        return otterSqlSessionTemplate.selectOne("UserAccountFlow.getOrderNumAndAmountFromOtter", param);
    }

    @Override
    public Map<String, Object> getOldUserOrderFromOtter(Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        return otterSqlSessionTemplate.selectOne("UserAccountFlow.getOldUserOrderFromOtter", param);
    }

    @Override
    public Integer getCountCashFlowFromOtterByDate(Timestamp beginDate, Timestamp endDate) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginDate", beginDate);
        param.put("endDate", endDate);
        return otterSqlSessionTemplate.selectOne("UserAccountFlow.getCountCashFlowFromOtterByDate", param);
    }
}
