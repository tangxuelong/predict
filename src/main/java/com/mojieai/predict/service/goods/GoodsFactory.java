package com.mojieai.predict.service.goods;

import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.SpringContextHolder;

public class GoodsFactory {
    private static GoodsFactory instance = new GoodsFactory();

    private GoodsFactory() {
    }

    public static GoodsFactory getInstance() {
        return instance;
    }

    public AbstractGoods getBean(String goodEn) {
        AbstractGoods abstractGoods = SpringContextHolder.getBean(goodEn + "Goods");
        if (abstractGoods == null) {
            throw new BusinessException("购买商品不存在:" + goodEn);
        }
        return abstractGoods;
    }
}
