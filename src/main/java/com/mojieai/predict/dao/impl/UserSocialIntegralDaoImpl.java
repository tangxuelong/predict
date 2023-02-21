package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSocialIntegralDao;
import com.mojieai.predict.entity.po.UserSocialIntegral;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserSocialIntegralDaoImpl extends BaseDao implements UserSocialIntegralDao {

    @Override
    public UserSocialIntegral getUserSocialIntegralByUserId(long gameId, Long userId, Boolean isLock) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserSocialIntegral.getUserSocialIntegralByUserId", params);
    }

    @Override
    public Integer insert(UserSocialIntegral userSocialIntegral) {
        return sqlSessionTemplate.insert("UserSocialIntegral.insert", userSocialIntegral);
    }

    @Override
    public Integer updateUserScore(long gameId, Long userScore, Long userId) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("userScore", userScore);
        return sqlSessionTemplate.update("UserSocialIntegral.updateUserScore", params);
    }
}
