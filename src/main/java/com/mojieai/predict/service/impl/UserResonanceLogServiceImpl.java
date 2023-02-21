package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.enums.TimelineEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.UserResonanceLogService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserResonanceLogServiceImpl implements UserResonanceLogService, BeanSelfAware {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;
    @Autowired
    private PayService payService;
    @Autowired
    private UserResonanceLogDao userResonanceLogDao;
    @Autowired
    private UserResonanceLogIdSeqDao userResonanceLogIdSeqDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private UserResonanceInfoDao userResonanceInfoDao;

    private UserResonanceLogService self;

    @Override
    public UserResonanceLog produceUserResonanceLog(Long userId, ExchangeMall exchangeMall, Long payAmount) {
        Integer beginPeriod = 0;
        Integer endPeriod = 0;

        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(exchangeMall.getGameId());
        GamePeriod purchasePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(exchangeMall.getGameId(),
                lastOpenPeriod.getPeriodId());

        //
        Integer count = exchangeMall.getItemCount().intValue();
        String startTimeLine = TimelineEnum.START_TIME.getTimelineKey(exchangeMall.getGameId());
        List<String> periods = redisService.kryoZRangeByScoreGet(startTimeLine, purchasePeriod.getStartTime().getTime
                (), Long.MAX_VALUE, 0, count, String.class);
        beginPeriod = Integer.valueOf(purchasePeriod.getPeriodId());
        endPeriod = Integer.valueOf(periods.get(periods.size() - 1));

        //1.check用户是否已经下单
        UserResonanceLog userResonanceLog = userResonanceLogDao.getUserResonanceLogByUnique(userId, exchangeMall
                .getGameId(), beginPeriod, endPeriod, exchangeMall.getItemPrice());
        if (userResonanceLog != null) {
            return userResonanceLog;
        }
        //2.check用户是否已经购买包括该期次的商品
        userResonanceLog = userResonanceLogDao.getRepeatUserResonanceLog(userId, exchangeMall.getGameId(), exchangeMall
                .getItemPrice(), beginPeriod);
        if (userResonanceLog != null && userResonanceLog.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return userResonanceLog;
        }
        //3.未支付的订单关闭
        if (userResonanceLog != null && userResonanceLog.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_NO)) {
            //3.1去三方查询交易状态
            Integer orderStatus = payService.checkOrderOutTradeStatus(userResonanceLog.getResonanceLogId(),
                    CommonConstant.PAY_TYPE_CASH, userId);
            if (orderStatus.equals(PayConstant.OUT_TRADE_ORDER_STATUS_NO_PAY)) {
                //3.2关闭订单
                userResonanceLogDao.updateUserResonanceLogStatus(userId, userResonanceLog.getResonanceLogId(),
                        PayConstant.OUT_TRADE_ORDER_CONFLICT_CLOSE, PayConstant.OUT_TRADE_ORDER_CONFLICT_INIT);
            } else {
                userResonanceLog.setIsPay(CommonConstant.PROGRAM_IS_PAY_YES);
                return userResonanceLog;
            }
        }
        //4.初始化一个新的购买记录
        userResonanceLog = new UserResonanceLog();
        try {
            String resonanceLogId = CommonUtil.generateStrId(userId, "RESONANCE", userResonanceLogIdSeqDao);
            userResonanceLog.initUserResonanceLog(resonanceLogId, userId, exchangeMall.getGameId(), beginPeriod,
                    endPeriod, exchangeMall.getItemPrice(), payAmount);
            userResonanceLogDao.insert(userResonanceLog);
        } catch (DuplicateKeyException e) {
            userResonanceLogDao.getUserResonanceLogByUnique(userId, exchangeMall.getGameId(), beginPeriod, endPeriod,
                    exchangeMall.getItemPrice());
        }
        return userResonanceLog;
    }

    @Override
    public Boolean updateUserResonanceInfoAfterPayed(Long userId, long gameId, String resonanceLogId) {
        //1.查询用户订阅情况
        UserResonanceInfo userResonanceInfo = userResonanceInfoDao.getUserResonanceInfo(userId, gameId, false);
        if (userResonanceInfo == null) {
            userResonanceInfo = new UserResonanceInfo();
            userResonanceInfo.initUserResonanceInfo(userId, gameId);
            userResonanceInfoDao.insert(userResonanceInfo);
        }
        //2.更新购买记录并更新用户订阅信息
        Boolean updateRes = self.updateUserResonanceLogPayed(userId, gameId, resonanceLogId);
        if (updateRes) {
            //3.更新redis
        }
        return updateRes;
    }

    @Override
    public Boolean callBackMakeResonanceEffective(String resonanceLogId, String exchangeFlowId) {
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
        UserResonanceLog userResonanceLog = userResonanceLogDao.getUserResonanceLogByPk(userAccountFlow.getUserId(),
                resonanceLogId);
        if (userResonanceLog == null) {
            log.error("异常 订购预测回调 userResonanceLogId:" + resonanceLogId + "payExchangeId:" + exchangeFlowId);
            return false;
        }
        //3.校验流水金额支付前后是否一样
        if (!userResonanceLog.getPayAmount().equals(userAccountFlow.getPayAmount())) {
            log.error("订阅订单金额不一致.userResonanceLogId:" + resonanceLogId + " 金额为" + userResonanceLog.getPayAmount()
                    + " userAccountFlow:" + exchangeFlowId + " 金额为:" + userAccountFlow.getPayAmount());
            return false;
        }
        if (userResonanceLog.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return true;
        }
        //4.更新用户方案状态
        res = updateUserResonanceInfoAfterPayed(userResonanceLog.getUserId(), userResonanceLog.getGameId(),
                resonanceLogId);
        return res;
    }

    @Transactional
    @Override
    public Boolean updateUserResonanceLogPayed(Long userId, long gameId, String resonanceLogId) {
        Boolean res = Boolean.FALSE;
        UserResonanceInfo userResonanceInfo = userResonanceInfoDao.getUserResonanceInfo(userId, gameId, true);
        UserResonanceLog userResonanceLog = userResonanceLogDao.getUserResonanceLogByPk(userId, resonanceLogId);
        if (userResonanceLog.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_NO)) {
            int updateRes = userResonanceInfoDao.updateLastPeriod(userId, gameId, userResonanceLog.getLastPeriod(),
                    userResonanceInfo.getLastPeriod());
            if (updateRes > 0) {
                int updateUserLog = userResonanceLogDao.updateUserResonanceLogStatus(userId, resonanceLogId,
                        CommonConstant.PROGRAM_IS_PAY_YES, CommonConstant.PROGRAM_IS_PAY_NO);
                if (updateUserLog > 0) {
                    res = Boolean.TRUE;
                }
            }
        }
        return res;
    }

    @Override
    public void setSelf(Object proxyBean) {
        self = (UserResonanceLogService) proxyBean;
    }
}
