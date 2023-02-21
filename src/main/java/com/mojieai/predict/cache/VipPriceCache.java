package com.mojieai.predict.cache;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.VipPriceDao;
import com.mojieai.predict.entity.po.VipPrice;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VipPriceCache {
    private static final Logger log = LogConstant.commonLog;

    private static final Map<Integer, List<VipPrice>> vipPricesRealMoneyMap = new HashMap<>();

    @Autowired
    private VipPriceDao vipPriceDao;

    private VipPriceCache() {
    }

    public void init() {
        log.info("refresh VipPriceCache");
        refresh();
    }

    public void refresh() {
        List<VipPrice> allVipPrice = vipPriceDao.getAllVipPrice();
        if (allVipPrice != null) {
            for (VipPrice vipPrice : allVipPrice) {
                if (vipPrice.getPayType().equals(VipMemberConstant.VIP_PRICE_PAY_TYPE_REAL_MONEY)) {
                    List<VipPrice> temp = null;
                    Integer key = vipPrice.getClientType();
                    if (vipPricesRealMoneyMap.containsKey(key)) {
                        temp = vipPricesRealMoneyMap.get(key);
                    } else {
                        temp = new ArrayList<>();
                    }
                    temp.add(vipPrice);
                    vipPricesRealMoneyMap.put(key, temp);
                }
            }
        }
    }

    public static List<VipPrice> getAllRealMoneyPriceByClientType(Integer clientType, Integer vipType) {
        List<VipPrice> priceList = vipPricesRealMoneyMap.get(clientType);
        if (vipType == null) {
            return priceList;
        }

        List<VipPrice> result = new ArrayList<>();
        for (VipPrice vipPrice : priceList) {
            if (vipPrice.getVipType().equals(vipType)) {
                result.add(vipPrice);
            }
        }
        return result;
    }

    public static boolean verifyDateAndMoney(Integer date, Long price, Integer priceId, Integer clientType) {
        List<VipPrice> vipPricesRealMoney = vipPricesRealMoneyMap.get(clientType);
        for (VipPrice vipPrice : vipPricesRealMoney) {
            if (vipPrice.getPrice().equals(price) && vipPrice.getVipDate().equals(date) && priceId.equals(vipPrice
                    .getVipPriceId())) {
                return true;
            }
        }
        return false;
    }

    public static VipPrice getVipPriceById(Integer priceId, Integer clientType) {
        List<VipPrice> vipPricesRealMoney = vipPricesRealMoneyMap.get(clientType);
        for (VipPrice temp : vipPricesRealMoney) {
            if (temp.getVipPriceId().equals(priceId)) {
                return temp;
            }
        }
        return null;
    }

    public static VipPrice getVipPriceById(Integer priceId) {
        VipPrice vipPrice = null;
        vipPrice = getVipPriceById(priceId, CommonConstant.CLIENT_TYPE_ANDRIOD);
        if (vipPrice == null) {
            vipPrice = getVipPriceById(priceId, CommonConstant.CLIENT_TYPE_IOS);
        }
        if (vipPrice == null) {
            vipPrice = getVipPriceById(priceId, CommonConstant.CLIENT_TYPE_IOS_WISDOM_ENTERPRISE);
        }
        return vipPrice;
    }

}
