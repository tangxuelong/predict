package com.mojieai.predict.service.manualpredicthistory;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.dao.PredictNumbersOperateDao;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictNumbersOperate;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.util.PredictUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public abstract class ManualPredictNum {
    protected static final Logger log = CronEnum.PERIOD.getLogger();

    @Autowired
    protected PredictRedBallDao predictRedBallDao;
    @Autowired
    protected PredictNumbersOperateDao predictNumbersOperateDao;

    public abstract String generateBigBonusByAwardLevel(String winningNum, int awardLevel, Set<String> predictNums,
                                                        List<String> redList, List<String> blueList, List<String>
                                                                redWinBalls, List<String> blueWinBalls);

    public abstract Set<String> autoFill500PredictNum(int redKillWrongCount, int blueKillWrongCount, List<String>
            redWinList, List<String> blueWinList, List<String> redList, List<String> blueList, String[] ruleArr);

    public abstract Integer getRedKillBallType();

    public abstract Integer getBlueKillBallType();

    public abstract String generateRandomNum(List<String> redBallList, List<String> blueBallList);

    /*操作预测号码历史*/
    public boolean operatePredictNums(long gameId, String periodId, Set<String> predictNums) {
        try {
            PredictNumbersOperate predictNumbersOperate = predictNumbersOperateDao.getPredictNumPoByGameIdAndPeriodId
                    (gameId, periodId);
            if (predictNumbersOperate != null && predictNumbersOperate.getOperateNums() != null &&
                    predictNumbersOperate.getOperateNums().length > 0) {
                Set<String> operateNums = PredictUtil.decompressGBList(predictNumbersOperate.getOperateNums(),
                        periodId);
                predictNums.addAll(operateNums);
                return true;
            }

            List<String> redList = new ArrayList<>();
            List<String> blueList = new ArrayList<>();
            redList.addAll(Arrays.asList(GameEnum.getGameEnumById(gameId).getRedBalls()));
            blueList.addAll(Arrays.asList(GameEnum.getGameEnumById(gameId).getBlueBalls()));

            ManualPredictNum manualPredictNum = ManualPredictHistoryFactory.getInstance().getManualPredictNum(GameCache
                    .getGame(gameId).getGameEn());

            //1.获取当期杀三码和蓝球杀码
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            Map<String, Object> killBallInfo = getKillBallInfoByGameId(gameId, periodId);
            if (killBallInfo == null) {
                throw new BusinessException("gameId:" + gameId + " periodId:" + periodId + "获取杀码信息异常");
            }
            List<String> redKillBalls = (List<String>) killBallInfo.get("redKillBalls");
            List<String> blueKillBalls = (List<String>) killBallInfo.get("blueKillBalls");
            Integer redKillWrongCount = (Integer) killBallInfo.get("redKillWrongCount");
            Integer blueKillWrongCount = (Integer) killBallInfo.get("blueKillWrongCount");

            //2.判断是否人工干预
            String[] ruleArr = getOperateRule(gameId, periodId);

            List<String> redWinList = new ArrayList<>();
            List<String> blueWinList = new ArrayList<>();
            convertWinNum(gamePeriod.getWinningNumbers(), redWinList, blueWinList);
            //4.红球和蓝球去除杀号和中奖号
            removeKillNumAndWinNum(redKillBalls, blueKillBalls, redList, blueList, redWinList, blueWinList);

            Collections.shuffle(redList, new Random(System.currentTimeMillis()));
            Collections.shuffle(blueList, new Random(System.currentTimeMillis()));

            //6.根据杀号情况拼凑500期数据
            Set<String> autoFillWinNum = autoFill500PredictNum(redKillWrongCount, blueKillWrongCount, redWinList,
                    blueWinList, redList, blueList, ruleArr);

            //3.生成大奖
            Set<String> realAutoFillWinNum = new HashSet<>();
            if (ruleArr != null) {
                Set<String> operateNums = generateBigBonusNum(gamePeriod.getWinningNumbers(), ruleArr, predictNums,
                        redList, blueList, redWinList, blueWinList);
                predictNums.addAll(operateNums);
                realAutoFillWinNum.addAll(operateNums);
            }

            for (String tempNum : autoFillWinNum) {
                if (predictNums.size() >= 10000) {
                    break;
                }
                predictNums.add(tempNum);
                realAutoFillWinNum.add(tempNum);
            }

            //如果不够一万注随机拼凑
            if (predictNums.size() < 10000) {
                while (true) {
                    if (predictNums.size() >= 10000) {
                        break;
                    }
                    predictNums.add(manualPredictNum.generateRandomNum(redList, blueList));
                    realAutoFillWinNum.add(manualPredictNum.generateRandomNum(redList, blueList));
                }
            }
            if (predictNumbersOperate == null) {
                predictNumbersOperate = new PredictNumbersOperate();
                predictNumbersOperate.setOperateNums(PredictUtil.compressGBList(realAutoFillWinNum, gameId, periodId));
                predictNumbersOperate.setGameId(gameId);
                predictNumbersOperate.setPeriodId(periodId);
                predictNumbersOperateDao.insert(predictNumbersOperate);
            } else {
                predictNumbersOperateDao.saveOperatePredictNums(gameId, periodId, PredictUtil.compressGBList
                        (realAutoFillWinNum, gameId, periodId));
            }


        } catch (Exception e) {
            log.error("人工干预一万注异常", e);
        }
        return true;
    }

    /*去除红球 蓝球中杀三码和中奖号，去除红球中奖list中的杀码*/
    public void removeKillNumAndWinNum(List<String> redKills, List<String> blueKills, List<String> redList,
                                       List<String> blueList, List<String> redWinList, List<String> blueWinList) {
        //1.号码集中去除中奖号
        for (String redWinNumTemp : redWinList) {
            redList.remove(redWinNumTemp);
        }
        for (String blueWinNumTemp : blueWinList) {
            blueList.remove(blueWinNumTemp);
        }
        //2.号码集中去除杀号
        removeKillNum(redList, redWinList, redKills);
        removeKillNum(blueList, blueWinList, blueKills);
    }

    /*产生大奖*/
    public Set<String> generateBigBonusNum(String winNum, String[] ruleArr, Set<String> predictNums, List<String>
            redList, List<String> blueList, List<String> redWinBalls, List<String> blueWinBalls) {
        Set<String> operateNums = new HashSet<>();
        if (Integer.valueOf(ruleArr[0]) == 1) {
            if (!predictNums.contains(winNum)) {
                operateNums.add(winNum);
            } else {
                log.info("the predict nums has predict head award, will add rand num");
                generateBigBonusByAwardLevel(winNum, -1, predictNums, redList, blueList, redWinBalls, blueWinBalls);
            }
        }
        for (int i = 1; i < ruleArr.length; i++) {
            for (int j = 0; j < Integer.valueOf(ruleArr[i]); j++) {
                operateNums.add(generateBigBonusByAwardLevel(winNum, i + 1, predictNums, redList, blueList,
                        redWinBalls, blueWinBalls));
            }
        }
        return operateNums;
    }

    /*获取杀三码信息*/
    public Map<String, Object> getKillBallInfoByGameId(long gameId, String periodId) {
        Map<String, Object> result = new HashMap<>();

        int redBallType = getRedKillBallType();
        int blueBallType = getBlueKillBallType();

        PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, periodId, redBallType);
        PredictRedBall predictBlueBall = predictRedBallDao.getPredictRedBall(gameId, periodId, blueBallType);
        String[] redKillThreeArr = predictRedBall.getNumStr().split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                .COMMON_STAR_STR);
        String[] blueKillThreeArr = predictBlueBall.getNumStr().split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                .COMMON_STAR_STR);
        //2.红蓝球杀错个数
        int redKillWrongCount = redKillThreeArr.length - 1;
        int blueKillWrongCount = blueKillThreeArr.length - 1;

        List<String> redKillBalls = new ArrayList<>();
        List<String> blueKillBalls = new ArrayList<>();
        redKillBalls.addAll(Arrays.asList(redKillThreeArr));
        blueKillBalls.addAll(Arrays.asList(blueKillThreeArr));

        result.put("redKillBalls", redKillBalls);
        result.put("blueKillBalls", blueKillBalls);
        result.put("redKillWrongCount", redKillWrongCount);
        result.put("blueKillWrongCount", blueKillWrongCount);
        return result;
    }

    /*获取人工干预信息*/
    public String[] getOperateRule(long gameId, String periodId) {
        String[] ruleArr = null;

        PredictNumbersOperate operatePredictNums = predictNumbersOperateDao.getPredictNumPoByGameIdAndPeriodId
                (gameId, periodId);
        if (operatePredictNums != null && operatePredictNums.getStatus() == PredictConstant
                .PREDICT_OPERATE_NUMS_STATUS_YES) {
            ruleArr = operatePredictNums.getRuleStr().split(CommonConstant.COMMON_COLON_STR);
        }
        return ruleArr;
    }

    public void convertWinNum(String winNum, List<String> redWinList, List<String> blueWinList) {
        String[] redWinNumArr = winNum.split(CommonConstant.COMMON_COLON_STR)[0].split(CommonConstant.SPACE_SPLIT_STR);
        String[] blueWinNumArr = winNum.split(CommonConstant.COMMON_COLON_STR)[1].split(CommonConstant.SPACE_SPLIT_STR);

        redWinList.addAll(Arrays.asList(redWinNumArr));
        blueWinList.addAll(Arrays.asList(blueWinNumArr));
    }

    private void removeKillNum(List<String> balls, List<String> winBalls, List<String> killBallArr) {
        for (String killBlueNum : killBallArr) {
            if (killBlueNum.contains(CommonConstant.SPACE_SPLIT_STR)) {
                String[] killBlueBallArr = killBlueNum.split(CommonConstant.SPACE_SPLIT_STR);
                for (String numCode : killBlueBallArr) {
                    balls.remove(numCode);
                    winBalls.remove(numCode);
                }
            } else {
                balls.remove(killBlueNum);
                winBalls.remove(killBlueNum);
            }
        }
    }
}
