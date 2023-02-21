package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.entity.po.SocialEncircle;
import com.mojieai.predict.entity.po.SocialKillCode;
import com.mojieai.predict.service.SocialStatisticService;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class StatisticTask implements Callable {
    private static final Logger log = LogConstant.commonLog;

    private Integer dataType;
    private SocialStatisticService socialStatisticService;
    private SocialEncircle socialEncircle;
    private SocialKillCode socialKillCode;

    public StatisticTask(Integer dataType, SocialStatisticService socialStatisticService, SocialEncircle
            socialEncircle, SocialKillCode socialKillCode) {
        this.dataType = dataType;
        this.socialStatisticService = socialStatisticService;
        this.socialEncircle = socialEncircle;
        this.socialKillCode = socialKillCode;
    }

    @Override
    public Object call() throws Exception {
        try {
            if (dataType.equals(SocialEncircleKillConstant.SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS)) {
                socialStatisticService.statisticSocialEncircleBigDate(socialEncircle);
            } else {
                socialStatisticService.statisticSocialKillBigDate(socialKillCode);
            }
        } catch (Exception e) {
            log.error("异常", e);
        }
        return 0;
    }
}
