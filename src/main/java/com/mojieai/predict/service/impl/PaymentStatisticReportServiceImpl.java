package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.dao.PaymentStatisticReportDao;
import com.mojieai.predict.dao.UserAccountFlowDao;
import com.mojieai.predict.entity.po.PaymentStatisticReport;
import com.mojieai.predict.entity.po.UserAccountFlow;
import com.mojieai.predict.entity.vo.MJPaymentReportVo;
import com.mojieai.predict.enums.BillEnum;
import com.mojieai.predict.service.PaymentStatisticReportService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentStatisticReportServiceImpl implements PaymentStatisticReportService {
    protected Logger log = LogConstant.commonLog;
    @Autowired
    private PaymentStatisticReportDao paymentStatisticReportDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;

    @Override
    public Map<String, Object> getPaymentStatisticReport() {
        Map<String, Object> result = new HashMap<>();
        List<MJPaymentReportVo> reports = new ArrayList<>();
        List<PaymentStatisticReport> paymentStatisticReports = paymentStatisticReportDao.getAllPaymentStatisticReport(100);
        if (paymentStatisticReports == null || paymentStatisticReports.size() == 0) {
            return null;
        }
        for (PaymentStatisticReport payment : paymentStatisticReports) {
            MJPaymentReportVo tempVo = convertPaymentStatistic2MJPaymentReportVo(payment);
            if (tempVo == null) {
                continue;
            }
            reports.add(tempVo);
        }
        result.put("data", reports);
        result.put("total", reports.size());
        return result;
    }

    @Override
    public void generateTodayLivingPaymentReportTiming() {
        Boolean insertFlag = true;
        String today = DateUtil.getCurrentDay();
        Integer statisticDate = Integer.valueOf(today);
        PaymentStatisticReport paymentReport = paymentStatisticReportDao.getPaymentStatisticReportByDate(statisticDate);
        if (paymentReport != null) {
            if (paymentReport.getStatisticFlag().equals(CommonConstant.PAYMENT_STATISTIC_FLAG_YES)) {
                return;
            }
            insertFlag = false;
        }
        Timestamp beginTime = DateUtil.getBeginOfOneDay(DateUtil.formatString(today, "yyyyMMdd"));
        Timestamp endTime = DateUtil.getEndOfOneDay(DateUtil.formatString(today, "yyyyMMdd"));

        paymentReport = statisticOneDatePayment(beginTime, endTime, statisticDate, paymentReport);
        paymentReport.setStatisticFlag(CommonConstant.PAYMENT_STATISTIC_FLAG_NO);
        try {
            if (insertFlag) {
                paymentStatisticReportDao.insert(paymentReport);
            } else {
                paymentStatisticReportDao.update(paymentReport);
            }
        } catch (DuplicateKeyException e) {
        }
    }

    @Override
    public void generatePaymentStatisticReportTiming() {
        Boolean insertFlag = true;
        String yesterday = DateUtil.getYesterday("yyyyMMdd");
        Integer statisticDate = Integer.valueOf(yesterday);
        PaymentStatisticReport paymentReport = paymentStatisticReportDao.getPaymentStatisticReportByDate(statisticDate);
        if (paymentReport != null) {
            if (paymentReport.getStatisticFlag().equals(CommonConstant.PAYMENT_STATISTIC_FLAG_YES)) {
                return;
            }
            insertFlag = false;
        }
        Timestamp beginTime = DateUtil.getBeginOfOneDay(DateUtil.formatString(yesterday, "yyyyMMdd"));
        Timestamp endTime = DateUtil.getEndOfOneDay(DateUtil.formatString(yesterday, "yyyyMMdd"));

        paymentReport = statisticOneDatePayment(beginTime, endTime, statisticDate, paymentReport);
        try {
            if (insertFlag) {
                paymentStatisticReportDao.insert(paymentReport);
            } else {
                paymentStatisticReportDao.update(paymentReport);
            }
        } catch (DuplicateKeyException e) {
        }
    }

    @Override
    public PaymentStatisticReport statisticOneDatePayment(Timestamp beginTime, Timestamp endTime, Integer
            statisticDate, PaymentStatisticReport paymentReport) {
        List<UserAccountFlow> flows = userAccountFlowDao.getAllCashFlowFromOtter(beginTime, endTime);
        if (flows == null || flows.size() == 0) {
            return paymentReport;
        }
        Long incomeAmount = 0l;
        Map<Integer, Long> productIncomeMap = new HashMap<>();
        Map<Integer, Map<String, Object>> payChannelMap = new HashMap<>();
        for (UserAccountFlow flow : flows) {
            if (flow == null || StringUtils.isBlank(flow.getRemark())) {
                continue;
            }
            Map<String, Object> remark = JSONObject.parseObject(flow.getRemark(), HashMap.class);
            String callBack = remark.get("clazzMethodName").toString();
            if (StringUtils.isBlank(callBack)) {
                continue;
            }
            BillEnum billEnum = BillEnum.getBillEnumByCallBackMethod(callBack);
            if (billEnum == null) {
                log.error("callBackMethod:" + callBack + " not exist billEnum");
                throw new IllegalArgumentException();
            }
            //计算产品收入
            Integer productIncomeKey = billEnum.getProductType();
            Long productIncome = flow.getPayAmount();
            if (productIncomeMap.containsKey(productIncomeKey)) {
                productIncome = productIncomeMap.get(productIncomeKey) + productIncome;
            }
            productIncomeMap.put(productIncomeKey, productIncome);

            //计算渠道信息
            Integer channelKey = flow.getChannel();
            Map<String, Object> payDetailMap = null;
            Integer count = 1;
            Long amount = flow.getPayAmount() == null ? 0l : flow.getPayAmount();
            if (payChannelMap.containsKey(channelKey)) {
                payDetailMap = payChannelMap.get(channelKey);
                amount = amount + Long.valueOf(payDetailMap.get("amount").toString());
                count += Integer.valueOf(payDetailMap.get("count").toString());
            } else {
                payDetailMap = new HashMap<>();
            }

            payDetailMap.put("amount", amount);
            payDetailMap.put("count", count);

            payChannelMap.put(channelKey, payDetailMap);

            incomeAmount += (flow.getPayAmount() == null ? 0l : flow.getPayAmount());
        }

        if (paymentReport == null) {
            paymentReport = new PaymentStatisticReport();
        }

        Integer payPersonCount = userAccountFlowDao.getPayPersonCountFromOtter(beginTime, endTime);
        Long repurchaseAmount = userAccountFlowDao.getRepurchaseAmountFromOtter(beginTime, endTime);

        paymentReport.setStatisticDate(statisticDate);
        paymentReport.setIncomeAmount(incomeAmount);
        paymentReport.setIncomeDetail(JSONObject.toJSONString(productIncomeMap));
        paymentReport.setPayChannelDetail(JSONObject.toJSONString(payChannelMap));
        paymentReport.setPayPersonCount(payPersonCount == null ? 0 : payPersonCount);
        paymentReport.setRepurchaseAmount(repurchaseAmount == null ? 0 : repurchaseAmount);
        paymentReport.setStatisticFlag(CommonConstant.PAYMENT_STATISTIC_FLAG_YES);

        return paymentReport;
    }

    private MJPaymentReportVo convertPaymentStatistic2MJPaymentReportVo(PaymentStatisticReport paymentStatisticReport) {
        if (paymentStatisticReport == null) {
            return null;
        }
        MJPaymentReportVo mjPaymentReportVo = new MJPaymentReportVo();

        Long appleIncome = 0l;
        Integer appleNum = 0;
        Long wxIncome = 0l;
        Integer wxNum = 0;
        Long aliIncome = 0l;
        Integer aliNum = 0;
        String payChannelDetail = paymentStatisticReport.getPayChannelDetail();
        if (StringUtils.isNotBlank(payChannelDetail)) {
            Map<Integer, Map<String, Object>> payChannelMap = JSONObject.parseObject(payChannelDetail, HashMap.class);
            appleIncome = getChannelIncomeFromPayChannelMap(payChannelMap, CommonConstant.APPLE_PAY_CHANNEL_ID);
            appleNum = getChannelCountFromPayChannelMap(payChannelMap, CommonConstant.APPLE_PAY_CHANNEL_ID);
            wxIncome = getChannelIncomeFromPayChannelMap(payChannelMap, CommonConstant.WX_PAY_CHANNEL_ID);
            wxNum = getChannelCountFromPayChannelMap(payChannelMap, CommonConstant.WX_PAY_CHANNEL_ID);
            aliIncome = getChannelIncomeFromPayChannelMap(payChannelMap, CommonConstant.ALI_PAY_CHANNEL_ID);
            aliNum = getChannelCountFromPayChannelMap(payChannelMap, CommonConstant.ALI_PAY_CHANNEL_ID);
        }

        Long bigDataIncome = 0l;
        Long wisdomIncome = 0l;
        Long vipIncome = 0l;
        Long subscribeIncome = 0l;
        Long recommendIncome = 0l;
        Long programIncome = 0l;
        String incomeDetail = paymentStatisticReport.getIncomeDetail();
        if (StringUtils.isNotBlank(incomeDetail)) {
            Map<Integer, Long> incomeMap = JSONObject.parseObject(incomeDetail, HashMap.class);
            bigDataIncome = getProductIncomeFromMap(incomeMap, CommonConstant.BILL_PRODUCT_TYPE_RESONANCE_DATA);
            recommendIncome = getProductIncomeFromMap(incomeMap, CommonConstant.BILL_PRODUCT_TYPE_FOOTBALL_RECOMMEND);
            programIncome = getProductIncomeFromMap(incomeMap, CommonConstant.BILL_PRODUCT_TYPE_PROGRAM);
            subscribeIncome = getProductIncomeFromMap(incomeMap, CommonConstant.BILL_PRODUCT_TYPE_SUBSCRIBE_KILL);
            vipIncome = getProductIncomeFromMap(incomeMap, CommonConstant.BILL_PRODUCT_TYPE_VIP);
            wisdomIncome = getProductIncomeFromMap(incomeMap, CommonConstant.BILL_PRODUCT_TYPE_WISDOM);
        }

        String repurchaseRate = CommonUtil.multiply(CommonUtil.divide(paymentStatisticReport.getRepurchaseAmount() + "",
                paymentStatisticReport.getIncomeAmount() + "", 2), "100").toString();

        mjPaymentReportVo.setApple_income(CommonUtil.convertFen2Yuan(appleIncome).intValue());
        mjPaymentReportVo.setApple_num(appleNum);
        mjPaymentReportVo.setBig_data_income(CommonUtil.convertFen2Yuan(bigDataIncome).intValue());
        mjPaymentReportVo.setCoin_income(CommonUtil.convertFen2Yuan(wisdomIncome).intValue());
        mjPaymentReportVo.setDate(String.valueOf(paymentStatisticReport.getStatisticDate()));
        mjPaymentReportVo.setPrediction_income(CommonUtil.convertFen2Yuan(recommendIncome).intValue());
        mjPaymentReportVo.setProgram_income(CommonUtil.convertFen2Yuan(programIncome).intValue());
        mjPaymentReportVo.setRepurchase_income(CommonUtil.convertFen2Yuan(paymentStatisticReport.getRepurchaseAmount
                ()).intValue());
        mjPaymentReportVo.setRepurchase_rate(repurchaseRate);
        mjPaymentReportVo.setSubscribe_income(CommonUtil.convertFen2Yuan(subscribeIncome).intValue());
        mjPaymentReportVo.setTotal_income(CommonUtil.convertFen2Yuan(paymentStatisticReport.getIncomeAmount()).intValue());
        mjPaymentReportVo.setTotal_pay_user_num(paymentStatisticReport.getPayPersonCount());
        mjPaymentReportVo.setVip_income(CommonUtil.convertFen2Yuan(vipIncome).intValue());
        mjPaymentReportVo.setWeixin_income(CommonUtil.convertFen2Yuan(wxIncome).intValue());
        mjPaymentReportVo.setWeixin_num(wxNum);
        mjPaymentReportVo.setZhifubao_income(CommonUtil.convertFen2Yuan(aliIncome).intValue());
        mjPaymentReportVo.setZhifubao_num(aliNum);
        return mjPaymentReportVo;
    }

    private Long getProductIncomeFromMap(Map<Integer, Long> incomeMap, Integer productType) {
        if (!incomeMap.containsKey(productType)) {
            return 0l;
        }
        return Long.valueOf(String.valueOf(incomeMap.get(productType)));
    }

    private Long getChannelIncomeFromPayChannelMap(Map<Integer, Map<String, Object>> payChannelMap, Integer channelId) {
        Long result = 0l;
        if (payChannelMap.containsKey(channelId)) {
            result = Long.valueOf(payChannelMap.get(channelId).get("amount").toString());
        }
        return result;
    }

    private Integer getChannelCountFromPayChannelMap(Map<Integer, Map<String, Object>> payChannelMap, Integer
            channelId) {
        Integer result = 0;
        if (payChannelMap.containsKey(channelId)) {
            result = Integer.valueOf(payChannelMap.get(channelId).get("count").toString());
        }
        return result;
    }
}
