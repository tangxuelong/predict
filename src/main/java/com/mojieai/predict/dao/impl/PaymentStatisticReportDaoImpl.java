package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PaymentStatisticReportDao;
import com.mojieai.predict.entity.po.PaymentStatisticReport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PaymentStatisticReportDaoImpl extends BaseDao implements PaymentStatisticReportDao {

    @Override
    public PaymentStatisticReport getPaymentStatisticReportByDate(Integer statisticDate) {
        return slaveSqlSessionTemplate.selectOne("PaymentStatisticReport.getPaymentStatisticReportByDate", statisticDate);
    }

    @Override
    public List<PaymentStatisticReport> getAllPaymentStatisticReport(Integer count) {
        Map<String, Object> param = new HashMap<>();
        param.put("count", count);
        return slaveSqlSessionTemplate.selectList("PaymentStatisticReport.getAllPaymentStatisticReport", param);
    }

    @Override
    public Integer update(PaymentStatisticReport paymentStatisticReport) {
        return sqlSessionTemplate.update("PaymentStatisticReport.update", paymentStatisticReport);
    }

    @Override
    public Integer insert(PaymentStatisticReport paymentStatisticReport) {
        return sqlSessionTemplate.insert("PaymentStatisticReport.insert", paymentStatisticReport);
    }
}
