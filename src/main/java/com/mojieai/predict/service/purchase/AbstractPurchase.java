package com.mojieai.predict.service.purchase;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.entity.bo.PrePayCheck;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.bo.TradePayResult;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PayUtil;
import com.mojieai.predict.util.ProgramUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class AbstractPurchase {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    protected PayService payService;
    @Autowired
    protected VipMemberService vipMemberService;
    @Autowired
    protected RedisService redisService;


    public abstract PrePayCheck checkBeforePurchase(Long userId, String goodsId);

    public abstract Map<String, Object> cashPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer channelId,
                                                          Integer bankId, Integer clientId, String clientIp, Integer
                                                                  versionCode);

    public abstract Map<String, Object> wisdomCoinPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer
            payChannelId);

    public TradePayResult purchaseGoods(Long userId, PrePayInfo prePayInfo, Integer payChannelId, Integer bankId, String
            clientIp, Integer clientId, Integer versionCode) {
        TradePayResult tradePayResult = new TradePayResult();
        //1.check业务
        PrePayCheck prePayCheck = checkBeforePurchase(userId, prePayInfo.getGoodsId());
        if (prePayCheck.getCode().equals(PayConstant.PAY_CHECK_ERROR_CODE)) {
            tradePayResult.setCode(PayConstant.PAY_CHECK_ERROR_CODE);
            tradePayResult.setMsg(prePayCheck.getMsg());
            return tradePayResult;
        }
        //2.下订单并获取支付信息
        Integer accountType = CommonConstant.ACCOUNT_TYPE_CASH;
        Map<String, Object> payInfo = null;
        if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            //智慧币支付
            payInfo = wisdomCoinPurchaseGoods(userId, prePayInfo, payChannelId);
            accountType = CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
        } else {
            //现金支付
            payInfo = cashPurchaseGoods(userId, prePayInfo, payChannelId, bankId, clientId, clientIp, versionCode);
        }


        payInfo.put("accountType", accountType);
        payInfo.put("channel", payChannelId);
        payInfo.put("webPay", CommonUtil.getWebPayStatus(payChannelId));
        tradePayResult.setCode(Integer.valueOf(payInfo.get("code").toString()));
        tradePayResult.setMsg(payInfo.get("msg").toString());
        tradePayResult.setPayInfo(payInfo);
        return tradePayResult;
    }

    protected Long getRealPayAmount(Long userId, PrePayInfo prePayInfo, Integer channelId) {
        return PayUtil.randomDiscountPrice(prePayInfo.getUserNeedPayAmount(vipMemberService.checkUserIsVip(userId,
                prePayInfo.getGoodsVipType())), channelId);
    }

    protected Map<String, Object> getOutTradeInfo(Long userId, String payId, String payDesc, Long totalAmount,
                                                  Integer payType, Integer channelId, Long payAmount, String
                                                          clientIp, Integer clientId, String callBackMethod, Integer
                                                          payOperateTypeDec, PrePayInfo prePayInfo, Integer bankId) {
        //1.创建支付流水并通知三方获取必要信息
        Map<String, Object> payMap = payService.payCreateFlow(userId, payId, totalAmount, payType, channelId,
                payAmount, payDesc, clientIp, clientId, callBackMethod, payOperateTypeDec, bankId);

        payMap.put("iosMallGoodId", prePayInfo.getIosMallId());
        if (vipMemberService.checkUserIsVip(userId, prePayInfo.getGoodsVipType())) {
            payMap.put("iosMallGoodId", prePayInfo.getVipIosMallId());
        }
        //2.解析结果并返回
        return PayUtil.analysisCashPayMap(payMap);
    }

    protected Map<String, Object> wisdomCoinPayedHandleFlow(boolean updateRes, Map payInfo) {
        Map<String, Object> res = new HashMap<>();
        if (updateRes) {
            // 业务处理成功
            String flowId = String.valueOf(payInfo.get("flowId"));
            payService.handledFlow(flowId);
            res.put("code", 0);
            res.put("msg", "购买成功");
        } else {
            log.error("已扣款订阅失败:" + JSONObject.toJSONString(payInfo));
            res.put("code", -1);
            res.put("msg", "订阅失败");
        }
        return res;
    }
}
