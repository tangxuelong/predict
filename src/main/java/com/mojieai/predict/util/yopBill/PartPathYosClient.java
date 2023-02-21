package com.mojieai.predict.util.yopBill;

import com.yeepay.g3.yop.sdk.services.yos.YosClient;

public class PartPathYosClient {
    private String partPath;
    private YosClient yosClient;

    public PartPathYosClient(YosClient yosClient, String partPath) {
        this.partPath = partPath;
        this.yosClient = yosClient;
    }

    public String getPartPath() {
        return this.partPath;
    }

    public YosClient getYosClient() {
        return this.yosClient;
    }
}
