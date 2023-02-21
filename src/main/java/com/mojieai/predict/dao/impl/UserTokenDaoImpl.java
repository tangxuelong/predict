package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserTokenDao;
import com.mojieai.predict.entity.po.UserToken;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserTokenDaoImpl extends BaseDao implements UserTokenDao {
    @Override
    public UserToken getTokenByUserIdByShardType(Long userId, String tokenSuffix) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("token", tokenSuffix);
        return sqlSessionTemplate.selectOne("UserToken.getTokenByUserIdByShardType", params);
    }

    @Override
    public Long getUserIdByToken(String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("token", token);
        return sqlSessionTemplate.selectOne("UserToken.getUserIdByToken", params);
    }

    @Override
    public UserToken getUserTokenByToken(String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("token", token);
        return sqlSessionTemplate.selectOne("UserToken.getUserTokenByToken", params);
    }

    @Override
    public int updateExpireTime(Long userId, String token, Timestamp oldExpireTime, Timestamp newExpireTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("token", token);
        param.put("oldExpireTime", oldExpireTime);
        param.put("newExpireTime", newExpireTime);
        return sqlSessionTemplate.update("UserToken.updateExpireTime", param);
    }

    @Override
    public void insert(UserToken userToken) {
        sqlSessionTemplate.insert("UserToken.insert", userToken);
    }
}