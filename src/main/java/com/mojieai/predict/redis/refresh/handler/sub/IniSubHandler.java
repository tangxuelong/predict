package com.mojieai.predict.redis.refresh.handler.sub;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.LogConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class IniSubHandler extends RedisSubscribeHandler {

    private final Logger log = LogConstant.commonLog;
    @Autowired
    private IniCache iniCache;

    @Override
    public void handle(String message) {
        if (StringUtils.isBlank(message) || iniCache == null) {
            log.warn("subscribe message or iniCache is blank,message:" + message + ",iniCache:" + iniCache);
            return;
        }
        log.info("iniCache自动刷新了！！！！！！！！！！！！！！");
        iniCache.refresh();
    }

}
