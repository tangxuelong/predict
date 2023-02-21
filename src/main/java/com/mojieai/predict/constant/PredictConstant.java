package com.mojieai.predict.constant;

public class PredictConstant {
    //预测类型
    public static final Integer PREDICT_RED_BALL_STR_TYPE_TWENTY = 0; //红球预测号码类型  红球20码
    public static final Integer PREDICT_RED_BALL_STR_TYPE_KILL_THREE = 1; //红球预测号码类型  红球杀3码
    public static final Integer KILL_THREE_BLUE = 2;
    public static final Integer KILL_ONE_BLUE = 3;
    public static final Integer LAST_KILL_CODE = 4;
    public static final Integer POSITION_KILL_THREE = 5; //定位杀3码
    public static final Integer THREE_KILL_CODE = 6; //3胆码
    public static final Integer COLD_STATE_KILL_THREE_RED_100 = 7;//红球冷态杀三
    public static final Integer HOT_STATE_KILL_THREE_RED_100 = 8;//红球热态杀三
    public static final Integer CALL_BACK_STATE_KILL_THREE_RED_100 = 9;//红球回补态杀三
    public static final Integer COLD_STATE_KILL_THREE_RED_200 = 10;//红球冷态杀三
    public static final Integer HOT_STATE_KILL_THREE_RED_200 = 11;//红球热态杀三
    public static final Integer CALL_BACK_STATE_KILL_THREE_RED_200 = 12;//红球回补态杀三
    public static final Integer COLD_STATE_KILL_THREE_RED_500 = 13;//红球冷态杀三
    public static final Integer HOT_STATE_KILL_THREE_RED_500 = 14;//红球热态杀三
    public static final Integer CALL_BACK_STATE_KILL_THREE_RED_500 = 15;//红球回补态杀三
    public static final Integer COLD_STATE_KILL_THREE_BLUE_100 = 16;//蓝球冷态杀三
    public static final Integer HOT_STATE_KILL_THREE_BLUE_100 = 17;//蓝球热态杀三
    public static final Integer CALL_BACK_STATE_KILL_THREE_BLUE_100 = 18;//蓝球回补态杀三
    public static final Integer COLD_STATE_KILL_THREE_BLUE_200 = 19;//蓝球冷态杀三
    public static final Integer HOT_STATE_KILL_THREE_BLUE_200 = 20;//蓝球热态杀三
    public static final Integer CALL_BACK_STATE_KILL_THREE_BLUE_200 = 21;//蓝球回补态杀三
    public static final Integer COLD_STATE_KILL_THREE_BLUE_500 = 22;//蓝球冷态杀三
    public static final Integer HOT_STATE_KILL_THREE_BLUE_500 = 23;//蓝球热态杀三
    public static final Integer CALL_BACK_STATE_KILL_THREE_BLUE_500 = 24;//蓝球回补态杀三
    public static final Integer COLD_STATE_KILL_ONE_BLUE_100 = 25;//蓝球冷态杀一
    public static final Integer HOT_STATE_KILL_ONE_BLUE_100 = 26;//蓝球热态杀一
    public static final Integer CALL_BACK_STATE_KILL_ONE_BLUE_100 = 27;//蓝球回补态杀一
    public static final Integer COLD_STATE_KILL_ONE_BLUE_200 = 28;//蓝球冷态杀一
    public static final Integer HOT_STATE_KILL_ONE_BLUE_200 = 29;//蓝球热态杀一
    public static final Integer CALL_BACK_STATE_KILL_ONE_BLUE_200 = 30;//蓝球回补态杀一
    public static final Integer COLD_STATE_KILL_ONE_BLUE_500 = 31;//蓝球冷态杀一
    public static final Integer HOT_STATE_KILL_ONE_BLUE_500 = 32;//蓝球热态杀一
    public static final Integer CALL_BACK_STATE_KILL_ONE_BLUE_500 = 33;//蓝球回补态杀一


    public static final Integer PREDICT_NUMBERS_COUNT = 9500; //产生预测号码个数

    //运营号码状态
    public static final Integer PREDICT_OPERATE_NUMS_STATUS_YES = 1; //可运营
    public static final Integer PREDICT_OPERATE_NUMS_STATUS_NO = 2; //不可运营

