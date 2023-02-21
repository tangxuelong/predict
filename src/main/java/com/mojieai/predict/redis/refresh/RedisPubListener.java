package com.mojieai.predict.redis.refresh;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.redis.refresh.handler.sub.RedisSubscribeHandler;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisPubListener extends JedisPubSub {
    private final Logger log = LogConstant.commonLog;

    private static Map<String, RedisSubscribeHandler> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void onMessage(String channel, String message) {
        try {
            log.info("onMessage channel=" + channel + ",message=" + message);
            RedisSubscribeHandler handler = handlerMap.get(channel);
            if (handler != null)
                handler.handle(message);
            else
                log.info("handler is null, onMessage channel=" + channel + ",message=" + message);
        } catch (Throwable e) {
            log.warn("onMessage error:" + e.getMessage(), e);
        }
    }

    public synchronized void registerHandler(String channel, RedisSubscribeHandler handler) {
        log.info("onMessage registerHandler:" + " channel=" + channel + ", handler=" + handler.getClass());
        handlerMap.put(channel, handler);
    }

    @Override
    public void onPMessage(String s, String s1, String s2) {

    }

    @Override
    public void onSubscribe(String s, int i) {

    }

    @Override
    public void onUnsubscribe(String s, int i) {

    }

    @Override
    public void onPUnsubscribe(String s, int i) {

    }

    @Override
    public void onPSubscribe(String s, int i) {

    }
}
