package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.OrderStatisticReportDao;
import com.mojieai.predict.dao.SportsRecommendOrderReportDao;
import com.mojieai.predict.dao.UserAccountFlowDao;
import com.mojieai.predict.dao.UserBuyRecommendDao;
import com.mojieai.predict.entity.po.OrderStatisticReport;
import com.mojieai.predict.entity.po.SportsRecommendOrderReport;
import com.mojieai.predict.entity.po.UserBuyRecommend;
import com.mojieai.predict.entity.vo.MJOrderStatisticReportVo;
import com.mojieai.predict.service.OrderStatisticReportService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
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
public class OrderStatisticReportServiceImpl implements OrderStatisticReportService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private OrderStatisticReportDao orderStatisticReportDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;
    @Autowired
    private SportsRecommendOrderReportDao sportsRecommendOrderReportDao;
    @Autowired
    private UserBuyRecommendDao userBuyRecommendDao;

    @Override
    public Map<String, Object> getOrderStatisticReport() {
        Map<String, Object> result = new HashMap<>();
        List<MJOrderStatisticReportVo> report = new ArrayList<>();

        List<OrderStatisticReport> orderStatisticReport = orderStatisticReportDao.getOrderStatisticReport(50);
        if (orderStatisticReport != null && orderStatisticReport.size() > 0) {
            for (OrderStatisticReport tempReport : orderStatisticReport) {
                MJOrderStatisticReportVo mjOrderStatisticReportVo = convertOrderStatis2MJVo(tempReport);
                if (mjOrderStatisticReportVo != null) {
                    report.add(mjOrderStatisticReportVo);
                }
            }
        }

        result.put("data", report);
        result.put("total", report.size());
        return result;
    }

    @Override
    public Map<String, Object> getSportsRecommendOrderReport(Integer beginDate, Integer endDate) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> reports = new ArrayList<>();
        List<SportsRecommendOrderReport> recommendReports = sportsRecommendOrderReportDao
                .getAllSportsRecommendOrderReportByDate(beginDate, endDate);

        if (recommendReports != null && recommendReports.size() > 0) {
            for (SportsRecommendOrderReport report : recommendReports) {

                Map<String, Object> tempReportMap = new HashMap<>();
                tempReportMap.put("date", report.getReportDate());
                tempReportMap.put("wisdomOrderCount", report.getWisdomOrderCount());
                tempReportMap.put("wisdomAmount", CommonUtil.convertFen2Yuan(report.getWisdomAmount()));
                tempReportMap.put("cashOrderCount", report.getCashOrderCount());
                tempReportMap.put("cashAmount", CommonUtil.convertFen2Yuan(report.getCashAmount()));
                tempReportMap.put("totalCount", report.getWisdomOrderCount() + report.getCashOrderCount());
                tempReportMap.put("totalAmount", CommonUtil.convertFen2Yuan(report.getCashAmount() + report
                        .getWisdomAmount()));
                tempReportMap.put("couponOrderCount", report.getCouponOrderCount());
                tempReportMap.put("couponAmount", CommonUtil.convertFen2Yuan(report.getCouponAmount()));
                reports.add(tempReportMap);
            }
        }

        result.put("reports", reports);
        return result;
    }

    @Override
    public void generateTodayLivingOrderReportTiming() {
        Boolean insertFlag = true;
        String today = DateUtil.getCurrentDay();
        Integer statisticDate = Integer.valueOf(today);
        OrderStatisticReport orderReport = orderStatisticReportDao.getOrderStatisticReportByDate(statisticDate);
        if (orderReport != null) {
            if (orderReport.getStatisticFlag().equals(CommonConstant.ORDER_STATISTIC_FLAG_YES)) {
                return;
            }
            insertFlag = false;
        }
        Timestamp beginTime = DateUtil.getBeginOfOneDay(DateUtil.formatString(today, "yyyyMMdd"));
        Timestamp endTime = DateUtil.getEndOfOneDay(DateUtil.formatString(today, "yyyyMMdd"));

        orderReport = statisticOneDateOrder(beginTime, endTime, statisticDate, orderReport);
        orderReport.setStatisticFlag(CommonConstant.ORDER_STATISTIC_FLAG_NO);
        try {
            if (insertFlag) {
                orderStatisticReportDao.insert(orderReport);
            } else {
                orderStatisticReportDao.updateOrderReport(orderReport);
            }
        } catch (DuplicateKeyException e) {
        }
    }

    @Override
    public void generateOrderStatisticReportTiming() {
        Boolean insertFlag = true;
        String yesterday = DateUtil.getYesterday("yyyyMMdd");
        Integer statisticDate = Integer.valueOf(yesterday);
        OrderStatisticReport orderReport = orderStatisticReportDao.getOrderStatisticReportByDate(statisticDate);
        if (orderReport != null) {
            if (orderReport.getStatisticFlag().equals(CommonConstant.ORDER_STATISTIC_FLAG_YES)) {
                return;
            }
            insertFlag = false;
        }
        Timestamp beginTime = DateUtil.getBeginOfOneDay(DateUtil.formatString(yesterday, "yyyyMMdd"));
        Timestamp endTime = DateUtil.getEndOfOneDay(DateUtil.formatString(yesterday, "yyyyMMdd"));

        orderReport = statisticOneDateOrder(beginTime, endTime, statisticDate, orderReport);
        orderReport.setStatisticFlag(CommonConstant.ORDER_STATISTIC_FLAG_YES);
        try {
            if (insertFlag) {
                orderStatisticReportDao.insert(orderReport);
            } else {
                orderStatisticReportDao.updateOrderReport(orderReport);
            }
        } catch (DuplicateKeyException e) {
        }
    }

    @Override
    public void generateZHCPSportsOrderReportTiming() {
        String yesterday = DateUtil.getYesterday("yyyyMMdd");
        Integer statisticDate = Integer.valueOf(yesterday);

        SportsRecommendOrderReport report = sportsRecommendOrderReportDao.getSportsRecommendOrderReportByDate
                (statisticDate);
        Boolean insertFlag = Boolean.TRUE;
        if (report != null) {
            if (report.getStatisticFlag().equals(CommonConstant.ORDER_STATISTIC_FLAG_YES)) {
                return;
            }
            insertFlag = Boolean.FALSE;
        }

        report = statisticZHCPSportsOrderReport(statisticDate);
        if (report == null) {
            return;
        }
        report.setStatisticFlag(CommonConstant.ORDER_STATISTIC_FLAG_YES);
        try {
            if (insertFlag) {
                sportsRecommendOrderReportDao.insert(report);
            } else {
                sportsRecommendOrderReportDao.update(report);
            }
        } catch (DuplicateKeyException e) {
        }
    }

    private SportsRecommendOrderReport statisticZHCPSportsOrderReport(Integer statisticDate) {
        SportsRecommendOrderReport report = new SportsRecommendOrderReport();
        Timestamp statisticDateT = DateUtil.formatString(statisticDate + "", "yyyyMMdd");
        if (statisticDateT == null) {
            return null;
        }
        Timestamp begin = DateUtil.getBeginOfOneDay(statisticDateT);
        Timestamp end = DateUtil.getEndOfOneDay(statisticDateT);

        Integer couponCount = 0;
        Long couponAmount = 0l;
        Map<String, Object> couponMap = userBuyRecommendDao.getCouponAmountAndCountFromOtter(begin, end);
        if (couponMap != null && !couponMap.isEmpty()) {
            if (couponMap.containsKey("amount")) {
                couponAmount = Long.valueOf(couponMap.get("amount").toString());
            }
            if (couponMap.containsKey("num")) {
                couponCount = Integer.valueOf(couponMap.get("num").toString());
            }
        }

        Integer cashCount = 0;
        Long cashAmount = 0L;
        Integer wisdomCount = 0;
        Long wisdomAmount = 0L;
        List<Map<String, Object>> userBuyRecommends = userBuyRecommendDao.getNotCouponOrderFromOtter(begin, end);
        if (userBuyRecommends != null && userBuyRecommends.size() > 0) {
            for (Map<String, Object> payRecommend : userBuyRecommends) {
                if (payRecommend.containsKey("channel")) {
                    Long tempAmount = Long.valueOf(payRecommend.get("amount").toString());
                    Integer tempCount = Integer.valueOf(payRecommend.get("num").toString());
                    if (payRecommend.get("channel").toString().equals(CommonConstant.WISDOM_COIN_CHANNEL_ID + "")) {
                        wisdomAmount += tempAmount;
                        wisdomCount += tempCount;
                    } else {
                        cashCount += tempCount;
                        cashAmount += tempAmount;
                    }
                }
            }
        }

        report.setCashAmount(cashAmount);
        report.setCashOrderCount(cashCount);
        report.setCouponAmount(couponAmount);
        report.setCouponOrderCount(couponCount);
        report.setReportDate(statisticDate);
        report.setStatisticFlag(0);
        report.setWisdomAmount(wisdomAmount);
        report.setWisdomOrderCount(wisdomCount);
        return report;
    }

    private OrderStatisticReport statisticOneDateOrder(Timestamp beginTime, Timestamp endTime, Integer statisticDate,
                                                       OrderStatisticReport orderReport) {
        OrderStatisticReport report = new OrderStatisticReport();

        Long totalAmount = 0l;
        Integer totalOrderNum = 0;
        Integer totalUserNum = 0;
        Map<String, Object> totalOrderMap = userAccountFlowDao.getOrderNumAndAmountFromOtter(beginTime, endTime, null);
        if (totalOrderMap != null && !totalOrderMap.isEmpty()) {
            totalAmount = Long.valueOf(totalOrderMap.get("amount").toString());
            totalOrderNum = Integer.valueOf(totalOrderMap.get("num").toString());
            totalUserNum = Integer.valueOf(totalOrderMap.get("user_num").toString());
        }

        Long cashAmount = 0l;
        Integer cashOrderNum = 0;
        Map<String, Object> cashOrderMap = userAccountFlowDao.getOrderNumAndAmountFromOtter(beginTime, endTime,
                CommonConstant.PAY_TYPE_CASH);
        if (cashOrderMap != null && !cashOrderMap.isEmpty()) {
            cashAmount = Long.valueOf(cashOrderMap.get("amount").toString());
            cashOrderNum = Integer.valueOf(cashOrderMap.get("num").toString());
        }

        Long oldUserAmount = 0l;
        Integer oldUserNum = 0;
        Map<String, Object> oldUserOrderMap = userAccountFlowDao.getOldUserOrderFromOtter(beginTime, endTime);
        if (oldUserOrderMap != null && !oldUserOrderMap.isEmpty()) {
            oldUserAmount = Long.valueOf(oldUserOrderMap.get("amount").toString());
            oldUserNum = Integer.valueOf(oldUserOrderMap.get("num").toString());
        }

        Integer newUserNum = totalUserNum - oldUserNum;
        Long newUserAmount = totalAmount - oldUserAmount;

        report.setNewUserAmount(newUserAmount);
        report.setNewUserNum(newUserNum);
        report.setOldUserAmount(oldUserAmount);
        report.setOldUserNum(oldUserNum);
        report.setRealPayAmount(cashAmount);
        report.setRealPayNum(cashOrderNum);
        report.setStatisticDate(statisticDate);
        report.setTotalAmount(totalAmount);
        report.setTotalOrderNum(totalOrderNum);
        report.setTotalUserNum(totalUserNum);
        return report;
    }

    private MJOrderStatisticReportVo convertOrderStatis2MJVo(OrderStatisticReport orderStatisticReport) {
        MJOrderStatisticReportVo mjOrderStatisticReportVo = new MJOrderStatisticReportVo();

        Integer oldUserNum = orderStatisticReport.getOldUserNum();
        Integer totalUserNum = orderStatisticReport.getTotalUserNum();
        String repurchaseRate = CommonUtil.multiply(CommonUtil.divide(oldUserNum + "", totalUserNum + "", 2), "100").toString();

        mjOrderStatisticReportVo.setDate(orderStatisticReport.getStatisticDate());
        mjOrderStatisticReportVo.setNew_user_amount(CommonUtil.convertFen2Yuan(orderStatisticReport.getNewUserAmount
                ()).intValue());
        mjOrderStatisticReportVo.setNew_user_num(orderStatisticReport.getNewUserNum());
        mjOrderStatisticReportVo.setOld_user_amount(CommonUtil.convertFen2Yuan(orderStatisticReport.getOldUserAmount
                ()).intValue());
        mjOrderStatisticReportVo.setOld_user_num(oldUserNum);
        mjOrderStatisticReportVo.setReal_pay_amount(CommonUtil.convertFen2Yuan(orderStatisticReport.getRealPayAmount
                ()).intValue());
        mjOrderStatisticReportVo.setReal_pay_num(orderStatisticReport.getRealPayNum());
        mjOrderStatisticReportVo.setRepurchase_rate(repurchaseRate);
        mjOrderStatisticReportVo.setTotal_amount(CommonUtil.convertFen2Yuan(orderStatisticReport.getTotalAmount())
                .intValue());
        mjOrderStatisticReportVo.setTotal_order_num(orderStatisticReport.getTotalOrderNum());
        mjOrderStatisticReportVo.setTotal_user_num(totalUserNum);
        return mjOrderStatisticReportVo;
    }
}
