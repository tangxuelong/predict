package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.SocialStatisticDao;
import com.mojieai.predict.dao.StatisticIdSequenceDao;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.SocialBigDataVo;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SocialStatisticService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

@Service
public class SocialStatisticServiceImpl implements SocialStatisticService, BeanSelfAware {
    private static final Logger log = LogConstant.commonLog;
    @Autowired
    private SocialStatisticDao socialStatisticDao;
    @Autowired
    private StatisticIdSequenceDao statisticIdSequenceDao;
    @Autowired
    private RedisService redisService;

    private SocialStatisticService self;

    @Override
    public Long generateSocialStatisticId() {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = statisticIdSequenceDao.insertStatisticIdSeq();
        Long statisticId = Long.parseLong(timePrefix + CommonUtil.formatSequence(seq));
        return statisticId;
    }

    @Override
    public void statisticSocialEncircleBigDate(SocialEncircle socialEncircle) {
        //1.如果当前期次已经结束不在统计
        boolean isEnd = SocialEncircleKillCodeUtil.getSocialIsEnd(socialEncircle.getGameId());
        if (isEnd) {
            return;
        }
        //2.获取当前统计对应的统计时间和上一次统计对应时间
        Timestamp statisticBeforeTime = SocialEncircleKillCodeUtil.getOneTimeStatisticTime(socialEncircle.getGameId(),
                SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_BEFOR_TIME, socialEncircle.getCreateTime());
        Timestamp statisticTime = SocialEncircleKillCodeUtil.getOneTimeStatisticTime(socialEncircle.getGameId(),
                SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_CURRENT_TIME, socialEncircle.getCreateTime());

        //3.查询数据库当前统计的最新数据
        SocialStatistic socialStatistic = socialStatisticDao.getSocialStatisticByUnitKey(socialEncircle.getGameId(),
                socialEncircle.getPeriodId(), statisticTime, SocialEncircleKillConstant
                        .SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS);
        //4.如果当前统计为空，将上次结果计为当前最新并插入数据库
        if (socialStatistic == null) {
            SocialStatistic socialStatisticBefore = socialStatisticDao.getSocialStatisticByUnitKey(socialEncircle
                    .getGameId(), socialEncircle.getPeriodId(), statisticBeforeTime, SocialEncircleKillConstant
                    .SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS);
            if (socialStatisticBefore != null && socialStatisticBefore.getPeriodId().equals(socialEncircle
                    .getPeriodId())) {
                socialStatistic = socialStatisticBefore;
            } else {
                socialStatistic = new SocialStatistic();
                socialStatistic.setGameId(socialEncircle.getGameId());
                socialStatistic.setDataType(SocialEncircleKillConstant.SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS);
            }
            socialStatistic.setPeriodId(socialEncircle.getPeriodId());
            socialStatistic.setStatisticId(generateSocialStatisticId());
            socialStatistic.setStatisticTime(statisticTime);
            try {
                socialStatisticDao.insert(socialStatistic);
            } catch (DuplicateKeyException e) {
                socialStatistic = socialStatisticDao.getSocialStatisticByUnitKey(socialEncircle.getGameId(),
                        socialEncircle.getPeriodId(), statisticTime, SocialEncircleKillConstant
                                .SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS);
            }
        }
        //5.事务锁更新最新统计
        self.updateSocialStatistic(socialEncircle.getUserEncircleCode(), socialStatistic.getStatisticId(),
                socialEncircle.getPeriodId());
    }

    @Override
    public void statisticSocialKillBigDate(SocialKillCode socialKillCode) {
        //1.如果结束直接返回
        boolean isEnd = SocialEncircleKillCodeUtil.getSocialIsEnd(socialKillCode.getGameId());
        if (isEnd) {
            return;
        }
        Timestamp statisticBeforeTime = SocialEncircleKillCodeUtil.getOneTimeStatisticTime(socialKillCode.getGameId(),
                SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_BEFOR_TIME, socialKillCode.getCreateTime());
        Timestamp statisticTime = SocialEncircleKillCodeUtil.getOneTimeStatisticTime(socialKillCode.getGameId(),
                SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_CURRENT_TIME, socialKillCode.getCreateTime());

        SocialStatistic socialStatistic = socialStatisticDao.getSocialStatisticByUnitKey(socialKillCode.getGameId(),
                socialKillCode.getPeriodId(), statisticTime, SocialEncircleKillConstant
                        .SOCIAL_BIG_DATA_HOT_KILL_NUMBERS);
        if (socialStatistic == null) {
            //判断是否为最初期次
            SocialStatistic socialStatisticBefore = socialStatisticDao.getSocialStatisticByUnitKey(socialKillCode
                    .getGameId(), socialKillCode.getPeriodId(), statisticBeforeTime, SocialEncircleKillConstant
                    .SOCIAL_BIG_DATA_HOT_KILL_NUMBERS);
            if (socialStatisticBefore != null && socialStatisticBefore.getPeriodId().equals(socialKillCode
                    .getPeriodId())) {
                socialStatistic = socialStatisticBefore;
            } else {
                socialStatistic = new SocialStatistic();
                socialStatistic.setDataType(SocialEncircleKillConstant.SOCIAL_BIG_DATA_HOT_KILL_NUMBERS);
                socialStatistic.setGameId(socialKillCode.getGameId());
            }
            socialStatistic.setPeriodId(socialKillCode.getPeriodId());
            socialStatistic.setStatisticId(generateSocialStatisticId());
            socialStatistic.setStatisticTime(statisticTime);

            try {
                socialStatisticDao.insert(socialStatistic);
            } catch (DuplicateKeyException e) {
                socialStatistic = socialStatisticDao.getSocialStatisticByUnitKey(socialKillCode.getGameId(),
                        socialKillCode.getPeriodId(), statisticTime, SocialEncircleKillConstant
                                .SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS);
            }
        }
        self.updateSocialStatistic(socialKillCode.getUserKillCode(), socialStatistic.getStatisticId(),
                socialKillCode.getPeriodId());
    }

