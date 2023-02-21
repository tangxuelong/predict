package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.TrendConstant;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.dao.PeriodScheduleDao;
import com.mojieai.predict.dao.PredictScheduleDao;
import com.mojieai.predict.dao.TrendDao;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.entity.po.PredictSchedule;
import com.mojieai.predict.entity.vo.ColdHotNumVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.spider.SpiderAwardInfoEnum;
import com.mojieai.predict.enums.trend.Fc3dTrendEnum;
import com.mojieai.predict.enums.trend.TrendEnum;
import com.mojieai.predict.enums.trend.TrendEnumInterface;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.AwardService;
import com.mojieai.predict.service.DingTalkRobotService;
import com.mojieai.predict.service.TrendService;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.GameUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrendServiceImpl implements TrendService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private TrendDao trendDao;
    @Autowired
    private GamePeriodDao gamePeriodDao;
    @Autowired
    private PeriodScheduleDao periodScheduleDao;
    @Autowired
    private AwardService awardService;
    @Autowired
    private PredictScheduleDao predictScheduleDao;
    @Autowired
    private PeriodRedisService periodRedisService;
    @Autowired
    private DingTalkRobotService dingTalkRobotService;
    @Autowired
    private RedisService redisService;

    @Override
    public void saveTrend2Db(Task task, PeriodSchedule dirtyPeriodSchedule) {
        if (dirtyPeriodSchedule != null && dirtyPeriodSchedule.getIfTrendDB() == CommonStatusEnum.YES.getStatus()) {
            return;
        }
        PeriodSchedule periodSchedule = periodScheduleDao.getPeriodSchedule(task.getGameId(), task.getPeriodId());
        if (periodSchedule.getIfTrendDB() == CommonStatusEnum.NO.getStatus()) {
            TrendEnum trendEnum = TrendEnum.getTrendEnumById(task.getGameId());
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriodDb(task.getGameId(), task.getPeriodId());
            if (gamePeriod == null || StringUtils.isEmpty(gamePeriod.getWinningNumbers())) {
                throw new BusinessException("计算走势图时没有中奖号码信息！");
            }
            String winningNumber = gamePeriod.getWinningNumbers();
            for (TrendEnumInterface tei : trendEnum.getTrendEnum()) {
                saveTrend2Db(gamePeriod.getGameId(), gamePeriod.getPeriodId(), tei.getTableName(gamePeriod.getGameId()),
                        winningNumber, tei);
            }
            periodScheduleDao.updatePeriodSchedule(periodSchedule.getGameId(), periodSchedule.getPeriodId(),
                    "IF_TREND_DB", "DB_TIME");
        }
    }

    private void saveTrend2Db(Long gameId, String periodId, String tableName, String winningNumber, TrendEnumInterface
            tei) {
        boolean ifExist = trendDao.existTrend(gameId, periodId, tableName);
        if (ifExist) {
            return;
        }
        GamePeriod gamePeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodIdDb(gameId, periodId);
        Map<String, Object> trend = trendDao.getTrendById(gameId, gamePeriod.getPeriodId(), tableName);
        if (trend == null) {
            throw new BusinessException("gameId:" + gameId + "periodId: " + periodId + " lastTrend not in db.");
        }
        tei.generateNewTrend(gameId, periodId, winningNumber, trend);
        //TrendUtil.processMissingNumber(periodId, balls, ballColumns, trend);
        trendDao.insert(tableName, trend);
    }

    @Override
    public boolean spiderAward(Task task, PeriodSchedule dirtyPeriodSchedule) {
        PeriodSchedule periodSchedule = null;
        if (dirtyPeriodSchedule != null && dirtyPeriodSchedule.getIfAward() == CommonStatusEnum.YES.getStatus()) {
            periodSchedule = dirtyPeriodSchedule;
        } else {
            periodSchedule = periodScheduleDao.getPeriodSchedule(task.getGameId(), task.getPeriodId());
        }

        if (periodSchedule == null || periodSchedule.getIfAward() == null) {
            return false;
        }

        String winningNums = null;
        if (periodSchedule.getIfAward() == CommonStatusEnum.NO.getStatus()) {
            for (SpiderAwardInfoEnum spiderAwardInfoEnum : SpiderAwardInfoEnum.values()) {
                try {
                    Map result = spiderAwardInfoEnum.getAwardInfo(GameCache.getGame(task.getGameId()), task
                            .getPeriodId());
                    if (result == null || !result.containsKey("winingNumber") || StringUtils.isBlank(result.get
                            ("winingNumber").toString())) {
                        log.info("init load awardInfo is error.gameID = " + task.getGameId() + " SpiderAwardInfoEnum " +
                                "name:" + spiderAwardInfoEnum.getNameStr());
                        continue;
                    }
                    winningNums = result.get("winingNumber").toString();
                    if (StringUtils.isNotBlank(winningNums)) {
                        String markdown = "#### 推送服务 \n" + "> " + spiderAwardInfoEnum.getNameStr() + "抓取到开奖号码为：" +
                                winningNums + " @18301552530\n" + "> " +
                                "###### " + DateUtil.formatNowTime(15) + "发布 \n";
                        List<String> at = new ArrayList<>();
                        at.add("18301552530");
                        dingTalkRobotService.sendMassageToAll("推送服务", markdown, at);
                        log.info(spiderAwardInfoEnum.getNameStr() + "抓取到开奖号码为：" + winningNums);
                    }
                    break;
                } catch (Exception e) {
                    log.info(spiderAwardInfoEnum.getNameStr() + "未抓取到开奖号码", e);
                    continue;
                }
            }
            //保存开奖号码
            if (StringUtils.isNotBlank(winningNums)) {
                String newWinningNumber = GameUtil.parseCommonGameWinningNumber(task.getGameId(), winningNums);
                int res = gamePeriodDao.updateGamePeriodWinningNumbers(task.getGameId(), task.getPeriodId(),
                        newWinningNumber);
                //更新ifAward为1
                if (res > 0) {
                    try {
                        predictScheduleDao.updatePredictSchedule(periodSchedule.getGameId(), periodSchedule
                                .getPeriodId(), "IF_AWARD", "IF_AWARD_TIME");
                    } catch (Exception e) {
                        log.error("更新predict失败", e);
                    }
                    periodScheduleDao.updatePeriodSchedule(periodSchedule.getGameId(), periodSchedule.getPeriodId(),
                            "IF_AWARD", "AWARD_TIME");

                    Set<String> periods = new HashSet<>();
                    periods.add(task.getPeriodId());
                    periodRedisService.consumePeriods(task.getGameId(), periods);

                }
            } else {
                return false;
            }
        } else if (periodSchedule.getIfAward() == CommonStatusEnum.YES.getStatus()) {
            PredictSchedule predicateSchedule = predictScheduleDao.getPredictSchedule(task.getGameId(), task
                    .getPeriodId());
            if (predicateSchedule != null && predicateSchedule.getIfAward() == CommonStatusEnum.NO.getStatus()) {
                try {
                    predictScheduleDao.updatePredictSchedule(periodSchedule.getGameId(), periodSchedule.getPeriodId(),
                            "IF_AWARD", "IF_AWARD_TIME");
                } catch (Exception e) {
                    log.error("更新predict失败", e);
                }
            }
        }
        return true;
    }

    @Override
    public void saveTrend2DbManul(long gameId, String periodId) {
        TrendEnum trendEnum = TrendEnum.getTrendEnumById(gameId);
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriodDb(gameId, periodId);
        if (gamePeriod == null || StringUtils.isEmpty(gamePeriod.getWinningNumbers())) {
            throw new BusinessException("计算走势图时没有中奖号码信息！");
        }
        String winningNumber = gamePeriod.getWinningNumbers();
        for (TrendEnumInterface tei : trendEnum.getTrendEnum()) {
            try {
                saveTrend2Db(gamePeriod.getGameId(), gamePeriod.getPeriodId(), tei.getTableName(gamePeriod.getGameId
                        ()), winningNumber, tei);
            } catch (Exception e) {
                break;
            }
        }
    }

    /*冷热选号*/
    @Override
    public Map<String, Object> getHotColdSelectNum(Game game) {
        String openAwardInfo = "";
        String chartName = RedisConstant.RED_COLD_HOT;
        Map<String, Object> result = new HashMap<>();

        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        if (game.getGameEn().equals(GameConstant.DLT)) {
            chartName = RedisConstant.FRONT_COLD_HOT;
        }
        Map<String, Object> areaTypeAndPeriod = TrendUtil.getAreaTypeAndPeriodTrend(game.getGameId(), redisService,
                chartName, 0, currentPeriod);
        int areaType = (int) areaTypeAndPeriod.get("areaType");
        GamePeriod awardCurrentPeriod = (GamePeriod) areaTypeAndPeriod.get("period");
        GamePeriod lastAwardPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(game.getGameId(),
                awardCurrentPeriod.getPeriodId());
        //如果是3区获取上一期的上一期
        if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_3) {
            openAwardInfo = "第" + lastAwardPeriod.getPeriodId() + "期开奖中";
            lastAwardPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(game.getGameId(), lastAwardPeriod
                    .getPeriodId());
        } else if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_3) {
            openAwardInfo = "第" + lastAwardPeriod.getPeriodId() + "期官方投注已截止";
        }

        String hotColdKey = RedisConstant.getCurrentChartKey(game.getGameId(), "", game.getGameEn() +
                "_COLD_HOT_SELECT_NUM", null);
        List<ColdHotNumVo> resultList = null;
        try {
            resultList = redisService.kryoGet(hotColdKey, ArrayList.class);
            if (resultList == null || resultList.size() <= 0) {
                resultList = TrendUtil.rebuildColdHotList(lastAwardPeriod, redisService);
                int expireTime = TrendUtil.getExprieSecond(currentPeriod.getAwardTime(), 3000);
                redisService.kryoSetEx(hotColdKey, expireTime, resultList);
            }
        } catch (Exception e) {
            log.error("获取冷热选号数据异常", e);
        }
        result.put("currentPeriod", awardCurrentPeriod.getPeriodId());
        result.put("openAwardInfo", openAwardInfo);
        result.put("dataList", resultList);
        result.put("allColor", new String[]{TrendConstant.TREND_COLOR_RED_VAL, TrendConstant.TREND_COLOR_ORANGE_VAL,
                TrendConstant.TREND_COLOR_GREEN_VAL});
        return result;
    }
}
