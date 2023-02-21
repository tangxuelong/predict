package com.mojieai.predict.constant;

/**
 * Created by Ynght on 2017/1/9.
 */
public class ConfigConstant {
    //分表@TableShard 属性值
    public static final String GAME_PERIOD_SHARD_TYPE = "%10";
    public static final String GAME_PERIOD_SHARD_BY = "gameId";
    public static final String GAME_PERIOD_TABLE_NAME = "tb_game_period";

    //user
    public static final String USER_TABLE_NAME = "tb_user";
    public static final String USER_SHARD_TYPE = "%100";
    public static final String USER_SHARD_BY = "userId";

    //mobile_user
    public static final String MOBILE_USER_TABLE_NAME = "tb_mobile_user";
    public static final String MOBILE_USER_SHARD_TYPE = "%100";
    public static final String MOBILE_USER_SHARD_BY = "mobile";

    //authId
    public static final String THIRD_USER_TABLE_NAME = "tb_third_user";
    public static final String THIRD_USER_SHARD_TYPE = "%100";
    public static final String THIRD_USER_SHARD_BY = "oauthId";

    //user_device
    public static final String USER_DEVICE_INFO_TABLE_NAME = "tb_user_device_info";
    public static final String USER_DEVICE_INFO_SHARD_TYPE = "%100";
    public static final String USER_DEVICE_INFO_SHARD_BY = "deviceId";

    //user_info
    public static final String USER_INFO_TABLE_NAME = "tb_user_info";
    public static final String USER_INFO_SHARD_TYPE = "%100";
    public static final String USER_INFO_SHARD_BY = "userId";

    //user_info
    public static final String USER_FOLLOW_MATCHES_TABLE_NAME = "tb_user_follow_matches";
    public static final String USER_FOLLOW_MATCHES_SHARD_TYPE = "%100";
    public static final String USER_FOLLOW_MATCHES_SHARD_BY = "userId";

    //user_token
    public static final String USER_TOKEN_TABLE_NAME = "tb_user_token";
    public static final String USER_TOKEN_SHARD_TYPE = "%100";
    public static final String USER_TOKEN_SHARD_BY = "token";

    //tb_user_social_encircle_code_00
    public static final String SOCIAL_ENCIRCLE_TABLE_NAME = "tb_user_social_encircle_code";
    public static final String SOCIAL_ENCIRCLE_SHARD_TYPE = "%100";
    public static final String SOCIAL_ENCIRCLE_SHARD_BY = "periodId";

    //tb_user_social_kill_code_00
    public static final String SOCIAL_KILL_CODE_TABLE_NAME = "tb_user_social_kill_code";
    public static final String SOCIAL_KILL_CODE_SHARD_TYPE = "%100";
    public static final String SOCIAL_KILL_CODE_SHARD_BY = "periodId";

    //tb_user_social_task_award_00
    public static final String SOCIAL_USER_SOCIAL_TASK_TABLE_NAME = "tb_user_social_task_award";
    public static final String SOCIAL_USER_SOCIAL_TASK_SHARD_TYPE = "%100";
    public static final String SOCIAL_USER_SOCIAL_TASK_SHARD_BY = "userId";

    //tb_social_user_record_00
    public static final String SOCIAL_STATISTIC_TABLE_NAME = "tb_social_statistic";
    public static final String SOCIAL_STATISTIC_SHARD_TYPE = "%100";
    public static final String SOCIAL_STATISTIC_SHARD_BY = "periodId";

    //tb_user_number_book_02
    public static final String SOCIAL_USER_NUMBER_BOOK_TABLE_NAME = "tb_user_number_book";
    public static final String SOCIAL_USER_NUMBER_BOOK_SHARD_TYPE = "%100";
    public static final String SOCIAL_USER_NUMBER_BOOK_SHARD_BY = "userId";

    //
    public static final String SOCIAL_USER_RECORD_TABLE_NAME = "tb_social_user_record";
    public static final String SOCIAL_USER_RECORD_SHARD_TYPE = "%100";
    public static final String SOCIAL_USER_RECORD_SHARD_BY = "userId";

    public static final String SOCIAL_USER_FOLLOW_TABLE_NAME = "tb_social_user_follow";
    public static final String SOCIAL_USER_FOLLOW_SHARD_TYPE = "%100";
    public static final String SOCIAL_USER_FOLLOW_SHARD_BY = "userId";

    public static final String SOCIAL_USER_FANS_TABLE_NAME = "tb_social_user_fans";
    public static final String SOCIAL_USER_FANS_SHARD_TYPE = "%100";
    public static final String SOCIAL_USER_FANS_SHARD_BY = "userId";

    public static final String SOCIAL_USER_FOLLOW_INFO_TABLE_NAME = "tb_social_user_follow_info";

    //vip_member
    public static final String VIP_MEMBER_TABLE_NAME = "tb_vip_member";
    public static final String VIP_MEMBER_SHARD_TYPE = "%100";
    public static final String VIP_MEMBER_SHARD_BY = "userId";

    //vip_member
    public static final String VIP_OPERATE_FOLLOW_TABLE_NAME = "tb_vip_operate_follow";
    public static final String VIP_OPERATE_FOLLOW_SHARD_TYPE = "%100";
    public static final String VIP_OPERATE_FOLLOW_SHARD_BY = "vipOperateCode";

    //user_sign
    public static final String USER_SIGN_TABLE_NAME = "tb_user_sign";
    public static final String USER_SIGN_SHARD_TYPE = "%100";
    public static final String USER_SIGN_SHARD_BY = "userId";

