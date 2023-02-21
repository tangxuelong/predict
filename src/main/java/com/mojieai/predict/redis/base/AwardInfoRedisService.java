package com.mojieai.predict.redis.base;

public interface AwardInfoRedisService {
    void refreshAwardInfo();

    void refreshAwardInfo(Long gameId);
}
