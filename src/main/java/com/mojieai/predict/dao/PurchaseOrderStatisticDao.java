package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PurchaseOrderStatistic;

import java.util.List;

public interface PurchaseOrderStatisticDao {

    List<PurchaseOrderStatistic> getPurchaseOrderStatisticByTime(Integer beginTime, Integer endTime, Integer
            orderClass);

    int insert(PurchaseOrderStatistic purchaseOrderStatistic);
}
