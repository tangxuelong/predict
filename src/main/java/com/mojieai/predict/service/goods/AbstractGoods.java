package com.mojieai.predict.service.goods;

import com.mojieai.predict.entity.bo.PrePayInfo;

public abstract class AbstractGoods {

    public PrePayInfo getPrePayInfo(String goodsId) {
        throw new AbstractMethodError("No method defined getPrePayInfo");
    }
}
