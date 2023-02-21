package com.mojieai.predict.thread;

import com.mojieai.predict.service.AllProductBillService;

public class AllProductBillTask implements Runnable {

    private Long userId;
    private String flowId;
    private AllProductBillService allProductBillService;

    public AllProductBillTask(Long userId, String flowId, AllProductBillService allProductBillService) {
        this.userId = userId;
        this.flowId = flowId;
        this.allProductBillService = allProductBillService;
    }

    @Override
    public void run() {
        allProductBillService.statisticCashPurchaseProduct(userId, flowId);
    }
}