    public static final String KILL_THREE_BLUE_EXPIRE_MSG = "本期官方投注已截止，以下杀3码结果仅供参考";
    public static final String KILL_THREE_BLUE_MSG = "蓝球杀3码，即预测3个不会开出的蓝球";
    public static final String KILL_ONE_BLUE_EXPIRE_MSG = "本期官方投注已截止，以下杀1码结果仅供参考";
    public static final String KILL_ONE_BLUE_MSG = "蓝球杀1码，即预测1个不会开出的蓝球";
    public static final String LAST_KILL_CODE_EXPIRE_MSG = "本期官方投注已截止，以下绝杀码结果仅供参考";
    public static final String LAST_KILL_CODE_MSG = "预测一定不会开出的红球和蓝球各一个";
    public static final String POSITION_KILL_CODE_EXPIRE_MSG = "本期官方投注已截止，以下定位杀1码结果仅供参考";
    public static final String POSITION_KILL_CODE_MSG = "定位杀1码，即三个位置各自不会出现的1个号码";
    public static final String THREE_DAN_CODE_EXPIRE_MSG = "本期官方投注已截止，以下3胆码结果仅供参考";
    public static final String THREE_DAN_CODE_MSG = "3胆码，即在组选里至少有1个号会开出";
    public static final String KILL_THREE_RED_MSG = "红球杀3码，即预测3个不会开出的红球";

    public static final Integer SSQ_OPERATE_PREDICT_MAX_COUNT_FOUR_PRIZE = 10;
    public static final Integer SSQ_OPERATE_PREDICT_MAX_COUNT_FIVE_PRIZE = 150;

    public static final String PREDICT_MORE_FIXED_KILL = "fixedKill";

    public static final Integer PREDICT_LEAD_LOGIN_FLAG_NO = 0;//不引导登录
    public static final Integer PREDICT_LEAD_LOGIN_FLAG_YES = 1;//引导登录
    public static final Integer PREDICT_LEAD_LOGIN_FLAG_VIP = 2;//引导购买vip

    public static final String PREDICT_FACTORY_CALCUlATE_2DB = "PredictDb";//保存数据到db的类型
    public static final String PREDICT_FACTORY_GET_INFO = "PredictInfo";//保存数据到db的类型
    public static final String PREDICT_FACTORY_PREDICT_VIEW = "PredictView";//获取数据

    //热态 冷态 回补态
    public static final Integer SSQ_RED_HOT_STATE_OPTION_SIZE = 11;//目前冷热号前12个是热号 后12个是冷态
    public static final Integer SSQ_BLUE_HOT_STATE_OPTION_SIZE = 7;//目前冷热号前7个是热号 后7个是冷态
    public static final Integer DLT_RED_HOT_STATE_OPTION_SIZE = 12;//目前冷热号前13个是热号 后13个是冷态
    public static final Integer DLT_BLUE_HOT_STATE_OPTION_SIZE = 6;//目前冷热号前6个是热号 后6个是冷态
    public static final String COLD_STATE_KILL_THREE_RED = "COLD_STATE_KILL_THREE_RED";
    public static final String HOT_STATE_KILL_THREE_RED = "HOT_STATE_KILL_THREE_RED";
    public static final String CALL_BACK_STATE_KILL_THREE_RED = "CALL_BACK_STATE_KILL_THREE_RED";

    public static final Integer PREDICT_REFUND_STATUS_NO_NEED = 0;//不需要赔付
    public static final Integer PREDICT_REFUND_STATUS_YES = 1;//已赔付

    public static final boolean SUBSCRIBE_STATUS_NO = false;//未订阅
    public static final boolean SUBSCRIBE_STATUS_YES = true;//已订阅

    public static final Integer SUBSCRIBE_PROGRAM_PAY_TYPE_FREE = 0;//免费预测
    public static final Integer SUBSCRIBE_PROGRAM_PAY_TYPE_PAY = 1;//付费预测

    public static final Integer PREDICT_NUM_TYPE_RED_BALL = 0;
    public static final Integer PREDICT_NUM_TYPE_BLUE_BALL = 1;

    public static final Integer PREDICT_STATE_PROGRAM_TYPE_RED = 0;//方案类型 0红球 1蓝球 2首购红球 3首购蓝球
    public static final Integer PREDICT_STATE_PROGRAM_TYPE_BLUE = 1;
    public static final Integer PREDICT_STATE_PROGRAM_TYPE_RED_FIRST = 2;
    public static final Integer PREDICT_STATE_PROGRAM_TYPE_BLUE_FIRST = 3;

    public static Integer USER_FIRST_BUY_COLD_HOT_STATE_PREDICT_NO = 0;//非冷热态预测首购
    public static Integer USER_FIRST_BUY_COLD_HOT_STATE_PREDICT_YES = 1;//冷热态预测首购
}
