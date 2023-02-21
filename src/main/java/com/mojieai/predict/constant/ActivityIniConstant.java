package com.mojieai.predict.constant;

/**
 * Created by tangxuelong on 2017/7/31.
 */
public class ActivityIniConstant {
    public static final String SSQ_AWARD_TITLE = "ssq:awardTitle";
    public static final String DLT_AWARD_TITLE = "dlt:awardTitle";

    /* 开奖信息*/
    public static final String AWARD_TITLE = ":awardTitle";

    public static final String ONLY_APPLE_PAY_FLAG = "onlyApplePayFlayIOS";
    public static final String ONLY_APPLE_PAY_CONTROL_VERSION = "onlyApplePayFlayControlVersion";
    public static final String IOS_REVIEW_FLAG = "iosReviewFlag";
    public static final String SPORTS_ROBOT_MATCH_RATIO = "sportsRobotMatchRatio";
    public static final String SPORTS_ROBOT_SELECT_MATCH_RATIO = "sportsRobotSelectMatchRatio";
    public static final String USER_SIGN_BTN_JUMP_URL = "userSignBtnJumpUrl";
    public static final String INTERNET_CELEBRITIES_RECOMMEND_ACTIVITY_ID = "internetCelebritiesRecommendActivityId";
    public static final String INTERNET_CELEBRITY_USER_BACK_IMG_PREFIX = "internetCelebrityUserBackImgPrefix";
    public static final String WX_PAY_LIMIT_AMOUNT = "wxPayLimitAmount";
    public static final String WX_JSAPI_PAY_JUMP_URL = "wxJsApiPayJumpUrl";
    public static String BEST_KILL_NUM_PRE = "bestKillNum";//最牛杀号前缀
    public static String ROBOT_FOLLOW_RANK_TOP_NUM = "robotFollowRankTopNum";
    public static String ROBOT_RECOMMEND_PRICE_RATIO = "robotRecommendPriceRatio";

    public static String SPORTS_INDEX_TOOL_NAVIGATION_CONF = "sportsIndexToolNavigationConf:";
    public static String SPORTS_INDEX_ACTIVITY_NAVIGATION_CONF = "sportsIndexActivityNavigationConf:";

    public static String getAwardTitle(String gameEn) {
        return gameEn + AWARD_TITLE;
    }

    /* 首页展示浮层*/
    public static final String INDEX_SHOW = "indexShow";

    public static final String COMMUNICATION_INFO = "communicationInfo";

    /* 版本号*/
    public static final String VERSION = "version";
    public static final String NEW_VERSION = "newVersion";

    /* 推送*/
    public static final String PUSH_SWITCH = "pushSwitch";
    public static final String PUSH_EACH_POLL_COUNT = "pushEachPollCount";

    public static final String ACCOUNT_GOLD_ICON = "accountGoldIcon";
    public static final String ACCOUNT_BALANCE_NAME = "accountBalanceName";

    /* 彩种展示排序*/
    public static final String INDEX_GAME_WEIGHT = "indexGameWeight";

    /*预测首页*/
    public static final String PREDICT_INDEX_LEAD_TO_LOGIN = "predictLeadToLogin";
    public static final String PREDICT_INDEX_PREPARING = "predictIndexPreparing";
    public static final String PREDICT_INDEX_PREPARING_ALTER_INFO = "predictIndexPreparingAlterInfo";
    public static final String PREDICT_INDEX_PREPARING_WAITE_OPENAWARD = "predictIndexWaitOpenAward";
    public static final String PREDCIT_INDEX_MORE_PREDICT_OPERATE_AD_SSQ = "morePredictAdMsgSsq";
    public static final String PREDCIT_INDEX_MORE_PREDICT_OPERATE_AD_DLT = "morePredictAdMsgDlt";
    public static final String PREDCIT_INDEX_MORE_PREDICT_OPERATE_SORT_SSQ = "predictIndexSortMorePredictSsq";
    public static final String PREDCIT_INDEX_MORE_PREDICT_OPERATE_SORT_DLT = "predictIndexSortMorePredictDlt";
    public static final String PREDCIT_INDEX_MORE_PREDICT_OPERATE_SORT_IMG_SSQ = "predictIndexSortMorePredictImgSsq";
    public static final String PREDCIT_INDEX_MORE_PREDICT_OPERATE_SORT_IMG_DLT = "predictIndexSortMorePredictImgDlt";

