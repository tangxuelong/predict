package com.mojieai.predict.redis.refresh.handler.sub;

import com.mojieai.predict.cache.AwardInfoCache;
import com.mojieai.predict.cache.PeriodCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class PeriodSubHandler extends RedisSubscribeHandler {

    private final Logger log = LogConstant.commonLog;
    @Autowired
    private RedisService redisService;

    @Override
    public void handle(String message) {
        if (StringUtils.isBlank(message)) {
            log.warn("subscribe message is blank,message:" + message);
            return;
        }
        log.info("PeriodSubHandler redis自动刷新了！！！！！！！！！！！！！！");
        String[] gameKeyId = message.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR);
        Long gameId = Long.parseLong(gameKeyId[0]);
        List<GamePeriod> periods = PeriodRedis.getLastAllOpenPeriodsByGameId(gameId);
        PeriodCache.getPeriodMap().put(message, periods);
    }

}
