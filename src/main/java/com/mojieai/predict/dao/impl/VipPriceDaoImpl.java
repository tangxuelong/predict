package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.VipPriceDao;
import com.mojieai.predict.entity.po.VipPrice;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VipPriceDaoImpl extends BaseDao implements VipPriceDao {
    @Override
    public List<VipPrice> getAllVipPrice() {
        Map param = new HashMap<>();
        return sqlSessionTemplate.selectList("VipPrice.getAllVipPrice", param);
    }

    @Override
    public Integer updateVipPriceEnable(Integer vipPriceId, Integer enable) {
        Map param = new HashMap();
        param.put("vipPriceId", vipPriceId);
        param.put("enable", enable);
        return sqlSessionTemplate.update("VipPrice.updateVipPriceEnable", param);
    }

    @Override
    public Integer insert(VipPrice vipPrice) {
        return sqlSessionTemplate.insert("VipPrice.insert", vipPrice);
    }
}
