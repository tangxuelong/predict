package com.mojieai.predict.service.predict;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.AwardInfoDao;
import com.mojieai.predict.dao.PredictNumbersDao;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.dao.PredictScheduleDao;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.PredictNumOperateService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PredictUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

public abstract class PredictInfo {
    protected static final Logger log = CronEnum.PERIOD.getLogger();

    @Autowired
    protected AwardInfoDao awardInfoDao;
    @Autowired
    protected RedisService redisService;
    @Autowired
    protected PredictScheduleDao predictScheduleDao;
    @Autowired
    private PredictRedBallDao predictRedBallDao;
    @Autowired
    protected PredictNumbersDao predictNumbersDao;
    @Autowired
    protected PredictNumOperateService predictNumOperateService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private PayService payService;

    public abstract Map<String, Object> generatePredictNums(GamePeriod period);

    public abstract void killBluePredict(Long gameId, String periodId, PredictSchedule predictScheduleDirty);

    public abstract String getShowText(String gameEn);

    public Game getGame(String gameEn) {
        return GameCache.getGame(gameEn);
    }

    // 福彩3d获取预测号码
    public Map<String, Object> getPredictNumber(Long userId, Long gameId, String timeSpan, Integer predictType) {
        return null;
    }

    // 福彩3d获取预测号码
    public List<String> productPredictNumbers(Long gameId) {
        return null;
    }

    // 福彩3d首页信息
    public Map<String, Object> predictNumbersIndex(Long userId, Long gameId) {
        return null;
    }

    // 福彩3d计算预测中奖
    public void analysisPredictNumbers(Long gameId, String periodId) {
    }

    public Map<String, Object> predictNumbersHistory(Long gameId, String periodId) {
        return null;
    }

    public Map<String, Object> positionKill(Long gameId) {
        return null;
    }

    public Map<String, Object> threeDanCode(Long gameId) {
        return null;
    }

    public void productPredict() {
    }

    public Set<String> getPredictNums(long gameId, String periodId) {
        Set<String> predictNums = new HashSet<>();
        String predictNumsKey = RedisConstant.getPredictNumsKey(gameId, periodId, RedisConstant
                .PREDICT_NUMS_TEN_THOUSAND, null);
        try {
            predictNums = redisService.kryoSmembers(predictNumsKey, String.class);
            if (predictNums == null || predictNums.size() == 0) {
                PredictNumbers predictNumsPo = predictNumbersDao.getPredictNums(gameId, periodId);
                predictNums = PredictUtil.decompressGBList(predictNumsPo.getPredictNumbers(), periodId);
            }
            //判断是否认为干预
            if (predictNums.size() < PredictConstant.PREDICT_NUMBERS_COUNT) {
                Map operatePredcitNum = predictNumOperateService.getOperatePredictNumRule(gameId, periodId);

            }
        } catch (Exception e) {
            log.error("获取预测号码失败", e);
        }
        return predictNums;
    }

    public String getRedTwentyNums(GamePeriod predictGamePeriod) {
        Game game = GameCache.getGame(predictGamePeriod.getGameId());
        StringBuffer sb = new StringBuffer(game.getGameEn()).append(predictGamePeriod.getPeriodId()).append(IniCache
                .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
        List<String> redList = Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getRedBalls());
        String killCode = getKillThreeCode(predictGamePeriod);

        Collections.shuffle(redList, new Random(new Long((long) sb.toString().hashCode())));
        StringBuffer redTwentyNum = new StringBuffer();
        int count = 0;
        for (int i = 0; i < redList.size(); i++) {
            if (count == 20) {
                break;
            }
            if (!killCode.contains(redList.get(i))) {
                redTwentyNum.append(redList.get(i)).append(CommonConstant.SPACE_SPLIT_STR);
                count++;
            }
        }
        return TrendUtil.orderNum(redTwentyNum.toString().trim());
    }

