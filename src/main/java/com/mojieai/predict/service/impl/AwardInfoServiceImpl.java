package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.AwardInfoCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.AwardInfoDao;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.dao.impl.PeriodScheduleDaoImpl;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.RedisPubEnum;
import com.mojieai.predict.enums.spider.SpiderAwardAreaEnum;
import com.mojieai.predict.enums.spider.SpiderAwardInfoEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.redis.refresh.handler.pub.RedisPublishHandler;
import com.mojieai.predict.service.AwardInfoService;
import com.mojieai.predict.service.AwardService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.GameUtil;
import com.mojieai.predict.util.JsonUtil;
import com.mojieai.predict.util.SerializeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AwardInfoServiceImpl implements AwardInfoService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private AwardInfoDao awardInfoDao;
    @Autowired
    private GamePeriodDao gamePeriodDao;
    @Autowired
    private PeriodScheduleDaoImpl periodScheduleDao;
    @Autowired
    private RedisPublishHandler redisPublishHandler;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AwardService awardService;
    @Autowired
    private PeriodRedisService periodRedisService;

    @Override
    public List<AwardInfo> getAwardInfos(Long gameId, String periodId) {
        return awardInfoDao.getAwardInfos(gameId, periodId);
    }

    @Override
    public Boolean downloadCommonGameAwardInfo(Long gameId, String periodId, PeriodSchedule dirtyPeriodSchedule) {
        if (dirtyPeriodSchedule.getIfAwardInfo() == CommonStatusEnum.YES.getStatus()) {
            return Boolean.TRUE;
        } else if (dirtyPeriodSchedule.getIfAward() == CommonStatusEnum.NO.getStatus()) {
            return Boolean.FALSE;
        }
        log.info("start downloadCommonGameAwardInfo");
        PeriodSchedule periodSchedule = periodScheduleDao.getPeriodSchedule(gameId, periodId);
        if (periodSchedule.getIfAwardInfo() == CommonStatusEnum.YES.getStatus()) {
            return Boolean.TRUE;
        }
        Game game = GameCache.getGame(gameId);
        if (game == null) {
            log.error("downloadCommonGameAwardInfo is error.彩种不存在.");
            return Boolean.TRUE;
        }
        try {
            if (!game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                return Boolean.TRUE;
            }
            //检查是否需要初始化加载
            String redisPeriodId = redisService.get(RedisConstant.getRedisAwardDownloadKey(game.getGameId()));
            if (redisPeriodId != null && redisPeriodId.equals(periodId)) {
                return Boolean.TRUE;
            }
            //下载奖级
            String remark = null;
            boolean flag = Boolean.FALSE;
            List<AwardInfo> awardInfos = null;
            for (SpiderAwardInfoEnum spiderAwardInfoEnum : SpiderAwardInfoEnum.values()) {
                try {
                    Map result = spiderAwardInfoEnum.getAwardInfo(game, periodId);

                    if (result == null) {
                        log.info("init load awardInfo is error.gameEn = " + game.getGameEn() + " SpiderAwardInfoEnum " +
                                "name:" + spiderAwardInfoEnum.getNameStr());
                        continue;
                    }
                    if (result.get("remark") != null) {
                        remark = result.get("remark").toString();
                        Map remarkMap = JSONObject.parseObject(remark, HashMap.class);
                        if (remarkMap != null && remarkMap.containsKey("sale") && StringUtils.isNotBlank(remarkMap
                                .get("sale").toString())) {
                            flag = Boolean.TRUE;
                        }
                    }
                    if (result.get("awardInfoList") != null) {
                        awardInfos = (List<AwardInfo>) result.get("awardInfoList");
                        flag = Boolean.TRUE;
                    }
                    if (flag) {
                        break;
                    }
                } catch (Exception e) {
                    log.info(spiderAwardInfoEnum.getNameStr() + "抓奖异常，继续下面抓奖", e);
                    continue;
                }
            }
            if (StringUtils.isNotBlank(remark)) {
                //更新销量
                updateRemark(gameId, periodId, remark);
            }

            if (awardInfos == null || awardInfos.isEmpty()) {
                log.info("get awardInfo is null" + ".gameId = " + game.getGameId());
                return Boolean.FALSE;
            }
            //更新DB和Redis
            updateAwardInfo2DBAndRedis(awardInfos, game.getGameId(), periodId);
        } catch (Exception e) {
            log.error("download awardInfo is error.gameEn = " + game.getGameEn() + ".periodId = " + periodId);
            return Boolean.FALSE;
        }
        log.info("[downloadCommonGameAwardInfo] end ", periodId);
        return Boolean.TRUE;
    }

    //更新奖级信息至数据库和redis
    private void updateAwardInfo2DBAndRedis(List<AwardInfo> awardInfoList, Long gameId, String periodId) {
        //更新数据库
        log.info("begin to update db and redis for " + awardInfoList);
        List<AwardInfo> awardInfos = awardInfoDao.getAwardInfos(gameId, periodId);
        Map<String, AwardInfo> map = new HashMap<>();
        if (awardInfos != null && !awardInfos.isEmpty()) {
            for (AwardInfo info : awardInfos) {
                map.put(info.getAwardLevel(), info);
            }
        }
        for (AwardInfo info : awardInfoList) {
            if (!map.containsKey(info.getAwardLevel())) {
                awardInfoDao.insert(info);
            }
        }
        //更新redis
        updateAwardInfo2Redis(gameId, periodId, awardInfoList);
        //更新awardInfo标志位
        periodScheduleDao.updatePeriodSchedule(gameId, periodId, "IF_AWARD_INFO", "AWARD_INFO_TIME");
    }

    //更新奖级信息至redis
    private void updateAwardInfo2Redis(Long gameId, String periodId, List<AwardInfo> awardInfos) {
        try {
            List<AwardInfo> result = redisService.kryoHget(RedisConstant.getAwardInfoKey(gameId),
                    periodId, ArrayList.class);
            if (result == null || result.isEmpty()) {
                byte[] infosBytes = SerializeUtil.KryoSerialize(awardInfos);
                byte[] periodIdBytes = SerializeUtil.KryoSerialize(periodId);
                redisService.getJedisCluster().hset(RedisConstant.getAwardInfoKey(gameId).getBytes(RedisConstant
                        .REDIS_DEFAULT_CHARSET), periodIdBytes, infosBytes);
                redisService.set(RedisConstant.getRedisAwardDownloadKey(gameId), periodId);
            }
            redisPublishHandler.publish(RedisPubEnum.AWARD_INFO_CONFIG.getChannel(), CommonUtil.mergeUnionKey(gameId,
                    periodId));
        } catch (Exception e) {
            log.error("updateAwardInfo2Redis is fail", e);
        }
    }

    //更新开奖号码
    private void updateWinningNumber(Long gameId, String periodId, String winningNumber, String remark) {
        try {
            Game game = GameCache.getGame(gameId);
            if (StringUtils.isBlank(winningNumber) || !Objects.equals(AbstractGame.getWinningNumberAllLength(game
                    .getGameEn()), winningNumber.length())) {
                return;
            }
            String newWinningNumber = GameUtil.parseCommonGameWinningNumber(gameId, winningNumber);
            GamePeriod period = gamePeriodDao.getPeriodByGameIdAndPeriod(gameId, periodId);
            Boolean flag = Boolean.FALSE;
            if (period != null) {
                if (StringUtils.isBlank(period.getWinningNumbers())) {
                    gamePeriodDao.updateGamePeriodWinningNumbers(gameId, periodId, newWinningNumber);
                    periodScheduleDao.updatePeriodSchedule(gameId, periodId, "IF_AWARD", "AWARD_TIME");
                    flag = Boolean.TRUE;
                }

                if (StringUtils.isNotBlank(remark)) {
//                  String jsonStr = JsonUtil.addJsonStr(period.getRemark(), "pool", poolBonus, "sale",periodSale);
                    String jsonStr = JsonUtil.addJsonStr(period.getRemark(), remark);
                    gamePeriodDao.updateRemark(gameId, periodId, period.getRemark(), jsonStr);
                    flag = Boolean.TRUE;
                }

                if (flag) {
                    Set<String> periods = new HashSet<>();
                    periods.add(periodId);
                    periodRedisService.consumePeriods(gameId, periods);
                }
            }
        } catch (Exception ex) {
            log.error("download from 163 and update winningNumber is error.gameId = " + gameId + ".periodId = " +
                    periodId + ".winningNumber = " + winningNumber, ex);
        }
    }

    private void updateRemark(Long gameId, String periodId, String remark) {
        try {
            Boolean flag = Boolean.FALSE;
            GamePeriod period = gamePeriodDao.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (StringUtils.isNotBlank(remark)) {
                String jsonStr = JsonUtil.addJsonStr(period.getRemark(), remark);
                gamePeriodDao.updateRemark(gameId, periodId, period.getRemark(), jsonStr);
                flag = Boolean.TRUE;
            }
            if (flag) {
                Set<String> periods = new HashSet<>();
                periods.add(periodId);
                periodRedisService.consumePeriods(gameId, periods);
            }
        } catch (Exception e) {
            log.error("update period remark error:", e);
        }
    }

    @Override
    public void downLoadAwardArea(long gameId, String periodId, PeriodSchedule dirtyPeriodSchedule) {
        if (dirtyPeriodSchedule == null || dirtyPeriodSchedule.getIfAwardArea() == CommonStatusEnum.YES.getStatus()) {
            return;
        }
        PeriodSchedule periodSchedule = periodScheduleDao.getPeriodSchedule(gameId, periodId);
        if (periodSchedule != null && periodSchedule.getIfAwardInfo() == CommonStatusEnum.YES.getStatus() &&
                periodSchedule.getIfAwardArea() == CommonStatusEnum.NO.getStatus()) {
            log.info("begin down AwardArea");
            String awardArea = "";
            for (SpiderAwardAreaEnum spiderAwardAreaEnum : SpiderAwardAreaEnum.values()) {
                awardArea = spiderAwardAreaEnum.getAwardAreaFromNet(gameId, periodId);
                if (StringUtils.isNotBlank(awardArea)) {
                    log.info(spiderAwardAreaEnum.getName() + "得到中奖地区" + awardArea);
                    break;
                }
            }
            boolean updateFlag = false;
            GamePeriod gamePeriod = gamePeriodDao.getPeriodByGameIdAndPeriod(gameId, periodId);
            //如果不为空，或者为空但是没有人中一等奖都更新
            if (StringUtils.isNotBlank(awardArea)) {
                updateFlag = true;
            } else {
                List<AwardInfo> awardInfos = AwardInfoCache.getAwardInfoList(gameId, periodId);
                for (AwardInfo awardInfo : awardInfos) {
                    if (awardInfo.getAwardLevel().equals("1") || awardInfo.getAwardLevel().equals("1_z")) {
                        if (awardInfo.getAwardCount() == 0) {
                            updateFlag = true;
                            break;
                        }
                    }
                }
            }

            if (updateFlag) {
                String oldRemark = gamePeriod.getRemark();
                Map<String, Object> newRemarkMap = JSONObject.parseObject(oldRemark, HashMap.class);
                if (newRemarkMap != null) {
                    newRemarkMap.put("area", awardArea);
                    String newRemark = JSONObject.toJSONString(newRemarkMap);
                    gamePeriodDao.updateRemark(gameId, periodId, oldRemark, newRemark);
                    int res = periodScheduleDao.updatePeriodSchedule(gameId, periodId, "IF_AWARD_AREA",
                            "AWARD_AREA_TIME");
                    if (res > 0) {
                        Set<String> periods = new HashSet<>();
                        periods.add(periodId);
                        periodRedisService.consumePeriods(gameId, periods);
                    }
                }
            }
        }
    }

    @Override
    public void downLoadTestNum() {
        Game game = GameCache.getGame(GameConstant.FC3D);
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        GamePeriod period = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), lastOpenPeriod.getPeriodId
                ());
        //1.判断标志位是否已经置位
        String redisPeriodId = redisService.kryoGet(RedisConstant.getRedisTestNumDownloadKey(game.getGameId()),
                String.class);
        if (redisPeriodId != null && redisPeriodId.equals(period.getPeriodId())) {
            return;
        }
        //2.判断是否已经抓取开奖号码
        PeriodSchedule dirtyPeriodSchedule = periodScheduleDao.getPeriodSchedule(game.getGameId(), period.getPeriodId
                ());
        //开奖号码有了之后不在抓取该期试机号
        if (dirtyPeriodSchedule.getIfAward() == CommonStatusEnum.YES.getStatus()) {
            recordDownLoadTestNum2Redis(game, period);
            return;
        }

        //下载试机号
        String remark = null;
        for (SpiderAwardInfoEnum spiderAwardInfoEnum : SpiderAwardInfoEnum.values()) {
            try {
                Map result = spiderAwardInfoEnum.getAwardInfo(game, period.getPeriodId());
                if (result != null && result.get("remark") != null) {
                    remark = result.get("remark").toString();
                    break;
                }
            } catch (Exception e) {
                log.info(spiderAwardInfoEnum.getNameStr() + "抓奖异常，继续下面抓奖", e);
                continue;
            }
        }
        if (StringUtils.isNotBlank(remark)) {
            //更新试机号
            updateRemark(game.getGameId(), period.getPeriodId(), remark);
            recordDownLoadTestNum2Redis(game, period);
        }
    }

    /* 刷新下载试机号redis*/
    private void recordDownLoadTestNum2Redis(Game game, GamePeriod period) {
        redisService.kryoSetEx(RedisConstant.getRedisTestNumDownloadKey(game.getGameId()), 172800, period
                .getPeriodId());
    }

    //初始化加载
