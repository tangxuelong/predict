package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.PaymentStatisticReport;

import java.sql.Timestamp;
import java.util.Map;

public interface PaymentStatisticReportService {

    Map<String, Object> getPaymentStatisticReport();

    void generateTodayLivingPaymentReportTiming();

    void generatePaymentStatisticReportTiming();

    PaymentStatisticReport statisticOneDatePayment(Timestamp beginTime, Timestamp endTime, Integer statisticDate,
                                                   PaymentStatisticReport paymentReport);
}
