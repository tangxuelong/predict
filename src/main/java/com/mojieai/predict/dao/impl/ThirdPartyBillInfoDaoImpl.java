package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.ThirdPartyBillInfoDao;
import com.mojieai.predict.entity.po.ThirdPartyBillInfo;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ThirdPartyBillInfoDaoImpl extends BaseDao implements ThirdPartyBillInfoDao {

    @Override
    public List<ThirdPartyBillInfo> getThirdPartyBillInfoByTime(Integer beginTime, Integer endTime, String mchId, String
            status, String businessType) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginRptDate", beginTime);
        param.put("endRptDate", endTime);
        param.put("mchId", mchId);
        param.put("status", status);
        param.put("businessType", businessType);
        return slaveSqlSessionTemplate.selectList("ThirdPartyBillInfo.getThirdPartyBillInfoByTime", param);
    }

    @Override
    public List<Map<String, Object>> getThirdPartSumInfoByIntervalTimeAndMerchant(Timestamp beginTime, Timestamp
            endTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        return slaveSqlSessionTemplate.selectList("ThirdPartyBillInfo.getThirdPartSumInfoByIntervalTimeAndMerchant", param);
    }

    @Override
    public Map<String, Object> getSumThirdPartyBillInfoByTimeAndType(Timestamp beginTime, Timestamp endTime, String
            businessType, String status, String mchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("businessType", businessType);
        params.put("status", status);
        params.put("mchId", mchId);
        return slaveSqlSessionTemplate.selectOne("ThirdPartyBillInfo.getSumThirdPartyBillInfoByTimeAndType", params);
    }

    @Override
    public Integer insert(ThirdPartyBillInfo thirdPartyBillInfo) {
        return sqlSessionTemplate.insert("ThirdPartyBillInfo.insert", thirdPartyBillInfo);
    }
}
