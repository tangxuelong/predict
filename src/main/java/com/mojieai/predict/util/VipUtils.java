package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.VipMemberConstant;

import java.util.HashMap;
import java.util.Map;

public class VipUtils {
    public static String getVipOperateFollowDesc(Integer operateType, String money, Integer dateCount, Integer
            sourceType) {
        String result = "";
        String moneyMsg = "";
        String operateMsg = "";
        String dateCountMsg = "";
        Map<String, Object> map = new HashMap<>();
        dateCountMsg = dateCount + "天";
        if (operateType.equals(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_CASH_PURCHASE)) {
            operateMsg = "现金购买VIP";
            moneyMsg = "支付金额:" + money;
        } else if (operateType.equals(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_ACTIVITY_SEND)) {
            operateMsg = "活动赠送VIP";
        } else if (operateType.equals(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_GOLD_PURCHASE)) {
            operateMsg = "金币兑换VIP";
            moneyMsg = "消耗" + money;
        } else if (operateType.equals(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_DISABLE)) {
            operateMsg = "VIP失效";
        } else if (operateType.equals(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_WISDOM_PURCHASE)) {
            operateMsg = "智慧币购买VIP";
            moneyMsg = "支付:" + money;
        }
        map.put("operateMsg", operateMsg);
        map.put("moneyMsg", moneyMsg);
        map.put("dateCountMsg", dateCountMsg);
        map.put("sourceType", sourceType);
        result = JSONObject.toJSONString(map);
        return result;
    }

    public static String convertDateShow(Integer vipDate) {
        return vipDate / 30 + "个月";
    }

    public static String getAmountStrByOperateType(Long exchangeAmount, Integer operateType) {
        String res = "";
        if (operateType.equals(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_CASH_PURCHASE)) {
            res = exchangeAmount + CommonConstant.CASH_MONETARY_UNIT_FEN;
        } else if (operateType.equals(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_GOLD_PURCHASE)) {
            res = exchangeAmount + CommonConstant.GOLD_COIN_MONETARY_UNIT;
        } else if (operateType.equals(VipMemberConstant.VIP_FOLLOW_OPERATE_TYPE_WISDOM_PURCHASE)) {
            res = exchangeAmount + CommonConstant.CASH_MONETARY_UNIT_FEN + CommonConstant.COMMON_COLON_STR +
                    CommonConstant.GOLD_WISDOM_COIN_MONETARY_UNIT;
        }
        return res;
    }

    public static String getVipCnByType(Integer vipType) {
        if (vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
            return "足彩";
        }
        return "数字彩";
    }
}
