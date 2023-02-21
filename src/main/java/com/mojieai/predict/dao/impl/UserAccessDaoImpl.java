package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserAccessDao;
import com.mojieai.predict.entity.po.UserAccess;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserAccessDaoImpl extends BaseDao implements UserAccessDao {
    @Override
    public UserAccess getUserAccess(Long userId, String periodId, Long gameId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("periodId", periodId);
        params.put("gameId", gameId);
        return sqlSessionTemplate.selectOne("UserAccess.getUserAccess", params);

    }

    @Override
    public void insert(UserAccess userAccess) {
        sqlSessionTemplate.insert("UserAccess.insert", userAccess);
    }

    @Override
    public void update(UserAccess userAccess) {
        sqlSessionTemplate.update("UserAccess.update", userAccess);
    }
}
