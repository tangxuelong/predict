package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PurchaseOrderStatisticDao;
import com.mojieai.predict.entity.po.PurchaseOrderStatistic;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PurchaseOrderStatisticDaoImpl extends BaseDao implements PurchaseOrderStatisticDao {

    @Override
    public List<PurchaseOrderStatistic> getPurchaseOrderStatisticByTime(Integer beginTime, Integer endTime, Integer
            orderClass) {
        Map params = new HashMap<>();
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("orderClass", orderClass);
        return sqlSessionTemplate.selectList("PurchaseOrderStatistic.getPurchaseOrderStatisticByTime", params);
    }

    @Override
    public int insert(PurchaseOrderStatistic purchaseOrderStatistic) {
        return sqlSessionTemplate.insert("PurchaseOrderStatistic.insert", purchaseOrderStatistic);
    }


}
