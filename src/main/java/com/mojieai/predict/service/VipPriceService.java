package com.mojieai.predict.service;

import java.util.Map;

public interface VipPriceService {

    Map<String, Object> getVipSaleList(Long userId, Integer clientType, Integer versionCode, Integer vipType);
}
