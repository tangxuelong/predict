package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.OrderStatisticReport;

import java.util.List;

public interface OrderStatisticReportDao {

    OrderStatisticReport getOrderStatisticReportByDate(Integer statisticDate);

    List<OrderStatisticReport> getOrderStatisticReport(Integer count);

    Integer updateOrderReport(OrderStatisticReport orderStatisticReport);

    Integer insert(OrderStatisticReport orderStatisticReport);
}
