package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserSignReward;

import java.util.List;

public interface UserSignRewardDao {
    List<UserSignReward> getAllSignReward();

    Integer insert(UserSignReward userSignReward);
}
