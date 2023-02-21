package com.mojieai.predict.enums.trend;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.base.RedisService;

import java.util.List;
import java.util.Map;

/**
 * Created by Singal
 */
public enum TrendEnum {
    SSQ(GameConstant.SSQ) {
        @Override
        public TrendEnumInterface[] getTrendEnum() {
            return SsqTrendEnum.values();
        }

        @Override
        public ChartEnumInterface[] getChartEnum() {
            return SsqChartEnum.values();
        }

        @Override
        public String getChartEnumName(int chatType) {
            return SsqChartEnum.getByChartType(chatType).getChartName();
        }

        @Override
        public List<Map<String, Object>> getChart(int chatType, GamePeriod lastPeriod, Long gameId, Integer
                showNum, RedisService redisService, String gameEn, Integer areaType) {
            return SsqChartEnum.getByChartType(chatType).getChart(lastPeriod, gameId, showNum, redisService, gameEn,
                    areaType);
        }
    }, DLT(GameConstant.DLT) {
        @Override
        public TrendEnumInterface[] getTrendEnum() {
            return DltTrendEnum.values();
        }

        @Override
        public ChartEnumInterface[] getChartEnum() {
            return DltChartEnum.values();
        }

        @Override
        public String getChartEnumName(int chatType) {
            return DltChartEnum.getByChartType(chatType).getChartName();
        }

        @Override
        public List getChart(int chatType, GamePeriod lastPeriod, Long gameId, Integer
                showNum, RedisService redisService, String gameEn, Integer areaType) {
            return DltChartEnum.getByChartType(chatType).getChart(lastPeriod, gameId, showNum, redisService, gameEn,
                    areaType);
        }
    }, FC3D(GameConstant.FC3D) {
        @Override
        public TrendEnumInterface[] getTrendEnum() {
            return Fc3dTrendEnum.values();
        }

        @Override
        public ChartEnumInterface[] getChartEnum() {
            return Fc3dChartEnum.values();
        }

        @Override
        public String getChartEnumName(int chatType) {
            return Fc3dChartEnum.getByChartType(chatType).getChartName();
        }

        @Override
        public List getChart(int chatType, GamePeriod lastPeriod, Long gameId, Integer showNum, RedisService
                redisService, String gameEn, Integer areaType) {
            return Fc3dChartEnum.getByChartType(chatType).getChart(lastPeriod, gameId, showNum, redisService, gameEn,
                    areaType);
        }
    };

    private String gameEn;

    TrendEnum(String gameEn) {
        this.gameEn = gameEn;
    }

    public static TrendEnum getTrendEnumByEn(String gameEn) {
        for (TrendEnum trendEnum : values()) {
            if (trendEnum.getGameEn().equals(gameEn)) {
                return trendEnum;
            }
        }
        return null;
    }


    public static TrendEnum getTrendEnumById(Long gameId) {
        String gameEn = GameCache.getGame(gameId).getGameEn();
        return getTrendEnumByEn(gameEn);
    }

    public String getGameEn() {
        return gameEn;
    }

    abstract public TrendEnumInterface[] getTrendEnum();

    abstract public ChartEnumInterface[] getChartEnum();

    abstract public String getChartEnumName(int chatType);

    abstract public List getChart(int chatType, GamePeriod lastPeriod, Long gameId, Integer
            showNum, RedisService redisService, String gameEn, Integer areaType);
}
