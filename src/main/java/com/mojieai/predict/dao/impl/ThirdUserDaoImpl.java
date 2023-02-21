package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.ThirdUserDao;
import com.mojieai.predict.entity.po.ThirdUser;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ThirdUserDaoImpl extends BaseDao implements ThirdUserDao {

    @Override
    public Long getUserIdByThird(String oauthId, Integer oauthType) {
        Map<String, Object> params = new HashMap<>();
        params.put("oauthId", oauthId);
        params.put("oauthType", oauthType);
        return sqlSessionTemplate.selectOne("ThirdUser.getUserIdByThird", params);
    }

    @Override
    public void insert(ThirdUser thirdUser) {
        sqlSessionTemplate.insert("ThirdUser.insert", thirdUser);
    }
}