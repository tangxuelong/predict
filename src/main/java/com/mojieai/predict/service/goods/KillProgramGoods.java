package com.mojieai.predict.service.goods;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.dao.SubscribeProgramDao;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.SubscribeProgram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KillProgramGoods extends AbstractGoods {

    @Autowired
    private SubscribeProgramDao subscribeProgramDao;

    @Override
    public PrePayInfo getPrePayInfo(String goodsId) {
        SubscribeProgram subscribeProgram = subscribeProgramDao.getSubscribePredictByProgramId(Integer.valueOf
                (goodsId));
        if (subscribeProgram == null) {
            return null;
        }
        Game game = GameCache.getGame(subscribeProgram.getGameId());
        String goodsName = game.getGameName() + subscribeProgram.getProgramName() + " (" + subscribeProgram
                .getSubscribeNum() + "期)";
        PrePayInfo prePayInfo = new PrePayInfo(goodsId, goodsName, subscribeProgram.getAmount(), subscribeProgram
                .getVipDiscount());
        prePayInfo.setVipDiscountWay(CommonConstant.VIP_DISCOUNT_WAY_LOW_PAY);//todo ios之后改
        prePayInfo.setVipPrice(subscribeProgram.getVipAmount());
        prePayInfo.setIosMallId(CommonConstant.KILL_PROGRAM_IOS_MALL_ID);//由于杀三的价格一样这里直接写死了
        prePayInfo.setVipIosMallId(CommonConstant.KILL_PROGRAM_IOS_MALL_ID_VIP);
        prePayInfo.setVipDiscount(subscribeProgram.getVipDiscount());//写死打6.7折
        return prePayInfo;
    }
}
