package com.mojieai.predict.redis.refresh.handler.sub;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.LogConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ActivityIniSubHandler extends RedisSubscribeHandler {

    private final Logger log = LogConstant.commonLog;
    @Autowired
    private ActivityIniCache activityIniCache;

    @Override
    public void handle(String message) {
        if (StringUtils.isBlank(message) || activityIniCache == null) {
            log.warn("subscribe message or iniCache is blank,message:" + message + ",iniCache:" + activityIniCache);
            return;
        }
        log.info("activityIniCache 刷新了！！！！！！！！！！！！！！");
        activityIniCache.refresh();
    }

}
