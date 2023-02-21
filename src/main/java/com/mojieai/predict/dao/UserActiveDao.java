package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserActive;

import java.util.List;

public interface UserActiveDao {

    UserActive getUserActive(Long userId, Integer activeDate);

    Integer getCountUserActive(Integer activeDate);

    List<UserActive> getActiveUsers(Integer activeDate);

    Integer insert(UserActive userActive);

}
