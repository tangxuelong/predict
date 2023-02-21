package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.RobotEncircle;
import com.mojieai.predict.entity.po.SocialEncircle;

import java.util.List;

public interface SocialRobotService {

    void killUserEncircleCodeByRobot();

    void robotKillNum(Integer robotId, Long robotUserId, SocialEncircle socialEncircle, Integer requireCount,
                      List<String> robotCanKillCodes);

    RobotEncircle initRobotEncircleInfo(Long gameId, String periodId, Integer robotId);
}
