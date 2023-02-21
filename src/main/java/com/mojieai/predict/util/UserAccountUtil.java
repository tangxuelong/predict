package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.UserAccountConstant;
import com.mojieai.predict.entity.po.ExchangeMall;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class UserAccountUtil {
    public static Map<String, Object> convertExchangeMall2WisdomMap(ExchangeMall exchangeMall, Integer clientId,
                                                                    Integer versionCode) {
        Map<String, Object> res = new HashMap<>();

        Integer fillAmountType = 1;
        String originCashText = "";
        String fillAmountIcon = exchangeMall.getItemImg();
        if (!exchangeMall.getItemName().equals("其它金额")) {
            fillAmountType = 0;
            res.put("fillAmount", CommonUtil.divide(exchangeMall.getItemPrice() + "", "100", 0));
            res.put("fillWisdomAmount", CommonUtil.divide(exchangeMall.getItemCount() + "", "100", 0));
            res.put("cashText", CommonConstant.COMMON_YUAN_STR + CommonUtil.removeZeroAfterPoint(CommonUtil
                    .convertFen2Yuan(exchangeMall.getItemPrice()).toString()));
            if (!exchangeMall.getItemOriginPrice().equals(exchangeMall.getItemPrice())) {
                originCashText = CommonConstant.COMMON_YUAN_STR + CommonUtil.removeZeroAfterPoint(CommonUtil
                        .convertFen2Yuan(exchangeMall.getItemOriginPrice()).toString());
            }
            res.put("originCashText", originCashText);
        }
        if (clientId.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) && StringUtils.isNotBlank(originCashText) && versionCode
                < CommonConstant.VERSION_CODE_4_2) {
            fillAmountIcon = "http://sportsimg.mojieai.com/wisdom_discount.png";
        } else if (clientId.equals(CommonConstant.CLIENT_TYPE_IOS) && StringUtils.isNotBlank(originCashText) && versionCode
                < CommonConstant.VERSION_CODE_4_0) {
            fillAmountIcon = "http://sportsimg.mojieai.com/wisdom_discount.png";
        }
        Integer weight = 0;
        String remark = exchangeMall.getRemark();
        if (StringUtils.isNotBlank(remark)) {
            Map<String, Object> remarkMap = JSONObject.parseObject(remark, HashMap.class);
            if (remarkMap != null && remarkMap.containsKey("weight")) {
                weight = Integer.valueOf(remarkMap.get("weight").toString());
            }
        }

        res.put("balanceText", exchangeMall.getItemName());
        res.put("fillAmountId", exchangeMall.getItemId());
        res.put("fillAmountType", fillAmountType);
        res.put("fillAmountIcon", fillAmountIcon);
        res.put("isDefault", exchangeMall.getIsDefault());
        res.put("weight", weight);
        return res;
    }

    public static String getInitUserWisdomCoinMemo(Integer exchangeType, Long amount, Long exchangeWisdomAmount) {
        Map<String, Object> res = new HashMap<>();
        String amountStr = "";
        if (amount != null) {
            amountStr = CommonUtil.convertFen2Yuan(amount) + CommonConstant.CASH_MONETARY_UNIT_YUAN;
        }
        res.put("amount", amountStr);
        res.put("operateType", getWisdomExchangeTypeCn(exchangeType));
        res.put("wisdomAmount", CommonUtil.convertFen2Yuan(exchangeWisdomAmount) + "智慧币");
        return JSONObject.toJSONString(res);
    }

    public static String getWisdomExchangeTypeCn(Integer exchangeType) {
        String operateType = "";
        if (exchangeType.equals(UserAccountConstant.WISDOM_COIN_EXCHANGE_TYPE_CASH_PURCHASE)) {
            operateType = "现金购买";
        } else if (exchangeType.equals(UserAccountConstant.WISDOM_COIN_EXCHANGE_TYPE_CONSUME)) {
            operateType = "消费智慧币";
        } else if (exchangeType.equals(UserAccountConstant.WISDOM_COIN_EXCHANGE_TYPE_PROGRAM_COMPENSATE)) {
            operateType = "方案赔付";
        } else if (exchangeType.equals(UserAccountConstant.WISDOM_COIN_EXCHANGE_TYPE_PROGRAM_OUT_LINE_TRANSFER)) {
            operateType = "现金转账人工充值";
        }
        return operateType;
    }
}
