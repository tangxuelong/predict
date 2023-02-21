package com.mojieai.predict.service.goods;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.dao.ExchangeMallDao;
import com.mojieai.predict.dao.SubscribeProgramDao;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.ExchangeMall;
import com.mojieai.predict.entity.po.SubscribeProgram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResonanceDataGoods extends AbstractGoods {

    @Autowired
    private ExchangeMallDao exchangeMallDao;

    @Override
    public PrePayInfo getPrePayInfo(String goodsId) {
        ExchangeMall goods = exchangeMallDao.getExchangeMall(Integer.valueOf(goodsId));
        if (goods == null) {
            return null;
        }
        String goodsName = goods.getItemName();
        PrePayInfo prePayInfo = new PrePayInfo(goodsId, goodsName, goods.getItemPrice(), goods.getVipDiscount());
        prePayInfo.setVipDiscountWay(CommonConstant.VIP_DISCOUNT_WAY_DISCOUNT);
        prePayInfo.setIosMallId(goods.getIosMallId());//
        prePayInfo.setVipIosMallId(goods.getIosMallId());
        return prePayInfo;
    }
}
