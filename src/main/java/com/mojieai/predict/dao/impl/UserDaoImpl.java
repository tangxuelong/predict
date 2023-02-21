package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserDao;
import com.mojieai.predict.entity.po.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserDaoImpl extends BaseDao implements UserDao {
    @Override
    public User getUserByUserId(Long userId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("User.getUserByUserId", params);
    }

    @Override
    public User getUserByMobileFromOtter(String mobile) {
        Map<String, Object> param = new HashMap<>();
        param.put("mobile", mobile);
        return otterSqlSessionTemplate.selectOne("User.getUserByMobileFromOtter", param);
    }

    @Override
    public int update(User user) {
        return sqlSessionTemplate.insert("User.update", user);
    }

    @Override
    public void insert(User user) {
        sqlSessionTemplate.insert("User.insert", user);
    }
}