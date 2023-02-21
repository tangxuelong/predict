package com.mojieai.predict.cache;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.util.CommonUtil;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bowu on 2017/7/18.
 */
public class PeriodCache {
    private static final Logger log = LogConstant.commonLog;

    private static Map<String, List<GamePeriod>> periodMap = new HashMap<>();

    private PeriodCache() {
    }


    public void init() {
        log.info("init PeriodCache");
        refresh();
    }

    public void refresh() {
        Map<String, List<GamePeriod>> tempPeriodMap = new HashMap<>();
        for (Map.Entry<Long, Game> entry : GameCache.getAllGameMap().entrySet()) {
            Game game = entry.getValue();
            if (game.getGameType() == Game.GAME_TYPE_COMMON) {
                //100000可以载入所有awardInfo信息
                List<GamePeriod> periods = PeriodRedis.getLastAllOpenPeriodsByGameId(game.getGameId());
                tempPeriodMap.put(CommonUtil.mergeUnionKey(game.getGameId(), RedisConstant.LAST_ALL_OPEN_PERIOD),
                        periods);
                log.info("refresh " + game.getGameEn() + " " + (periods == null ? 0 : periods.size()) + " periods");
            }
        }
        periodMap = tempPeriodMap;
        log.info("refresh " + (periodMap == null ? 0 : periodMap.size()) + " periodMap");
    }

    public static Map<String, List<GamePeriod>> getPeriodMap() {
        return periodMap;
    }
}
