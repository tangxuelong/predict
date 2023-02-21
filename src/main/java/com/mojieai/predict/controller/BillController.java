package com.mojieai.predict.controller;

import com.mojieai.predict.constant.PurchaseOrderConstant;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对账业务
 */
@RequestMapping("/bill")
@Controller
public class BillController extends BaseController {

    @Autowired
    private BillService billService;
    @Autowired
    private PurchaseOrderStatisticService purchaseOrderStatisticService;
    @Autowired
    private AllProductBillService allProductBillService;
    @Autowired
    private PaymentStatisticReportService paymentStatisticReportService;
    @Autowired
    private OrderStatisticReportService orderStatisticReportService;

    /**
     * 获取对账业务信息
     *
     * @param endTime   2018-01-02
     * @param beginTime
     * @return
     */
    @RequestMapping("/getBillInfoWithOutSign")
    @ResponseBody
    public Object getBillInfo(@RequestParam String endTime, String beginTime) {

        Date endTimeTemp = DateUtil.formatToDate(endTime, DateUtil.DEFAULT_DATE_FORMAT);
        if (endTimeTemp == null) {
            return buildErrJson("日期格式错误");
        }
        endTime = DateUtil.formatDate(endTimeTemp) + " 23:59:59";
        Timestamp endTimeT = DateUtil.formatString(endTime, DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS);
        Timestamp beginTimeT = null;
        if (StringUtils.isNotBlank(beginTime)) {
            beginTime = beginTime + " 00:00:00";
            beginTimeT = DateUtil.formatString(beginTime, DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS);
        }
        Map res = billService.getDailyBill(beginTimeT, endTimeT);
        return buildSuccJson(res);
    }

    @RequestMapping("/getOrderStatisticWithOutSign")
    @ResponseBody
    public Object getOrderStatisticData(@RequestParam String beginTime, @RequestParam String endTime) {
        String beginTimeStr = DateUtil.formatTime(DateUtil.formatToTimestamp(beginTime, "yyyy-MM-dd"), "yyyyMMdd");
        String endTimeStr = DateUtil.formatTime(DateUtil.formatToTimestamp(endTime, "yyyy-MM-dd"), "yyyyMMdd");
        int beginDate = Integer.valueOf(beginTimeStr);
        int endDate = Integer.valueOf(endTimeStr);
        Map res = purchaseOrderStatisticService.getPurchaseOrderStatisticInfo(beginDate, endDate, PurchaseOrderConstant
                .PURCHASE_ORDER_CLASS_VIP);
        return buildSuccJson(res);
    }

    @RequestMapping("/getProgramOrderStatisticWithOutSign")
    @ResponseBody
    public Object getProgramOrderStatisticWithOutSign(@RequestParam String beginTime, @RequestParam String endTime,
                                                      @RequestParam Integer isVip) {
        Integer beginDate = Integer.valueOf(beginTime);
        Integer endDate = Integer.valueOf(endTime);
        List<Map<String, Object>> res = billService.getProgramSaleDailyStats(beginDate, endDate, isVip);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("statisticDatas", res);
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/getAllProductPurchaseInfoWithOutSign")
    @ResponseBody
    public Object getAllProductPurchaseInfo(@RequestParam String beginTime, @RequestParam String endTime) {

        Integer beginDate = Integer.valueOf(beginTime);
        Integer endDate = Integer.valueOf(endTime);
        Map res = allProductBillService.getAllProductBills(beginDate, endDate);
        return buildSuccJson(res);
    }

    /*
     * @用户设备和注册报表
     * */
    @RequestMapping("/user_statistic_WithOutSign")
    @ResponseBody
    public Object user_statistic_with_out_sign(@RequestParam(required = false) Integer page) {
        Map<String, Object> resultMap = billService.getUserStatisticTable(page);
        return buildSuccJson(resultMap);
    }

    /*
     * @用户设备和注册报表
     * */
    @RequestMapping("/user_statistic_report_WithOutSign")
    @ResponseBody
    public Object user_statistic_with_out_sign(@RequestParam(required = false) Integer page, @RequestParam Integer
            type) {
        Map<String, Object> resultMap = billService.getUserStatisticTableByType(page, type);
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/payment_statistics_report_WithOutSign")
    @ResponseBody
    public Object paymentStatisticReport() {
        return buildSuccJson(paymentStatisticReportService.getPaymentStatisticReport());
    }

    @RequestMapping("/order_statistic_report_WithOutSign")
    @ResponseBody
    public Object orderStatisticReport() {
        return buildSuccJson(orderStatisticReportService.getOrderStatisticReport());
    }

    @RequestMapping("/cp_sports_recommend_order_report_no_sign")
    @ResponseBody
    public Object zhcpSportsRecommendOrderReport(String beginDateStr, String endDateStr) {
        Integer beginDate = StringUtils.isBlank(beginDateStr) ? null : Integer.valueOf(beginDateStr);
        Integer endDate = StringUtils.isBlank(endDateStr) ? null : Integer.valueOf(endDateStr);
        if (endDate != null && beginDate != null && endDate <= beginDate) {
            endDate = null;
        }
        return buildSuccJson(orderStatisticReportService.getSportsRecommendOrderReport(beginDate, endDate));
    }

    @RequestMapping("/get_yop_history_account_no_sign")
    @ResponseBody
    public Object getYopHistoryAccount(@RequestParam Integer beginTime, @RequestParam Integer endTime, @RequestParam
            String mchId) {
        return buildSuccJson(billService.getYopHistoryAccount(beginTime, endTime, mchId));
    }

    @RequestMapping("/get_jd_history_account_no_sign")
    @ResponseBody
    public Object getJdHistoryAccount(@RequestParam Integer beginTime, @RequestParam Integer endTime, @RequestParam
            String mchId, String status, String businessType) {
        if (StringUtils.isBlank(status)) {
            status = null;
        }
        if (StringUtils.isBlank(businessType)) {
            businessType = null;
        }
        return buildSuccJson(billService.getJdHistoryAccount(beginTime, endTime, mchId, status, businessType));
    }

    @RequestMapping("/get_withdraw_bill_no_sign")
    @ResponseBody
    public Object getWithdrawBill(@RequestParam String endTime, String beginTime) {

        Timestamp endTimeTemp = DateUtil.formatString(endTime, DateUtil.DEFAULT_DATE_FORMAT);
        if (endTimeTemp == null) {
            return buildErrJson("日期格式错误");
        }
        Timestamp endTimeT = CommonUtil.getSomeDateJoinTime(endTimeTemp, "23:59:59");
        Timestamp beginTimeT = null;
        if (StringUtils.isNotBlank(beginTime)) {
            beginTimeT = DateUtil.formatString(beginTime, DateUtil.DEFAULT_DATE_FORMAT);
            beginTimeT = CommonUtil.getSomeDateJoinTime(beginTimeT, "00:00:00");
        }
        return buildSuccJson(billService.getWithdrawReportForm(beginTimeT, endTimeT));
    }

    @RequestMapping("/getJdBillCompensateWithOutSign")
    @ResponseBody
    public void getJDBILLCompensate(@RequestParam String date) {

       billService.downloadJDBillCompensate(date);
    }
}
