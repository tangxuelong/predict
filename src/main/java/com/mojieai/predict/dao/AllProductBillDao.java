package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.AllProductBill;

import java.util.List;

public interface AllProductBillDao {

    AllProductBill getProductBillByPk(Integer dateNum, Integer orderType, boolean isLock);

    List<AllProductBill> getAllProductBills(Integer beginDate, Integer endDate);

    Integer update(AllProductBill allProductBill);

    Integer insert(AllProductBill allProductBill);
}

