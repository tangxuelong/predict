package com.mojieai.predict.service.goods;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.dao.ExchangeMallDao;
import com.mojieai.predict.dao.UserSportSocialRecommendDao;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.ExchangeMall;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import com.mojieai.predict.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FootballRecommendGoods extends AbstractGoods {

    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private ExchangeMallDao exchangeMallDao;

    @Override
    public PrePayInfo getPrePayInfo(String goodsId) {
        //1.取得商品信息
        UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(CommonUtil
                .getUserIdSuffix(goodsId), goodsId, false);
        if (recommend == null) {
            return null;
        }
        ExchangeMall exchangeMall = exchangeMallDao.getExchangeMall(recommend.getItemId());
        String goodsName = "赛事推荐";
        PrePayInfo payInfo = new PrePayInfo(goodsId, goodsName, recommend.getPrice(), 100);
        payInfo.setVipDiscountWay(CommonConstant.VIP_DISCOUNT_WAY_LOW_PAY);
        payInfo.setIosMallId(exchangeMall.getIosMallId());//
        payInfo.setVipIosMallId(exchangeMall.getIosMallId());
        return payInfo;
    }
}