//    private Boolean initLoad(Game game, String periodIdParam) {
//        boolean resultBl = false;
//        log.info("init load for " + game.getGameEn());
//        String poolBonus = null;
//        String periodSale = null;
//        for (SpiderAwardInfoEnum spiderAwardInfoEnum : SpiderAwardInfoEnum.values()) {
//            try {
//                Map result = spiderAwardInfoEnum.getAwardInfo(game, periodIdParam);
//
//                if (result == null || StringUtils.isBlank(result.get("winingNumber").toString())) {
//                    log.info("init load awardInfo is error.gameEn = " + game.getGameEn() + " SpiderAwardInfoEnum
// name:"
//                            + spiderAwardInfoEnum.getNameStr());
//                    continue;
//                }
//                poolBonus = (String) result.get("poolBonus");
//                periodSale = (String) result.get("periodSale");
//                updateWinningNumber(game.getGameId(), periodIdParam, result.get("winingNumber").toString(), );
//
//                if (result.get("awardInfoList") != null) {
//                    List<AwardInfo> awardInfos = (List<AwardInfo>) result.get("awardInfoList");
//
//                    if (awardInfos == null || awardInfos.isEmpty()) {
//                        log.error("parseElement2AwardInfo is ex.content = " + awardInfos + ".gameId = " + game
//                                .getGameId());
//                        continue;
//                    }
//                    //更新DB和Redis
//                    updateAwardInfo2DBAndRedis(awardInfos, game.getGameId(), periodIdParam);
//                    resultBl = true;
//                }
//            } catch (Exception e) {
//                log.info(spiderAwardInfoEnum.getNameStr() + "抓奖异常，继续下面抓奖", e);
//                continue;
//            }
//        }
//        return resultBl;
//    }

