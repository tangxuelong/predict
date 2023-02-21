package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SportsRecommendOrderReport;

import java.util.List;

public interface SportsRecommendOrderReportDao {

    SportsRecommendOrderReport getSportsRecommendOrderReportByDate(Integer date);

    List<SportsRecommendOrderReport> getAllSportsRecommendOrderReportByDate(Integer beginDate, Integer endDate);

    Integer insert(SportsRecommendOrderReport sportsRecommendOrderReport);

    Integer update(SportsRecommendOrderReport report);
}
