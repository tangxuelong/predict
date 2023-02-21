package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ThirdPartyBillInfo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface ThirdPartyBillInfoDao {

    List<ThirdPartyBillInfo> getThirdPartyBillInfoByTime(Integer beginTime, Integer endTime, String mchId, String
            status, String businessType);

    List<Map<String, Object>> getThirdPartSumInfoByIntervalTimeAndMerchant(Timestamp beginTime, Timestamp endTime);

    Map<String, Object> getSumThirdPartyBillInfoByTimeAndType(Timestamp beginTimeT, Timestamp endTimeT, String
            businessType, String status, String mchId);

    Integer insert(ThirdPartyBillInfo thirdPartyBillInfo);
}