//    private Document connectAward163(String gameEn) {
//        try {
//            String url = new StringBuffer().append(CommonConstant.AWARD_163_DOWNLOAD_URL_PREFIX).append(gameEn)
//                    .append(CommonConstant.URL_SPLIT_STR).toString();
//            Document doc = Jsoup.connect(url).timeout(CommonConstant.AWARD_163_DOWNLOAD_TIMEOUT_MSEC).get();
//            return doc;
//        } catch (Exception ex) {
//            log.error("connection award163 is error.awardInfo download maybe is fail.gameEn = " + gameEn);
//            return null;
//        }
//    }

    //    private List<AwardInfo> parseElement2AwardInfo(Element element, Long gameId, String periodId) {
//        //dlt  bonus="1,3,10000000|1_z,0,0|2,70,124952|2_z,11,74971|3,552,5808|3_z,148,3484|4,26346,200|4_z,8184,
//        // 100|5,486166,10|5_z,158084,5|6,4859763,5"
//        log.info("begin to parse awardinfo for " + CommonUtil.mergeUnionKey(gameId, periodId));
//        List<AwardInfo> result = new ArrayList<>();
//        if (element == null) {
//            log.error("parseElement2AwardInfo is ex.content is null.gameId = " + gameId);
//            return null;
//        }
//        Elements allElements = element.getAllElements();
//        String bonus = allElements.attr("bonus");
//        String poolBonus = allElements.attr("pool");
//        String periodSale = allElements.attr("sale");
//        String winingNumber = allElements.attr("matchBall");
//        updateWinningNumber(gameId, periodId, winingNumber, poolBonus, periodSale);
//        if (StringUtils.isBlank(bonus) || StringUtils.isBlank(poolBonus) || StringUtils.isBlank(periodSale)) {
//            log.error("download awardInfo is error. element bonus or poolBonus or periodSale is blank." + allElements
//                    .toString());
//            return null;
//        }
//        String[] bonusArray = bonus.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR);
//        List<AwardInfo> awardInfoList = GameFactory.getInstance().getGameBean(gameId).getDefaultAwardInfoList();
//        if (awardInfoList == null || awardInfoList.isEmpty()) {
//            return null;
//        }
//        int count = 0;
//        for (AwardInfo info : awardInfoList) {
//            String[] split = bonusArray[count].split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
// .COMMA_SPLIT_STR);
//            for (String str : split) {
//                if (str.equals("--")) {
//                    return null;
//                }
//            }
//            result.add(new AwardInfo(gameId, periodId, info.getAwardLevel(), info.getLevelName(), new BigDecimal
//                    (split[2]), Integer.parseInt(split[1])));
//            count++;
//        }
//        log.info("end to parse awardinfo for " + CommonUtil.mergeUnionKey(gameId, periodId));
//        return result;
//    }
}

