package com.mojieai.predict.thread;


import com.mojieai.predict.cache.AwardInfoCache;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.AwardInfoDao;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.TestAwardService;
import com.mojieai.predict.service.game.AbstractGame;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TestCalcAwardTask implements Callable<AwardDetail> {
    private static final Logger log = LogConstant.commonLog;

    private int type;
    private GamePeriod period;
    private AbstractGame ag;
    private AwardInfoDao awardInfoDao;
    private TestAwardService testAwardService;
    private RedisService redisService;

    public TestCalcAwardTask(int type, AbstractGame ag, GamePeriod period, AwardInfoDao awardInfoDao, TestAwardService
            testAwardService, RedisService redisService) {
        this.type = type;
        this.ag = ag;
        this.period = period;
        this.awardInfoDao = awardInfoDao;
        this.testAwardService = testAwardService;
        this.redisService = redisService;
    }

    @Override
    public AwardDetail call() {
//        List<String> numberList = testAwardService.generateNumberList(type, period);
        //获取期次预测
        String key = RedisConstant.getPredictNumsKey(period.getGameId(), period.getPeriodId(),
                RedisConstant.PREDICT_NUMS_TEN_THOUSAND, null);
//        log.info("key:" + key);
        List<String> numberList = redisService.kryoGet(key, ArrayList.class);
        int[] totalAward = null;
        for (String lotteryNumber : numberList) {
            int[] result = ag.analyseBidAwardLevels(lotteryNumber, period);
            if (totalAward == null) {
                totalAward = result;
            } else {
                for (int t = 0; t < totalAward.length; t++) {
                    totalAward[t] = totalAward[t] + result[t];
                }
            }
        }
        //计算总奖金
        List<AwardInfo> awardInfos = AwardInfoCache.getAwardInfoList(period.getGameId(), period.getPeriodId());
        if (awardInfos == null) {
            awardInfos = awardInfoDao.getAwardInfos(period.getGameId(), period.getPeriodId());
        }
        //包装奖级信息，有些期次对应奖级并没有奖金需要按照官方法则再次计算
        BigDecimal bonus = ag.processAwardInfo(totalAward, period, awardInfos);
        AwardDetail detail = new AwardDetail(period.getGameId(), period.getPeriodId(), bonus, totalAward);
        return detail;
    }
}
