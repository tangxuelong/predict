package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductBillVo {
    private String groupName = "智慧彩票";
    private String productName;
    private String orderAmount;
    private String wxOrderAmount;
    private String aliOrderAmount;
    private String appleOrderAmount;
    private String wx2OrderAmount;
    private String wx3OrderAmount;
    private String jingdongAmount;
    private String yibaoAmount;
    private String offLineAmount;
    private String haoDianAmount;
    private Integer weight;

    public ProductBillVo(String productName, String orderAmount, String wxOrderAmount, String aliOrderAmount, String
            appleOrderAmount, String wx2OrderAmount, String wx3OrderAmount, String jingdongAmount, String
                                 yibaoAmount, String offLineAmount, String haoDianAmount, Integer weight) {
        this.orderAmount = orderAmount;
        this.productName = productName;
        this.wxOrderAmount = wxOrderAmount;
        this.aliOrderAmount = aliOrderAmount;
        this.appleOrderAmount = appleOrderAmount;
        this.wx2OrderAmount = wx2OrderAmount;
        this.wx3OrderAmount = wx3OrderAmount;
        this.yibaoAmount = yibaoAmount;
        this.jingdongAmount = jingdongAmount;
        this.offLineAmount = offLineAmount;
        this.haoDianAmount = haoDianAmount;
        this.weight = weight;
    }
}
