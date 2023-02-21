package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.SocialEncircle;

import java.util.List;
import java.util.Map;

public interface SocialClassicEncircleCodeService {

    Map<String, Object> getClassicEncircleList(long gameId, String lastPeriodId, Integer socialType);

    void saveSocialClassicEncircle(SocialEncircle socialEncircle);

    void saveClassicEncircle2Redis(long gameId, String periodId, Map<String, Integer> socialKillAwardLevel);

    void rebuildSocialClassicRedis(long gameId);

    void saveSocialEncircle2ClassicDb(long gameId);
}
