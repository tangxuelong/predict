package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.MobileUserDao;
import com.mojieai.predict.entity.po.MobileUser;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class MobileUserDaoImpl extends BaseDao implements MobileUserDao {
    @Override
    public void insert(MobileUser mobileUser) {
        sqlSessionTemplate.insert("MobileUser.insert", mobileUser);
    }

    @Override
    public Long getUserIdByMobile(String mobile) {
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", mobile);
        return sqlSessionTemplate.selectOne("MobileUser.getUserIdByMobile", params);
    }
}