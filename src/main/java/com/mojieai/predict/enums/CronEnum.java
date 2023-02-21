package com.mojieai.predict.enums;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.util.SpringContextHolder;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Singal
 */
public enum CronEnum {
    PERIOD("period") {
        @Override
        public void cron(Game game) {
            PeriodRedisService periodRedisService = SpringContextHolder.getBean("periodRedisServiceImpl");
            periodRedisService.refreshExpirePeriodInfo(game.getGameId());
        }

        @Override
        public String getCronString(Game game) {
            return IniCache.getIniValue(IniConstant.DEFAULT_GAME_PERIOD_CRON, DEFAULT_GAME_PERIOD_CRON);
        }

        @Override
        public long getDefaultDelay(Game game) {
            return DEFAULT_GAME_PERIOD_DEFAULT_DELAY;
        }
    };

    abstract public void cron(Game game);

    public String getCronEnum() {
        return cron;
    }

    abstract public String getCronString(Game game);

    abstract public long getDefaultDelay(Game game);

    private String cron;

    CronEnum(String cron) {
        this.cron = cron;
    }

    public String getCron() {
        return cron;
    }

    private static final String DEFAULT_GAME_PERIOD_CRON = "* * * * * ?";

    private static final long DEFAULT_GAME_PERIOD_DEFAULT_DELAY = 3 * 1000;

    public static final long DEFAULT_TIME_TO_DEADLINE = 2000L;

    private static Map<String, Logger> loggerFactory = new HashMap<String, Logger>();

    public Logger getLogger() {
        String cronEnum = getCronEnum();
        if (loggerFactory.containsKey(cronEnum)) {
            return loggerFactory.get(cronEnum);
        }
        synchronized (cronEnum.concat("Platform").intern()) {
            LogConstant.dynamicLog4j2(loggerFactory, cronEnum);
            return loggerFactory.get(cronEnum);
        }
    }
}
