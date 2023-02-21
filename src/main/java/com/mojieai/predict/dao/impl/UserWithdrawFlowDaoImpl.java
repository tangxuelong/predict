package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserWithdrawFlowDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserWithdrawFlow;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserWithdrawFlowDaoImpl extends BaseDao implements UserWithdrawFlowDao {

    @Override
    public UserWithdrawFlow getUserWithdrawFlowById(Long userId, String withdrawId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("withdrawId", withdrawId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserWithdrawFlow.getUserWithdrawFlowById", params);
    }

    @Override
    public PaginationList<UserWithdrawFlow> getUserWithdrawFlow(Long userId, Integer pageSize, Integer page) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        PaginationInfo paginationInfo = new PaginationInfo(page, pageSize);
        return selectPaginationList("UserWithdrawFlow.getUserWithdrawFlow", params, paginationInfo);
    }

    @Override
    public Long getUserWithdrawSumByTime(Long userId, Timestamp beginTime, Timestamp endTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        return sqlSessionTemplate.selectOne("UserWithdrawFlow.getUserWithdrawSumByTime", param);
    }

    @Override
    public Integer updateWithdrawFlowStatusAndRemark(Long userId, String withdrawId, Integer newStatus, Integer
            oldStatus, String remark, Boolean saveWithdrawTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("withdrawId", withdrawId);
        params.put("newStatus", newStatus);
        params.put("oldStatus", oldStatus);
        params.put("remark", remark);
        params.put("saveWithdrawTime", saveWithdrawTime);
        return sqlSessionTemplate.update("UserWithdrawFlow.updateWithdrawFlowStatus", params);
    }

    @Override
    public Integer updateWithdrawFlowStatus(Long userId, String withdrawId, Integer newStatus, Integer oldStatus,
                                            Boolean saveWithdrawTime) {
        return updateWithdrawFlowStatusAndRemark(userId, withdrawId, newStatus, oldStatus, null, saveWithdrawTime);
    }

    @Override
    public Integer insert(UserWithdrawFlow userWithdrawFlow) {
        return sqlSessionTemplate.insert("UserWithdrawFlow.insert", userWithdrawFlow);
    }

    @Override
    public List<UserWithdrawFlow> getAllWithdrawOrderByStatusFromOtter(Integer withdrawStatus) {
        Map<String, Object> param = new HashMap<>();
        param.put("withdrawStatus", withdrawStatus);
        return otterSqlSessionTemplate.selectList("UserWithdrawFlow.getAllWithdrawOrderByStatusFromOtter", param);
    }

    @Override
    public Long getUserWithdrawTotalAmountByOtter(Timestamp beginTimeT, Timestamp endTimeT) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginTime", beginTimeT);
        param.put("endTime", endTimeT);
        return otterSqlSessionTemplate.selectOne("UserWithdrawFlow.getUserWithdrawTotalAmountByOtter", param);
    }
}
