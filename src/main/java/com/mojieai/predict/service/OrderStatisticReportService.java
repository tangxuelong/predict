package com.mojieai.predict.service;

import java.util.Map;

public interface OrderStatisticReportService {

    Map<String, Object> getOrderStatisticReport();

    Map<String, Object> getSportsRecommendOrderReport(Integer beginDate, Integer endDate);

    void generateTodayLivingOrderReportTiming();

    void generateOrderStatisticReportTiming();

    void generateZHCPSportsOrderReportTiming();
}
