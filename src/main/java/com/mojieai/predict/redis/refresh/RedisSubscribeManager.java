package com.mojieai.predict.redis.refresh;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.enums.RedisPubEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.NetUtil;
import com.mojieai.predict.util.SpringContextHolder;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisCluster;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;

public class RedisSubscribeManager {
    private final Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;

    private boolean ifOnlineConfirm = false;

    private void init() {
        log.info("[subscribe] enter RedisSubscribeManager init");

        ifOnlineConfirm = checkIfGameOnlineConfirmMachine();

        final RedisPubListener redisPubListener = new RedisPubListener();

        for (RedisPubEnum pub : RedisPubEnum.values()) {
            try {
                redisPubListener.registerHandler(ifOnlineConfirm ? pub.getChannelConfirm() : pub.getChannel(),
                        SpringContextHolder.getBean(Introspector.decapitalize(pub.getHandlerClass().getSimpleName())));
            } catch (Exception e) {
                log.warn("RedisSubscribeManager init error.", e);
            }
        }

        new Thread(() -> {
            try {
                JedisCluster jedisCluster = redisService.getJedisCluster();
                List<String> channels = new ArrayList<>();
                for (RedisPubEnum pub : RedisPubEnum.values()) {
                    channels.add(ifOnlineConfirm ? pub.getChannelConfirm() : pub.getChannel());
                }
                jedisCluster.subscribe(redisPubListener, channels.toArray(new String[channels.size()]));
                log.info("refresh redis pubSub initialized.");
            } catch (Throwable e) {
                log.warn("refresh initialize redis pubSub error.", e);
            }
        }, "redisRefresh_thread").start();
    }

    private boolean checkIfGameOnlineConfirmMachine() {
        String ConfirmIps = IniCache.getIniValue(IniConstant.REDIS_SUB_AND_PUB_ONLINE_CONFIRM_IP, "10.120.118.85");
        return NetUtil.containsIp(ConfirmIps);
    }
}
