package com.mojieai.predict.thread;

import com.mojieai.predict.entity.po.RobotEncircle;
import com.mojieai.predict.service.RobotEncircleService;

import java.util.concurrent.Callable;

public class SportRobotTask implements Callable {
    private Integer robotId;
    private Integer matchId;
    private RobotEncircleService robotEncircleService;

    public SportRobotTask(Integer robotId, Integer matchId, RobotEncircleService robotEncircleService) {
        this.robotId = robotId;
        this.matchId = matchId;
        this.robotEncircleService = robotEncircleService;
    }

    @Override
    public Object call() throws Exception {
        return robotEncircleService.sportRobotRecommend(robotId, matchId);
    }
}
