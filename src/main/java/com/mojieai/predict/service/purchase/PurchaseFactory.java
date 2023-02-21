package com.mojieai.predict.service.purchase;

import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.SpringContextHolder;

public class PurchaseFactory {
    private static PurchaseFactory instance = new PurchaseFactory();

    private PurchaseFactory() {
    }

    public static PurchaseFactory getInstance() {
        return instance;
    }

    public AbstractPurchase getPurchaseInfo(String goodsType) {
        AbstractPurchase abstractPurchase = SpringContextHolder.getBean(goodsType + PayConstant
                .ABSTRACT_PURCHASE_AFTER);
        if (abstractPurchase == null) {
            throw new BusinessException("支付工厂中的对象不存在:" + goodsType);
        }
        return abstractPurchase;
    }
}
