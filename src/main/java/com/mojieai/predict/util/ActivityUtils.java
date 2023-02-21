package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.entity.po.ActivityInfo;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class ActivityUtils {

    public static Long getActivityWisdomCount(ActivityInfo activityInfo, Long itemCount) {
        if (activityInfo == null || StringUtils.isBlank(activityInfo.getRemark())) {
            return itemCount;
        }

        Map<String, Object> remarkMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);
        String discount = remarkMap.get("discount").toString();

        return CommonUtil.multiply(itemCount + "", "100").divide(new BigDecimal(discount), 0, RoundingMode.HALF_UP).longValue();
    }

    public static Long getActivityWisdomPrice(ActivityInfo activityInfo, Long itemPrice) {
        if (activityInfo == null || StringUtils.isBlank(activityInfo.getRemark())) {
            return itemPrice;
        }

        Map<String, Object> remarkMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);
        String discount = remarkMap.get("discount").toString();

        return CommonUtil.multiply(CommonUtil.divide(discount, "100", 2), itemPrice + "").longValue();
    }

    public static Boolean wisdomActivityVersionControl(Integer clientType, Integer versionCode) {
        if (clientType != null && clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) && versionCode >= CommonConstant
                .VERSION_CODE_4_3) {
            return Boolean.TRUE;
        }

        if (clientType != null && clientType.equals(CommonConstant.CLIENT_TYPE_IOS)) {
            if (CommonUtil.getIosReview(versionCode) == 0) {
                return Boolean.FALSE;
            }
            if (versionCode >= CommonConstant.VERSION_CODE_4_1) {
                return Boolean.TRUE;
            }
        }

        if (clientType != null && clientType.equals(CommonConstant.CLIENT_TYPE_IOS_WISDOM_ENTERPRISE)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}