package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSubscribeLogDao;
import com.mojieai.predict.entity.po.UserSubscribeLog;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserSubscribeLogDaoImpl extends BaseDao implements UserSubscribeLogDao {

    @Override
    public UserSubscribeLog getUserSubscribeLogByPk(String subscribeId, Long userId) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        params.put("subscribeId", subscribeId);
        return sqlSessionTemplate.selectOne("UserSubscribeLog.getUserSubscribeLogByPk", params);
    }

    @Override
    public UserSubscribeLog getUserSubScribeLogByUniqueKey(Long userId, Integer programId, Long programAmount, Integer
            beginPeriod, Integer endPeriod) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        params.put("programId", programId);
        params.put("programAmount", programAmount);
        params.put("beginPeriod", beginPeriod);
        params.put("endPeriod", endPeriod);
        return sqlSessionTemplate.selectOne("UserSubscribeLog.getUserSubScribeLogByUniqueKey", params);
    }

    @Override
    public UserSubscribeLog getRepeatUserSubscribeLog(Long userId, Integer programId, Long programAmount, Integer
            beginPeriod) {
        Map params = new HashMap();
        params.put("userId", userId);
        params.put("programId", programId);
        params.put("programAmount", programAmount);
        params.put("beginPeriod", beginPeriod);
        return sqlSessionTemplate.selectOne("UserSubscribeLog.getRepeatUserSubscribeLog", params);
    }

    @Override
    public Integer insert(UserSubscribeLog userSubscribeLog) {
        return sqlSessionTemplate.insert("UserSubscribeLog.insert", userSubscribeLog);
    }

    @Override
    public Integer updateUserSubscribeLogStatus(String subscribeId, Long userId, Integer setStatus, Integer
            originStatus) {
        Map params = new HashMap();
        params.put("userId", userId);
        params.put("subscribeId", subscribeId);
        params.put("setStatus", setStatus);
        params.put("originStatus", originStatus);
        return sqlSessionTemplate.update("UserSubscribeLog.updateUserSubscribeLogStatus", params);
    }

    @Override
    public Integer updateUserSubscribeLogPayStatus(String subscribeId, Long userId, Integer setPayStatus, Integer
            originPayStatus) {
        Map params = new HashMap();
        params.put("userId", userId);
        params.put("subscribeId", subscribeId);
        params.put("setPayStatus", setPayStatus);
        params.put("originPayStatus", originPayStatus);
        return sqlSessionTemplate.update("UserSubscribeLog.updateUserSubscribeLogPayStatus", params);
    }
}
