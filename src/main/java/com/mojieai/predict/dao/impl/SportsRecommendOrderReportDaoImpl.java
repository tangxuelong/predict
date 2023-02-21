package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SportsRecommendOrderReportDao;
import com.mojieai.predict.entity.po.SportsRecommendOrderReport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SportsRecommendOrderReportDaoImpl extends BaseDao implements SportsRecommendOrderReportDao {

    @Override
    public SportsRecommendOrderReport getSportsRecommendOrderReportByDate(Integer date) {
        Map<String, Object> param = new HashMap<>();
        param.put("date", date);
        return slaveSqlSessionTemplate.selectOne("SportsRecommendOrderReport.getSportsRecommendOrderReportByDate",
                param);
    }

    @Override
    public List<SportsRecommendOrderReport> getAllSportsRecommendOrderReportByDate(Integer beginDate, Integer endDate) {
        Map<String, Object> param = new HashMap<>();
        param.put("beginDate", beginDate);
        param.put("endDate", endDate);
        return slaveSqlSessionTemplate.selectList("SportsRecommendOrderReport.getAllSportsRecommendOrderReportByDate",
                param);
    }

    @Override
    public Integer insert(SportsRecommendOrderReport sportsRecommendOrderReport) {
        return sqlSessionTemplate.insert("SportsRecommendOrderReport.insert", sportsRecommendOrderReport);
    }

    @Override
    public Integer update(SportsRecommendOrderReport report) {
        return sqlSessionTemplate.update("SportsRecommendOrderReport.update", report);
    }


}
