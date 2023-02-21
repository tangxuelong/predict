package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.ThirdPartBillOrderInfo;

import java.sql.Timestamp;
import java.util.Map;

public interface ThirdPartyBillInfoService {

    Map<Integer, ThirdPartBillOrderInfo> getThirdPartySumIntervalTime(Timestamp beginTime, Timestamp endTime);
}
