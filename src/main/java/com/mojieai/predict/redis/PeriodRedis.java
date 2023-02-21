package com.mojieai.predict.redis;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.base.PeriodStaticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by Singal
 */
@Component
@Transactional(propagation = Propagation.NEVER)
public class PeriodRedis {

    private static PeriodRedis self;
    @Autowired
    private PeriodStaticService periodStaticService;
    @Autowired
    private GamePeriodDao gamePeriodDao;

    // 根据彩种和期次ID获取期次对象
    public static GamePeriod getPeriodByGameIdAndPeriod(Long gameId, String periodId) {
        return self.periodStaticService.getPeriodByGameIdAndPeriod(gameId, periodId);
    }

    public static GamePeriod getPeriodByGameIdAndPeriodDb(Long gameId, String periodId) {
        GamePeriod gamePeriod = self.periodStaticService.getPeriodByGameIdAndPeriod(gameId, periodId);
        if (gamePeriod == null) {
            gamePeriod = self.gamePeriodDao.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (gamePeriod == null) {
                LogConstant.commonLog.error("gameId:" + gameId + "periodId: " + periodId + "  is not exist in db.");
            }
        }
        return gamePeriod;
    }

    // 获取上一期期次信息根据awardTime
    public static List<GamePeriod> getLastAwardPeriodByGameId(Long gameId) {
        return self.periodStaticService.getLastAwardPeriodByGameId(gameId);
    }

    // openTime最近的开奖期次
    public static GamePeriod getLastOpenPeriodByGameId(Long gameId) {
        return self.periodStaticService.getLastOpenPeriodByGameId(gameId);
    }

    // 获取当前彩种的今天所有的期次
    public static List<GamePeriod> getTodayAllPeriods(Long gameId) {
        return self.periodStaticService.getTodayAllPeriods(gameId);
    }

    // 获取当前彩种的期次
    public static GamePeriod getCurrentPeriod(Long gameId) {
        return self.periodStaticService.getCurrentPeriod(gameId);
    }

    public static GamePeriod getAwardCurrentPeriod(Long gameId){
        return self.periodStaticService.getAwardCurrentPeriod(gameId);
    }

    // 获取当前期次信息列表
    public static List<GamePeriod> getCurrentPeriods(Long gameId) {
        return self.periodStaticService.getCurrentPeriods(gameId);
    }

    // 获取最近3期的期次信息
    public static List<GamePeriod> getRecent3Periods(Long gameId) {
        return self.periodStaticService.getRecent3Periods(gameId);
    }

    /*// 获取昨天的所有期次
    public static List<String> getDayBeforePeriods(Long gameId) {
        return self.periodStaticService.getDayBeforePeriods(gameId);
    }

    // 获取指定数目的历史期次信息
    public static List<GamePeriod> getHistory10AwardPeriod(Long gameId) {
        return self.periodStaticService.getHistory10AwardPeriod(gameId);
    }*/

    // 根据gameIdList获取各个彩种的最近已开奖的一期
    public static List<GamePeriod> getLastPeriodsByGameIds(List<Long> gameIds) {
        return self.periodStaticService.getLastPeriodsByGameIds(gameIds);
    }

    // 根据gameId获取彩种的所有已开奖的期次，限于大盘彩
    public static List<GamePeriod> getLastAllOpenPeriodsByGameId(Long gameId) {
        if (GameCache.getGame(gameId).getGameType() != Game.GAME_TYPE_COMMON) {
            return null;
        }
        return self.periodStaticService.getLastAllOpenPeriodsByGameId(gameId);
    }

    /* 走势图方法*/
    /* 根据彩种ID获取最近100期的期次*/
    public static List<GamePeriod> getHistory100AwardPeriod(Long gameId) {
        return self.periodStaticService.getHistory100AwardPeriod(gameId);
    }

    /* 根据彩种ID获取最近50期的期次*/
    public static List<GamePeriod> getHistory50AwardPeriod(Long gameId) {
        return self.periodStaticService.getHistory50AwardPeriod(gameId);
    }

    /* 根据彩种ID获取最近30期的期次*/
    public static List<GamePeriod> getHistory30AwardPeriod(Long gameId) {
        return self.periodStaticService.getHistory30AwardPeriod(gameId);
    }

    /* 获取指定彩种&期次的上一期*/
    public static GamePeriod getLastPeriodByGameIdAndPeriodId(Long gameId, String periodId) {
        return self.periodStaticService.getLastPeriodByGameIdAndPeriodId(gameId, periodId);
    }

    public static GamePeriod getNextPeriodByGameIdAndPeriodId(Long gameId, String periodId) {
        return self.periodStaticService.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
    }

    /* 获取指定彩种&期次的上一期，找不到从db找*/
    public static GamePeriod getLastPeriodByGameIdAndPeriodIdDb(Long gameId, String periodId) {
        GamePeriod gamePeriod = self.periodStaticService.getLastPeriodByGameIdAndPeriodId(gameId, periodId);
        if (gamePeriod == null) {
            gamePeriod = self.gamePeriodDao.getLastPeriodByGameIdAndPeriod(gameId, periodId);
            if (gamePeriod == null) {
                LogConstant.commonLog.error("gameId:" + gameId + "periodId: " + periodId + " lastPeriod not in db.");
            }
        }
        return gamePeriod;
    }

    public static Map<String, Object> getLast100PredictHistory(long gameId){
        return self.periodStaticService.getLast100PredictHistory(gameId);
    }

    @PostConstruct
    public void init() {
        self = this;
    }
}
