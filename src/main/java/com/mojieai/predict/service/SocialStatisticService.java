package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.SocialEncircle;
import com.mojieai.predict.entity.po.SocialKillCode;
import com.mojieai.predict.entity.vo.SocialBigDataVo;

import java.util.List;

public interface SocialStatisticService {

    Long generateSocialStatisticId();

    void statisticSocialEncircleBigDate(SocialEncircle socialEncircle);

    void statisticSocialKillBigDate(SocialKillCode socialKillCode);

    void updateSocialStatistic(String encircleCode, Long statisticId, String periodId);

    void socialStatisticCompensateTimer();

    List<SocialBigDataVo> rebuildSocialStatistic(long gameId, String periodId);
}
