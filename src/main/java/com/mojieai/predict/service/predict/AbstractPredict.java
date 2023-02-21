package com.mojieai.predict.service.predict;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.dao.MissionDao;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.dao.SubscribeProgramDao;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.Mission;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.entity.po.SubscribeProgram;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SubScribeProgramService;
import com.mojieai.predict.service.UserSubscribeInfoService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.PredictUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPredict {

    protected Logger log = LogConstant.commonLog;
    @Autowired
    protected RedisService redisService;
    @Autowired
    private SubscribeProgramDao subScribeProgramDao;
    @Autowired
    protected PredictRedBallDao predictRedBallDao;
    @Autowired
    private MissionDao missionDao;

    public Boolean calculateHistoryPrize(long gameId, String periodId, Integer strType, Integer numType) {
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        if (gamePeriod == null || StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
            return false;
        }
        //1.获取预测号码
        PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, periodId, strType);
        if (predictRedBall == null || StringUtils.isBlank(predictRedBall.getNumStr())) {
            return false;
        }
        Boolean res = false;
        //2.拆分数据
        String blueNumber = gamePeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[1];
        String redNumber = gamePeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];

        String[] predictNum = predictRedBall.getNumStr().split(CommonConstant.COMMON_COLON_STR);
        String predictNumber = predictNum[0];

        String numStr = "";
        Boolean calculateFlag = Boolean.FALSE;
        if (numType.equals(PredictConstant.PREDICT_NUM_TYPE_RED_BALL)) {
            numStr = CommonUtil.markerStarByModelNum(redNumber, predictNumber);
            calculateFlag = Boolean.TRUE;
        } else if (numType.equals(PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL)) {
            numStr = CommonUtil.markerStarByModelNum(blueNumber, predictNumber);
            calculateFlag = Boolean.TRUE;
        }
        boolean ifRefund = false;
        if (calculateFlag) {
            predictRedBallDao.updateNumStrByGameIdPeriodId(gameId, periodId, numStr, strType);
            if (numStr.contains(CommonConstant.COMMON_STAR_STR)) {
                ifRefund = true;
            }
            res = true;
        }

        //4.判断方案并
        if (res) {
            // TODO: 18/3/27 目前只有红蓝球方案暂时只区分红和蓝 方案id只有0 1
            Integer programType = null;
            if (numType.equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED)) {
                programType = PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED;
            }
            if (numType.equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE)) {
                programType = PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE;
            }
            if (programType != null) {
                //4.将该预测类型的退款更新 String key = gameId period programType  predictType userId  partMoney
                SubscribeProgram subscribeProgram = subScribeProgramDao.getSubscribePredictByUnique(predictRedBall
                        .getGameId(), programType, predictRedBall.getStrType());
                if (subscribeProgram != null && subscribeProgram.getBuyType().equals(CommonConstant
                        .PROGRAM_BUY_TYPE_COMPENSATE)) {
                    String partKey = PredictUtil.getPartKillStateRefundMissionId(gameId, Integer.valueOf(periodId),
                            programType, subscribeProgram.getPredictType());
                    Integer refundStatus = Mission.MISSION_STATUS_NO_REFUND;
                    if (ifRefund) {
                        refundStatus = Mission.MISSION_STATUS_REFUND_WAITE;
                    }
                    missionDao.updateMissionStatusByPartKey(partKey, refundStatus, Mission.MISSION_STATUS_INTI);
                    log.error("SubscribeProgram refund finished pls check periodId:" + periodId);
                }
            }
        }
        return res;
    }
}
