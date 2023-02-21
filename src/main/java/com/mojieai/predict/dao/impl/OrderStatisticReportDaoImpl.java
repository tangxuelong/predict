package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.OrderStatisticReportDao;
import com.mojieai.predict.entity.po.OrderStatisticReport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderStatisticReportDaoImpl extends BaseDao implements OrderStatisticReportDao {

    @Override
    public OrderStatisticReport getOrderStatisticReportByDate(Integer statisticDate) {
        return slaveSqlSessionTemplate.selectOne("OrderStatisticReport.getOrderStatisticReportByDate", statisticDate);
    }

    @Override
    public List<OrderStatisticReport> getOrderStatisticReport(Integer count) {
        Map<String, Object> param = new HashMap<>();
        param.put("count", count);
        return slaveSqlSessionTemplate.selectList("OrderStatisticReport.getOrderStatisticReport", param);
    }

    @Override
    public Integer updateOrderReport(OrderStatisticReport orderStatisticReport) {
        return sqlSessionTemplate.update("OrderStatisticReport.updateOrderReport", orderStatisticReport);
    }

    @Override
    public Integer insert(OrderStatisticReport orderStatisticReport) {
        return sqlSessionTemplate.insert("OrderStatisticReport.insert", orderStatisticReport);
    }


}
