package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PurchaseOrderStatistic implements Serializable {
    private static final long serialVersionUID = 2313177300134038783L;

    private Integer orderClass;
    private Integer orderType;
    private Integer statisticDate;
    private Integer orderPersonCount = 0;//下单人数
    private Integer payPersonCount = 0;//付款人数
    private Integer firstOrderPersonCount = 0;//首次下单人数
    private Integer fisrtPayPersonCount = 0;//首次支付人数
    private Integer orderCount = 0;//订单总数
    private Integer payCount = 0;//成功支付订单数
    private Integer totalOrderAmount = 0;//下单总金额
    private Integer totalPayAmount = 0;//下单支付总金额
    private Timestamp createTime;

    public void addOrderStatisOrderData(PurchaseOrderStatistic target) {
        this.orderPersonCount += target.getOrderPersonCount();
        this.payPersonCount += target.getPayPersonCount();
        this.firstOrderPersonCount += target.getFirstOrderPersonCount();
        this.fisrtPayPersonCount += target.getFisrtPayPersonCount();
        this.orderCount += target.getOrderCount();
        this.payCount += target.getPayCount();
        this.totalOrderAmount += target.getTotalOrderAmount();
        this.totalPayAmount += target.getTotalPayAmount();
    }
}
