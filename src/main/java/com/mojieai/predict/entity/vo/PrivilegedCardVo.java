package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ynght on 2018/8/28
 */
@Data
@NoArgsConstructor
public class PrivilegedCardVo {
    private Long couponConfigId;
    private String cardName;
    private String originalPrice;
    private String currentPrice;
    //    private String priceUnit;
    private String tips;
    private String buttonMsg;
    private String payMemo;
    private Long payAmount;

    public PrivilegedCardVo(Long couponConfigId, String cardName, String originalPrice, String currentPrice, String
            tips, String buttonMsg, String payMemo, Long payAmount) {
        this.couponConfigId = couponConfigId;
        this.cardName = cardName;
        this.originalPrice = originalPrice;
        this.currentPrice = currentPrice;
        this.tips = tips;
        this.buttonMsg = buttonMsg;
        this.payMemo = payMemo;
        this.payAmount = payAmount;
    }
}
