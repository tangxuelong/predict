package com.mojieai.predict.cache;


import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.redis.base.RedisService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwardInfoCache {
    private static final Logger log = LogConstant.commonLog;
    //long=>gameId,String=>periodId
    private static Map<Long, Map<String, List<AwardInfo>>> awardInfoMap = new HashMap<>();

    @Autowired
    private RedisService redisService;

    private AwardInfoCache() {
    }

    public void init() {
        log.info("init AwardInfoCache");
        refresh();
    }

    public void refresh() {
        Map<Long, Map<String, List<AwardInfo>>> tempAwardInfoMap = new HashMap<>();
        for (Map.Entry<Long, Game> entry : GameCache.getAllGameMap().entrySet()) {
            Game game = entry.getValue();
            if (game.getGameType() == Game.GAME_TYPE_COMMON) {
                Map<String, ArrayList> tempMap = redisService.kryoHgetAll(RedisConstant.getAwardInfoKey(game
                        .getGameId()), String.class, ArrayList.class);
                if (tempMap != null) {
                    for (Map.Entry<String, ArrayList> gameEntry : tempMap.entrySet()) {
                        String tempPeriodId = gameEntry.getKey();
                        List<AwardInfo> list = gameEntry.getValue();
                        if (!tempAwardInfoMap.containsKey(game.getGameId())) {
                            tempAwardInfoMap.put(game.getGameId(), new HashMap<>());
                        }
                        Map<String, List<AwardInfo>> periodMap = tempAwardInfoMap.get(game.getGameId());
                        periodMap.put(tempPeriodId, list);
                    }
                }
            }
        }
        awardInfoMap = tempAwardInfoMap;
        log.info("refresh " + (tempAwardInfoMap == null ? 0 : tempAwardInfoMap.size()) + " awardInfo");
    }

    public static List<AwardInfo> getAwardInfoList(Long gameId, String periodId) {
        return awardInfoMap.get(gameId).get(periodId);
    }

    public static Map<String, List<AwardInfo>> getPeriodAwardInfoMap(Long gameId) {
        return awardInfoMap.get(gameId);
    }
}