package com.mojieai.predict.service;

public interface GoldTaskAwardService {

    void distributeAward(Long userId, String goldTaskType);
}
