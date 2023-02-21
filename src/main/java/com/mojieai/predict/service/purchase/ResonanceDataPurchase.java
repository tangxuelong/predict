package com.mojieai.predict.service.purchase;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.ExchangeMallDao;
import com.mojieai.predict.entity.bo.PrePayCheck;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.bo.TradePayResult;
import com.mojieai.predict.entity.po.ExchangeMall;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.UserResonanceLog;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.UserResonanceInfoService;
import com.mojieai.predict.service.UserResonanceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResonanceDataPurchase extends AbstractPurchase {
    @Autowired
    private ExchangeMallDao exchangeMallDao;
    @Autowired
    private UserResonanceLogService userResonanceLogService;
    @Autowired
    private UserResonanceInfoService userResonanceInfoService;

    @Override
    public PrePayCheck checkBeforePurchase(Long userId, String goodsId) {
        //1.check商品是否存在
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(Integer.valueOf(goodsId));
        if (exchangeMall == null) {
            return new PrePayCheck(-1, "商品不存在");
        }
        //2.check用户是否已经购买
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(exchangeMall.getGameId());
        GamePeriod period = PeriodRedis.getNextPeriodByGameIdAndPeriodId(exchangeMall.getGameId(), lastOpenPeriod
                .getPeriodId());
        Integer payStatus = userResonanceInfoService.checkUserResonanceInfoPayStatus(exchangeMall.getGameId(), userId,
                Integer.valueOf(period.getPeriodId()));
        if (payStatus.equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return new PrePayCheck(-1, "商品已购买");
        }
        return new PrePayCheck(1, "");
    }

    @Override
    public Map<String, Object> cashPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer channelId, Integer
            bankId, Integer clientId, String clientIp, Integer versionCode) {
        //1.取得商品信息
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(Integer.valueOf(prePayInfo.getGoodsId()));

        long price = getRealPayAmount(userId, prePayInfo, channelId);

        //2.购买方案流水
        UserResonanceLog userResonanceLog = userResonanceLogService.produceUserResonanceLog(userId, exchangeMall,
                price);

        //3.获取三方结果来调起支付
        String payDesc = prePayInfo.getGoodsName();
        return getOutTradeInfo(userId, userResonanceLog.getResonanceLogId(), payDesc, price, CommonConstant
                .PAY_TYPE_CASH, channelId, price, clientIp, clientId, CommonConstant
                .SOCIAL_RESONANCE_DATA_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_DEC, prePayInfo, bankId);
    }

    @Override
    public Map<String, Object> wisdomCoinPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer payChannelId) {
        Map<String, Object> res = new HashMap<>();
        //1.取得商品信息
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(Integer.valueOf(prePayInfo.getGoodsId()));

        long price = getRealPayAmount(userId, prePayInfo, CommonConstant.WISDOM_COIN_CHANNEL_ID);

        //1.初始化用户方案
        UserResonanceLog userResonanceLog = userResonanceLogService.produceUserResonanceLog(userId, exchangeMall,
                price);
        if (userResonanceLog.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            res.put("code", -1);
            res.put("msg", "已购买");
            return res;
        }

        // 2.支付
        String payDesc = prePayInfo.getGoodsName();
        Map<String, Object> payInfo = getOutTradeInfo(userId, userResonanceLog.getResonanceLogId(), payDesc, price,
                CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, price, null, null, null, CommonConstant
                        .PAY_OPERATE_TYPE_DEC, prePayInfo, null);

        if (ResultConstant.ERROR == Integer.valueOf(payInfo.get("code").toString())) {
            return payInfo;
        }

        // 3.支付成功 更新用户订阅
        Boolean updateRes = userResonanceLogService.updateUserResonanceInfoAfterPayed(userId, exchangeMall.getGameId
                (), userResonanceLog.getResonanceLogId());
        return wisdomCoinPayedHandleFlow(updateRes, payInfo);
    }
}
