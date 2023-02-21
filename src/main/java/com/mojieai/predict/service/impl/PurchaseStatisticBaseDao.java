package com.mojieai.predict.service.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface PurchaseStatisticBaseDao {

    /* 统计支付所有类型的订单的 下单总数和订单总金额 */
    List<Map> getAllOrderCountAndTotalAmount(String userId, Timestamp beginTime, Timestamp endTime);

    /* 统计下单人数*/
    List<Map> getOrderPersonCount(String userId, Timestamp beginTime, Timestamp endTime);

    /* 支付成功人数*/
    List<Map> getPaySuccessPersonCount(String userId, Timestamp beginTime, Timestamp endTime);

    /* 支付成功订单数和总金额*/
    List<Map> getPaySuccessOrderCount(String userId, Timestamp beginTime, Timestamp endTime);

    /* 首次下单人数*/
    List<Map> getFisrtOrderPersons(String userId);

    /* 首次支付人数*/
    List<Map> getFisrtPayPersons(String userId);
}
