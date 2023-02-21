package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.ThirdPartyBillInfoDao;
import com.mojieai.predict.entity.bo.ThirdPartBillOrderInfo;
import com.mojieai.predict.service.ThirdPartyBillInfoService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ThirdPartyBillInfoServiceImpl implements ThirdPartyBillInfoService {
    private Logger log = LogConstant.commonLog;

    @Autowired
    private ThirdPartyBillInfoDao thirdPartyBillInfoDao;

    @Override
    public Map<Integer, ThirdPartBillOrderInfo> getThirdPartySumIntervalTime(Timestamp beginTime, Timestamp endTime) {
        Map<Integer, ThirdPartBillOrderInfo> result = new HashMap<>();

        List<Map<String, Object>> billInfos = thirdPartyBillInfoDao.getThirdPartSumInfoByIntervalTimeAndMerchant
                (beginTime, endTime);
        if (billInfos != null && billInfos.size() > 0) {
            for (Map<String, Object> billInfo : billInfos) {
                Integer channelId = getChannelIdByMchId(billInfo.get("mchId").toString());
                if (channelId == null) {
                    continue;
                }
                String totalAmountStr = billInfo.get("amount").toString();
                String poundageStr = billInfo.get("poundage").toString();
                Double totalAmount = StringUtils.isBlank(totalAmountStr) ? 0d : Double.valueOf(CommonUtil.divide
                        (totalAmountStr, "1", 2));
                Double poundage = StringUtils.isBlank(poundageStr) ? 0d : Double.valueOf(CommonUtil.divide(poundageStr,
                        "1", 2));
                ThirdPartBillOrderInfo orderInfo = new ThirdPartBillOrderInfo();
                orderInfo.setTotalAmount(totalAmount);
                orderInfo.setTotalPoundage(poundage);
                result.put(channelId, orderInfo);
            }
        }

        //获取好店渠道金额
        result.put(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID, getHaoDianThirdPartBillOrderInfo(beginTime, endTime));
        return result;
    }

    private ThirdPartBillOrderInfo getHaoDianThirdPartBillOrderInfo(Timestamp beginTime, Timestamp endTime) {
        ThirdPartBillOrderInfo billOrderInfo = new ThirdPartBillOrderInfo();
        while (DateUtil.compareDate(beginTime, endTime) || beginTime.equals(endTime)) {
            String date = DateUtil.formatTime(beginTime, "yyyyMMdd");
            ThirdPartBillOrderInfo tempBill = getHaoDianThirdPartBillOrderInfo(date);
            if (tempBill == null) {
                beginTime = DateUtil.getIntervalDays(beginTime, 1l);
                continue;
            }
            billOrderInfo.setTotalPoundage(tempBill.getTotalPoundage() + billOrderInfo.getTotalAmount());
            billOrderInfo.setTotalAmount(tempBill.getTotalAmount() + billOrderInfo.getTotalAmount());
            beginTime = DateUtil.getIntervalDays(beginTime, 1l);
        }
        return billOrderInfo;
    }

    /**
     * @param date yyyyMMdd
     * @return
     */
    private ThirdPartBillOrderInfo getHaoDianThirdPartBillOrderInfo(String date) {
        ThirdPartBillOrderInfo billOrderInfo = new ThirdPartBillOrderInfo();
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "get_zhihui_haodian_account");
        params.put("rpt_date", date);
        String data = CommonUtil.getSignMoJieData(params);
        if (StringUtils.isBlank(data)) {
            return null;
        }
        Map<String, Object> dataMap = JSONObject.parseObject(data, HashMap.class);
        if (dataMap == null || dataMap.isEmpty() || dataMap.get("resp") == null) {
            return null;
        }
        List<Map<String, Object>> billArray = (List<Map<String, Object>>) dataMap.get("resp");
        BigDecimal totalAmount = new BigDecimal(0);
        BigDecimal feeAmount = new BigDecimal(0);
        for (Map<String, Object> temp : billArray) {
            BigDecimal tempTotalAmount = CommonUtil.convertFen2Yuan(temp.get("pay_fee").toString());
            BigDecimal tempFeeAmount = CommonUtil.convertFen2Yuan(temp.get("commission_fee").toString());//分
            totalAmount = totalAmount.add(tempTotalAmount);
            feeAmount = feeAmount.add(tempFeeAmount);
        }
        billOrderInfo.setTotalAmount(totalAmount.doubleValue());
        billOrderInfo.setTotalPoundage(feeAmount.doubleValue());
        return billOrderInfo;
    }

    private Integer getChannelIdByMchId(String mchId) {
        if (mchId.equals("10023629882")) {
            return CommonConstant.YOP_PAY_CHANNEL_ID;
        } else if (mchId.equals("111069740002")) {
            return CommonConstant.JD_PAY_CHANNEL_ID;
        }
        return null;
    }
}
