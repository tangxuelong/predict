package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.AllProductBillDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.AllProductBill;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AllProductBillDaoImpl extends BaseDao implements AllProductBillDao {

    @Override
    public AllProductBill getProductBillByPk(Integer dateNum, Integer orderType, boolean isLock) {
        Map params = new HashMap<>();
        params.put("dateNum", dateNum);
        params.put("orderType", orderType);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("AllProductBill.getProductBillByPk", params);
    }

    @Override
    public List<AllProductBill> getAllProductBills(Integer beginDate, Integer endDate) {
        return sqlSessionTemplate.selectList("AllProductBill.getAllProductBills");
    }

    @Override
    public Integer update(AllProductBill allProductBill) {
        return sqlSessionTemplate.update("AllProductBill.update", allProductBill);
    }

    @Override
    public Integer insert(AllProductBill allProductBill) {
        return sqlSessionTemplate.insert("AllProductBill.insert", allProductBill);
    }
}
