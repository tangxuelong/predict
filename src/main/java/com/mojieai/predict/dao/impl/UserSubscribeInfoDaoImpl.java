package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSubscribeInfoDao;
import com.mojieai.predict.entity.po.UserSubscribeInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserSubscribeInfoDaoImpl extends BaseDao implements UserSubscribeInfoDao {

    @Override
    public UserSubscribeInfo getUserSubscribeInfoByPk(Long userId, Integer predictType, long gameId, boolean isLock) {
        Map params = new HashMap<>();
        params.put("userId", userId);
        params.put("predictType", predictType);
        params.put("gameId", gameId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserSubscribeInfo.getUserSubscribeInfoByPk", params);
    }

    @Override
    public Integer getUserSubscribeProgramCount(long gameId, Long userId, Integer programType) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("programType", programType);
        return sqlSessionTemplate.selectOne("UserSubscribeInfo.getUserSubscribeProgramCount", params);
    }

    @Override
    public Integer insert(UserSubscribeInfo userSubscribeInfo) {
        return sqlSessionTemplate.insert("UserSubscribeInfo.insert", userSubscribeInfo);
    }

    @Override
    public int updatePeriodIdByPk(Long userId, Integer predictType, long gameId, Integer setPeriodId, Integer
            originPeriodId) {
        Map params = new HashMap();
        params.put("userId", userId);
        params.put("predictType", predictType);
        params.put("setPeriodId", setPeriodId);
        params.put("originPeriodId", originPeriodId);
        params.put("gameId", gameId);
        return sqlSessionTemplate.update("UserSubscribeInfo.updatePeriodIdByPk", params);
    }
}
