package com.mojieai.predict.redis.refresh.handler.sub;

import com.mojieai.predict.cache.BannerCache;
import com.mojieai.predict.constant.LogConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Ynght on 2016/11/30.
 */
@Service
public class BannerSubHandler extends RedisSubscribeHandler {

    private final Logger log = LogConstant.commonLog;
    @Autowired
    private BannerCache bannerCache;

    @Override
    public void handle(String message) {
        if (StringUtils.isBlank(message) || bannerCache == null) {
            log.warn("subscribe message or bannerCache is blank,message:" + message + ",bannerCache:" + bannerCache);
            return;
        }
        bannerCache.refresh();
    }
}
