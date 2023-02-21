package com.mojieai.predict.redis.refresh.handler.pub;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.redis.base.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;


@Repository
public class RedisPublishHandler {
    @Autowired
    private RedisService redisService;

    protected final Logger log = LogConstant.commonLog;

    public long publish(String channel, String message) {
        if (StringUtils.isBlank(message) || StringUtils.isBlank(channel)) {
            log.warn("refresh param is blank");
            return ResultConstant.ERROR;
        }
        try {
            JedisCluster jedisCluster = redisService.getJedisCluster();
            Long result = jedisCluster.publish(channel, message);
            return result;
        } catch (Exception e) {
            log.warn("error in redisPublish!", e);
            return ResultConstant.ERROR;
        }
    }
}
