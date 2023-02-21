package com.mojieai.predict.constant;

public class VipMemberConstant {

    /* 会员类型*/
    public static Integer VIP_MEMBER_TYPE_DIGIT = 0;
    public static Integer VIP_MEMBER_TYPE_SPORTS = 1;

    /* 会员状态*/
    public static Integer VIP_MEMBER_STATUS_ENABLE = 1;
    public static Integer VIP_MEMBER_STATUS_DISENABLE = 0;

    /* 会员价格 类型*/
    public static Integer VIP_PRICE_PAY_TYPE_REAL_MONEY = 1;

    /* 会员操作类型 操作类型  0:vip到期失效 1:购买  2:活动赠送vip  3:金豆购买会员*/
    public static Integer VIP_FOLLOW_OPERATE_TYPE_DISABLE = 0;
    public static Integer VIP_FOLLOW_OPERATE_TYPE_CASH_PURCHASE = 1;
    public static Integer VIP_FOLLOW_OPERATE_TYPE_ACTIVITY_SEND = 2;
    public static Integer VIP_FOLLOW_OPERATE_TYPE_GOLD_PURCHASE = 3;
    public static Integer VIP_FOLLOW_OPERATE_TYPE_WISDOM_PURCHASE = 4;

    /* 会员等级*/
    public static Integer VIP_LEVEL_ONE = 1;

    /* 是否支付*/
    public static Integer VIP_IS_PAIED_NO = 0;
    public static Integer VIP_IS_PAIED_YES = 1;

    public static Integer PURCHASE_VIP_LAZY_SECOUND = 5;

    public static String VIP_PURCHASE_CALL_BACK_METHOD = "vipMemberServiceImpl.callBackMakeVipEffective";

    public static Integer VIP_MAX_CLOUD_NUMBER_BOOK = 10000;
    public static Integer NOT_VIP_MAX_CLOUD_NUMBER_BOOK = 200;

    public static Integer ACTIVITY_SEND_VIP_WAWA = 0;//抓娃娃song

    public static Integer ACTIVITY_SEND_VIP_UNIQUE = 0;//唯一派发
    public static Integer ACTIVITY_SEND_VIP_UN_UNIQUE = 1;//不唯一派发

    public static Integer VIP_SOURCE_TYPE_ADMIN = 12;//管理员赠送
    public static Integer VIP_SOURCE_TYPE_WAWA_ACTIVIYT = 13;//管理员赠送
    public static Integer VIP_SOURCE_TYPE_SIGN_TASK = 21;//签到赠送
    public static Integer VIP_SOURCE_TYPE_WX_JSAPI = 24;//微信公众号

    public static Integer VIP_PREDICT_NUM_MORE_TIMES = 5;

}
