package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.service.UserSocialTaskAwardService;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class SocialTaskAwardTask implements Callable {
    private static final Logger log = LogConstant.commonLog;

    private Long gameId;
    private String periodId;
    private Long userId;
    private Integer taskType;
    private String clientIp;
    private Integer clientId;
    private UserSocialTaskAwardService userSocialTaskAwardService;

    public SocialTaskAwardTask(Long gameId, String periodId, Long userId, Integer taskType, String clientIp, Integer
            clientId, UserSocialTaskAwardService userSocialTaskAwardService) {
        this.clientId = clientId;
        this.clientIp = clientIp;
        this.gameId = gameId;
        this.periodId = periodId;
        this.taskType = taskType;
        this.userId = userId;
        this.userSocialTaskAwardService = userSocialTaskAwardService;
    }

    @Override
    public Object call() throws Exception {
        try {
            userSocialTaskAwardService.doUserSocialTask(gameId, periodId, userId, taskType, clientIp, clientId);
        } catch (Exception e) {
            log.error("派发奖励异常", e);
        }
        return 0;
    }
}
