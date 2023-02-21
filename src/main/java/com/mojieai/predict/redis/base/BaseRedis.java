package com.mojieai.predict.redis.base;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.util.PropertyUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BaseRedis {
    private static final Logger log = LogConstant.commonLog;
    //使用threadLocal避免释放的时候传递jedis对象
    private static ThreadLocal<Jedis> jedisLocal = new ThreadLocal<>();
    private JedisCluster redisCluster;
    private GenericObjectPoolConfig config;

    /**
     * spring注入的时候立即执行初始化
     */
    public void initPool() {
        if (config == null) {
            config = new GenericObjectPoolConfig();
            // Maximum active connections to Redis instance
            config.setMaxTotal(PropertyUtils.getPropertyIntValue("caipiao.redis_max_active", 3000));
            // Number of connections to Redis that just sit there and do nothing
            config.setMaxIdle(PropertyUtils.getPropertyIntValue("caipiao.redis_max_idle", 300));
            // Minimum number of idle connections to Redis
            // these can be seen as always open and ready to serve
            config.setMinIdle(PropertyUtils.getPropertyIntValue("caipiao.redis_min_idle", 100));
            config.setMaxWaitMillis(PropertyUtils.getPropertyIntValue("caipiao.redis_max_wait", 2000));//ms
        }
        if (null == redisCluster) {
            Set<HostAndPort> hostAndPortSet = new HashSet();
            String hostAndPortStr = PropertyUtils.getProperty("caipiao.redis_cluster");
            String[] hostAndPortArray = hostAndPortStr.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .COMMA_SPLIT_STR);
            for (String hostAndPort : hostAndPortArray) {
                String[] ipAndPort = hostAndPort.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                        .COMMON_COLON_STR);
                HostAndPort hap = new HostAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
                hostAndPortSet.add(hap);
            }
            int timeout = PropertyUtils.getPropertyIntValue("caipiao.redis_time_out", 10000);
            int maxRedirection = PropertyUtils.getPropertyIntValue("caipiao.redis_max_redirection", 3);
            redisCluster = new JedisCluster(hostAndPortSet, timeout, timeout, maxRedirection, config);
        }
        log.info("BaseClusterRedis initPool over.");
    }

    /**
     * 获取jedis
     *
     * @return
     */
    public JedisCluster getJedisCluster() {
        return redisCluster;
    }

    public static void main(String[] args) throws InterruptedException {
        final BaseRedis baseRedis = new BaseRedis();
        baseRedis.initPool();
        for (int i = 0; i < 20; i++) {
            JedisCluster jedisCluster = baseRedis.getJedisCluster();
            jedisCluster.set(String.valueOf(i), String.valueOf(i));
            System.out.println("set " + i + ". value:" + jedisCluster.get(String.valueOf(i)));
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }
}
