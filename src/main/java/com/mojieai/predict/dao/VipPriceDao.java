package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.VipPrice;

import java.util.List;

public interface VipPriceDao {
    List<VipPrice> getAllVipPrice();

    Integer updateVipPriceEnable(Integer vipPriceId, Integer enable);

    Integer insert(VipPrice vipPrice);
}
