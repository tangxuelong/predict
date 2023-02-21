package com.mojieai.predict.service;

import java.util.Map;

public interface AllProductBillService {

    Map<String, Object> getAllProductBills(Integer beginDate, Integer endDate);

    void statisticCashPurchaseProduct(Long userId, String flowId);

    void updateAllProductBill(boolean personAdd, Integer dateNum, Integer orderType, Long amount);
}
