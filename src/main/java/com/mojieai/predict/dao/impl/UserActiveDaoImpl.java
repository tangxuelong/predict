package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserActiveDao;
import com.mojieai.predict.entity.po.UserActive;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserActiveDaoImpl extends BaseDao implements UserActiveDao {
    @Override
    public UserActive getUserActive(Long userId, Integer activeDate) {

        Map params = new HashMap<>();
        params.put("userId", userId);
        params.put("activeDate", activeDate);
        return sqlSessionTemplate.selectOne("UserActive.getUserActive", params);
    }

    @Override
    public Integer getCountUserActive(Integer activeDate) {
        Map params = new HashMap<>();
        params.put("activeDate", activeDate);
        return sqlSessionTemplate.selectOne("UserActive.getCountUserActive", params);
    }

    @Override
    public List<UserActive> getActiveUsers(Integer activeDate) {
        Map params = new HashMap<>();
        params.put("activeDate", activeDate);
        return sqlSessionTemplate.selectList("UserActive.getActiveUsers", params);
    }

    @Override
    public Integer insert(UserActive userActive) {
        return sqlSessionTemplate.insert("UserActive.insert", userActive);
    }
}
