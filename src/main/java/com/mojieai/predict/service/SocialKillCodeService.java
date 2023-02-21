package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.SocialKillCode;

import java.util.Map;

public interface SocialKillCodeService {

    Long generateKillCodeId();

    Map<String, Object> addKillCode(long gameId, GamePeriod gamePeriod, long userId, Long encircleId, String
            userKillCode, String clientIp, Integer clientId);

    Map<String, Object> getKillNumsInfoByEncircleId(long gameId, String periodId, Long encircleCodeId, Long userId,
                                                    Integer page, Map<String, Integer> socialKillNumAwardLevel,
                                                    Integer periodEncircleStatus, Integer killNumDetaillType, Long
                                                            encircleUserId, String versionCode);

    Map<String, Object> getKillNumDetailByEncircleId(long gameId, String periodId, Long encircleId, Long
            killNumUserId, Integer page, Integer killNumDetaillType, String versionCode);

    SocialKillCode saveUserKillNumTransaction(long gameId, String periodId, long userId, Long encircleId, String
            userKillCode, Integer killNumCount, Map<String, Object> result, Long userRank, String taskId);

    Map<String, Object> getMyKillNumsV2_3(long gameId, Long userId, Long lookUpUserId, Integer lastIndex,
                                          String versionCode);
}