    @Transactional
    @Override
    public void updateSocialStatistic(String encircleCode, Long statisticId, String periodId) {
        String socialData = "";
        //1.查询最新统计信息并加锁
        SocialStatistic socialStatistic = socialStatisticDao.getSocialStatisticByIdForUpdate(statisticId, periodId,
                true);
        if (encircleCode != null && socialStatistic != null) {
            try {
                Map<String, Integer> socialDataMap = null;
                if (StringUtils.isNotBlank(socialStatistic.getSocialData())) {
                    //2.paser db中的统计信息
                    socialDataMap = JSONObject.parseObject(socialStatistic.getSocialData(), HashMap.class);
                } else {
                    socialDataMap = new HashMap<>();
                }

                //3.将该次用户圈号计入统计
                if (encircleCode.contains(CommonConstant.COMMON_STAR_STR)) {
                    encircleCode = encircleCode.replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                            .COMMON_STAR_STR, CommonConstant.SPACE_NULL_STR);
                }

                String[] userEncircleCode = encircleCode.split(CommonConstant.COMMA_SPLIT_STR);
                for (String code : userEncircleCode) {
                    Integer value = socialDataMap.get(code) == null ? 0 : socialDataMap.get(code);
                    socialDataMap.put(code, value + 1);
                }
                socialData = JSONObject.toJSONString(socialDataMap);
                socialStatisticDao.updateSocialBigData(statisticId, periodId, socialData);
            } catch (Exception e) {
                throw new BusinessException("社区大数据异常转化", e);
            }
        }
    }

    @Override
    public List<SocialBigDataVo> rebuildSocialStatistic(long gameId, String periodId) {
        Timestamp statisticTime = null;
        boolean ifContainEnd = false;
        boolean res = SocialEncircleKillCodeUtil.getSocialIsEnd(gameId);
        if (res) {
            statisticTime = SocialEncircleKillCodeUtil.getPeriodLastStatisticTime(gameId);
            ifContainEnd = true;
        } else {
            statisticTime = SocialEncircleKillCodeUtil.getOneTimeStatisticTime(gameId, SocialEncircleKillConstant
                    .SOCIAL_BIG_DATA_STATISTIC_CURRENT_TIME, DateUtil.getCurrentTimestamp());
        }

        //1.获取该期所有统计数据
        List<SocialStatistic> socialStatistics = socialStatisticDao.getOnePeriodSocialStatistic(gameId, periodId,
                statisticTime, ifContainEnd);
        List<SocialBigDataVo> result = new ArrayList<>();
        for (SocialStatistic socialStatistic : socialStatistics) {
            boolean newDate = true;
            SocialBigDataVo socialBigDataVo = null;
            String statisTime = DateUtil.formatTime(socialStatistic.getStatisticTime(), "MM-dd HH:mm");
            for (SocialBigDataVo bigDataVo : result) {
                if (bigDataVo.getPeriodId().equals(socialStatistic.getPeriodId()) && statisTime.contains(bigDataVo
                        .getStatisticHour()) && statisTime.contains(bigDataVo.getStatisticDate())) {
                    socialBigDataVo = bigDataVo;
                    newDate = false;
                    break;
                }
            }
            if (socialBigDataVo == null) {
                socialBigDataVo = new SocialBigDataVo();
                socialBigDataVo.setPeriodId(socialStatistic.getPeriodId());
                String[] times = statisTime.split(CommonConstant.SPACE_SPLIT_STR);
                socialBigDataVo.setStatisticDate(times[0]);
                socialBigDataVo.setStatisticHour(times[1]);
            }
            if (socialStatistic.getDataType().equals(SocialEncircleKillConstant.SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS)) {
                String socialData = SocialEncircleKillCodeUtil.getHotStatisticCode(socialStatistic.getSocialData(), 10);
                socialBigDataVo.setHotEncircleData(socialData);
            } else {
                String socialData = SocialEncircleKillCodeUtil.getHotStatisticCode(socialStatistic.getSocialData(), 5);
                socialBigDataVo.setHotKillData(socialData);
            }
            if (newDate) {
                result.add(socialBigDataVo);
            }
        }
        if (result.size() > 0) {
            String socialBigDataKey = RedisConstant.getSocialBigDataKey(gameId, periodId);
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            Timestamp timestamp = statisticTime;
            int latesySecond = 0;
            if (SocialEncircleKillCodeUtil.getSocialIsEnd(gameId)) {
                timestamp = gamePeriod.getAwardTime();
                latesySecond = 10800;
            }
            int expireTime = TrendUtil.getExprieSecond(timestamp, latesySecond);
            redisService.kryoSetEx(socialBigDataKey, expireTime, result);
        }
        return result;
    }

    @Override
    public void socialStatisticCompensateTimer() {
        for (GameEnum ge : GameEnum.values()) {
            Game game = ge.getGame();
            if (game != null && game.GAME_TYPE_COMMON == game.getGameType()) {
                statististicNoDataAutoGene(ge, SocialEncircleKillConstant.SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS);
                statististicNoDataAutoGene(ge, SocialEncircleKillConstant.SOCIAL_BIG_DATA_HOT_KILL_NUMBERS);
            }
        }
    }

    public void statististicNoDataAutoGene(GameEnum ge, Integer dataType) {
        Game game = ge.getGame();
        String key = RedisConstant.getLastStatisticTimeKey(game.getGameId(), dataType);
        Timestamp lastTime = redisService.kryoGet(key, Timestamp.class);
        //1.如果该时间段已经有了统计直接返回
        Timestamp currentTime = DateUtil.getCurrentTimestamp();
        if (lastTime != null && DateUtil.compareDate(currentTime, lastTime)) {
            return;
        }
        //2.
        Timestamp currStatisticTime = SocialEncircleKillCodeUtil.getOneTimeStatisticTime(game.getGameId(), null,
                currentTime);
        Timestamp beforeStatisticTime = SocialEncircleKillCodeUtil.getOneTimeStatisticTime(game.getGameId(),
                SocialEncircleKillConstant.SOCIAL_BIG_DATA_STATISTIC_BEFOR_TIME, currentTime);
        if (currStatisticTime == null || DateUtil.getDiffSeconds(currentTime, currStatisticTime) >= 480) {
            return;
        }
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        SocialStatistic socialStatistic = socialStatisticDao.getSocialStatisticByUnitKey(game.getGameId(), gamePeriod
                .getPeriodId(), currStatisticTime, dataType);
        SocialStatistic socialStatisticBefore = socialStatisticDao.getSocialStatisticByUnitKey(game.getGameId(),
                gamePeriod.getPeriodId(), beforeStatisticTime, dataType);
        if (socialStatistic == null) {
            List<String> redBalls = Arrays.asList(ge.getRedBalls());
            Map<String, Integer> result = new HashMap<>();
            try {
                String socialData = "";
                //3.随机生成一组统计数
                for (String ball : redBalls) {
                    int val = new Random().nextInt(99);
                    result.put(ball, val);
                }
                if (socialStatisticBefore != null && StringUtils.isNotBlank(socialStatisticBefore.getSocialData())) {
                    Map<String, Integer> lastData = JSONObject.parseObject(socialStatisticBefore.getSocialData(),
                            HashMap.class);
                    for (Map.Entry<String, Integer> entry : lastData.entrySet()) {
                        Integer value = entry.getValue() + result.get(entry.getKey());
                        result.put(entry.getKey(), value);
                    }
                }

                socialData = JSONObject.toJSONString(result);
                socialStatistic = new SocialStatistic();
                socialStatistic.setDataType(dataType);
                socialStatistic.setStatisticId(generateSocialStatisticId());
                socialStatistic.setGameId(game.getGameId());
                socialStatistic.setPeriodId(gamePeriod.getPeriodId());
                socialStatistic.setSocialData(socialData);
                socialStatistic.setStatisticTime(currStatisticTime);

                socialStatisticDao.insert(socialStatistic);
                rebuildSocialStatistic(game.getGameId(), gamePeriod.getPeriodId());
            } catch (Exception e) {
                if (!(e instanceof DuplicateKeyException)) {
                    log.error("定时自动产生大数据异常", e);
                }
            }
        }
        //3.设置缓存
        int expireTime = TrendUtil.getExprieSecond(currStatisticTime, 3600);
        redisService.kryoSetEx(key, expireTime, currStatisticTime);
    }


    @Override
    public void setSelf(Object proxyBean) {
        self = (SocialStatisticService) proxyBean;
    }
}
