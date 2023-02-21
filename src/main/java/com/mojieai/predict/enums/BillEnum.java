package com.mojieai.predict.enums;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.CouponConstant;
import com.mojieai.predict.constant.UserAccountConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import org.apache.commons.lang3.StringUtils;

/**
 * 对账统计
 */
public enum BillEnum {
    VIP(CommonConstant.BILL_PRODUCT_TYPE_VIP, VipMemberConstant.VIP_PURCHASE_CALL_BACK_METHOD, 1) {
        @Override
        public String getBillShowName() {
            return "VIP收入";
        }
    },
    WISDOM(CommonConstant.BILL_PRODUCT_TYPE_WISDOM, UserAccountConstant.WISDOM_COIN_PURCHASE_CALL_BACK_METHOD, 2) {
        @Override
        public String getBillShowName() {
            return "购买智慧币收入";
        }
    },
    PROGRAM(CommonConstant.BILL_PRODUCT_TYPE_PROGRAM, CommonConstant.PROGRAM_PURCHASE_CALL_BACK_METHOD, 3) {
        @Override
        public String getBillShowName() {
            return "购买方案收入";
        }
    }, SUBSCRIBE_KILL(CommonConstant.BILL_PRODUCT_TYPE_SUBSCRIBE_KILL, CommonConstant
            .SUBSCRIBE_PURCHASE_CALL_BACK_METHOD, 4) {
        @Override
        public String getBillShowName() {
            return "订阅方案收入";
        }
    }, RESONANCE_DATA(CommonConstant.BILL_PRODUCT_TYPE_RESONANCE_DATA, CommonConstant
            .SOCIAL_RESONANCE_DATA_CALL_BACK_METHOD, 5) {
        @Override
        public String getBillShowName() {
            return "共振数据收入";
        }
    }, FOOTBALL_RECOMMEND(CommonConstant.BILL_PRODUCT_TYPE_FOOTBALL_RECOMMEND, CommonConstant
            .FOOTBALL_RECOMMEND_PROGRAM_CALL_BACK_METHOD, 6) {
        @Override
        public String getBillShowName() {
            return "足球推单收入";
        }
    }, COUPON_CARD(CommonConstant.BILL_PRODUCT_TYPE_COUPON_CARD, CouponConstant
            .CELEBRITY_RECOMMEND_PAY_CALL_BACK_METHOD, 7) {
        @Override
        public String getBillShowName() {
            return "大咖组包卡收入";
        }
    }, VIP_PROGRAM(CommonConstant.BILL_PRODUCT_TYPE_VIP_PROGRAM, CommonConstant.SPORT_VIP_PROGRAM_CALL_BACK_METHOD, 8) {
        @Override
        public String getBillShowName() {
            return "会员专区";
        }
    }, ROBOT_FOOTBALL_RECOMMEND(CommonConstant.BILL_PRODUCT_TYPE_ROBOT_FOOTBALL_RECOMMEND, "", 9) {
        @Override
        public String getBillShowName() {
            return "足彩预测收入";
        }
    }, USER_FOOTBALL_RECOMMEND(CommonConstant.BILL_PRODUCT_TYPE_USER_FOOTBALL_RECOMMEND, "", 98) {
        @Override
        public String getBillShowName() {
            return "用户推单收入";
        }
    }, USER_RECOMMEND_INCOME(CommonConstant.BILL_PRODUCT_TYPE_USER_FOOTBALL_RECOMMEND_INCOME, "", 99) {
        @Override
        public String getBillShowName() {
            return "用户推单分成";
        }
    }, DEFAULT_ACTIVITY_INCOME(CommonConstant.BILL_PRODUCT_TYPE_DEFAULT_ACTIVITY_INCOME, "", 10) {
        @Override
        public String getBillShowName() {
            return "活动收入";
        }
    };

    private Integer productType;
    private String callBackMethod;
    private Integer weight;

    BillEnum(Integer productType, String callBackMethod, Integer weight) {
        this.productType = productType;
        this.callBackMethod = callBackMethod;
        this.weight = weight;
    }

    public static BillEnum getBillEnumByProductType(Integer productType) {
        for (BillEnum be : BillEnum.values()) {
            if (be.getProductType().equals(productType)) {
                return be;
            }
        }
        return null;
    }

    public static BillEnum getBillEnumByCallBackMethod(String callBackMethod) {
        if (StringUtils.isBlank(callBackMethod)) {
            return null;
        }
        for (BillEnum be : BillEnum.values()) {
            if (be.getCallBackMethod().equals(callBackMethod)) {
                return be;
            }
        }
        //if not write the callback method will return default activity enum
        return DEFAULT_ACTIVITY_INCOME;
    }

    public Integer getProductType() {
        return productType;
    }

    public String getCallBackMethod() {
        return callBackMethod;
    }

    public Integer getWeight() {
        return weight;
    }

    public abstract String getBillShowName();
}
