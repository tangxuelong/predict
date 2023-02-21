package com.mojieai.predict.service;

public interface UserTitleLogService {

    String generateTitleLogId(Long userId);

    void distributeTitleCompensateTiming();
}
