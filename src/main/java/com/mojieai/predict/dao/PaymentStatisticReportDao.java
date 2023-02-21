package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PaymentStatisticReport;

import java.util.List;

public interface PaymentStatisticReportDao {

    PaymentStatisticReport getPaymentStatisticReportByDate(Integer statisticDate);

    List<PaymentStatisticReport> getAllPaymentStatisticReport(Integer count);

    Integer update(PaymentStatisticReport paymentStatisticReport);

    Integer insert(PaymentStatisticReport paymentStatisticReport);
}
