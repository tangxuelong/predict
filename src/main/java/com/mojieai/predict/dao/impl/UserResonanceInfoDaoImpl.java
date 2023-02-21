package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserResonanceInfoDao;
import com.mojieai.predict.entity.po.UserResonanceInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserResonanceInfoDaoImpl extends BaseDao implements UserResonanceInfoDao {

    @Override
    public UserResonanceInfo getUserResonanceInfo(Long userId, long gameId, boolean isLock) {
        Map params = new HashMap<>();

        params.put("userId", userId);
        params.put("gameId", gameId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserResonanceInfo.getUserResonanceInfo", params);
    }

    @Override
    public Integer updateLastPeriod(Long userId, long gameId, Integer setPeriod, Integer originPeriod) {
        Map params = new HashMap();

        params.put("userId", userId);
        params.put("gameId", gameId);
        params.put("setPeriod", setPeriod);
        params.put("originPeriod", originPeriod);
        return sqlSessionTemplate.update("UserResonanceInfo.updateLastPeriod", params);
    }

    @Override
    public Integer insert(UserResonanceInfo userResonanceInfo) {
        return sqlSessionTemplate.insert("UserResonanceInfo.insert", userResonanceInfo);
    }


}
