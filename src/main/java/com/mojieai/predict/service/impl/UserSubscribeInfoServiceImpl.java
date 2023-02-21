package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.MissionDao;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.dao.SubscribeProgramDao;
import com.mojieai.predict.dao.UserSubscribeInfoDao;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.enums.predict.PickNumEnum;
import com.mojieai.predict.enums.predict.PickNumPredict;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.UserSubscribeInfoService;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserSubscribeInfoServiceImpl implements UserSubscribeInfoService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UserSubscribeInfoDao userSubscribeInfoDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PayService payService;
    @Autowired
    private SubscribeProgramDao subscribeProgramDao;
    @Autowired
    private MissionDao missionDao;
    @Autowired
    private PredictRedBallDao predictRedBallDao;

    @Override
    public Boolean checkUserSubscribePredict(Long userId, Integer subscribeProgramId) {
        if (userId == null) {
            return PredictConstant.SUBSCRIBE_STATUS_NO;
        }
        SubscribeProgram program = subscribeProgramDao.getSubscribePredictByProgramId(subscribeProgramId);//todo cache
        if (program == null) {
            return PredictConstant.SUBSCRIBE_STATUS_NO;
        }
        //1.check期次信息
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(program.getGameId());
        if (currentPeriod == null || StringUtils.isBlank(currentPeriod.getPeriodId())) {
            log.error("checkUserSubscribePredict 期次异常，请迅速check gameId" + program.getGameId());
            return PredictConstant.SUBSCRIBE_STATUS_NO;
        }
        //2.从缓存中取出用户订阅预测的信息
        String userSubscribeTypeKey = RedisConstant.getUserSubscribeTypeKey(program.getGameId(), userId, program
                .getPredictType());
        Integer subPeriodId = redisService.kryoGet(userSubscribeTypeKey, Integer.class);
        if (subPeriodId == null) {
            //2.1为空就重新构建
            subPeriodId = rebuildUserSubscribeInfoRedis(userId, program.getGameId(), program.getPredictType(),
                    program.getProgramType());
        }
        //3.check用户是否已经订阅
        if (subPeriodId != null && subPeriodId >= Integer.valueOf(currentPeriod.getPeriodId())) {
            return PredictConstant.SUBSCRIBE_STATUS_YES;
        }
        return PredictConstant.SUBSCRIBE_STATUS_NO;
    }

    @Override
    public Integer rebuildUserSubscribeInfoRedis(Long userId, long gameId, Integer predictType, Integer programType) {
        //1.用户首购缓存
        Integer firstProgramType = PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED;
        if (programType.equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE_FIRST)) {
            firstProgramType = PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE;
        }
        String userFirstByColdHotStatePredictKey = RedisConstant.getUserFirstBuyColdHotStatePredictKey(gameId, userId,
                programType);
        redisService.del(userFirstByColdHotStatePredictKey);
        checkUserFirstBuyStatus(gameId, userId, firstProgramType);

        //2.用户购买缓存
        String userSubscribeTypeKey = RedisConstant.getUserSubscribeTypeKey(gameId, userId, predictType);
        UserSubscribeInfo subscribeInfo = userSubscribeInfoDao.getUserSubscribeInfoByPk(userId, predictType, gameId,
                false);
        if (subscribeInfo == null || subscribeInfo.getPeriodId() == null) {
            return null;
        }

        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
        int expireTime = TrendUtil.getExprieSecond(currentPeriod.getEndTime(), 3600);
        redisService.kryoSetEx(userSubscribeTypeKey, expireTime, subscribeInfo.getPeriodId());
        return subscribeInfo.getPeriodId();
    }

    @Override
    public Integer checkUserFirstBuyStatus(long gameId, Long userId, Integer programType) {
        if (userId == null) {
            return PredictConstant.USER_FIRST_BUY_COLD_HOT_STATE_PREDICT_YES;
        }
        //1.check user first buy redis
        String userFirstByColdHotStatePredictKey = RedisConstant.getUserFirstBuyColdHotStatePredictKey(gameId, userId,
                programType);
        Integer status = redisService.kryoGet(userFirstByColdHotStatePredictKey, Integer.class);
        if (status == null) {
            Integer count = userSubscribeInfoDao.getUserSubscribeProgramCount(gameId, userId, programType);
            if (count != null && count > 0) {
                status = PredictConstant.USER_FIRST_BUY_COLD_HOT_STATE_PREDICT_NO;
            } else {
                status = PredictConstant.USER_FIRST_BUY_COLD_HOT_STATE_PREDICT_YES;
            }

            redisService.kryoSetEx(userFirstByColdHotStatePredictKey, 604800, status);//保存七天
        }
        return status;
    }

    @Override
    public void subscribeRefundWisdom2User() {
        List<Long> missionIds = missionDao.getSlaveMissionIdsByTaskType(Mission.MISSION_TYPE_KILL_STATE_REFUND,
                Mission.MISSION_STATUS_REFUND_WAITE);
        for (Long missionId : missionIds) {
            Mission mission = missionDao.getTaskById(missionId);
            if (!mission.getStatus().equals(Mission.MISSION_STATUS_REFUND_WAITE)) {
                continue;
            }
            //gameId period programType predictType userId amount
            String[] arrInfo = mission.getKeyInfo().split(":");
            if (arrInfo.length < 6) {
                log.error("missionId " + missionId + "refund subscribe error");
                continue;
            }
            long gameId = Long.valueOf(arrInfo[0]);
            String periodId = arrInfo[1];
            Integer programType = Integer.valueOf(arrInfo[2]);
            Integer predictType = Integer.valueOf(arrInfo[3]);
            Long userId = Long.valueOf(arrInfo[4]);
            Long amount = Long.valueOf(arrInfo[5]);
            //check 预测是否真的不中
            PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(gameId, periodId, predictType);
            if (predictRedBall == null) {
                log.error("subscribeRefundWisdom2User predictRedBall is null Mission" + mission.toString());
                continue;
            }
            //如果全中就直接更新为不用退款
            if (!predictRedBall.getNumStr().contains(CommonConstant.COMMON_STAR_STR)) {
                missionDao.updateMissionStatus(missionId, Mission.MISSION_STATUS_NO_REFUND, Mission
                        .MISSION_STATUS_REFUND_WAITE);
                continue;
            }
            Game game = GameCache.getGame(gameId);
            PickNumPredict pickNumPredict = PickNumEnum.getPickNumEnum(game.getGameEn()).getGamePickNumEnum
                    (predictType);
            String ballColor = "红";
            if (programType.equals(PredictConstant.PREDICT_NUM_TYPE_BLUE_BALL)) {
                ballColor = "蓝";
            }
            String payDesc = game.getGameName() + "杀" + pickNumPredict.getNumCount() + ballColor + "赔付";
            Map<String, Object> resMap = payService.fillAccount(userId, "REFUND" + mission.getKeyInfo(), amount,
                    CommonConstant.PAY_TYPE_WISDOM_COIN, null, amount, payDesc, null, null);
            if (resMap == null || !resMap.containsKey("payStatus")) {
                log.error("退款失败 返回结果：" + JSONObject.toJSONString(resMap));
                continue;
            }
            Integer status = Integer.valueOf(resMap.get("payStatus").toString());
            if (status.equals(ResultConstant.REPEAT_CODE) || status.equals(ResultConstant.PAY_SUCCESS_CODE)) {
                // 更新mission
                missionDao.updateMissionStatus(missionId, Mission.MISSION_STATUS_REFUND, Mission
                        .MISSION_STATUS_REFUND_WAITE);
            }

        }

    }

    @Override
    public Integer checkIsRefund(String partMissionId) {
        Mission mission = missionDao.getPartMissionByKeyInfo(partMissionId);
        if (mission != null && mission.getStatus().equals(Mission.MISSION_STATUS_REFUND)) {
            return PredictConstant.PREDICT_REFUND_STATUS_YES;
        }
        return PredictConstant.PREDICT_REFUND_STATUS_NO_NEED;
    }
}
