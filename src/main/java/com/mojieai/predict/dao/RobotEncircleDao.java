package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.RobotEncircle;

public interface RobotEncircleDao {

    RobotEncircle getRobotEncircleById(long gameId, Integer robootId, String period);

    Integer robotKillNumSuccessUpdateInfo(long gameId, String periodId, Integer robotId);

    Integer robotEncircleNumSuccessUpdateInfo(long gameId, String periodId, Integer robotId);

    int deleteByPrimaryKey(RobotEncircle key);

    int insert(RobotEncircle record);

    int insertSelective(RobotEncircle record);

    RobotEncircle selectByPrimaryKey(RobotEncircle key);

    int updateByPrimaryKeySelective(RobotEncircle record);

}