package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PredictNumService;
import com.mojieai.predict.service.PredictRedBallService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.manualpredicthistory.ManualPredictHistoryFactory;
import com.mojieai.predict.service.predict.PredictFactory;
import com.mojieai.predict.service.predict.PredictInfo;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PredictUtil;
import com.mojieai.predict.util.ProgramUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PredictNumServiceImpl implements PredictNumService {
    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Autowired
    private RedisService redisService;
    @Autowired
    private PredictScheduleDao predictScheduleDao;
    @Autowired
    private PredictNumbersDao predictNumbersDao;
    @Autowired
    private MobileUserDao mobileUserDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private PredictRedBallService predictRedBallService;
    @Autowired
    private PredictNumbersOperateDao predictNumbersOperateDao;
    @Autowired
    private GamePeriodDao gamePeriodDao;
    @Autowired
    private PredictRedBallDao predictRedBallDao;
    @Autowired
    private VipMemberService vipMemberService;

    /*获取首页预测信息*/
    @Override
    public Map<String, Object> getPredictIndexInfo(String userIdUniqueStr, String deviceIdUniqueStr, long gameId,
                                                   String versionCode) {
        Map<String, Object> resultMap = new HashMap<>();
        int leftCount = 0;
        int areaType = 1;
        String predictNum = "";

        Map gameRealPeriod = TrendUtil.getAreaTypeAndPeriod(gameId, redisService);
        GamePeriod gamePeriod = null;
        if (gameRealPeriod != null && gameRealPeriod.get("period") != null) {
            gamePeriod = (GamePeriod) gameRealPeriod.get("period");

            Map<String, String> predictedInfo = getPredictInfoByUniqueStr(gameId, userIdUniqueStr, deviceIdUniqueStr,
                    gamePeriod.getPeriodId());
            String userPredictTimesKey = predictedInfo.get("userPredictTimesKey");
            areaType = Integer.valueOf(gameRealPeriod.get("areaType").toString());
            if (redisService.isKeyExist(userPredictTimesKey)) {
                leftCount = Integer.valueOf(predictedInfo.get("maxCount")) - Integer.valueOf(redisService
                        .get(userPredictTimesKey));
                if (leftCount < 0) {
                    leftCount = 0;
                }
            } else {
                leftCount = Integer.valueOf(predictedInfo.get("maxCount"));
            }
//            GamePeriod currentPrediod = PeriodRedis.getCurrentPeriod(gameId);

            //更多预测运营文案
            String adKey = ActivityIniConstant.PREDCIT_INDEX_MORE_PREDICT_OPERATE_AD_SSQ;
            String morePredicts = ActivityIniConstant.PREDCIT_INDEX_MORE_PREDICT_OPERATE_SORT_SSQ;
            String morePredictsImg = ActivityIniConstant.PREDCIT_INDEX_MORE_PREDICT_OPERATE_SORT_IMG_SSQ;
            String defultAd = ActivityIniConstant.PREDCIT_INDEX_MORE_PREDICT_OPERATE_AD_DEFAULT_SSQ;
            if (GameEnum.getGameEnumById(gameId).getGameEn().equals(GameConstant.DLT)) {
                adKey = ActivityIniConstant.PREDCIT_INDEX_MORE_PREDICT_OPERATE_AD_DLT;
                morePredicts = ActivityIniConstant.PREDCIT_INDEX_MORE_PREDICT_OPERATE_SORT_DLT;
                morePredictsImg = ActivityIniConstant.PREDCIT_INDEX_MORE_PREDICT_OPERATE_SORT_IMG_DLT;
                defultAd = ActivityIniConstant.PREDCIT_INDEX_MORE_PREDICT_OPERATE_AD_DEFAULT_DLT;
            }
            String operatePredictAd = ActivityIniCache.getActivityIniValue(adKey, defultAd);
            String operatePredictImgs = ActivityIniCache.getActivityIniValue(morePredictsImg, "");
            String morePredictSorts = ActivityIniCache.getActivityIniValue(morePredicts, "");
            try {
                Map<String, Object> operatePredictMapAd = JSONObject.parseObject(operatePredictAd, HashMap.class);
                Map<String, Object> morePredictsImgMap = JSONObject.parseObject(operatePredictImgs, HashMap.class);
                List<Map> morePredictSortList = JSONObject.parseObject(morePredictSorts, ArrayList.class);
                for (int i = 0; i < morePredictSortList.size(); i++) {
                    Map temp = morePredictSortList.get(i);
                    if (Integer.valueOf(versionCode) < CommonConstant.VERSION_CODE_3_0 && temp.get("sortName").equals
                            (PredictConstant.PREDICT_MORE_FIXED_KILL)) {
                        morePredictSortList.remove(temp);
                    }
                    temp.put("titleImg", morePredictsImgMap.get(temp.get("sortName")));
                    if (temp.get("sortName").toString().equals("social")) {
                        temp.put("rightImg", morePredictsImgMap.get("socialLeft"));
                    }
                }
                resultMap.putAll(operatePredictMapAd);
                resultMap.put("predictSorts", morePredictSortList);
            } catch (Exception e) {
                log.error("获取配置错误", e);
            }

            Map historyInfo = getHitoryWinInfo(gameId, gamePeriod.getPeriodId());
            resultMap.put("historyInfo", historyInfo);
            resultMap.put("currentPeriodId", gamePeriod.getPeriodId());
        }
        String periodId = "";
        if (null != gamePeriod) {
            periodId = gamePeriod.getPeriodId();
        }
        Map showMsg = generateShowMsg(leftCount, userIdUniqueStr, areaType, leftCount, gameId, periodId);

        resultMap.put("predictBehandBtnMsg", "点击“智慧”获取预测号码");
        resultMap.put("adInfo", ActivityIniCache.getActivityIniValue(ActivityIniConstant.PREDICT_INDEX_ADINFO,
                "大数据每期专注预测10000注"));
        resultMap.put("predictWinNum", predictNum);
        resultMap.put("leftCount", leftCount);
        resultMap.put("showMsg", showMsg.get("showMsg"));

        // 双色球 设备次数 直接登录
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.SSQ)&&StringUtils.isBlank(userIdUniqueStr)){
            resultMap.put("leadLoginFlag", 1);
            resultMap.put("leftCount", 0);
            resultMap.put("showMsg", "登录独享3次");
        }else {
            resultMap.put("leadLoginFlag", showMsg.get("leadLoginFlag"));
        }


        // 添加智慧方案入口列表
        List<Map<String, Object>> programList = ProgramUtil.getProgramListFromActivityIni(gameId);

        for (Map<String, Object> temp : programList) {
            String programDesc = temp.get("programDesc").toString();
            programDesc = "<font color='#F9FF1D'>" + programDesc + "</font>";
            temp.put("programDesc", programDesc);
        }
        resultMap.put("programList", programList);
        return resultMap;
    }

    /*获取历史预测战绩*/
    @Override
    public Map<String, Object> getPredictHistoryList(long gameId) {
        String historyAwardSum = "";
        List<Map> awardDetails = new ArrayList<>();
        String[] awardLevelSumArr = new String[3];
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> awardDetailMap = PeriodRedis.getLast100PredictHistory(gameId);
        if (awardDetailMap != null) {
            awardDetails = (List<Map>) awardDetailMap.get("awardDetails");
            awardLevelSumArr = (String[]) awardDetailMap.get("awardLevelSumArr");
            if (awardDetails != null && awardDetails.size() > 0) {
                historyAwardSum = awardDetails.get(0).get("historyPredictAwardSum").toString();
            }
        }

        historyAwardSum = TrendUtil.packageMoney(historyAwardSum);
        resultMap.put("adInfo", historyAwardSum);
        resultMap.put("predictHistoryList", awardDetails);
        resultMap.put("awardLevelSumArr", awardLevelSumArr);
        return resultMap;
    }

    @Override
    public Map<String, Object> getPredictNums(String userIdUniqueStr, String deviceIdUniqueStr, long gameId) {
        Map<String, Object> resultMap = new HashMap<>();
        int leftCount = 0;
        int lastLeftCount = 0;
        String predictNum = "";
        int areaType = 1;

        Map gameRealPeriod = TrendUtil.getAreaTypeAndPeriod(gameId, redisService);

        GamePeriod gamePeriod = null;
        if (gameRealPeriod != null) {
            gamePeriod = (GamePeriod) gameRealPeriod.get("period");
            if (gamePeriod != null) {
                Map<String, String> predictedInfo = getPredictInfoByUniqueStr(gameId, userIdUniqueStr,
                        deviceIdUniqueStr, gamePeriod.getPeriodId());
                String userPredictTimesKey = predictedInfo.get("userPredictTimesKey");
                areaType = Integer.valueOf(gameRealPeriod.get("areaType").toString());

                String userPredictTimes = redisService.get(userPredictTimesKey);
                Long userId = StringUtils.isBlank(userIdUniqueStr) ? null : Long.parseLong(userIdUniqueStr);
                lastLeftCount = getUserPredictMaxNums(gameId, gamePeriod.getPeriodId(), userId) - Integer.valueOf
                        (userPredictTimes == null ? "0" : userPredictTimes);
                //
                if (areaType != GameConstant.PERIOD_TIME_AREA_TYPE_3 && lastLeftCount > 0) {
                    long tempNums = redisService.incr(userPredictTimesKey);
                    lastLeftCount = (int) (getUserPredictMaxNums(gameId, gamePeriod.getPeriodId(), userId) + 1 -
                            tempNums);
                    if (tempNums <= Integer.valueOf(predictedInfo.get("maxCount"))) {
                        predictNum = selectNumFromRedis(gameId, gamePeriod, predictedInfo.get
                                ("userUniqueStr"), lastLeftCount, 1);
                        leftCount = (int) (Long.valueOf(predictedInfo.get("maxCount")) - tempNums);
                    }
                }
                resultMap.put("currentPeriodId", gamePeriod.getPeriodId());
            }
        }
        String periodId = "";
        if (gamePeriod != null) {
            periodId = gamePeriod.getPeriodId();
        }
        Map showMsg = generateShowMsg(leftCount, userIdUniqueStr, areaType, lastLeftCount, gameId, periodId);
        Map foregroundFlag = judgeIfOpenFlag(lastLeftCount, userIdUniqueStr, areaType);

        resultMap.put("ifRunOutFlag", foregroundFlag.get("ifRunOutFlag"));
        resultMap.put("ifRunOutMsg", foregroundFlag.get("ifRunOutMsg"));
        resultMap.put("predictWinNum", predictNum);
        resultMap.put("leftCount", leftCount);
        resultMap.put("showMsg", showMsg.get("showMsg"));
        resultMap.put("leadLoginFlag", showMsg.get("leadLoginFlag"));

        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.SSQ)&&StringUtils.isBlank(userIdUniqueStr)){
            resultMap.put("leadLoginFlag", 1);
            resultMap.put("leftCount", 0);
            resultMap.put("showMsg", "登录独享3次");
        }else {
            resultMap.put("leadLoginFlag", showMsg.get("leadLoginFlag"));
        }
        resultMap.put("openAwardFlag", foregroundFlag.get("openAwardFlag"));
        resultMap.put("openAwardAlterMsg", foregroundFlag.get("openAwardAlterMsg"));
        return resultMap;
    }

    /*计算产生1W注号码*/
    @Override
    public void generatePredictNums(Task task, PredictSchedule dirtyPredictSchedule) {
        if (dirtyPredictSchedule == null || (dirtyPredictSchedule != null && dirtyPredictSchedule.getIfPredict() ==
                CommonStatusEnum.YES.getStatus())) {
            return;
        }
        PredictSchedule predictSchedule = predictScheduleDao.getPredictSchedule(task.getGameId(), task.getPeriodId());
        if (predictSchedule != null && predictSchedule.getIfAward() == CommonStatusEnum.YES.getStatus() &&
                predictSchedule.getIfPredict() == CommonStatusEnum.NO.getStatus() && predictSchedule
                .getIfPredictBlueThree() == CommonStatusEnum.YES.getStatus()) {

            try {
                GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(task.getGameId(), task.getPeriodId());
                GamePeriod predictPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(task.getGameId(), task
                        .getPeriodId());
                String predictNumskey = RedisConstant.getPredictNumsKey(task.getGameId(), predictPeriod.getPeriodId(),
                        RedisConstant.PREDICT_NUMS_TEN_THOUSAND, null);

                PredictNumbers predictNums = predictNumbersDao.getPredictNums(task.getGameId(), predictPeriod
                        .getPeriodId());

                if (predictNums != null && predictNums.getPredictNumbers().length > 0) {
                    Set<String> predictNum = PredictUtil.decompressGBList(predictNums.getPredictNumbers(), task
                            .getPeriodId());
                    if (predictNum != null && redisService.kryoSAddSets(predictNumskey, predictNum)) {
                        predictScheduleDao.updatePredictSchedule(task.getGameId(), task.getPeriodId(),
                                "IF_PREDICT", "IF_PREDICT_TIME");
                    }
                } else {
                    Map<String, Object> predictInfo = PredictFactory.getInstance().getPredictInfo(GameCache.getGame
                            (task.getGameId()).getGameEn()).generatePredictNums(predictPeriod);

                    boolean saveKillThree = Boolean.FALSE;
                    String killNum = (String) predictInfo.get("killNum");
                    int res = predictRedBallService.savePredictKillCodeAndBonusKillCode(predictPeriod, gamePeriod,
                            killNum);
                    if (res > 0) {
                        //保存杀三码
                        if (StringUtils.isNotBlank(killNum)) {
                            String killThreeKey = RedisConstant.getPredictNumsKey(predictPeriod.getGameId(),
                                    predictPeriod.getPeriodId(), RedisConstant.PREDICT_RED_KILL_THREE_NUMS, null);
                            int expireTime = TrendUtil.getExprieSecond(predictPeriod.getAwardTime(), 36000);
                            saveKillThree = redisService.kryoSetEx(killThreeKey, expireTime, killNum);
                        }
                    }
                    log.info("kill three red code finish, begin save hundred predict nums");
                    Set<String> predictNum = (Set<String>) predictInfo.get("numberList");
                    if (saveKillThree && redisService.kryoSAddSets(predictNumskey, predictNum)) {
                        int diffDays = TrendUtil.getExprieSecond(predictPeriod.getEndTime(), 36000);
                        redisService.expire(predictNumskey, diffDays);
                        PredictNumbers predictNumbers = new PredictNumbers();
                        predictNumbers.setGameId(task.getGameId());
                        predictNumbers.setPeriodId(predictPeriod.getPeriodId());
                        predictNumbers.setPredictNumbers(PredictUtil.compressGBList(predictNum, task.getGameId(),
                                predictPeriod.getPeriodId()));

                        int insertRes = predictNumbersDao.insert(predictNumbers);
                        if (insertRes > 0 && predictNum != null && predictNum.size() > 0) {
                            predictScheduleDao.updatePredictSchedule(task.getGameId(), task.getPeriodId(),
                                    "IF_PREDICT", "IF_PREDICT_TIME");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("预测号码异常 periodId:" + task.getPeriodId(), e);
            }
        }
    }

    /*计算预测历史战绩(奖级)*/
    @Override
    public void updateHistoryPredict(Task task, PredictSchedule dirtyPredictSchedule) {
        if (dirtyPredictSchedule == null || (dirtyPredictSchedule != null && dirtyPredictSchedule.getIfHistoryWinning
                () == CommonStatusEnum.YES.getStatus())) {
            return;
        }

        PredictSchedule predictSchedule = predictScheduleDao.getPredictSchedule(task.getGameId(), task.getPeriodId());
        if (predictSchedule.getIfHistoryWinning() == CommonStatusEnum.NO.getStatus()) {
            try {
                //1.获取开奖号码
                PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(GameCache.getGame(task
                        .getGameId()).getGameEn());
                Set<String> predictNums = predictInfo.getPredictNums(task.getGameId(), task.getPeriodId());
                //2.计算奖级信息
                GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(task.getGameId(), task.getPeriodId());
                int[] awardLevel = predictInfo.calculateAwardLevel(gamePeriod, predictNums);
                //3.添加运营号码
                boolean addOpereateFlag = ManualPredictHistoryFactory.getInstance().getManualPredictNum(GameCache
                        .getGame(task.getGameId()).getGameEn()).operatePredictNums(task.getGameId(), task.getPeriodId
                        (), predictNums);
                if (addOpereateFlag) {
                    awardLevel = predictInfo.calculateAwardLevel(gamePeriod, predictNums);
                }
                //3.计算累计奖级
                GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodIdDb(task.getGameId(), task
                        .getPeriodId());
                String historyAwardLevelSumKey = RedisConstant.getPredictNumsKey(lastPeriod.getGameId(), lastPeriod
                        .getPeriodId(), RedisConstant.PREDICT_NUMS_HISTORY_AWARD_LEVEL_SUM, null);
                String lastHistoryAwardLevelSum = redisService.kryoGet(historyAwardLevelSumKey, String.class);

                if (awardLevel.length > 0) {
                    Boolean isFloatBonus = false;
                    int floatLevelCount = AbstractGame.floatAwardLevelCount(GameCache.getGame(task.getGameId())
                            .getGameEn());
                    if (floatLevelCount > 0) {
                        int levelSum = 0;
                        for (int i = 0; i < floatLevelCount; i++) {
                            levelSum += awardLevel[i];
                        }
                        isFloatBonus = levelSum > 0;
                    }
                    if (StringUtils.isBlank(lastHistoryAwardLevelSum)) {
                        lastHistoryAwardLevelSum = predictNumbersDao.getHistoryAwardLevelSum(task.getGameId(),
                                lastPeriod.getPeriodId());
                    }
                    lastHistoryAwardLevelSum = calcuHistoryAwardLevel(lastHistoryAwardLevelSum, awardLevel);

                    if (StringUtils.isNotBlank(lastHistoryAwardLevelSum)) {
                        StringBuffer predictNumsLevel = new StringBuffer();
                        for (int i = 0; i < awardLevel.length; i++) {
                            predictNumsLevel.append(awardLevel[i]);
                            if (i != awardLevel.length - 1) {
                                predictNumsLevel.append(CommonConstant.COMMA_SPLIT_STR);
                            }
                        }
                        AwardDetail awardDetail = new AwardDetail();
                        awardDetail.setGameId(task.getGameId());
                        awardDetail.setPeriodId(task.getPeriodId());
                        awardDetail.setAwardLevel(awardLevel);
                        awardDetail.setHistoryPredictAwardLevelSum(lastHistoryAwardLevelSum);
                        //如果是小奖直接更新
                        if (!isFloatBonus) {
                            Map mapRes = getPredictHistoryBonus(predictInfo, predictNumsLevel.toString(), lastPeriod,
                                    gamePeriod, false);
                            if (mapRes != null) {
                                awardDetail.setBonus((BigDecimal) mapRes.get("bonus"));
                                awardDetail.setHistoryPredictAwardSum((Integer) mapRes.get("historyAwardSum"));
                            }
                        }

                        int res = predictNumbersDao.updatePredictNumAwardLevel(task.getGameId(), task.getPeriodId(),
                                predictNumsLevel.toString(), lastHistoryAwardLevelSum);
                        if (res > 0) {
                            saveAwardDetailToRedis(awardDetail, gamePeriod);
                            int expireTime = TrendUtil.getExprieSecond(gamePeriod.getEndTime(), 36000);
                            redisService.kryoSetEx(historyAwardLevelSumKey, expireTime, lastHistoryAwardLevelSum);
                            saveCumulateAward(lastPeriod, gamePeriod, awardDetail.getBonus(), new BigDecimal
                                    (awardDetail.getHistoryPredictAwardSum()));
                            if (!isFloatBonus) {
                                predictScheduleDao.updatePredictSchedule(task.getGameId(), task.getPeriodId(),
                                        "IF_AWARD_INFO", "IF_AWARD_INFO_TIME");
                                predictScheduleDao.updatePredictSchedule(task.getGameId(), task.getPeriodId(),
                                        "IF_HISTORY_WIN_BONUS", "IF_HISTORY_WIN_BONUS_TIME");
                            }
                            predictScheduleDao.updatePredictSchedule(task.getGameId(), task.getPeriodId(),
                                    "IF_HISTORY_WINNING", "IF_HISTORY_WINNING_TIME");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("计算奖级信息异常", e);
            }
        }
    }

    /*计算历史预测金额*/
    @Override
    public Boolean updateHistoryPredictBonus(Task task, PredictSchedule dirtyPredictSchedule) {
        boolean result = false;
        if (dirtyPredictSchedule == null || (dirtyPredictSchedule != null && dirtyPredictSchedule
                .getIfHistoryWinBonus() == CommonStatusEnum.YES.getStatus())) {
            return true;
        }

        PredictSchedule predictSchedule = predictScheduleDao.getPredictSchedule(task.getGameId(), task.getPeriodId());
        if (predictSchedule.getIfAwardInfo() == CommonStatusEnum.YES.getStatus() && predictSchedule
                .getIfHistoryWinning() == CommonStatusEnum.YES.getStatus() && predictSchedule.getIfHistoryWinBonus()
                == CommonStatusEnum.NO.getStatus()) {

            try {
                PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(GameConstant.SSQ);
                //1.拿奖级信息
                Map<String, String> awardLevelMap = predictNumbersDao.getAllAwardLevelStr(task.getGameId(), task
                        .getPeriodId());
                if (awardLevelMap != null && (predictSchedule.getIfAwardInfo() == CommonStatusEnum.YES
                        .getStatus() || predictSchedule.getIfAwardInfo() == CommonStatusEnum.YES.getStatus())) {

                    String awardLevel = awardLevelMap.get("AWARD_LEVEL");
                    String historyAwardLevel = awardLevelMap.get("HISTORY_AWARD_LEVEL_SUM");

                    GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(task.getGameId(), task
                            .getPeriodId());
                    GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(task.getGameId(), task.getPeriodId
                            ());

                    Map mapRes = getPredictHistoryBonus(predictInfo, awardLevel, lastPeriod, gamePeriod, true);
                    if (mapRes != null) {
                        BigDecimal bonus = (BigDecimal) mapRes.get("bonus");
                        int historyAwardSum = (Integer) mapRes.get("historyAwardSum");

                        //4.更新缓存
                        if (updateAwardDetailRedis(gamePeriod, bonus, historyAwardSum, awardLevel, historyAwardLevel)) {
                            saveCumulateAward(lastPeriod, gamePeriod, bonus, new BigDecimal(historyAwardSum));
                            predictScheduleDao.updatePredictSchedule(task.getGameId(), task.getPeriodId(),
                                    "IF_HISTORY_WIN_BONUS", "IF_HISTORY_WIN_BONUS_TIME");
                            result = true;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("计算历史战绩异常 periodId:" + task.getPeriodId(), e);
            }
        } else if (predictSchedule.getIfHistoryWinBonus() == CommonStatusEnum.YES.getStatus()) {
            result = true;
        }
        return result;
    }

    /*检查缓存中时候有预测号码*/
    @Override
    public void checkPredictNumRedis() {
        Game game = GameCache.getGame(GameConstant.SSQ);
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        if (gamePeriod != null) {
            String predictNumKey = RedisConstant.getPredictNumsKey(game.getGameId(), gamePeriod.getPeriodId(),
                    RedisConstant.PREDICT_NUMS_TEN_THOUSAND, null);

            List<String> predictNums = redisService.kryoGet(predictNumKey, ArrayList.class);
            if (predictNums == null || predictNums.size() == 0) {
                PredictNumbers predictNumbers = predictNumbersDao.getPredictNums(game.getGameId(), gamePeriod
                        .getPeriodId());
                if (predictNumbers != null) {
                    Set<String> predictNumList = PredictUtil.decompressGBList(predictNumbers.getPredictNumbers(),
                            gamePeriod.getPeriodId());
                    if (predictNumList != null && predictNumList.size() > 0) {
                        redisService.kryoSet(predictNumKey, predictNumList);
                    }
                }
            }
        }
    }

    @Override
    public Boolean clearTimes(long gameId, String mobile, String type) {
        boolean res = false;
        String deviceIdStr = "";
        String userPredictTimesKey = "";

        long userId = mobileUserDao.getUserIdByMobile(mobile);
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        GamePeriod currentPeriod = PeriodRedis.getAwardCurrentPeriod(gameId);

        if (userInfo != null) {
            deviceIdStr = userInfo.getDeviceId();
            deviceIdStr = deviceIdStr.substring(0, deviceIdStr.length() - 2);
        } else {
            return res;
        }

        if (type.equals("shebei")) {
            userPredictTimesKey = RedisConstant.getDevicePredictNumsKey(gameId, "", deviceIdStr, "times");
        } else {
            userPredictTimesKey = RedisConstant.getPredictNumsKey(gameId, currentPeriod.getPeriodId(), userId + "",
                    "times");
        }

        long delRes = 0;
        if (redisService.isKeyExist(userPredictTimesKey)) {
            delRes = redisService.del(userPredictTimesKey);
        } else {
            delRes = 1;
        }

        if (delRes > 0) {
            res = true;
        }
        return res;
    }

    @Override
    public void upatePeriodId(long gameId, String oldPeriodId, String newPeriod) {
        predictNumbersDao.updatePeriodId(gameId, oldPeriodId, newPeriod);
    }

    /*奖预测中奖情况放入缓存中*/
    private void saveAwardDetailToRedis(AwardDetail awardDetail, GamePeriod gamePeriod) {
        //1.保存历史
        String history100PredictWinkey = RedisConstant.getPredictNumsKey(gamePeriod.getGameId(), "", RedisConstant
                .HISTORY_100_PREDICT_WIN, null);

        dealAwardDeatail(awardDetail);

        long score = gamePeriod.getEndTime().getTime();
        redisService.kryoZAddSet(history100PredictWinkey, score, awardDetail);

        //2.构建首页缓存
        BigDecimal historyAwardSum = predictNumbersDao.getLastHistoryAwardSum(gamePeriod.getGameId(), gamePeriod
                .getPeriodId());
        buildPredictIndexRedis(gamePeriod, historyAwardSum.intValue());
    }

    /*更新预测历史的award中奖信息*/
    private Boolean updateAwardDetailRedis(GamePeriod gamePeriod, BigDecimal predictNumsBonus, int historyAwardSum,
                                           String awardLevel, String historyAwardLevel) {
        boolean result = false;
        if (gamePeriod == null) {
            return result;
        }
        String history100PredictWinkey = RedisConstant.getPredictNumsKey(gamePeriod.getGameId(), "", RedisConstant
                .HISTORY_100_PREDICT_WIN, null);

        //1.获取缓存中这期的数据
        long score = gamePeriod.getEndTime().getTime();
        List<AwardDetail> awardDetails = redisService.kryoZRevRangeByScoreGet(history100PredictWinkey, Long
                .MIN_VALUE, score + 1, 0, 1, AwardDetail.class);

        if (awardDetails == null || awardDetails.size() <= 0) {
            return result;
        }
        AwardDetail awardDetail = awardDetails.get(0);
        AwardDetail awardDetailNew = (AwardDetail) awardDetail.clone();

        if (awardDetailNew.getPeriodId().equals(gamePeriod.getPeriodId())) {
            awardDetailNew.setHistoryPredictAwardSum(historyAwardSum);
            awardDetailNew.setBonus(predictNumsBonus);
            if (redisService.kryoZRem(history100PredictWinkey, awardDetail) > 0) {
                result = redisService.kryoZAddSet(history100PredictWinkey, score + 1, awardDetailNew);
            }
        } else {
            awardDetail = new AwardDetail();
            int[] awardLevelIntArr = TrendUtil.parseStrToIntArr(awardLevel, CommonConstant
                    .COMMA_SPLIT_STR);

            awardDetail.setGameId(gamePeriod.getGameId());
            awardDetail.setPeriodId(gamePeriod.getPeriodId());
            awardDetail.setBonus(predictNumsBonus);
            awardDetail.setHistoryPredictAwardSum(historyAwardSum);
            awardDetail.setHistoryPredictAwardLevelSum(historyAwardLevel);
            awardDetail.setAwardLevel(awardLevelIntArr);
            dealAwardDeatail(awardDetail);
            result = redisService.kryoZAddSet(history100PredictWinkey, score + 1, awardDetail);
        }

        //2.更新首页redis
        try {
            buildPredictIndexRedis(gamePeriod, historyAwardSum);
        } catch (Exception e) {
            log.error("构建首页redis异常", e);
        }

        return result;
    }

    /*获取某一期的历史中奖情况*/
    private Map<String, Object> getHitoryWinInfo(long gameId, String periodId) {
        Map<String, Object> resultMap = new HashMap<>();
        String cumulateWinMoney = "";
        String lastPeriodId = "";
        String lastPeriodWinNum = "";
        String lastPeriodWinNumOpenText = "";
        int[] awardLevelInfo = null;
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodIdDb(gameId, periodId);
        String predictIndexKey = getPredictIndexShowKey(gameId, lastPeriod);

        Map<String, Object> historyPredictMap = redisService.kryoGet(predictIndexKey, HashMap.class);
        if (historyPredictMap != null) {
            awardLevelInfo = (int[]) historyPredictMap.get("awardLevelInfo");
            cumulateWinMoney = historyPredictMap.get("cumulateWinMoney").toString();
            lastPeriodId = historyPredictMap.get("lastPeriodId").toString();
            lastPeriodWinNum = historyPredictMap.get("lastPeriodWinNum").toString();
        } else {
            lastPeriodId = lastPeriod.getPeriodId();
            lastPeriodWinNum = lastPeriod.getWinningNumbers();
            if (StringUtils.isBlank(lastPeriodWinNum)) {
                lastPeriodWinNumOpenText = "开奖中";
            }
        }

        resultMap.put("awardLevelInfo", awardLevelInfo);
        resultMap.put("cumulateWinMoney", cumulateWinMoney);
        resultMap.put("lastPeriodId", lastPeriodId);
        resultMap.put("lastPeriodWinNum", lastPeriodWinNum);
        resultMap.put("lastPeriodWinNumOpenText", lastPeriodWinNumOpenText);
        return resultMap;
    }

    /*获取用户设备串*/
    private Map<String, String> getPredictInfoByUniqueStr(long gameId, String userIdStr, String deviceIdStr, String
            currentPeriodId) {
        String userPredictTimesKey = "";
        Map<String, String> resultMap = new HashMap<>();

        if (StringUtils.isEmpty(userIdStr)) {
            userPredictTimesKey = RedisConstant.getDevicePredictNumsKey(gameId, "", deviceIdStr, "times");
            resultMap.put("maxCount", CommonConstant.DEVICE_PREDICT_MAX_TIMES.toString());
        } else {
            userPredictTimesKey = RedisConstant.getPredictNumsKey(gameId, currentPeriodId, userIdStr, "times");
            resultMap.put("maxCount", getUserPredictMaxNums(gameId, currentPeriodId, Long.parseLong
                    (userIdStr)).toString());
            resultMap.put("userUniqueStr", userIdStr);
        }

        resultMap.put("userPredictTimesKey", userPredictTimesKey);
        return resultMap;
    }

    private Map generateShowMsg(Integer leftCount, String userId, int areaType, int lastLeftCount, Long gameId, String
            periodId) {
        Map resmap = new HashMap();
        String resStr = "";
        int leadLoginFlag = PredictConstant.PREDICT_LEAD_LOGIN_FLAG_NO;//不引导
        //预测按钮文案信息
        if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_3) {
            resStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant.PREDICT_INDEX_PREPARING, "预测准备中");
        } else {
            if (StringUtils.isEmpty(userId)) {
                resStr = "共*" + CommonConstant.DEVICE_PREDICT_MAX_TIMES + "#次";
                if (leftCount <= 0) {
                    resStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant.PREDICT_INDEX_LEAD_TO_LOGIN,
                            "登录多3次");
                    if (leftCount <= 0 && lastLeftCount <= 0) {
                        leadLoginFlag = PredictConstant.PREDICT_LEAD_LOGIN_FLAG_YES;
                    }
                } else if (leftCount < CommonConstant.DEVICE_PREDICT_MAX_TIMES) {
                    resStr = "还剩*" + leftCount + "#次";
                }
            } else {
                Integer userPredictMaxNums = CommonConstant.USERID_PREDICT_MAX_TIMES;
                if (StringUtils.isNotBlank(periodId)) {
                    userPredictMaxNums = getUserPredictMaxNums(gameId, periodId, Long.parseLong(userId));
                }
                resStr = "共*" + userPredictMaxNums + "#次";
                if (userPredictMaxNums != leftCount) {
                    boolean isVip = vipMemberService.checkUserIsVip(Long.valueOf(userId), VipMemberConstant
                            .VIP_MEMBER_TYPE_DIGIT);
                    if (leftCount <= 0) {
                        leftCount = 0;
                        resStr = "还剩" + leftCount + "次";
                        if (!isVip && lastLeftCount == 0) {
                            leadLoginFlag = PredictConstant.PREDICT_LEAD_LOGIN_FLAG_VIP;
                        }
                        if (!isVip) {
                            resStr = "获取更多";
                        }
                    } else {
                        resStr = "还剩*" + leftCount + "#次";
                    }
                }
            }
        }

        resmap.put("showMsg", resStr);
        resmap.put("leadLoginFlag", leadLoginFlag);
        return resmap;
    }

    /*产生预测号*/
    private String selectNumFromRedis(long gameId, GamePeriod period, String userId, int lastLeftCount, int
            checkTimes) {

        String predictNum = "";
        String allPredictNumKey = RedisConstant.getPredictNumsKey(gameId, period.getPeriodId(), RedisConstant
                .PREDICT_NUMS_TEN_THOUSAND, null);
        String hasGetPredictNumKey = RedisConstant.getPredictNumsKey(gameId, period.getPeriodId(), userId +
                RedisConstant.PREDICT_USER_GOT_NUM_SET, null);

        predictNum = redisService.kryoSrandMember(allPredictNumKey, String.class);
        predictNum = TrendUtil.orderNum(predictNum);
        boolean isInSet = redisService.kryoSismemberSet(hasGetPredictNumKey, predictNum);
        if (isInSet && checkTimes <= CommonConstant.PREDICT_GET_NUM_TIMES) {//
            predictNum = selectNumFromRedis(gameId, period, userId, lastLeftCount, checkTimes + 1);
        }
        redisService.kryoSAddSet(hasGetPredictNumKey, predictNum);
        Long userIdL = StringUtils.isBlank(userId) ? null : Long.parseLong(userId);
        if (lastLeftCount == getUserPredictMaxNums(gameId, period.getPeriodId(), userIdL)) {
            redisService.expire(hasGetPredictNumKey, TrendUtil.getExprieSecond(period.getEndTime(), 36000));//
            // periodId  fix period
        }

        return predictNum;
    }

    /*包装历史预测*/
    private void dealAwardDeatail(AwardDetail awardDetail) {
        Map awardDetailMap = new HashMap();

        if (awardDetail != null && awardDetail.getAwardLevel() != null) {
            int[] awardLevel = awardDetail.getAwardLevel();
            awardDetailMap = convertAwardLevel2Str(awardLevel);
            awardDetail.setAwardLevelStr((String[]) awardDetailMap.get("resArr"));
            awardDetail.setPeriodName(awardDetail.getPeriodId() + "期");
            awardDetail.setMaxAwardLevel((Integer) awardDetailMap.get("maxAwardLevel"));
        }
    }

    private Map<String, Object> convertAwardLevel2Str(int[] levelArr) {
        if (levelArr.length <= 0) {
            return null;
        }
        int maxAwardLevel = 0;
        Map resMap = new HashMap();
        List<String> resList = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < levelArr.length; i++) {
            if (Integer.valueOf(levelArr[i]) != 0) {
                if (maxAwardLevel == 0) {
                    maxAwardLevel = i + 1;
                    resMap.put("maxAwardLevel", maxAwardLevel);
                }
                String tempLeve = stringBuffer.append(TrendUtil.GetCH(i + 1)).append("等奖").
                        append(CommonConstant.SPACE_SPLIT_STR).append(levelArr[i]).append("注").toString();
                resList.add(tempLeve);
            }
            if (stringBuffer.length() > 0) {
                stringBuffer.delete(0, stringBuffer.length());
            }
        }
        resMap.put("resArr", resList.toArray(new String[resList.size()]));
        return resMap;
    }

    private Map judgeIfOpenFlag(int leftCount, String userId, int areaType) {
        int openAwardFlag = 0;
        int ifRunOutFlag = 0;
        String openAwardAlterMsg = "";
        String ifRunOutMsg = "";
        Map resMap = new HashMap();

        if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_3) {
            openAwardFlag = 2;
            openAwardAlterMsg = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                            .PREDICT_INDEX_PREPARING_ALTER_INFO,
                    "请耐心等待，新一期预测马上开始");

        } else {
            if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_2 && leftCount > 0) {
                openAwardFlag = 1;
                openAwardAlterMsg = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                        .PREDICT_INDEX_PREPARING_WAITE_OPENAWARD, "本期官方投注已截止，预测结果仅供参考");
            }
        }

        if (leftCount <= 0 && StringUtils.isNotEmpty(userId) && areaType != GameConstant.PERIOD_TIME_AREA_TYPE_3) {
            ifRunOutFlag = 1;
            ifRunOutMsg = ActivityIniCache.getActivityIniValue(ActivityIniConstant.PREDICT_INDEX_RUN_OUT_TIMES,
                    "本期智慧次数已用完");
        }

        resMap.put("ifRunOutFlag", ifRunOutFlag);
        resMap.put("ifRunOutMsg", ifRunOutMsg);
        resMap.put("openAwardFlag", openAwardFlag);
        resMap.put("openAwardAlterMsg", openAwardAlterMsg);
        return resMap;
    }

    /*获取上一期预测历史展示数据的key*/
    private String getpredictHistoryShowKey(long gameId, String CurrentPeriodId) {
        String resultStr = "";
        GamePeriod lastPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodIdDb(gameId, CurrentPeriodId);
        resultStr = RedisConstant.getPredictNumsKey(gameId, lastPeriod.getPeriodId(), RedisConstant
                .PREDICT_NUMS_HISTROY_PAGE, null);
        if (!redisService.isKeyByteExist(resultStr)) {
            getpredictHistoryShowKey(gameId, lastPeriod.getPeriodId());
        }
        return resultStr;
    }

    /*获取上一期预测首页展示数据的key*/
    private String getPredictIndexShowKey(long gameId, GamePeriod lastPeriod) {
        String resultStr = "";

        resultStr = RedisConstant.getPredictNumsKey(gameId, lastPeriod.getPeriodId(), RedisConstant
                .PREDICT_NUMS_INDEX_INFO, null);
        return resultStr;
    }

    private String calcuHistoryAwardLevel(String historyAwardLevelStr, int[] currentAwardLeveStr) {
        StringBuffer resultStrBuf = new StringBuffer();
        String[] tempHistory = historyAwardLevelStr.split(CommonConstant.COMMA_SPLIT_STR);
        if (tempHistory == null || currentAwardLeveStr == null || tempHistory.length < 6 || currentAwardLeveStr
                .length < 6) {
            throw new BusinessException("历史中奖等级计算异常");
        }
        for (int i = 0; i < tempHistory.length; i++) {
            if (i >= currentAwardLeveStr.length) {
                resultStrBuf.append(Integer.valueOf(tempHistory[i]));
            } else {
                resultStrBuf.append(Integer.valueOf(tempHistory[i]) + currentAwardLeveStr[i]);
            }
            if (i < tempHistory.length - 1) {
                resultStrBuf.append(CommonConstant.COMMA_SPLIT_STR);
            }
        }

        return resultStrBuf.toString();
    }

    private void buildPredictIndexRedis(GamePeriod gamePeriod, int historyAwardSum) {
        Map<String, Object> predictIndex;
        String predictIndexKey = RedisConstant.getPredictNumsKey(gamePeriod.getGameId(), gamePeriod.getPeriodId(),
                RedisConstant.PREDICT_NUMS_INDEX_INFO, null);

        predictIndex = redisService.kryoGet(predictIndexKey, HashMap.class);
        if (predictIndex == null) {
            String historyAwardLevelSum = predictNumbersDao.getHistoryAwardLevelSum(gamePeriod.getGameId(),
                    gamePeriod.getPeriodId());
            String[] awardLevelArr = historyAwardLevelSum.split(CommonConstant.COMMA_SPLIT_STR);
            int[] awardLevelInfo = new int[3];
            if (awardLevelArr != null && awardLevelArr.length > 3) {
                awardLevelInfo[0] = Integer.valueOf(awardLevelArr[0]);
                awardLevelInfo[1] = Integer.valueOf(awardLevelArr[1]);
                awardLevelInfo[2] = Integer.valueOf(awardLevelArr[2]);
            }
            if (predictIndex == null) {
                predictIndex = new HashMap<>();
            }
            predictIndex.put("awardLevelInfo", awardLevelInfo);
            predictIndex.put("cumulateWinMoney", historyAwardSum);
            predictIndex.put("lastPeriodId", gamePeriod.getPeriodId());
            predictIndex.put("lastPeriodWinNum", gamePeriod.getWinningNumbers());

        } else {
            predictIndex.put("cumulateWinMoney", historyAwardSum);
        }
        GamePeriod nextGamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gamePeriod.getGameId(), gamePeriod
                .getPeriodId());
        redisService.kryoSetEx(predictIndexKey, TrendUtil.getExprieSecond(nextGamePeriod.getEndTime(),
                36000), predictIndex);
    }

    private Map<String, Object> getPredictHistoryBonus(PredictInfo predictInfo, String predictNumsLevel, GamePeriod
            lastPeriod, GamePeriod gamePeriod, Boolean floatBonusFlag) {
        Map<String, Object> result = new HashMap<>();
        BigDecimal bonus = predictInfo.calcuAwardBonusByAwardLevel(gamePeriod.getGameId(), gamePeriod.getPeriodId(),
                predictNumsLevel, floatBonusFlag);
        if (bonus != null && bonus.intValue() > 0) {
            //1.获取历史中奖金额，
            String historyAwardSumKey = RedisConstant.getPredictNumsKey(lastPeriod.getGameId(),
                    lastPeriod.getPeriodId(), RedisConstant.PREDICT_NUMS_HISTORY_AWARD_SUM, null);
            String lastHistoryAwardSum = redisService.kryoGet(historyAwardSumKey, String.class);
            if (StringUtils.isBlank(lastHistoryAwardSum)) {
                BigDecimal lastHistoryAwardSumDB = predictNumbersDao.getLastHistoryAwardSum(gamePeriod.getGameId(),
                        gamePeriod.getPeriodId());
                lastHistoryAwardSum = lastHistoryAwardSumDB.toString();
                BigDecimal historyAwardSum = bonus.add(new BigDecimal(lastHistoryAwardSum));
                result.put("historyAwardSum", historyAwardSum.intValue());
            }
            result.put("bonus", bonus);
        }

        return result;
    }

    /**/
    private void saveCumulateAward(GamePeriod lastPeriod, GamePeriod gamePeriod, BigDecimal bonus,
                                   BigDecimal lastHistoryAwardSum) {
        if (bonus != null && bonus.intValue() > 0) {
            String historyAwardSumKey = RedisConstant.getPredictNumsKey(lastPeriod.getGameId(),
                    lastPeriod.getPeriodId(), RedisConstant.PREDICT_NUMS_HISTORY_AWARD_SUM, null);
            //1.保存累计奖金
            BigDecimal historyAwardSum = bonus.add(lastHistoryAwardSum);
            predictNumbersDao.updateHistoryAwardSum(gamePeriod.getGameId(), gamePeriod.getPeriodId(), historyAwardSum
                    .longValue());

            int expireTime = TrendUtil.getExprieSecond(gamePeriod.getEndTime(), 36000);
            redisService.kryoSetEx(historyAwardSumKey, expireTime, historyAwardSum);
        }
    }

    @Override
    public Boolean rebuild100History(Long gameId) {
        List<Map<String, Object>> predicts = predictNumbersDao.getPredictNumsPartInfo(gameId, 100);
        if (predicts.size() != 100) {
            log.error("rebuild100History数量有问题");
            return false;
        }
        for (Map<String, Object> predict : predicts) {
            rebuildHistory(gameId, predict.get("PERIOD_ID").toString());
        }
        return true;
    }

    @Override
    public void rebuildHistory(long gameId, String periodId) {
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);

        PredictNumbers predictNumbers = predictNumbersDao.getPredictNums(gameId, periodId);
        String[] awardLevelStr = predictNumbers.getAwardLevel().split(",");
        int[] awardLevel = new int[awardLevelStr.length];
        for (int i = 0; i < awardLevelStr.length; i++) {
            awardLevel[i] = Integer.valueOf(awardLevelStr[i]);
        }

        AwardDetail awardDetail = new AwardDetail();
        awardDetail.setGameId(gameId);
        awardDetail.setPeriodId(periodId);
        awardDetail.setAwardLevel(awardLevel);
        awardDetail.setHistoryPredictAwardLevelSum(predictNumbers.getHistoryAwardLevelSum());
        awardDetail.setHistoryPredictAwardSum(predictNumbers.getHistoryAwardSum().intValue());

        saveAwardDetailToRedis(awardDetail, gamePeriod);
    }

    @Override
    public void updateUserPredictMaxNums(long gameId, String periodId, Long userId, Integer addNums) {
        /* 给用户增加预测次数*/
        String predictMaxNumbsKey = RedisConstant.getPredictMaxNumsKey(gameId, periodId, userId);
        Integer predictMaxNums = redisService.kryoGet(predictMaxNumbsKey, Integer.class);
        if (null == predictMaxNums) {
            predictMaxNums = CommonConstant.USERID_PREDICT_MAX_TIMES;
        }
        if (predictMaxNums > 200) {
            return;
        }
        predictMaxNums += addNums;
        GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        int expireSeconds = (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), period.getAwardTime());
        redisService.kryoSetEx(predictMaxNumbsKey, expireSeconds, predictMaxNums);
    }

    @Override
    public Integer getUserPredictMaxNums(long gameId, String periodId, Long userId) {
        String predictMaxNumbsKey = RedisConstant.getPredictMaxNumsKey(gameId, periodId, userId);
        Integer predictMaxNums = redisService.kryoGet(predictMaxNumbsKey, Integer.class);
        if (null == predictMaxNums) {
            predictMaxNums = CommonConstant.USERID_PREDICT_MAX_TIMES;
        }
        //vip每个彩种多5次
        if (vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT)) {
            predictMaxNums += VipMemberConstant.VIP_PREDICT_NUM_MORE_TIMES;
        }
        return predictMaxNums;
    }
}