package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方案销售统计
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class ProgramSaleStats {
    private Integer id;
    private Integer orderDate;
    private String goodsType;
    private String buyType;
    private String price;
    private Integer firstOrder;
    private Integer firstSuccess;
    private Integer againOrder;
    private Integer againSuccess;
    private Integer totalOrder;
    private Integer totalSuccess;
    private Integer wechatCount;
    private Integer wechatAmount;
    private Integer wisdomCount;
    private Integer wisdomAmount;
    private Integer isVip;

    public ProgramSaleStats(Integer orderDate, String goodsType, String buyType, String price, Integer firstOrder,
                            Integer firstSuccess, Integer againOrder, Integer againSuccess, Integer totalOrder,
                            Integer totalSuccess, Integer wechatCount, Integer wechatAmount, Integer wisdomCount,
                            Integer wisdomAmount) {
        this.orderDate = orderDate;
        this.goodsType = goodsType;
        this.buyType = buyType;
        this.price = price;
        this.firstOrder = firstOrder;
        this.firstSuccess = firstSuccess;
        this.againOrder = againOrder;
        this.againSuccess = againSuccess;
        this.totalOrder = totalOrder;
        this.totalSuccess = totalSuccess;
        this.wechatCount = wechatCount;
        this.wechatAmount = wechatAmount;
        this.wisdomCount = wisdomCount;
        this.wisdomAmount = wisdomAmount;
        this.isVip = 0;
    }
}