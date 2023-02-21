package com.mojieai.predict.service.purchase;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.UserSportSocialRecommendDao;
import com.mojieai.predict.entity.bo.PrePayCheck;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.UserBuyRecommend;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import com.mojieai.predict.service.UserBuyRecommendService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class FootballRecommendPurchase extends AbstractPurchase {

    @Autowired
    private UserBuyRecommendService userBuyRecommendService;
    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;

    @Override
    public PrePayCheck checkBeforePurchase(Long userId, String goodsId) {
        //1.check方案是否未付费方案 或者过期方案
        Long userIdPrix = CommonUtil.getUserIdSuffix(goodsId);
        UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(userIdPrix,
                goodsId, false);
        if (recommend == null) {
            return new PrePayCheck(PayConstant.PAY_CHECK_ERROR_CODE, "推荐不存在");
        }
        if (DateUtil.compareDate(recommend.getEndTime(), new Date())) {
            return new PrePayCheck(PayConstant.PAY_CHECK_ERROR_CODE, "方案已过期");
        }

        //2.check用户是否已经购买
        if (userBuyRecommendService.checkUserPurchaseFootballProgramStatus(userId, goodsId)) {
            return new PrePayCheck(PayConstant.PAY_CHECK_ERROR_CODE, "已购买该方案");
        }
        return new PrePayCheck(1, "");
    }

    @Override
    public Map<String, Object> cashPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer channelId, Integer bankId,
                                                 Integer clientId, String clientIp, Integer versionCode) {
        //1.取得商品信息
        Long userIdPrix = CommonUtil.getUserIdSuffix(prePayInfo.getGoodsId());
        UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(userIdPrix,
                prePayInfo.getGoodsId(), false);
        long price = getRealPayAmount(userId, prePayInfo, channelId);
        //2.购买足球推单流水
        UserBuyRecommend userBuyRecommend = userBuyRecommendService.initUserBuyRecommend(userId, price, recommend);

        //3.获取三方结果来调起支付
        String payDesc = "智慧师推单";
        return getOutTradeInfo(userId, userBuyRecommend.getFootballLogId(), payDesc, price, CommonConstant
                .PAY_TYPE_CASH, channelId, price, clientIp, clientId, CommonConstant
                .FOOTBALL_RECOMMEND_PROGRAM_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_DEC, prePayInfo, bankId);
    }

    @Override
    public Map<String, Object> wisdomCoinPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer payChannelId) {
        //1.取得商品信息
        Long userIdPrix = CommonUtil.getUserIdSuffix(prePayInfo.getGoodsId());
        UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(userIdPrix,
                prePayInfo.getGoodsId(), false);
        long price = getRealPayAmount(userId, prePayInfo, CommonConstant.WISDOM_COIN_CHANNEL_ID);
        //2.初始化购买方案
        UserBuyRecommend userBuyRecommend = userBuyRecommendService.initUserBuyRecommend
                (userId, price, recommend);
        // 3.支付
        String payDesc = "智慧师推单";
        Map<String, Object> payInfo = getOutTradeInfo(userId, userBuyRecommend.getFootballLogId(), payDesc, price,
                CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, price, null, null, null, CommonConstant
                        .PAY_OPERATE_TYPE_DEC, prePayInfo, null);

        if (ResultConstant.ERROR == Integer.valueOf(payInfo.get("code").toString())) {
            return payInfo;
        }

        // 3.支付成功 更新用户购买足球方案
        Boolean updateRes = userBuyRecommendService.updateUserBuyRecommendAfterPayed(userId, prePayInfo
                .getGoodsId(), false, false);
        return wisdomCoinPayedHandleFlow(updateRes, payInfo);
    }
}
