package com.mojieai.predict.constant;

public class UserAccountConstant {

    public static String WISDOM_COIN_PURCHASE_CALL_BACK_METHOD = "userWisdomCoinFlowServiceImpl.callBackAddAccount";

    /* 是否支付*/
    public static Integer IS_PAIED_NO = 0;
    public static Integer IS_PAIED_YES = 1;

    public static Integer WISDOM_COIN_EXCHANGE_TYPE_CASH_PURCHASE = 0;//现金购买智慧币
    public static Integer WISDOM_COIN_EXCHANGE_TYPE_CONSUME = 1;//消费智慧币
    public static Integer WISDOM_COIN_EXCHANGE_TYPE_PROGRAM_COMPENSATE = 2;//方案赔付
    public static Integer WISDOM_COIN_EXCHANGE_TYPE_PROGRAM_OUT_LINE_TRANSFER = 3;//人工转账充值
}