    public String getKillThreeCode(GamePeriod predictGamePeriod) {
        String killThreeCodeKey = RedisConstant.getPredictNumsKey(predictGamePeriod.getGameId(), predictGamePeriod
                .getPeriodId(), RedisConstant.PREDICT_RED_KILL_THREE_NUMS, null);
        String killCode = redisService.kryoGet(killThreeCodeKey, String.class);
        if (StringUtils.isBlank(killCode)) {
            PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(predictGamePeriod.getGameId(),
                    predictGamePeriod.getPeriodId(), PredictConstant.PREDICT_RED_BALL_STR_TYPE_KILL_THREE);
            if (predictRedBall != null && StringUtils.isNotBlank(predictRedBall.getNumStr())) {
                killCode = predictRedBall.getNumStr();
            } else {
                throw new BusinessException(predictGamePeriod.getPeriodId() + "期杀3码不存在");
            }
        }
        return killCode;
    }

    public List<String> getKillBlue(String gameEn, Integer type) {
        try {
            /* 先从缓存中取，再从数据库取，如果缓存中没有，*/
            List<String> killThreeBlue = getPredictNumber(gameEn, type);
            return killThreeBlue;
        } catch (Exception e) {
            log.error("getKillThreeBlue error" + e.getMessage());
            throw new BusinessException("getKillThreeBlue error" + e.getMessage());
        }
    }

