package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserResonanceLogDao;
import com.mojieai.predict.entity.po.UserResonanceLog;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserResonanceLogDaoImpl extends BaseDao implements UserResonanceLogDao {

    @Override
    public UserResonanceLog getUserResonanceLogByPk(Long userId, String resonanceLogId) {
        Map params = new HashMap<>();

        params.put("userId", userId);
        params.put("resonanceLogId", resonanceLogId);
        return sqlSessionTemplate.selectOne("UserResonanceLog.getUserResonanceLogByPk", params);
    }

    @Override
    public UserResonanceLog getUserResonanceLogByUnique(Long userId, long gameId, Integer startPeriod, Integer
            lastPeriod, Long amount) {
        Map params = new HashMap();

        params.put("userId", userId);
        params.put("gameId", gameId);
        params.put("startPeriod", startPeriod);
        params.put("lastPeriod", lastPeriod);
        params.put("amount", amount);
        return sqlSessionTemplate.selectOne("UserResonanceLog.getUserResonanceLogByUnique", params);
    }

    @Override
    public UserResonanceLog getRepeatUserResonanceLog(Long userId, Long gameId, Long amount, Integer beginPeriod) {
        Map params = new HashMap();

        params.put("userId", userId);
        params.put("gameId", gameId);
        params.put("beginPeriod", beginPeriod);
        params.put("amount", amount);
        return sqlSessionTemplate.selectOne("UserResonanceLog.getRepeatUserResonanceLog", params);
    }

    @Override
    public Integer updateUserPayStatus(String resonanceLogId, Integer setPayStatus, Long userId) {
        Map params = new HashMap();

        params.put("resonanceLogId", resonanceLogId);
        params.put("setPayStatus", setPayStatus);
        params.put("userId", userId);
        return sqlSessionTemplate.update("UserResonanceLog.updateUserPayStatus", params);
    }

    @Override
    public Integer updateUserResonanceLogStatus(Long userId, String resonanceLogId, Integer setStatus, Integer
            originStatus) {
        Map params = new HashMap();

        params.put("userId", userId);
        params.put("resonanceLogId", resonanceLogId);
        params.put("setStatus", setStatus);
        params.put("originStatus", originStatus);
        return sqlSessionTemplate.update("UserResonanceLog.updateUserResonanceLogStatus", params);
    }

    @Override
    public Integer insert(UserResonanceLog userResonanceLog) {
        return sqlSessionTemplate.insert("UserResonanceLog.insert", userResonanceLog);
    }

}
