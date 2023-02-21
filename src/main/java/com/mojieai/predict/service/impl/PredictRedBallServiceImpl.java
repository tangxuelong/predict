package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.dao.PredictScheduleDao;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.entity.po.PredictSchedule;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.predict.PickNumEnum;
import com.mojieai.predict.enums.predict.PickNumPredict;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PredictRedBallService;
import com.mojieai.predict.service.SendEmailService;
import com.mojieai.predict.service.predict.AbstractPredictDb;
import com.mojieai.predict.service.predict.PredictFactory;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PredictRedBallServiceImpl implements PredictRedBallService {
    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Autowired
    private RedisService redisService;
    @Autowired
    private PredictRedBallDao predictRedBallDao;
    @Autowired
    private PredictScheduleDao predictScheduleDao;
    @Autowired
    private GamePeriodDao gamePeriodDao;
    @Autowired
    private SendEmailService sendEmailService;

    @Override
    public void generateRedTwentyNums(Task task, PredictSchedule dirtyPredictSchedule) {
        if (dirtyPredictSchedule == null || (dirtyPredictSchedule != null && dirtyPredictSchedule
                .getIfPredictRedBallTwenty() == CommonStatusEnum.YES.getStatus())) {
            return;
        }

        PredictSchedule predictSchedule = predictScheduleDao.getPredictSchedule(task.getGameId(), task.getPeriodId());

        if (predictSchedule.getIfAward() == CommonStatusEnum.YES.getStatus() && predictSchedule
                .getIfPredictRedBallTwenty() == CommonStatusEnum.NO.getStatus()) {

            try {
                //1.给历史期算奖
                Game game = GameCache.getGame(task.getGameId());
                GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(task.getGameId(), task.getPeriodId());
                PredictRedBall oldPredictRedBall = predictRedBallDao.getPredictRedBall(task.getGameId(), task
                        .getPeriodId(), PredictConstant.PREDICT_RED_BALL_STR_TYPE_TWENTY);
                String[] winningNumArr = gamePeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR);
                if (winningNumArr != null && winningNumArr.length > 0) {
                    String redTwentyBall = oldPredictRedBall.getNumStr().replaceAll(CommonConstant.COMMON_ESCAPE_STR +
                            CommonConstant.COMMON_STAR_STR, CommonConstant.SPACE_NULL_STR);
                    String[] redWinNumArr = winningNumArr[0].split(CommonConstant.SPACE_SPLIT_STR);
                    int count = 0;
                    for (String redWinNum : redWinNumArr) {
                        if (redTwentyBall.contains(redWinNum)) {
                            redTwentyBall = redTwentyBall.replace(redWinNum, CommonConstant.COMMON_STAR_STR +
                                    redWinNum);
                            count++;
                        }
                    }
                    if (count > 0) {
                        int updateRes = predictRedBallDao.updateNumStrByGameIdPeriodId(task.getGameId(), task
                                .getPeriodId(), redTwentyBall, PredictConstant.PREDICT_RED_BALL_STR_TYPE_TWENTY);
                        if (updateRes > 0 && ((game.getGameEn().equals(GameConstant.SSQ) && count >= 5) || (game
                                .getGameEn().equals(GameConstant.DLT) && count >= 4))) {
//                            sendEmailService.SendEmail(game.getGameEn() + "红20中" + count, "periodId:" + task
//                                    .getPeriodId() + "期" + redTwentyBall);
                        }
                    }
                }

                //1.拿到当期杀3码
                GamePeriod nextGamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(task.getGameId(), task
                        .getPeriodId());
                String redTwentyNum = PredictFactory.getInstance().getPredictInfo(game.getGameEn()).getRedTwentyNums
                        (nextGamePeriod);

                if (StringUtils.isNotBlank(redTwentyNum) && nextGamePeriod != null) {
                    int insertNum = 0;
                    PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(nextGamePeriod.getGameId(),
                            nextGamePeriod.getPeriodId(), PredictConstant.PREDICT_RED_BALL_STR_TYPE_TWENTY);
                    if (predictRedBall == null) {
                        predictRedBall = new PredictRedBall();
                        predictRedBall.setGameId(nextGamePeriod.getGameId());
                        predictRedBall.setPeriodId(nextGamePeriod.getPeriodId());
                        predictRedBall.setNumStr(redTwentyNum);
                        predictRedBall.setStrType(PredictConstant.PREDICT_RED_BALL_STR_TYPE_TWENTY);

                        if (saveRedTwentyNums2Reids(nextGamePeriod, redTwentyNum)) {
                            insertNum = predictRedBallDao.insert(predictRedBall);
                        }
                    } else {
                        redTwentyNum = predictRedBall.getNumStr();
                        if (saveRedTwentyNums2Reids(nextGamePeriod, redTwentyNum)) {
                            insertNum = 1;
                        }
                    }
                    if (insertNum > 0) {
                        predictScheduleDao.updatePredictSchedule(task.getGameId(), task.getPeriodId(),
                                "IF_PREDICT_RED_BALL_TWENTY", "IF_PREDICT_RED_BALL_TWENTY_TIME");
                    }
                }
            } catch (Exception e) {
                log.error("预测红20码异常 periodId:" + task.getPeriodId(), e);
            }
        }
    }

    @Override
    public Map<String, Object> getRedTwentyNumsByGameId(long gameId) {
        String redNums = "";
        String periodId = "";
        int btnShowFlag = CommonStatusEnum.YES.getStatus();
        String openAwardAlterMsg = "";
        Map<String, Object> resultMap = new HashMap<>();
        String[] redArr = new String[]{"?", "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", "?",
                "?", "?", "?", "?", "?",};

        Map areaPeriodMap = TrendUtil.getAreaTypeAndPeriod(gameId, redisService);
        if (areaPeriodMap != null) {
            GamePeriod gamePeriod = (GamePeriod) areaPeriodMap.get("period");
            if (gamePeriod != null) {
                periodId = gamePeriod.getPeriodId();
                int areaType = (int) areaPeriodMap.get("areaType");

                if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_3) {
                    openAwardAlterMsg = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                            .RED_TWENTY_PREDICT_PREPARING_MSG, "预测需要时间，请耐心等待哦");
                    btnShowFlag = CommonStatusEnum.NO.getStatus();
                } else if (areaType == GameConstant.PERIOD_TIME_AREA_TYPE_2) {
                    openAwardAlterMsg = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                            .RED_TWENTY_PREPARING_WAITE_OPENAWARD, "本期官方投注已截止，以下结果仅供参考");
                }

                String redTwentyNumsKey = RedisConstant.getPredictNumsKey(gameId, gamePeriod.getPeriodId(),
                        RedisConstant.PREDICT_RED_TWENTY_NUMS, null);
                redNums = redisService.kryoGet(redTwentyNumsKey, String.class);
                if (StringUtils.isNotBlank(redNums)) {
                    redNums = TrendUtil.orderNum(redNums);
                    redArr = redNums.split(CommonConstant.SPACE_SPLIT_STR);
                }
            }
        }

        resultMap.put("adTitle", "预测说明");
        resultMap.put("redTwentyNums", redArr);
        resultMap.put("btnShowFlag", btnShowFlag);
        resultMap.put("currentPeriodId", periodId);
        resultMap.put("openAwardAlterMsg", openAwardAlterMsg);
        resultMap.put("adMsg", ActivityIniCache.getActivityIniValue(ActivityIniConstant.PERIOD_RED_TWENTY_NUMS,
                "小智为您精心准备红球20码。"));
        return resultMap;
    }

    @Override
    public Map<String, Object> getKillThreeCodeByGameId(long gameId) {
        String openAwardInfo = "";
        Map<String, Object> resultMap = new HashMap<>();
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        GamePeriod willOpenPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastOpenPeriod
                .getPeriodId());
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);

        List<Map<String, Object>> periodList = getRedKillThreeList(gameId, lastOpenPeriod, willOpenPeriod,
                currentPeriod);
        if (!willOpenPeriod.getPeriodId().equals(currentPeriod.getPeriodId())) {
            openAwardInfo = "本期官方投注已截止，以下结果仅供参考";
        }

        resultMap.put("periodList", periodList);
        resultMap.put("openAwardInfo", openAwardInfo);
        resultMap.put("currentPeriodId", currentPeriod.getPeriodId());
        return resultMap;
    }

    /*获取杀3码*/
    @Override
    public List<Map<String, Object>> getRedKillThreeList(long gameId, GamePeriod lastOpenPeriod, GamePeriod
            openPeriodNextPeriod, GamePeriod currentPeriod) {
        List<Map<String, Object>> resultList = null;

        if (lastOpenPeriod != null && openPeriodNextPeriod != null && currentPeriod != null) {
            try {
                String redKillThreeCodeKey = RedisConstant.getPredictNumsKey(gameId, openPeriodNextPeriod
                        .getPeriodId
                                (), RedisConstant.PREDICT_RED_KILL_THREE_NUMS, null);
                resultList = redisService.kryoGet(redKillThreeCodeKey, ArrayList.class);
                if (resultList == null || resultList.size() <= 0) {
                    resultList = refreshRedKillThreeList(gameId);
                }
            } catch (Exception e) {
                log.error("getRedKillThreeList error", e);
            }
        } else {
            log.error("获取杀三码列表错误：lastOpenPeriod:" + lastOpenPeriod + " openPeriodNextPeriod: " +
                    openPeriodNextPeriod + " currentPeriod: " + currentPeriod);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> refreshRedKillThreeList(long gameId) {
        String periodIdDB = "";
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            List<PredictRedBall> predictRedBalls = predictRedBallDao.getPredictRedBalls(gameId,
                    PredictConstant.PREDICT_RED_BALL_STR_TYPE_KILL_THREE, 100);
            if (predictRedBalls != null && predictRedBalls.size() > 0) {
                for (PredictRedBall predictRedBall : predictRedBalls) {
                    Map tempMap = new HashMap();
                    tempMap.put("periodName", predictRedBall.getPeriodId() + "期");
                    tempMap.put("killCode", predictRedBall.getNumStr());
                    resultList.add(tempMap);
                }
                periodIdDB = predictRedBalls.get(0).getPeriodId();
                GamePeriod currentKillPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodIdDB);
                String redKillThreeCodeKey = RedisConstant.getPredictNumsKey(gameId, periodIdDB, RedisConstant
                        .PREDICT_RED_KILL_THREE_NUMS, null);
                int expireTime = TrendUtil.getExprieSecond(currentKillPeriod.getAwardTime(), 120);
                redisService.kryoSetEx(redKillThreeCodeKey, expireTime, resultList);
            } else {
                log.error("从db中未能查询杀三码，请确认");
            }
        } catch (Exception e) {
            log.error("refreshRedKillThreeList error", e);
        }
        return resultList;
    }

    /*保存预测杀3码，并对上期杀3码算奖*/
    @Override
    public int savePredictKillCodeAndBonusKillCode(GamePeriod predictPeriod, GamePeriod gamePeriod, String
            predictKillNum) {
        int result = 0;
        predictKillNum = TrendUtil.orderNum(predictKillNum);
        //1。保存预测号码
        int predictRes = saveKillThreeCode(predictPeriod.getGameId(), predictPeriod.getPeriodId(), predictKillNum);
        int bonusRes = saveKillThreeCode(gamePeriod.getGameId(), gamePeriod.getPeriodId(), "");
        if (predictRes > 0 && bonusRes > 0) {
            result = 1;
        }
        return result;
    }

    @Override
    public int saveKillThreeCode(long gameId, String periodId, String killNum) {
        int res = 0;
        PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, periodId, PredictConstant
                .PREDICT_RED_BALL_STR_TYPE_KILL_THREE);
        if (predictRedBall == null) {
            res = predictRedBallDao.insert(new PredictRedBall(gameId, periodId, PredictConstant
                    .PREDICT_RED_BALL_STR_TYPE_KILL_THREE, killNum));
        } else if (StringUtils.isNotBlank(predictRedBall.getNumStr())) {
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            String winnNum = gamePeriod.getWinningNumbers();
            if (StringUtils.isBlank(winnNum)) {
                winnNum = gamePeriodDao.getPeriodByGameIdAndPeriod(gameId, periodId).getWinningNumbers();
            }
            killNum = bonusKillThreeNum(predictRedBall.getNumStr(), winnNum.substring(0, winnNum.lastIndexOf
                    (CommonConstant.COMMON_COLON_STR)));
            res = predictRedBallDao.updateNumStrByGameIdPeriodId(gameId, periodId, killNum, PredictConstant
                    .PREDICT_RED_BALL_STR_TYPE_KILL_THREE);
        } else {
            log.error("saveKillThreeCode error predictRedBall" + predictRedBall.toString());
        }
        return res;
    }

    private Boolean saveRedTwentyNums2Reids(GamePeriod nextGamePeriod, String allNums) {
        Boolean res = false;
        if (nextGamePeriod != null) {
            String redTwentyNumsKey = RedisConstant.getPredictNumsKey(nextGamePeriod.getGameId(), nextGamePeriod
                    .getPeriodId(), RedisConstant.PREDICT_RED_TWENTY_NUMS, null);
            int expireTime = TrendUtil.getExprieSecond(nextGamePeriod.getEndTime(), 36000);
            res = redisService.kryoSetEx(redTwentyNumsKey, expireTime, allNums);
        }
        return res;
    }

    private String bonusKillThreeNum(String killNum, String winningNums) {
        if (StringUtils.isBlank(killNum) || killNum.contains(CommonConstant.COMMON_STAR_STR)) {
            return killNum;
        }
        String[] killNumArr = killNum.split(CommonConstant.SPACE_SPLIT_STR);
        for (String tempNum : killNumArr) {
            if (winningNums.contains(tempNum)) {
                killNum = killNum.replaceAll(tempNum, CommonConstant.COMMON_STAR_STR + tempNum);
            }
        }
        return killNum.trim();
    }

    @Override
    public void predictRedBallTiming() {
        for (Game game : GameCache.getAllGameMap().values()) {
            if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                if (!game.getGameEn().equals(GameConstant.FC3D)) {
                    GamePeriod gamePeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
                    GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), gamePeriod
                            .getPeriodId());
                    String predictRedBallKey = RedisConstant.getPredictRedBallFlagKey(game.getGameEn());
                    String periodId = redisService.kryoGet(predictRedBallKey, String.class);
                    if (periodId != null && periodId.equals(nextPeriod.getPeriodId())) {
                        continue;
                    }
                    Boolean res = Boolean.TRUE;
                    AbstractPredictDb abstractPredictDb = PredictFactory.getInstance().getPredictDb(game.getGameEn());
                    for (PickNumPredict pickNumPredict : PickNumEnum.getPickNumEnum(game.getGameEn())
                            .getGamePickNumEnum()) {
                        Boolean generateRes = pickNumPredict.generatePredictNum(abstractPredictDb, nextPeriod
                                .getPeriodId());
                        if (!generateRes) {
                            res = Boolean.FALSE;
                        }
                    }
                    if (res) {
                        //保存标志为
                        int expireTime = TrendUtil.getExprieSecond(nextPeriod.getAwardTime(), 3600);
                        redisService.kryoSetEx(predictRedBallKey, expireTime, nextPeriod.getPeriodId());
                    }
                }
            }
        }
    }
}
