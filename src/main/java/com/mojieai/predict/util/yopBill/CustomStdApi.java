package com.mojieai.predict.util.yopBill;

import com.yeepay.g3.yop.sdk.api.StdApi;
import com.yeepay.g3.yop.sdk.services.yos.YosClient;
import com.yeepay.g3.yop.sdk.services.yos.model.ObjectMetadata;
import com.yeepay.g3.yop.sdk.services.yos.model.YosObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class CustomStdApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(StdApi.class);
    private final String bucketName = "std";
    private final String bucketKeyTradeDay = "bill/tradedaydownload";
    private final String bucketKeyTradeMonth = "bill/trademonthdownload";
    private final String bucketKeyRemitDay = "bill/remitdaydownload";
    private final String bucketKeyFeeMonth = "bill/feemonthdownload";
    private final String bucketKeyDivideDay = "bill/dividedaydownload";
    private final String bucketKeyDivideMonth = "bill/dividemonthdownload";
    private final YosClient yosClient;

    public CustomStdApi(String partPath) {
        this.yosClient = PathConfiguration.getDefaultYosClient(partPath);
    }

    public CustomStdApi(YosClient yosClient) {
        this.yosClient = yosClient;
    }

    public InputStream tradeDayBillDownload(String merchantNo, String dayString) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("merchantNo", merchantNo);
        metadata.addUserMetadata("dayString", dayString);
        YosObject yosObject = this.yosClient.getObject("std", "bill/tradedaydownload", metadata);
        return yosObject.getObjectContent();
    }

    public InputStream tradeMonthBillDownload(String merchantNo, String monthString) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("merchantNo", merchantNo);
        metadata.addUserMetadata("monthString", monthString);
        YosObject yosObject = this.yosClient.getObject("std", "bill/trademonthdownload", metadata);
        return yosObject.getObjectContent();
    }

    public InputStream remitDayBillDownload(String merchantNo, String dayString, String dataType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("merchantNo", merchantNo);
        metadata.addUserMetadata("dayString", dayString);
        metadata.addUserMetadata("dataType", dataType);
        YosObject yosObject = this.yosClient.getObject("std", "bill/remitdaydownload", metadata);
        return yosObject.getObjectContent();
    }

    public InputStream feeMonthBillDownload(String merchantNo, String monthString, String dataType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("merchantNo", merchantNo);
        metadata.addUserMetadata("monthString", monthString);
        metadata.addUserMetadata("dataType", dataType);
        YosObject yosObject = this.yosClient.getObject("std", "bill/feemonthdownload", metadata);
        return yosObject.getObjectContent();
    }

    public InputStream divideDayBillDownload(String merchantNo, String dayString) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("merchantNo", merchantNo);
        metadata.addUserMetadata("dayString", dayString);
        YosObject yosObject = this.yosClient.getObject("std", "bill/dividedaydownload", metadata);
        return yosObject.getObjectContent();
    }

    public InputStream diviedMonthBillDownload(String merchantNo, String monthString) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("merchantNo", merchantNo);
        metadata.addUserMetadata("monthString", monthString);
        YosObject yosObject = this.yosClient.getObject("std", "bill/dividemonthdownload", metadata);
        return yosObject.getObjectContent();
    }
}
