package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSignRewardDao;
import com.mojieai.predict.entity.po.UserSignReward;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserSignRewardDaoImpl extends BaseDao implements UserSignRewardDao {

    @Override
    public List<UserSignReward> getAllSignReward() {
        return sqlSessionTemplate.selectList("UserSignReward.getAllSignReward");
    }

    @Override
    public Integer insert(UserSignReward userSignReward) {
        return sqlSessionTemplate.insert("UserSignReward.insert", userSignReward);
    }
}
