package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.enums.TimelineEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.UserSubscribeInfoLogService;
import com.mojieai.predict.service.UserSubscribeInfoService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.*;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserSubscribeInfoLogServiceImpl implements UserSubscribeInfoLogService, BeanSelfAware {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private SubscribeProgramDao subscribeProgramDao;
    @Autowired
    private UserSubscribeLogDao userSubscribeLogDao;
    @Autowired
    private UserSubscribeInfoDao userSubscribeInfoDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private UserSubscribeLogIdSequenceDao userSubscribeLogIdSequenceDao;
    @Autowired
    private PayService payService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserSubscribeInfoService userSubscribeInfoService;
    @Autowired
    private MissionDao missionDao;

    private UserSubscribeInfoLogService self;

    @Override
    public Boolean updateUserSubscribeInfoAfterPayed(Long userId, SubscribeProgram program, String subscribeLogId) {
        //1.查询用户订阅情况
        UserSubscribeInfo userSubscribeInfo = userSubscribeInfoDao.getUserSubscribeInfoByPk(userId, program
                .getPredictType(), program.getGameId(), false);
        if (userSubscribeInfo == null) {
            Integer programType = PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED;
            if (program.getProgramType().equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE_FIRST)) {
                programType = PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE;
            }
            userSubscribeInfo = new UserSubscribeInfo();
            userSubscribeInfo.initUserSubscribeInfo(userId, program.getGameId(), program.getPredictType(), programType);
            userSubscribeInfoDao.insert(userSubscribeInfo);
        }
        //2.更新购买记录并更新用户订阅信息
        Boolean updateRes = self.updateUserSubscribeLogPayed(userId, program.getPredictType(), program.getGameId(),
                subscribeLogId);
        if (updateRes) {
            //3.更新用户购买缓存
            userSubscribeInfoService.rebuildUserSubscribeInfoRedis(userId, program.getGameId(), program
                    .getPredictType(), userSubscribeInfo.getProgramType());
            //4.如果方案是不中包赔插入mission
            if ((program.getProgramType().equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED) || program
                    .getProgramType().equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE)) && program.getBuyType
                    ().equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE)) {
                try {
                    List<Mission> missions = generateMissions(subscribeLogId, program, userId);
                    if (missions != null && missions.size() > 0) {
                        missionDao.insertBatch(missions);
                    }
                } catch (Exception e) {
                    log.error("用户订阅退款异常", e);
                }
            }
        }
        return updateRes;
    }

    private List<Mission> generateMissions(String subscribeLogId, SubscribeProgram program, Long userId) {
        UserSubscribeLog subLog = userSubscribeLogDao.getUserSubscribeLogByPk(subscribeLogId, userId);
        if (subLog == null) {
            return null;
        }
        List<Mission> missions = new ArrayList<>();
        int begin = subLog.getBeginPeriod();
        int end = subLog.getLastPeriod();
        String partMoney = CommonUtil.divide(subLog.getPayAmount() + "", program.getSubscribeNum() + "", 0);

        while (begin <= end) {
            String key = PredictUtil.getKillStateRefundMissionId(program.getGameId(), begin, program.getProgramType(),
                    program.getPredictType(), subLog.getUserId(), partMoney);
            Mission temp = new Mission(key, Mission.MISSION_TYPE_KILL_STATE_REFUND, Mission.MISSION_STATUS_INTI,
                    DateUtil.getCurrentTimestamp(), "", null);
            missions.add(temp);
            GamePeriod next = PeriodRedis.getNextPeriodByGameIdAndPeriodId(program.getGameId(), String.valueOf(begin));
            begin = Integer.valueOf(next.getPeriodId());
        }
        return missions;
    }

    @Override
    public Map<String, Object> givePredictStateNum2User(Long userId, SubscribeProgram program) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", -1);
        res.put("msg", "方案已订阅");
        //1.check用户是否已经参与
        Integer programType = PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED;
        if (program.getProgramType().equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE_FIRST)) {
            programType = PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE;
        }
        Integer firstBuy = userSubscribeInfoService.checkUserFirstBuyStatus(program.getGameId(), userId, programType);
        if (firstBuy.equals(PredictConstant.USER_FIRST_BUY_COLD_HOT_STATE_PREDICT_NO)) {
            return res;
        }
        //1.初始化用户
        UserSubscribeLog userSubscribeLog = produceUserSubscribeLog(userId, program, 0);
        if (userSubscribeLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return res;
        }
        if ((userSubscribeLog.getLastPeriod() - userSubscribeLog.getBeginPeriod()) != (program.getSubscribeNum() - 1)) {
            res.put("msg", "方案订阅失败");
            return res;
        }

        //2.给用户兑换期次
        Boolean updateRes = updateUserSubscribeInfoAfterPayed(userId, program, userSubscribeLog.getSubscribeId());
        if (updateRes) {
            res.put("code", 0);
            res.put("msg", "订阅成功");
        }
        return res;

    }

    @Transactional
    @Override
    public Boolean updateUserSubscribeLogPayed(Long userId, Integer predictType, long gameId, String subscribeId) {
        Boolean res = Boolean.FALSE;
        UserSubscribeInfo userSubscribeInfo = userSubscribeInfoDao.getUserSubscribeInfoByPk(userId, predictType,
                gameId, true);
        UserSubscribeLog userSubscribeLog = userSubscribeLogDao.getUserSubscribeLogByPk(subscribeId, userId);
        if (userSubscribeLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_NO)) {
            int updateRes = userSubscribeInfoDao.updatePeriodIdByPk(userId, predictType, gameId, userSubscribeLog
                    .getLastPeriod(), userSubscribeInfo.getPeriodId());
            if (updateRes > 0) {
                int updateUserLog = userSubscribeLogDao.updateUserSubscribeLogPayStatus(subscribeId, userId,
                        CommonConstant.PROGRAM_IS_PAY_YES, CommonConstant.PROGRAM_IS_PAY_NO);
                if (updateUserLog > 0) {
                    res = Boolean.TRUE;
                }
            }
        }
        return res;
    }


    @Override
    public Boolean callBackMakeSubscribeEffective(String userSubscribeLogId, String exchangeFlowId) {
        Boolean res = Boolean.FALSE;
        //1.check流水
        Long userIdSuffix = Long.valueOf(exchangeFlowId.substring(exchangeFlowId.length() - 2, exchangeFlowId.length
                ()));
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(exchangeFlowId, userIdSuffix,
                false);
        if (userAccountFlow == null || !userAccountFlow.getStatus().equals(CommonConstant.PAY_STATUS_FINISH)) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("流水id:" + exchangeFlowId + " 异常." + "流水状态为:" + status);
            return false;
        }
        //2.验证订阅流水是否已经置位
        UserSubscribeLog userSubscribeLog = userSubscribeLogDao.getUserSubscribeLogByPk(userSubscribeLogId,
                userIdSuffix);
        if (userSubscribeLog == null) {
            log.error("异常 订购预测回调 userSubscribeLogId:" + userSubscribeLogId + "payExchangeId:" + exchangeFlowId);
            return false;
        }
        //3.校验流水金额支付前后是否一样
        if (!userSubscribeLog.getPayAmount().equals(userAccountFlow.getPayAmount())) {
            log.error("订阅订单金额不一致.userSubscribeLogId:" + userSubscribeLogId + " 金额为" + userSubscribeLog.getPayAmount()
                    + " userAccountFlow:" + exchangeFlowId + " 金额为:" + userAccountFlow.getPayAmount());
            return false;
        }
        if (userSubscribeLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return true;
        }
        //4.更新用户方案状态
        SubscribeProgram program = subscribeProgramDao.getSubscribePredictByProgramId(userSubscribeLog.getProgramId());
        res = updateUserSubscribeInfoAfterPayed(userSubscribeLog.getUserId(), program, userSubscribeLog
                .getSubscribeId());
        return res;
    }

    @Override
    public UserSubscribeLog produceUserSubscribeLog(Long userId, SubscribeProgram program, long payAmount) {
        Integer beginPeriod = 0;
        Integer endPeriod = 0;

        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(program.getGameId());
        GamePeriod purchasePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(program.getGameId(), lastOpenPeriod
                .getPeriodId());

        // TODO: 18/3/24 这里考虑做成一个缓存毕竟不是一只在换
        String startTimeLine = TimelineEnum.START_TIME.getTimelineKey(program.getGameId());
        List<String> periods = redisService.kryoZRangeByScoreGet(startTimeLine, purchasePeriod.getStartTime().getTime
                (), Long.MAX_VALUE, 0, program.getSubscribeNum(), String.class);
        beginPeriod = Integer.valueOf(purchasePeriod.getPeriodId());
        endPeriod = Integer.valueOf(periods.get(periods.size() - 1));

        //1.check用户是否已经下单
        UserSubscribeLog userSubscribeLog = userSubscribeLogDao.getUserSubScribeLogByUniqueKey(userId, program
                .getProgramId(), program.getAmount(), beginPeriod, endPeriod);
        if (userSubscribeLog != null) {
            return userSubscribeLog;
        }
        //2.check用户有没有购买包含该期次的商品
        userSubscribeLog = userSubscribeLogDao.getRepeatUserSubscribeLog(userId, program.getProgramId(), program
                .getAmount(), beginPeriod);
        if (userSubscribeLog != null && userSubscribeLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return userSubscribeLog;
        }
        //3.未支付的订单关闭
        if (userSubscribeLog != null && userSubscribeLog.getPayStatus().equals(CommonConstant.PROGRAM_IS_PAY_NO)) {
            //3.1去三方查询交易状态
            Integer orderStatus = payService.checkOrderOutTradeStatus(userSubscribeLog.getSubscribeId(),
                    CommonConstant.PAY_TYPE_CASH, userId);
            if (orderStatus.equals(PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY)) {
                userSubscribeLogDao.updateUserSubscribeLogStatus(userSubscribeLog.getSubscribeId(), userId, PayConstant
                        .OUT_TRADE_ORDER_CONFLICT_CLOSE, PayConstant.OUT_TRADE_ORDER_CONFLICT_INIT);
            } else {
                userSubscribeLog.setPayStatus(CommonConstant.PROGRAM_IS_PAY_YES);
                return userSubscribeLog;
            }
        }

        String userSubscribeLogId = CommonUtil.generateStrId(userId, "SUBSCRIBELOG", userSubscribeLogIdSequenceDao);
        userSubscribeLog = new UserSubscribeLog();
        userSubscribeLog.initUserSubscribeLog(userSubscribeLogId, userId, program.getProgramId(), payAmount, program
                .getAmount(), beginPeriod, endPeriod);
        try {
            userSubscribeLogDao.insert(userSubscribeLog);
        } catch (DuplicateKeyException e) {
            userSubscribeLog = userSubscribeLogDao.getUserSubScribeLogByUniqueKey(userId, program.getProgramId(),
                    program.getAmount(), beginPeriod, endPeriod);
        }

        return userSubscribeLog;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserSubscribeInfoLogService) proxyBean;
    }
}
