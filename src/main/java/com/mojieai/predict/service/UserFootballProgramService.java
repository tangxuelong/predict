package com.mojieai.predict.service;

import java.util.Map;

public interface UserFootballProgramService {

    Map<String, Object> getSportSocialPersonCenter(Long userId, Long visitorUserId, Integer lotteryCode, String
            lastIndex);

    Map<String, Object> getSportSocialRecommendDetail(String programId, Long userId, String versionCode);
}