    //tb_user_title
    public static final String USER_TITLE_TABLE_NAME = "tb_user_title";
    public static final String USER_TITLE_SHARD_TYPE = "%100";
    public static final String USER_TITLE_SHARD_BY = "userId";

    //tb_user_title_log
    public static final String USER_TITLE_LOG_TABLE_NAME = "tb_user_title_log";
    public static final String USER_TITLE_LOG_SHARD_TYPE = "%100";
    public static final String USER_TITLE_LOG_SHARD_BY = "userId";

    //user_sign_statistic
    public static final String USER_SIGN_STATISTIC_TABLE_NAME = "tb_user_sign_statistic";
    public static final String USER_SIGN_STATISTIC_SHARD_TYPE = "%100";
    public static final String USER_SIGN_STATISTIC_SHARD_BY = "userId";

    //tb_user_social_integral_log
    public static final String USER_SOCIAL_INTEGRAL_LOG_TABLE_NAME = "tb_user_social_integral_log";
    public static final String USER_SOCIAL_INTEGRAL_LOG_SHARD_TYPE = "%100";
    public static final String USER_SOCIAL_INTEGRAL_LOG_SHARD_BY = "userId";

    //tb_user_social_integral
    public static final String USER_SOCIAL_INTEGRAL_TABLE_NAME = "tb_user_social_integral";
    public static final String USER_SOCIAL_INTEGRAL_SHARD_TYPE = "%100";
    public static final String USER_SOCIAL_INTEGRAL_SHARD_BY = "userId";

    //tb_user_program
    public static final String USER_PROGRAM_TABLE_NAME = "tb_user_program";
    public static final String USER_PROGRAM_SHARD_TYPE = "%100";
    public static final String USER_PROGRAM_SHARD_BY = "userId";

    //tb_user_wisdom_coin_flow
    public static final String USER_WISDOM_COIN_FLOW_TABLE_NAME = "tb_user_wisdom_coin_flow";
    public static final String USER_WISDOM_COIN_FLOW_SHARD_TYPE = "%100";
    public static final String USER_WISDOM_COIN_FLOW_SHARD_BY = "userId";

    //tb_user_subscribe_info
    public static final String USER_SUBSCRIBE_INFO_TABLE_NAME = "tb_user_subscribe_info";
    public static final String USER_SUBSCRIBE_INFO_SHARD_TYPE = "%100";
    public static final String USER_SUBSCRIBE_INFO_SHARD_BY = "userId";

    //tb_user_subscribe_log
    public static final String USER_SUBSCRIBE_LOG_TABLE_NAME = "tb_user_subscribe_log";
    public static final String USER_SUBSCRIBE_LOG_SHARD_TYPE = "%100";
    public static final String USER_SUBSCRIBE_LOG_SHARD_BY = "userId";

    //tb_user_resonance_info
    public static final String USER_RESONANCE_INFO_TABLE_NAME = "tb_user_resonance_info";
    public static final String USER_RESONANCE_INFO_SHARD_TYPE = "%100";
    public static final String USER_RESONANCE_INFO_SHARD_BY = "userId";

    //tb_user_resonance_log
    public static final String USER_RESONANCE_LOG_TABLE_NAME = "tb_user_resonance_log";
    public static final String USER_RESONANCE_LOG_SHARD_TYPE = "%100";
    public static final String USER_RESONANCE_LOG_SHARD_BY = "userId";

    //tb_user_buy_recommend
    public static final String USER_BUY_RECOMMEND_TABLE_NAME = "tb_user_buy_recommend";
    public static final String USER_BUY_RECOMMEND_SHARD_TYPE = "%100";
    public static final String USER_BUY_RECOMMEND_SHARD_BY = "userId";

    //tb_user_sport_social_recommend
    public static final String USER_SPORT_SOCIAL_RECOMMEND_TABLE_NAME = "tb_user_sport_social_recommend";
    public static final String USER_SPORT_SOCIAL_RECOMMEND_SHARD_TYPE = "%100";
    public static final String USER_SPORT_SOCIAL_RECOMMEND_SHARD_BY = "userId";

    //tb_user_coupon
    public static final String USER_COUPON_TABLE_NAME = "tb_user_coupon";
    public static final String USER_COUPON_SHARD_TYPE = "%100";
    public static final String USER_COUPON_SHARD_BY = "userId";

    //tb_user_coupon_flow
    public static final String USER_COUPON_FLOW_TABLE_NAME = "tb_user_coupon_flow";
    public static final String USER_COUPON_FLOW_SHARD_TYPE = "%100";
    public static final String USER_COUPON_FLOW_SHARD_BY = "userId";

    //tb_user_withdraw_flow
    public static final String USER_WITHDRAW_FLOW_TABLE_NAME = "tb_user_withdraw_flow";
    public static final String USER_WITHDRAW_FLOW_SHARD_TYPE = "%100";
    public static final String USER_WITHDRAW_FLOW_SHARD_BY = "userId";

    public static final String USER_ACCOUNT_TABLE_NAME = "tb_user_account";

    public static final String USER_ACCOUNT_FLOW_TABLE_NAME = "tb_user_account_flow";


    //定义常量, appId、appKey、masterSecret 采用本文档 "第二步 获取访问凭证 "中获得的应用配置
    /*public static final String PUSH_APP_ID = "KwTwpqqtk6AYPokdnrtVJA";
    public static final String PUSH_APP_KEY = "rkVJYhgPQJ7Ix1d3LSPye9";
    public static final String PUSH_MASTER_SECRET = "GlwNUy5EPj7f1dv8QzklT4";
    public static final String PUSH_HOST = "http://sdk.open.api.igexin.com/apiex.htm";*/
}