    public List<String> getPredictNumber(String gameEn, Integer predictType) {
        // redis key
        List<String> resultList = new ArrayList<>();
        String redisKey = RedisConstant.getPredictTypeKey(gameEn, predictType);
        Map<String, String> predictNumbers = redisService.kryoGet(redisKey, TreeMap.class);
        if (null == predictNumbers) {
            predictNumbers = new TreeMap<>(Comparator.reverseOrder());
            List<PredictRedBall> predictRedBallList = predictRedBallDao.getPredictRedBalls(GameCache.getGame
                    (gameEn).getGameId(), predictType, 100);

            for (PredictRedBall predictRedBall : predictRedBallList) {
                predictNumbers.put(predictRedBall.getPeriodId(), predictRedBall.getNumStr());
            }

            /* 设置缓存*/
            redisService.kryoSet(redisKey, predictNumbers);
            /* 保持缓存可以刷新*/
            GamePeriod period = PeriodRedis.getCurrentPeriod(GameCache.getGame(gameEn).getGameId());
            redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                    DateUtil.getIntervalSeconds(period.getAwardTime(), 3600)));
        }

        for (Map.Entry entry : predictNumbers.entrySet()) {
            resultList.add(entry.getKey() + "期" + CommonConstant.COMMON_COLON_STR + entry.getValue());
        }
        return resultList;
    }

    public Map<String, Object> getLastKillCode(Long userId, String gameEn, Integer predictType) {
        Game game = GameCache.getGame(gameEn);
        Map<String, Object> lastKillCodeMap = new HashMap<>();
        List<String> lastKillCodeList = getPredictNumber(gameEn, predictType);
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(),
                lastOpenPeriod.getPeriodId());
        Boolean isHaveAccess = Boolean.FALSE;
        lastKillCodeMap.put("isShowButton", Boolean.FALSE);
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());

        // 判断一下有没有权限
        Integer lastKillCodeAccess = (Integer) payService.getAccessIdByType(CommonConstant.ACCESS_LAST_KILL_CODE,
                game.getGameId()).get("itemId");
        if (null != userId && (vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT) ||
                payService.checkUserAccess(userId, game.getGameId(), nextPeriod.getPeriodId(), lastKillCodeAccess))) {
            isHaveAccess = Boolean.TRUE;
        }

        if (!nextPeriod.getPeriodId().equals(currentPeriod.getPeriodId())) {
            // 当前期次结束后 直接放开号码
            isHaveAccess = Boolean.TRUE;
        }

        String currentKillCode = lastKillCodeList.remove(0);
        if (isHaveAccess) {
            lastKillCodeMap.put("currentLastKillCode", currentKillCode);
        } else {
            lastKillCodeMap.put("currentLastKillCode", nextPeriod.getPeriodId() + "期" + CommonConstant
                    .COMMON_COLON_STR + "?" + CommonConstant.COMMON_COLON_STR + "?");
            lastKillCodeMap.put("isShowButton", Boolean.TRUE);
        }
        lastKillCodeMap.put("lastKillCodes", lastKillCodeList);
        return lastKillCodeMap;
    }

    public void rebuildRedKillThree(Long gameId) {
        try {
            /* 获取到所有的红球杀三码记录*/
            List<PredictRedBall> predictRedBallList = predictRedBallDao.getAllPredictRedBall(gameId, PredictConstant
                    .PREDICT_RED_BALL_STR_TYPE_KILL_THREE);
            for (PredictRedBall predictRedBall : predictRedBallList) {
                /* 替换掉所有的*号 相当于计算命中回滚*/
                predictRedBall.setNumStr(predictRedBall.getNumStr().replaceAll(CommonConstant
                        .COMMON_ESCAPE_STR + CommonConstant.COMMON_STAR_STR, ""));
                /* 重新计算命中*/
                GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, predictRedBall.getPeriodId());
                if (StringUtils.isNotBlank(period.getWinningNumbers())) {
                    String redBalls = period.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
                    for (String predictBall : predictRedBall.getNumStr().split(CommonConstant.SPACE_SPLIT_STR)) {
                        if (redBalls.contains(predictBall)) {
                            predictRedBall.setNumStr(predictRedBall.getNumStr().replaceAll(predictBall, CommonConstant
                                    .COMMON_STAR_STR + predictBall));
                        }
                    }
                    predictRedBallDao.updateNumStrByGameIdPeriodId(gameId, period.getPeriodId(), predictRedBall
                                    .getNumStr()
                            , PredictConstant.PREDICT_RED_BALL_STR_TYPE_KILL_THREE);
                }
            }
            /* 刷新redis*/
            GamePeriod predictPeriod = PeriodRedis.getCurrentPeriod(gameId);
            String killThreeCodeKey = RedisConstant.getPredictNumsKey(gameId, predictPeriod.getPeriodId(), RedisConstant
                    .PREDICT_RED_KILL_THREE_NUMS, null);
            redisService.expire(killThreeCodeKey, 100);
        } catch (Exception e) {
            throw new BusinessException("rebuildRedKillThree error" + e.getMessage());
        }
    }

    public void killBluePredictRebuild(String gameEn) {
        Game game = GameCache.getGame(gameEn);
        GamePeriod period = PeriodRedis.getCurrentPeriod(game.getGameId());
        /* 没有完成的schedule*/
        List<PredictSchedule> predictSchedules = predictScheduleDao.getUnFinishedSchedules(game.getGameId(), period
                .getPeriodId());

        Map<Integer, Integer> killBlue = new HashMap<>();
        killBlue.put(PredictConstant.KILL_THREE_BLUE, 3);
        killBlue.put(PredictConstant.KILL_ONE_BLUE, 1);
        for (Map.Entry<Integer, Integer> temp : killBlue.entrySet()) {
            for (PredictSchedule predictSchedule : predictSchedules) {
                killBluePredictCal(game.getGameId(), predictSchedule.getPeriodId(), predictSchedule, temp.getKey(),
                        temp.getValue());
            }
        }
    }

    public void killBluePredictCal(Long gameId, String periodId, PredictSchedule predictScheduleDirty, Integer type,
                                   Integer predictNum) {
        try {
            /* 检查dirtySchedule*/
//            if (null == predictScheduleDirty || predictScheduleDirty.getIfPredictBlueThree().equals
// (CommonStatusEnum.YES
//                    .getStatus())) {
//                log.info("period is already predict three blue code, finish " + CommonUtil.mergeUnionKey(gameId,
//                        periodId));
//                return;
//            }
            /* 检查predictSchedule*/
            GamePeriod predictPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
            PredictSchedule predictSc = predictScheduleDao.getPredictSchedule(gameId, predictPeriod.getPeriodId());
            if (predictSc.getIfPredictBlueThree().equals(CommonStatusEnum.YES.getStatus())) {
                log.info("period is already predict three blue code, finish " + CommonUtil.mergeUnionKey
                        (gameId, periodId));
                return;
            }

            /* 更新上期中奖*//* schedule期次其实为上一期*/
            GamePeriod lastPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            Game game = GameCache.getGame(gameId);
            String redisKey = RedisConstant.getPredictTypeKey(game.getGameEn(), type);
            Map<String, String> predictNumbers = redisService.kryoGet(redisKey, TreeMap.class);

            /* 如果缓存为空，从数据库取*/
            if (null == predictNumbers) {
                predictNumbers = new TreeMap<>(Comparator.reverseOrder());
                /* 取100期，可以从新构建缓存*/
                List<PredictRedBall> predictBalls = predictRedBallDao.getPredictRedBalls(gameId, type, 100);
                if (null != predictBalls) {
                    for (PredictRedBall predictRedBall : predictBalls) {
                        predictNumbers.put(predictRedBall.getPeriodId(), predictRedBall.getNumStr());
                    }
                }
            }

            /* 如果缓存没有，数据库也没有 就不用计算上一期*/
            if (null != predictNumbers) {
                String lastPredictNumber = predictNumbers.get(lastPeriod.getPeriodId());
                if (StringUtils.isNotBlank(lastPredictNumber)) {
                    String blueNumber = lastPeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[1];
                    for (String number : lastPredictNumber.split(CommonConstant.SPACE_SPLIT_STR)) {
                        if (blueNumber.contains(number)) {
                            lastPredictNumber = lastPredictNumber.replaceAll(number, CommonConstant.COMMON_STAR_STR +
                                    number);
                        }
                    }
                    /* 更新开奖数据到数据数据库*/
                    predictRedBallDao.updateNumStrByGameIdPeriodId(gameId, lastPeriod.getPeriodId(), lastPredictNumber,
                            type);
                    predictNumbers.put(lastPeriod.getPeriodId(), lastPredictNumber);
                }
            }

            /* 预测蓝球杀三*//* 检查是否预测*/
            PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, predictPeriod.getPeriodId(),
                    type);
            if (null == predictRedBall) {
                StringBuffer sb = new StringBuffer(game.getGameEn()).append(predictPeriod.getPeriodId()).append(IniCache
                        .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
                List<String> blueList = Arrays.asList(GameEnum.getGameEnumById(gameId).getBlueBalls());

                String[] predictArr = new String[predictNum];
                Collections.shuffle(blueList, new Random(new Long((long) sb.toString().hashCode())));
                for (int i = 0; i < predictNum; i++) {
                    predictArr[i] = blueList.get(i);
                }
                Arrays.sort(predictArr);
                String predictNumber = String.join(CommonConstant.SPACE_SPLIT_STR, predictArr);
                /* 插入数据库*/
                predictRedBall = new PredictRedBall(gameId, predictPeriod.getPeriodId(), type, predictNumber);
                predictRedBallDao.insert(predictRedBall);
                predictNumbers.put(predictPeriod.getPeriodId(), predictNumber);
            }
            // 更新预测期次redis /* 更新开奖期次缓存*/
            redisService.kryoSet(redisKey, predictNumbers);

            redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                    DateUtil.getIntervalSeconds(predictPeriod.getAwardTime(), 3600)));
            /* 更新schedule*/
            predictScheduleDao.updatePredictSchedule(gameId, predictPeriod.getPeriodId(), "IF_PREDICT_BLUE_THREE",
                    "IF_PREDICT_BLUE_THREE_TIME");

        } catch (Exception e) {
            log.error("killBluePredict error type:" + type + "." + e.getMessage());
            throw new BusinessException("killBluePredict error" + e.getMessage());
        }
    }

    public void lastKillCodePredictCal(Long gameId, String periodId, PredictSchedule predictScheduleDirty) {
        /* 检查predictSchedule*/
        GamePeriod predictPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, periodId);
        PredictSchedule predictSc = predictScheduleDao.getPredictSchedule(gameId, predictPeriod.getPeriodId());
        if (predictSc != null && predictSc.getIfPredictLastKillCode().equals(CommonStatusEnum.YES.getStatus())) {
            log.info("period is already predict last kill code, finish " + CommonUtil.mergeUnionKey
                    (gameId, periodId));
            return;
        }

        /* 更新上期中奖*//* schedule期次其实为上一期*/
        GamePeriod lastPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        Game game = GameCache.getGame(gameId);
        String redisKey = RedisConstant.getPredictTypeKey(game.getGameEn(), PredictConstant.LAST_KILL_CODE);
        Map<String, String> predictNumbers = redisService.kryoGet(redisKey, TreeMap.class);

        /* 如果缓存为空，从数据库取*/
        if (null == predictNumbers) {
            predictNumbers = new TreeMap<>(Comparator.reverseOrder());
            /* 取100期，可以从新构建缓存*/
            List<PredictRedBall> predictBalls = predictRedBallDao.getPredictRedBalls(gameId,
                    PredictConstant.LAST_KILL_CODE, 100);
            if (null != predictBalls) {
                for (PredictRedBall predictRedBall : predictBalls) {
                    predictNumbers.put(predictRedBall.getPeriodId(), predictRedBall.getNumStr());
                }
            }
        }

        /* 如果缓存没有，数据库也没有 就不用计算上一期*/
        if (null != predictNumbers) {
            String lastPredictNumber = predictNumbers.get(lastPeriod.getPeriodId());
            if (StringUtils.isNotBlank(lastPredictNumber)) {
                String blueNumber = lastPeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[1];
                String redNumber = lastPeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
                String predictBlueNumber = lastPredictNumber.split(CommonConstant.COMMON_COLON_STR)[1];
                String predictRedNumber = lastPredictNumber.split(CommonConstant.COMMON_COLON_STR)[0];

                if (blueNumber.contains(predictBlueNumber)) {
                    predictBlueNumber = predictBlueNumber.replaceAll(predictBlueNumber, CommonConstant.COMMON_STAR_STR +
                            predictBlueNumber);
                }

                if (redNumber.contains(predictRedNumber)) {
                    predictRedNumber = predictRedNumber.replaceAll(predictRedNumber, CommonConstant.COMMON_STAR_STR +
                            predictRedNumber);
                }

                lastPredictNumber = predictRedNumber + CommonConstant.COMMON_COLON_STR + predictBlueNumber;
                /* 更新开奖数据到数据数据库*/
                predictRedBallDao.updateNumStrByGameIdPeriodId(gameId, lastPeriod.getPeriodId(), lastPredictNumber,
                        PredictConstant.LAST_KILL_CODE);
                predictNumbers.put(lastPeriod.getPeriodId(), lastPredictNumber);
            }
        }
        /* 预测绝杀码*//* 检查是否预测*/
        PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, predictPeriod.getPeriodId(),
                PredictConstant.LAST_KILL_CODE);
        if (null == predictRedBall) {
            // 红球
            StringBuffer redSb = new StringBuffer(game.getGameEn()).append(predictPeriod.getPeriodId()).append(IniCache
                    .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
            List<String> redList = Arrays.asList(GameEnum.getGameEnumById(game.getGameId()).getRedBalls());

            Collections.shuffle(redList, new Random(new Long((long) redSb.toString().hashCode())));

            String predictRedLastCode = redList.get(20);

            // 篮球
            StringBuffer sb = new StringBuffer(game.getGameEn()).append(predictPeriod.getPeriodId()).append(IniCache
                    .getIniValue(IniConstant.RANDOM_CODE, CommonConstant.RANDOM_CODE));
            List<String> blueList = Arrays.asList(GameEnum.getGameEnumById(gameId).getBlueBalls());

            Collections.shuffle(blueList, new Random(new Long((long) sb.toString().hashCode())));

            String predictBlueLastCode = blueList.get(4);
            String predictNumber = predictRedLastCode + CommonConstant.COMMON_COLON_STR + predictBlueLastCode;


            /* 插入数据库*/
            predictRedBall = new PredictRedBall(gameId, predictPeriod.getPeriodId(), PredictConstant.LAST_KILL_CODE,
                    predictNumber);
            predictRedBallDao.insert(predictRedBall);
            predictNumbers.put(predictPeriod.getPeriodId(), predictNumber);
        }
        // 更新预测期次redis /* 更新开奖期次缓存*/
        redisService.kryoSet(redisKey, predictNumbers);

        redisService.expire(redisKey, (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                DateUtil.getIntervalSeconds(predictPeriod.getAwardTime(), 3600)));
        /* 更新schedule*/
        predictScheduleDao.updatePredictSchedule(gameId, predictPeriod.getPeriodId(), "IF_PREDICT_LAST_KILL_CODE",
                "IF_PREDICT_LAST_KILL_CODE_TIME");

    }

    public List<String> getPredictIndexFromFile(GamePeriod period, String allNumber, String gameEn, String fileName) {
        String[] redBalls = allNumber.split(CommonConstant.SPACE_SPLIT_STR);
        String urlSeparator30 = String.valueOf(File.separatorChar);
        String path30 = getClass().getResource(CommonConstant.COMMON_DOT_STR).getPath();
        String subPath30 = path30.substring(0, path30.indexOf("WEB-INF") + 8);
        String rootPath30 = subPath30.substring(0, subPath30.lastIndexOf(urlSeparator30));
        rootPath30 = rootPath30 + File.separatorChar + "classes" + File.separatorChar + "filterfile" + File
                .separatorChar + gameEn + File.separatorChar + fileName;
        File file30 = new File(rootPath30);
        List<String> tempList = new ArrayList<>();

        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(file30));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while (StringUtils.isNotBlank(lineTxt = bufferedReader.readLine())) {
                if (lineTxt.contains(CommonConstant.COMMON_ADD_STR)) {
                    lineTxt = lineTxt.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_ADD_STR)[0];
                }
                String[] placeArray = lineTxt.split(CommonConstant.SPACE_SPLIT_STR);
                String number30 = "";
                for (String place : placeArray) {
                    number30 = number30 + redBalls[Integer.parseInt(place) - 1] + CommonConstant.SPACE_SPLIT_STR;
                }
                tempList.add(TrendUtil.orderNum(number30.substring(0, number30.length() - 1)));
            }
            read.close();
        } catch (Throwable e) {
            log.error("read 30 file error" + period, e);
        }
        return tempList;
    }

    public AwardDetail calcuAwardDetail(long gameId, Set<String> predictNumberList, String periodId, Boolean
            ifFloatBonus) {
        AwardDetail result = new AwardDetail();
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);

        if (gamePeriod == null || StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
            return result;
        }

        try {
            int[] awardLevel = calculateAwardLevel(gamePeriod, predictNumberList);
            result = calculateBonusByAwardLevel(gamePeriod, awardLevel, ifFloatBonus);
        } catch (Throwable e) {
            log.error("calculate " + periodId + " AwardDetail error. ", e);
        }
        return result;
    }

    /*计算预测号码的奖级*/
    public int[] calculateAwardLevel(GamePeriod period, Set<String> numberList) {
        int[] totalAward = null;
        long gameId = period.getGameId();
        AbstractGame ag = GameFactory.getInstance().getGameBean(gameId);
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
        return totalAward;
    }

    public BigDecimal calcuAwardBonusByAwardLevel(long gameId, String periodId, String awardLevel, Boolean
            floatBonusFlag) {
        BigDecimal result = null;
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        String[] totalAwardArr = awardLevel.split(CommonConstant.COMMA_SPLIT_STR);
        if (totalAwardArr != null && totalAwardArr.length > 0) {
            int[] totalAward = new int[totalAwardArr.length];
            for (int i = 0; i < totalAwardArr.length; i++) {
                totalAward[i] = Integer.valueOf(totalAwardArr[i]);
            }
            AwardDetail awardDetail = calculateBonusByAwardLevel(gamePeriod, totalAward, floatBonusFlag);
            if (awardDetail != null && awardDetail.getBonus().intValue() > 0) {
                result = awardDetail.getBonus();
            }
        }
        return result;
    }

    /*通过中奖等级计算中奖金额（必须已经开奖才能调）*/
    private AwardDetail calculateBonusByAwardLevel(GamePeriod period, int[] totalAward, Boolean isFloatBonus) {
        long gameId = period.getGameId();
        AbstractGame ag = GameFactory.getInstance().getGameBean(gameId);
        //计算总奖金
        List<AwardInfo> awardInfos = GameFactory.getInstance().getGameBean(period.getGameId())
                .getDefaultAwardInfoList();
        if (isFloatBonus) {
            awardInfos = redisService.kryoHget(RedisConstant.getAwardInfoKey(period.getGameId()), period.getPeriodId
                    (), ArrayList.class);
            if (awardInfos == null) {
                awardInfos = awardInfoDao.getAwardInfos(period.getGameId(), period.getPeriodId());
            }
        }

        //包装奖级信息，有些期次对应奖级并没有奖金需要按照官方法则再次计算
        BigDecimal bonus = ag.processAwardInfo(totalAward, period, awardInfos);
        AwardDetail detail = new AwardDetail(period.getGameId(), period.getPeriodId(), bonus, totalAward);
        return detail;
    }

    public String getLastKillCodeShowText(String gameEn) {
        return PredictUtil.getShowTextCal(gameEn, PredictConstant.LAST_KILL_CODE_EXPIRE_MSG, PredictConstant
                .LAST_KILL_CODE_MSG);
    }
}
