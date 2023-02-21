package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialRobot;

import java.util.List;

public interface SocialRobotDao {

    List<SocialRobot> getAllSocialRobot(Integer isEnable, Integer robotType);

    List<Long> getAllRobotUserIds();

    int insert(SocialRobot record);

    int insertSelective(SocialRobot record);

    SocialRobot selectByPrimaryKey(Integer robotId);

    int updateByPrimaryKeySelective(SocialRobot record);
}