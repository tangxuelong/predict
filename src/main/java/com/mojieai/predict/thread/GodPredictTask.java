package com.mojieai.predict.thread;

import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.service.PushService;

import java.util.concurrent.Callable;

public class GodPredictTask implements Callable {

    private Long userId;
    private Integer pushType;
    private PushDto pushDto;
    private PushService pushService;

    public GodPredictTask(Long userId, Integer pushType, PushDto pushDto, PushService pushService) {
        this.userId = userId;
        this.pushType = pushType;
        this.pushDto = pushDto;
        this.pushService = pushService;
    }

    @Override
    public Object call() throws Exception {
        return pushService.godPredictPush(userId, pushType, pushDto);
    }
}
