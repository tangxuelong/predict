package com.mojieai.predict.redis.refresh.handler.sub;

import com.mojieai.predict.cache.AwardInfoCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.redis.base.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class AwardInfoSubHandler extends RedisSubscribeHandler {

    private final Logger log = LogConstant.commonLog;
    @Autowired
    private RedisService redisService;

    @Override
    public void handle(String message) {
        if (StringUtils.isBlank(message)) {
            log.warn("subscribe message is blank,message:" + message);
            return;
        }
        log.info("AwardInfoSubHandler redis自动刷新了！！！！！！！！！！！！！！");
        String[] gamePeriodId = message.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR);
        Long gameId = Long.parseLong(gamePeriodId[0]);
        switch (gamePeriodId.length) {
            case 1:
                Map<String, ArrayList> tempMap = redisService.kryoHgetAll(RedisConstant.getAwardInfoKey(gameId),
                        String.class, ArrayList.class);
                for (Map.Entry<String, ArrayList> entry : tempMap.entrySet()) {
                    String tempPeriodId = entry.getKey();
                    List<AwardInfo> list = entry.getValue();
                    AwardInfoCache.getPeriodAwardInfoMap(gameId).put(tempPeriodId, list);
                }
                break;
            case 2:
                String periodId = gamePeriodId[1];
                List<AwardInfo> awardInfos = redisService.kryoHget(RedisConstant.getAwardInfoKey(gameId), periodId,
                        ArrayList.class);
                AwardInfoCache.getPeriodAwardInfoMap(gameId).put(periodId, awardInfos);
                break;
            default:
                log.error("the length of gamePeriodId is error,message:" + message);
        }


    }

}