    public static final String PREDCIT_INDEX_MORE_PREDICT_OPERATE_AD_DEFAULT_SSQ = "{\"redTwentyAd\":\"红球围20码\"," +
            "\"redKillThreeAd\":\"红球杀3码\"," + "\"blueKillThreeAd\":\"蓝球杀3码\"," + "\"socialAd\":\"预测社区\"}";

    public static final String PREDCIT_INDEX_MORE_PREDICT_OPERATE_AD_DEFAULT_DLT = "{\"redTwentyAd\":\"红球围20码\"," +
            "\"redKillThreeAd\":\"红球杀3码\"," + "\"blueKillOneAd\":\"蓝球杀3码\"}";

    /*红20码*/
    public static final String RED_TWENTY_PREDICT_PREPARING_MSG = "redTwentyPredictPreparingMsg";
    public static final String RED_TWENTY_PREPARING_WAITE_OPENAWARD = "redTwentyPreparingWaiteOpenAward";
    public static final String PERIOD_RED_TWENTY_NUMS = "predictRedTwentyNums";

    /* 预测首页文案*/
    public static final String PREDICT_INDEX_ADINFO = "predictIndexAdInfo";
    public static final String PREDICT_INDEX_RUN_OUT_TIMES = "predictIndexRunOutTimes";//预测次数用完

    /*qq不抓取奖级*/
    public static final String DOWNLOAD_AWARD_INFO_SWITCH_QQ = "downloadAwardInfoSwitchQQ";

    /*围号规则*/
    public static final String SOCIAL_ENCIRCLE_RULE = "socialEncircleRule";

    /* 每次随机的机器人最大数量*/
    public static final String KILL_NUM_ROBOT_RANDOM_MAX_COUNT = "killNumsRobotRandomMaxCount";
    public static final Integer KILL_NUM_ROBOT_MAX_COUNT = 5;

    public static final String ROBOT_DELAY_KILL_NUM_MAX_SECOND = "robotDelayKillNumMaxSecond";
    public static final Integer ROBOT_DELAY_KILL_NUM_DEFAULT_SECOND = 300;

    public static final String ROBOT_ENCIRCLE_PERCENT_AFTER_KILL_NUM = "robotEncirclePercentAfterKillNum";
    public static final Integer ROBOT_ENCIRCLE_PERCENT_AFTER_KILL_NUM_DEFAULT = 10;

    public static final String USER_ENCIRCLE_SEND_TO_ROBOT_PERCENT = "userEncircleSend2RobotPercent";

    /* android是否更新*/
    public static final String VERSION_UPDATE_URL = "versionUpdateUrl";
    public static final String VERSION_UPDATE_TEXT = "versionUpdateText";
    public static final String IS_FORCE_UPDATE = "isForceUpdate";

    public static final String APP_SHOW_GAME_FLAG = "appShowGameFlag";//ios发版时显示游戏
    public static final Integer APP_SHOW_GAME = 1;//ios发版时显示游戏
    public static final Integer APP_HIDDEN_GAME = 0;//ios发版时不显示游戏

    public static final String IS_SHOW_FILTER = "isShowFilter";
    public static final String IS_SHOW_ANALYZE = "isShowAnalyze";

    public static final String INDEX_H_TOOLS = "indexHTools";
    public static final String INDEX_V_TOOLS = "indexVTools";

    public static final String SOCIAL_PERIOD_RANK_PREDICT_NUMS = "socialPeriodRankPredictNums";
    public static final String SOCIAL_WEEK_RANK_PREDICT_NUMS = "socialWeekRankPredictNums";
    public static final String SOCIAL_MONTH_RANK_PREDICT_NUMS = "socialMonthRankPredictNums";
    public static final String SOCIAL_RANK_PREDICT_NUMS_DEFAULT = "default";

    public static final String TOURIST_MODLE_SWITCH = "touristModelSwitch";
    public static final String TOURIST_MODLE_SWITCH_ON = "on";

    public static final String SPLASH_SCREEN_CONFIG = "splashScreenConfig";
    public static final String SPLASH_SCREEN_CONFIG_DEFAULT = "{\"img\": \"http://ovqsyejql.bkt.clouddn" +
            ".com/8b8b64064d3420f2a79f0594ca43b31f.png\",\"jumpUrl\": " +
            "\"mjLottery://mjNative?page=kjlb&gameName=大乐透&gameEn=dlt\",\"showSecond\": \"2\"}";

