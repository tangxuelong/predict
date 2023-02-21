package com.mojieai.predict.thread;


import com.mojieai.predict.service.GoldTaskAwardService;

import java.util.concurrent.Callable;

public class GoldCoinTask implements Callable {

    private Long userId;
    private String goldTaskType;
    private GoldTaskAwardService goldTaskAwardService;

    public GoldCoinTask(Long userId, String goldTaskType, GoldTaskAwardService goldTaskAwardService) {
        this.userId = userId;
        this.goldTaskType = goldTaskType;
        this.goldTaskAwardService = goldTaskAwardService;
    }

    @Override
    public Object call() throws Exception {
        goldTaskAwardService.distributeAward(userId, goldTaskType);
        return 1;
    }
}
