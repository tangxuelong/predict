package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.ExchangeMallDao;
import com.mojieai.predict.entity.po.ExchangeMall;
import com.mojieai.predict.entity.po.UserAccount;
import com.mojieai.predict.service.ExchangeMallService;
import com.mojieai.predict.service.PayService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeMallServiceImpl implements ExchangeMallService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private ExchangeMallDao exchangeMallDao;
    @Autowired
    private PayService payService;

    @Override
    public Map<String, Object> getGoldCoinGoods(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> groupGoods = new ArrayList<>();

        Map<String, Object> digitMap = new HashMap<>();
        List<Map<String, Object>> digitGoods = new ArrayList<>();
        List<ExchangeMall> digitExchangeMalls = exchangeMallDao.getExchangeMallList(ExchangeMallConstant
                .EXCHANGE_MALL_DIGIT_VIP);
        for (ExchangeMall exchangeMall : digitExchangeMalls) {
            digitGoods.add(convertExchangeMall2Map(exchangeMall, CommonConstant.LOTTERY_TYPE_DIGIT));
        }
        digitMap.put("typeName", "数字彩兑换");
        digitMap.put("itemList", digitGoods);


        Map<String, Object> sportsMap = new HashMap<>();
        List<Map<String, Object>> sportsGoods = new ArrayList<>();
        List<ExchangeMall> sportsFreeTicket = exchangeMallDao.getExchangeMallList(ExchangeMallConstant
                .EXCHANGE_MALL_SPORTS_COUPON);
        List<ExchangeMall> sportsVips = exchangeMallDao.getExchangeMallList(ExchangeMallConstant
                .EXCHANGE_MALL_SPORTS_VIP);
        sportsFreeTicket.addAll(sportsVips);
        for (ExchangeMall exchangeMall : sportsFreeTicket) {
            sportsGoods.add(convertExchangeMall2Map(exchangeMall, CommonConstant.LOTTERY_TYPE_SPORTS));
        }

        sportsMap.put("typeName", "足彩兑换");
        sportsMap.put("itemList", sportsGoods);
        groupGoods.add(sportsMap);
        groupGoods.add(digitMap);

        result.put("groupGoods", groupGoods);
        result.putAll(getUserAccountInfo(userId));
        return result;
    }

    private Map<String, Object> convertExchangeMall2Map(ExchangeMall exchangeMall, Integer lotteryType) {
        Map<String, Object> itemMap = new HashMap<>();
        String itemNameInfo = "";
        if (exchangeMall.getItemName().contains("免单")) {
            itemNameInfo = "(每天限购一次,次日失效)";
        }
        itemMap.put("itemId", exchangeMall.getItemId());
        itemMap.put("itemName", exchangeMall.getItemName());
        itemMap.put("itemNameInfo", itemNameInfo);
        itemMap.put("lotteryType", lotteryType);
        itemMap.put("itemImg", exchangeMall.getItemImg());
        itemMap.put("exchangeCount", exchangeMall.getItemPrice());
        itemMap.put("exchangeName", exchangeMall.getItemPrice() + "金币");
        itemMap.put("originExchangeName", exchangeMall.getItemOriginPrice() + CommonConstant.GOLD_COIN_MONETARY_UNIT);
        return itemMap;
    }

    private Map<String, Object> getUserAccountInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        UserAccount userAccount = payService.getUserAccount(userId, CommonConstant.ACCOUNT_TYPE_GOLD, Boolean.FALSE);
        result.put("userAccountBalance", userAccount == null ? 0 : userAccount.getAccountBalance());
        result.put("userAccountIcon", ActivityIniCache.getActivityIniValue(ActivityIniConstant.ACCOUNT_GOLD_ICON,
                "http://sportsimg.mojieai.com/gold_coin.png"));
        return result;
    }
}
