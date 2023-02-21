package com.mojieai.predict.service;

import java.sql.Timestamp;
import java.util.Map;

public interface PurchaseOrderStatisticService {

    Map<String, Object> getPurchaseOrderStatisticInfo(Integer beginDate, Integer endDate, Integer orderClass);

    void statisticPurchaseOrderTiming();

    boolean rebuildOneDateStatistic(Timestamp statisticDate);

}