    public static final String INNER_SITE_POP_CONFIG = "innerSitePopConfig";
    public static final String USER_INFO_POP_CONFIG = "userInfoPopConfig";
    public static final String INDEX_FOOTER_POP_CONFIG = "indexFooterPopConfig";
    public static final String INNER_SITE_POP_CONFIG_DEFAULT = "";

    public static final String USER_UPDATE_SOCIAL_LEVEl_POP_AD = "userUpdateSocialLevelPopAd";

    /* VIP*/
    public static final String VIP_PRIVILEGE_KEYS = "vipPrivilegeKeys";
    public static final String VIP_PRIVILEGE_IMG_AFTER = "VipPrivilegeImgAfter";
    public static final String VIP_SALE_DESC = "vipSaleDesc";

    public static final String VIP_SPORTS_PRIVILEGE_KEYS = "vipSportsPrivilegeKeys";
    public static final String VIP_SPORTS_PRIVILEGE_IMG_AFTER = "VipSportsPrivilegeImgAfter";

    public static final String TASK_LIST_MAP = "taskListMap";

    public static final String PREDICT_MORE = "predictSorts";

    public static final String OUR_PEOPLE_PHONE_NUM = "ourPeoplePhone";


    public static final String KILL_NUM_LIST_FILTER_LIST = "filterList";

    public static final String FOOTBALL_WITHDRAW_OCCUPY_RATIO = "footballWithdrawOccupyRatio";

    public static final String MIN_WITHDRAW_MONEY = "minWithdrawMoney";
    public static final String AUTO_WITHDRAW_SWITCH = "autoWithdrawSwitch";
    public static final String THIRD_PARTY_WITHDRAW_SWITCH = "thirdPartyWithdrawSwitch";

    public static final String FOOTBALL_PLAY_TYPE_ACCORDING = "FOOTBALL_PLAY_TYPE_ACCORDING:";
    public static final String FOOTBALL_IS_SHOW_RECOMMEND_TITLE = "FOOTBALL_IS_SHOW_RECOMMEND_TITLE";
    public static final String FOOTBALL_RECOMMEND_TITLE_NUMBERS = "FOOTBALL_RECOMMEND_TITLE_NUMBERS";

    // 智慧方案
    public static final String PROGRAM_LIST = "programList";
    public static final String PROGRAM_PRICE_AFTER = "ProgramPrice";

    // h5协议
    public static final String PLATE_AGREEMENT_URL = "plateAgreementUrl";
    public static final String PLATE_WITHDRAW_AGREEMENT_URL = "plateWithDrawAgreementUrl";

    public static final String WORLD_CUP_GROUP_NAME = "worldCupGroupName";

    public static final String INTERNET_CELEBRITY_MASTER = "internetCelebrityMaster";
    public static final String SMS_USER = "zhycyzm";
    public static final String SMS_PWD = "zhycyzm_8888";

    public static final String DIGIT_INDEX_DEFAULT_NAV = "digitIndexDefaultNav:";

    public static final String JING_DONG_BANK_LIST = "jdBankList";

    public static final String MANUAL_CONFIRM_WITHDRAW_AMOUNT_UPPER_LIMIT = "manualConfirmWithdrawAmountUpperLimit";
    public static final String MANUAL_MAX_WITHDRAW_AMOUNT = "manualMaxWithdrawAmount";

    public static String getIndexHToolKey(String gameEn) {
        return new StringBuffer(gameEn).append(CommonConstant.COMMON_COLON_STR).append(INDEX_H_TOOLS).toString();
    }

    public static String getIndexVToolKey(String gameEn) {
        return new StringBuffer(gameEn).append(CommonConstant.COMMON_COLON_STR).append(INDEX_V_TOOLS).toString();
    }

    public static String getIndexPredictMoreKey(String gameEn) {
        return new StringBuffer().append(PREDICT_MORE).append(CommonConstant.COMMON_COLON_STR).append(gameEn)
                .toString();
    }

    public static String getDigitIndexDefaultNav(long gameId) {
        return new StringBuffer().append(DIGIT_INDEX_DEFAULT_NAV).append(gameId).toString();
    }
}
