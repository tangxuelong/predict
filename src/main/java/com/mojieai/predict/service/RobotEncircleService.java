package com.mojieai.predict.service;

import java.util.Map;

public interface RobotEncircleService {

    Map<String, Object> registerRobot(Integer count, Integer robotType);

    Map<String, Object> modifyRobotName(Integer robotType);

    Boolean sportRobotRecommend(Integer robotId, Integer matchId);

    void sportRobotAddRecommendTiming();

    Boolean divideRobotRecommend(Integer robotType);
}
