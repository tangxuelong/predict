package com.mojieai.predict.service.purchase;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.SubscribeProgramDao;
import com.mojieai.predict.entity.bo.PrePayCheck;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.bo.TradePayResult;
import com.mojieai.predict.entity.po.SubscribeProgram;
import com.mojieai.predict.entity.po.UserSubscribeInfo;
import com.mojieai.predict.entity.po.UserSubscribeLog;
import com.mojieai.predict.service.UserSubscribeInfoLogService;
import com.mojieai.predict.service.UserSubscribeInfoService;
import com.mojieai.predict.util.PayUtil;
import com.mojieai.predict.util.ProgramUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KillProgramPurchase extends AbstractPurchase {

    @Autowired
    private UserSubscribeInfoLogService userSubscribeInfoLogService;
    @Autowired
    private UserSubscribeInfoService userSubscribeInfoService;
    @Autowired
    private SubscribeProgramDao subscribeProgramDao;

    @Override
    public PrePayCheck checkBeforePurchase(Long userId, String goodsId) {
        //1.check用户是否已经购买
        if (userSubscribeInfoService.checkUserSubscribePredict(userId, Integer.valueOf(goodsId))) {
            return new PrePayCheck(PayConstant.PAY_CHECK_ERROR_CODE, "已订阅该预测");
        }
        return new PrePayCheck(PayConstant.PAY_CHECK_SUCCESS_CODE, "");
    }

    @Override
    public Map<String, Object> cashPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer channelId, Integer bankId,
                                                 Integer clientId, String clientIp, Integer versionCode) {
        Map<String, Object> res = new HashMap<>();
        //1.取得商品信息
        SubscribeProgram program = subscribeProgramDao.getSubscribePredictByProgramId(Integer.valueOf
                (prePayInfo.getGoodsId()));//todo cache
        if (program == null) {
            res.put("code", -1);
            res.put("msg", "商品不存在");
            return res;
        }
        long price = getRealPayAmount(userId, prePayInfo, channelId);

        //2.购买方案流水
        UserSubscribeLog userSubscribeLog = userSubscribeInfoLogService.produceUserSubscribeLog(userId, program, price);

        //3.获取三方结果来调起支付
        String payDesc = prePayInfo.getGoodsName();
        return getOutTradeInfo(userId, userSubscribeLog.getSubscribeId(), payDesc, price, CommonConstant
                .PAY_TYPE_CASH, channelId, price, clientIp, clientId, CommonConstant
                .SUBSCRIBE_PURCHASE_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_DEC, prePayInfo, bankId);
    }

    @Override
    public Map<String, Object> wisdomCoinPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer payChannelId) {
        Map<String, Object> res = new HashMap<>();
        //1.取得商品信息
        SubscribeProgram program = subscribeProgramDao.getSubscribePredictByProgramId(Integer.valueOf(prePayInfo
                .getGoodsId()));
        if (program == null) {
            res.put("code", -1);
            res.put("msg", "商品不存在");
            return res;
        }
        long price = getRealPayAmount(userId, prePayInfo, CommonConstant.WISDOM_COIN_CHANNEL_ID);

        //1.初始化用户方案
        UserSubscribeLog userSubscribeLog = userSubscribeInfoLogService.produceUserSubscribeLog(userId, program, price);
        if (userSubscribeLog.getPayStatus() != null && userSubscribeLog.getPayStatus().equals(CommonConstant
                .PROGRAM_IS_PAY_YES)) {
            res.put("code", -1);
            res.put("msg", "方案已订阅");
            return res;
        }

        // 2.支付
        String payDesc = prePayInfo.getGoodsName();
        Map<String, Object> payInfo = getOutTradeInfo(userId, userSubscribeLog.getSubscribeId(), payDesc, price,
                CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, price, null, null, null, CommonConstant
                        .PAY_OPERATE_TYPE_DEC, prePayInfo, null);

        if (ResultConstant.ERROR == Integer.valueOf(payInfo.get("code").toString())) {
            return payInfo;
        }

        // 3.支付成功 更新用户订阅
        Boolean updateRes = userSubscribeInfoLogService.updateUserSubscribeInfoAfterPayed(userId, program,
                userSubscribeLog.getSubscribeId());
        return wisdomCoinPayedHandleFlow(updateRes, payInfo);
    }
}
