package com.mojieai.predict.util;

import com.mojieai.predict.constant.ExchangeMallConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.entity.po.ExchangeMall;

public class ExchangeMallUtil {

    public static Integer getVipTypeByExchangeMall(ExchangeMall exchangeMall) {
        Integer result = VipMemberConstant.VIP_MEMBER_TYPE_DIGIT;
        if (exchangeMall == null) {
            return result;
        }
        if (exchangeMall.getItemType() != null && exchangeMall.getItemType().equals(ExchangeMallConstant
                .EXCHANGE_MALL_SPORTS_VIP)) {
            result = VipMemberConstant.VIP_MEMBER_TYPE_SPORTS;
        }
        return result;
    }
}
