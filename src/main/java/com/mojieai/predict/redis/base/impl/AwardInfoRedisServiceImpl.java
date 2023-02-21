package com.mojieai.predict.redis.base.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.AwardInfoDao;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.RedisPubEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.AwardInfoRedisService;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.redis.refresh.handler.pub.RedisPublishHandler;
import com.mojieai.predict.util.CommonUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//redis基础操作类，业务组合操作请使用RedisUtilServiceImpl

@Service
public class AwardInfoRedisServiceImpl implements AwardInfoRedisService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;
    @Autowired
    private AwardInfoDao awardInfoDao;
    @Autowired
    private RedisPublishHandler redisPublishHandler;

    @Override
    public void refreshAwardInfo() {
        log.info("begin to refresh award info ================>");
        List<Long> gameIds = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIds) {
            refreshAwardInfo(gameId);
        }
    }

    @Override
    public void refreshAwardInfo(Long gameId) {
        Game game = GameCache.getGame(gameId);
        if (game == null) {
            throw new BusinessException("彩种信息不存在！");
        }
        if (!game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
            return;
        }
        // 查询该彩种所涉及的期次信息
        List<AwardInfo> infoList = awardInfoDao.getGameAwardInfos(gameId, CommonConstant.AWARD_INFO_MAX_NUM);
        Map<String, List<AwardInfo>> infoMap = new HashMap<>();
        for (AwardInfo info : infoList) {
            if (!infoMap.containsKey(info.getPeriodId())) {
                infoMap.put(info.getPeriodId(), new ArrayList<>());
            }
            infoMap.get(info.getPeriodId()).add(info);
        }
        storeAwardInfoList(gameId, infoMap);
    }

    private void storeAwardInfoList(Long gameId, Map<String, List<AwardInfo>> infoMap) {
        try {
            redisService.kryoHmset(RedisConstant.getAwardInfoKey(gameId), infoMap);
            redisService.expire(RedisConstant.getAwardInfoKey(gameId), IniCache.getIniIntValue
                    (IniConstant.PERIOD_DETAILS_EXPIRE_TIME, 1036800));
            redisPublishHandler.publish(RedisPubEnum.AWARD_INFO_CONFIG.getChannel(), String.valueOf(gameId));
        } catch (Exception e) {
            log.error("storeAwardInfoList error." + CommonUtil.mergeUnionKey(gameId, infoMap.size()), e);
        }
    }
}