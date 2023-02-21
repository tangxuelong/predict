package com.mojieai.predict.constant;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.util.DateUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Singal
 */
public class RedisConstant {
    public final static String REDIS_DEFAULT_OK = "ok";
    public final static Integer REDIS_DEFAULT_RESULT = 1;
    public final static Long REDIS_DEFAULT_RESULT_LONG = 1L;
    public final static Long REDIS_PREDICT_NUM_RESULT = 10000L;
    public final static Long REDIS_LAST_INDEX = -1L;
    public final static Integer GET_1_PERIOD = 1;
    public final static Integer GET_3_PERIOD = 3;
    public final static Integer GET_1_YEAR_PERIOD = 365;
    public final static Integer GET_10_PERIOD = 10;
    public final static Integer GET_100_PERIOD = 100;
    public final static Integer GET_50_PERIOD = 50;
    public final static Integer GET_30_PERIOD = 30;
    // periodExpireTime
    public final static Integer PERIOD_EXPIRE = 600;
    public final static Integer PERIOD_LARGE_EXPIRE = 3600;
    // periodTimeline
    public final static String TODAY_PERIODS = ":todayPeriods";
    public final static String CURRENT_PERIOD = ":currentPeriod";
    public final static String CURRENT_PERIODS = ":currentPeriods";
    public final static String LAST_OPEN_PERIOD = ":lastOpenedPeriod";
    public final static String LAST_ALL_OPEN_PERIOD = ":lastAllOpenPeriod";
    public final static String RECENT_SALE_PERIOD = ":recentSalePeriod";
    public final static String HISTORY_100_AWARD_PERIOD = ":historyAwardPeriod";
    public final static String HISTORY_50_AWARD_PERIOD = ":historyFiftyAwardPeriod";
    public final static String HISTORY_30_AWARD_PERIOD = ":historyThirtyAwardPeriod";
    public final static String LAST_AWARD_PERIOD = ":lastAwardPeriod";
    public final static String CURRENT_AWARD_PERIOD = ":currentAwardPeriod";
    public final static String LAST_100_PREDICT_HISTORY = ":lastHundaryPredictHistory";
    //预测
    public final static String HISTORY_100_PREDICT_WIN = ":historyHundaryPredictWin";//历史预测100期数据
    public final static String PREDICT_NUMS_TEN_THOUSAND = ":tenThousandPredictNums";//预测1万注号码
    public final static String PREDICT_NUMS_HISTORY_AWARD_SUM = ":predictNumsHistroyAwardSum";//历史累计
    public final static String PREDICT_NUMS_HISTORY_AWARD_LEVEL_SUM = ":predictNumsHistroyAwardLevelSum";//历史中奖累计
    public final static String PREDICT_USER_GOT_NUM_SET = ":predictUserGotNumSet";//用户获取过的预测号码
    public final static String PREDICT_NUMS_INDEX_INFO = ":predictIndexInfo";//预测首页展示数据
    public final static String PREDICT_NUMS_HISTROY_PAGE = ":predictHistoryPage";
    public final static String PREDICT_RED_TWENTY_NUMS = ":predictRedTwentyNums";
    public final static String PREDICT_RED_KILL_THREE_NUMS = ":predictRedKillThreeNums";//杀三
    public final static String PREDICT_RED_KILL_THREE_NUMS_LIST = ":predictRedKillThreeNumsList";//杀三列表

    public final static String PREDICT_RED_BALL_FLAG = "predictRedBallFlag:";
    public final static String PREDICT_RED_BALL_SUBSCRIBE_INDEX_DATA = "subscribeIndexData:";
    public final static String USER_SUBSCRIBE_PREDICT_LAST_PERIOD_ID = "userSubscribePredictLastPeriodId:";
    public final static String USER_FIRST_BUY_COLD_HOT_STATE_PREDICT = "userFisrtBuyColdHotStatePredict:";

    //社交杀号
    public final static String SOCIAL_KILL_NUM_LIST_UPDATE_FLAG = ":socialKillNumListUpdateFlag";
    public final static String SOCIAL_PERIOD_KILL_NUM_TOTAL_TIMES = ":socialPeriodKillNumTotalTimes";
    public final static String SOCIAL_ENCIRCLE_NUM_TEMP_LIST = ":socialEncircleNumTempList";
    public final static String SOCIAL_KILL_NUM_TEMP_LIST = ":socialKillNumTempList";
    public final static String SOCIAL_PERIOD_ENCIRCLE_SORTED_SET = ":socialPeriodEncircleSortedSet";
    public final static String SOCIAL_PERIOD_ENCIRCLE_SORTED_SET_TEMP = ":socialPeriodEncircleSortedSetTemp";


    //前缀
    public final static String PREFIX_PERIOD = "predictPeriodRedis:";
    public final static String AWARD_INFO_PERIOD = "awardInfo:";
    public final static String PERIOD_CHANGE_LIST = PREFIX_PERIOD + "changeList";
    public final static String PREFIX_TREND = "trendRedis:";
    public final static String REDIS_AWARD_DOWNLOAD_PRE_KEY = "awardDownload:";//开奖公告
    public final static String REDIS_TEST_NUM_DOWNLOAD_PRE_KEY = "testNumDownload:";//试机号
    public final static String PREFIX_HISTORY_AWARD = "historyAward:";
    public final static String PREFIX_PREDICT_NUM = "predictNum:";//预测号码
    public final static String PREFIX_PREDICT_MAX_NUM = "predictMaxNum:";//用户预测号码最大次数
    public final static String PREFIX_PREDICT_NUM_DEVICE = "devicePredictNum:";//预测号码
    public final static String PREFIX_SOCIAL_ENCIRCLE_TIMES = "socialEncircleTimes:";
    public final static String PREFIX_SOCIAL_ENCIRCLE_USER_RANK = "socialEncircleUserRank:";
    public final static String PREFIX_SOCIAL_USER_KILL_NUM_LOCK = "socialUserKillNumLock:";
    public final static String PREFIX_SOCIAL_KILL_NUM_LIST = "socialKillNumList:";
    public final static String PREFIX_SOCIAL_KILL_NUM = "socialKillNumRedis:";
    public final static String PREFIX_SOCIAL_ENCIRCLE_NUM = "socialEncircleNumRedis:";
    public final static String PREFIX_SOCIAL_CURRENT_ENCIRCLEVO = "socialCurrentEncircleVo:";
    public final static String SOCIAL_CLASSIC_ENCIRCLE_LIST = "socialClassicEncircleList:";
    public final static String SOCIAL_HOT_ENCIRCLE_LIST = "socialHotEncircleList:";
    public final static String SOCIAL_BIG_DATA_LIST = "socialBigDataList:";
    public final static String USER_DATE_SIGN = "userDateSign:";
    public final static String SOCIAL_ENCIRCLE_LIST_ROBOT_KILL = "socialEncircleListRobotKill:";
    public final static String SOCIAL_LAST_STATISTIC_TIME = "socialLastStatisticTime:";
    public final static String PREFIX_SAME_PERIOD = "samePeriod:";

    /* 登录*/
    public final static String PREFIX_USER_LOGIN_VO = "userLoginVo:";
    public final static String PREFIX_FOLLOW_INFO_VO = "followInfoVoNew:";
    public final static String PREFIX_USER_TOKEN = "userToken:";
    public final static String PREFIX_SEND_VERIFY_CODE = "sendVerifyCode:";
    public final static String PREFIX_SEND_BIND_BANK_VERIFY_CODE = "sendBindBankVerifyCode:";
    public final static String PREFIX_VERIFY_CODE_TIMES = "verifyCodeTimes:";
    public final static String PREFIX_PASSWORD_TIMES = "passwordTimes:";
    public final static String USER_DEVICE = "USER_DEVICE";


    /* 推送*/
    public final static String REDIS_CLUSTER_PUSH = "{redisClusterPush}:";
    public final static String ALI_REDIS_CLUSTER_PUSH = "{aliRedisClusterPush}:";
    public final static String PUSH_CLIENT_LIST = "pushList";
    public final static String ALI_PUSH_CLIENT_LIST = "pushList";
    public final static String DEFAULT_CLIENT_SET = "pushDefault";
    public final static String PUSH_FLAG = "pushFlag:";
    public final static String ALI_PUSH_FLAG = "aliPushFlag:";
    public final static String PUSH_SNAP = "pushSnap";
    public final static String PUSH_KEY_LIST = "pushKeyList:";
    public final static String ALI_PUSH_KEY_LIST = "aliPushKeyList:";
    public final static String PUSHING_KEY = "pushingList:";
    public final static String Ali_PUSHING_KEY = "aliPushingList:";
    public final static String PUSHED_COUNT = "pushedCount:";
    public final static String ALI_PUSHED_COUNT = "aliPushedCount:";

    /* 号码分析*/
    public final static String BLUE_COLD_HOT = "BLUE_COLD_HOT";
    public final static String RED_COLD_HOT = "RED_COLD_HOT";
    public final static String FRONT_COLD_HOT = "FRONT_COLD_HOT";
    public final static String BACK_COLD_HOT = "BACK_COLD_HOT";
    public final static String COLD_HOT = "COLD_HOT";
    public final static String HUNDRED_COLD_HOT = "HUNDRED_COLD_HOT";
    public final static String TEN_COLD_HOT = "TEN_COLD_HOT";
    public final static String ONE_COLD_HOT = "ONE_COLD_HOT";

    public final static String BLUE_OMIT = "BLUE";
    public final static String RED_OMIT = "RED";
    public final static String FRONT_OMIT = "FRONT";
    public final static String BACK_OMIT = "BACK";
    public final static String HUNDRED_OMIT = "HUNDRED";
    public final static String TEN_OMIT = "TEN";
    public final static String ONE_OMIT = "ONE";

    public final static String LAST_BEHAVE = "LAST_BEHAVE";
    public final static String SHOW_TITLE = "SHOW_TITLE:";
    public final static String RED_PRE = "red:";
    public final static String RED_AFTER = ":red";
    public final static String BLUE_PRE = "blue:";
    public final static String BLUE_AFTER = ":blue";
    public final static String HUNDRED_AFTER = ":hundred";
    public final static String HUNDRED_PRE = "hundred:";
    public final static String TEN_AFTER = ":ten";
    public final static String TEN_PRE = "ten:";
    public final static String ONE_AFTER = ":one";
    public final static String ONE_PRE = "one:";
    public final static String HISTORY_OMIT = "HISTORY_OMIT:";


    /* 过滤*/
    public final static String MATRIX_LIST = "MATRIX_LIST:";
    public final static String MATRIX_ACTION_RESULT = "MATRIX_ACTION_RESULT:";
    public final static String MATRIX_ACTION_RESULT_TEXT = "MATRIX_ACTION_RESULT_TEXT:";
    public final static String MATRIX_ACTION_RESULT_BLUE = "BLUE:";

    /* 管理后台*/
    public final static String ADMIN_KEY = "ADMIN_KEY:";

    public final static String PREDICT_TYPE = "PREDICT_TYPE:";

    /* 算奖*/
    public final static String DISTRIBUTE_FLAG = "DISTRIBUTE_FLAG:";
    public final static String ENCIRCLE_PERIOD_RANK = "ENCIRCLE_PERIOD_RANK:";
    public final static String ENCIRCLE_WEEK_RANK = "ENCIRCLE_WEEK_RANK:";
    public final static String ENCIRCLE_MONTH_RANK = "ENCIRCLE_MONTH_RANK:";
    public final static String KILL_PERIOD_RANK = "KILL_PERIOD_RANK:";
    public final static String KILL_WEEK_RANK = "KILL_WEEK_RANK:";
    public final static String KILL_MONTH_RANK = "KILL_MONTH_RANK:";

    /* 成就*/
    public final static String ACHIEVEMENT_FLAG = "achievement_flag:";
    public final static String PERSIONAL_ACHIEVEMENT = "persionalAchievement:";

    /* VIP*/
    public final static String USER_VIP_MEMBER_DETAIL = "userVipMemberDetail:";
    public final static String USER_VIP_PURCHASE_LAZY_SECOND = "purchaseVipLazySecond:";

    public final static String ENCIRCLE_RANK_FLAG = "ENCIRCLE_RANK_FLAG:";
    public final static String KILL_RANK_FLAG = "KILL_RANK_FLAG:";
    public final static String USER_DISTRIBUTE_FLAG = "USER_DISTRIBUTE_FLAG:";
    public final static String USER_KILL_NUM_DISTRIBUTE_FLAG = "USER_KILL_NUM_DISTRIBUTE_FLAG:";
    public final static String USER_RANK_UPDATE_FLAG = "USER_RANK_UPDATE_FLAG:";

    public final static String USER_SHARE_WX = "USER_SHARE_WX";

    public final static String AWARD_POPUP_FLAG = "AWARD_POPUP_FLAG:";
    public final static String USER_TREND_SAVE_NUM_POP_FLAG = "userTrendSaveNumPopFlag:";

    public final static String SOCIAL_POPUP_FLAG = "socialPopupFlag:";

    public final static String SOCIAL_POP_USER_LEVEL_UPGRADE_LIST = "socialPopupUserLevelUpgradeList:";
    public final static String SOCIAL_POP_USER_ACHIEVE_LIST = "socialPopupUserAchieveList:";
    public final static String SPORTS_SOCIAL_POP_BECOME_MASTER_LIST = "sportsSocialPopBecomeMasterList:";

    public final static String FOOTBALL_INDEX_MARQUEE_KEY = "footballIndexMarqueeKey";

    public final static String USER_PURCHASE_FOOTBALL_PROGRAM = "userPurchaseFootballProgram:";

    public final static String USER_ONE_DAY_SAVE_NUM_BOOK_TIMES = "userOneDaySaveNumBookTimes:";

    // 预测号码大集合
    public final static String PREDICT_NUMBERS_SET = "PREDICT_NUMBERS_SET:";
    public final static String PREDICT_NUMBERS_SET_COUNT_FLAG = "PREDICT_NUMBERS_SET_COUNT_FLAG:";
    public final static String USER_USE_NUMS = "USER_USE_NUMS:";
    public final static String MARQUEE_INDEX = "MARQUEE_INDEX:";

    public final static String POSITION_KILL_THREE = "POSITION_KILL_THREE:";
    public final static String THREE_DAN_CODE = "THREE_DAN_CODE:";
    public final static String FC_PREDICT = "FC_PREDICT:";

    public final static String USER_TITLE = "userRedisTitle:";
    public final static String USER_TITLE_DISTRIBUTE_FLAG = "userTitleDistributeFlag:";

    public final static String TEST_DB_USER_MONEY = "testDbUserMoney";

    public final static String STATISTIC_PURCHASE_ORDER = "statisticPurchaeOrderKey";

    public final static String OTHRE_PLATEFORM_ACTIVITY_MOBILE = "otherPlateFormActivityMobile:";

    public final static String SOCIAL_INTEGRAL_DISTRIBUTE_FALG = "socialIntegralDistributeFlag:";
    public final static String USER_SOCIAL_INTEGRAL = "userSocialIntegral:";//用户社区积分

    public final static String GAME_PRODUCT_PROGRAM = "gameProductProgram:";//生成方案标志位
    public final static String USER_PURCHASE_PROGRAM = "userPurchaseProgram:";//用户购买方案
    public final static String PROGRAM_TYPE_LIST = "programTypeList:";//用户购买方案列表

    public final static String FC3D_HISTORY = "FC3D_HISTORY:";//用户购买方案列表


    public final static String SPORT_SOCIAL_INDEX_HOT_RECOMMEND = "sportSocialIndexHotRecommend";//首页热门推荐
    //用户发送推荐临时存储
    public final static String SPORT_SOCIAL_RECOMMEND_TEMP_STORAGE_LIST = "sportSocialRecommendTempStorageList";

    public final static String SPORT_SOCIAL_RECOMMEND_LIST = "sportSocialRecommendList:";
    public final static String SPORT_SOCIAL_ONE_MATCH_RECOMMEND_LIST = "sportSocialOneMatchRecommendList:";

    public final static String SPORT_SOCIAL_RANK = "SPORT_SOCIAL_RANK:";
    public final static String SPORT_SOCIAL_RANK_VO = "SPORT_SOCIAL_RANK_VO:";

    public final static String USER_RECOMMEND_MATCHS = "USER_RECOMMEND_MATCHS:";
    public final static String SPORT_ALL_CAN_PREDICT_MATCH_LIST = "sportAllCanPredictMatchList:";
    public final static String INNER_SITE_POP_LIMIT = "innerSitePopLimit:";

    public final static String START_USER_RANK = "starUserRank:";
    public final static String STAR_USER_ID_SET = "starUserIdSet:";

    public final static String MATCH_MAX_HIT_RATIO = "matchMaxHitRatio:";

    public final static String SPORT_INDEX_FOCUS_MATCHES = "sportIndexFocusMatches";

    public final static String SPORT_MATCH_TAG_LIST = "sportMatchTagList";
    public final static String SPORTS_TAG_MATCH_LIST = "sportTagMatchList:";
    public final static String SPORTS_USER_FOLLOW_MATCH_LIST = "sportUserFollowMatchList:";
    public final static String SPORTS_MATCH_INFO = "sportMatchInfo:";

    public final static String HTTP_SPORTS_INFO_KEY = "httpSportInfoKey:";

    private static final String WITHDRAW_MERCHANT_BALANCE = "withdrawMerchantBalance:";

    private static final String USER_WITHDRAW_TIMES = "userWithdrawTimes:";

    private static final String RED_VIP_PROGRAM = "redVipProgram";

    public final static int EXPIRE_TIME_SECOND_THIRTY_DAY = 60 * 60 * 24 * 30;
    public final static int EXPIRE_TIME_SECOND_TWO_HOUR = 60 * 60 * 2;
    public final static int EXPIRE_TIME_SECOND_ONE_HOUR = 60 * 60;
    public final static int EXPIRE_TIME_SECOND_FIVE_MINUTE = 60 * 5;
    // redis
    public static String REDIS_DEFAULT_CHARSET = IniCache.getIniValue(IniConstant.REDIS_DEFAULT_CHARSET, "UTF-8");
    //token过期时间
    public static int EXPIRE_LOGIN_TOKEN = IniCache.getIniIntValue(IniConstant.TOKEN_CACHE_TIME, 24 * 60 * 60);
    //trend过期时间
    public static int EXPIRE_TREND_COMMON = 12 * 24 * 60 * 60;

    public static String getPeriodDetailKey(Long gameId, String key) {
        return new StringBuffer().append(RedisConstant.PREFIX_PERIOD).append(gameId).append(key).toString();
    }

    public static String getAwardInfoKey(Long gameId) {
        return new StringBuffer().append(RedisConstant.AWARD_INFO_PERIOD).append(gameId).toString();
    }

    public static String getCurrentChartKey(Long gameId, String periodId, String chartName, Integer periodNum) {
        return new StringBuffer().append(RedisConstant.PREFIX_TREND).append(gameId).append(periodId).append
                (chartName).append(periodNum == null ? "" : periodNum).toString();
    }

    public static String getRedisAwardDownloadKey(Long gameId) {
        return new StringBuffer().append(RedisConstant.REDIS_AWARD_DOWNLOAD_PRE_KEY).append(gameId).toString();
    }

    public static String getRedisTestNumDownloadKey(Long gameId) {
        return new StringBuffer().append(RedisConstant.REDIS_TEST_NUM_DOWNLOAD_PRE_KEY).append(gameId).toString();
    }

    public static String getPredictNumsKey(long gameId, String periodId, String userUniqueStr, String type) {
        return new StringBuffer().append(RedisConstant.PREFIX_PREDICT_NUM).append(gameId).append(periodId).append
                (userUniqueStr).append(type == null ? "" : type).toString();
    }

    public static String getPredictMaxNumsKey(long gameId, String periodId, Long userId) {
        return new StringBuffer(PREFIX_PREDICT_MAX_NUM).append(gameId).append(periodId).append(userId).toString();
    }

    public static String getDevicePredictNumsKey(long gameId, String periodId, String userUniqueStr, String type) {
        return new StringBuffer().append(RedisConstant.PREFIX_PREDICT_NUM_DEVICE).append(gameId).append(periodId).append
                (userUniqueStr).append(type == null ? "" : type).toString();
    }

    public static String getHistoryPredictNumsKey(long gameId, String numStr) {
        return new StringBuffer().append(RedisConstant.PREFIX_PREDICT_NUM).append(gameId).append(numStr).toString();
    }

    /* 推送标志位 gameId + openingPeriod.getPeriodId() + RedisConstant.PUSH_FLAG*/
    public static String getPushFlagKey(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.ALI_PUSH_FLAG).append(gameId).append(periodId).toString();
    }

    public static String getAliPushFlagKey(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.PUSH_FLAG).append(gameId).append(periodId).toString();
    }

    /* 推送筷照LIST period.getGameId() + RedisConstant.PUSH_KEY_LIST*/
    public static String getPushKeyList(long gameId) {
        return new StringBuffer().append(RedisConstant.PUSH_KEY_LIST).append(gameId).toString();
    }

    public static String getAliPushKeyList(long gameId) {
        return new StringBuffer().append(RedisConstant.ALI_PUSH_KEY_LIST).append(gameId).toString();
    }

    /* 推送中已推送KEY period.getGameId() + RedisConstant.PUSHING_KEY*/
    public static String getPushingKey(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.PUSHING_KEY).append(gameId).append(periodId).toString();
    }

    public static String getAliPushingKey(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.Ali_PUSHING_KEY).append(gameId).append(periodId).toString();
    }

    /* 推送中已推送个数 period.getGameId() + period.getPeriodId() + RedisConstant.PUSHED_COUNT*/
    public static String getPushedCountKey(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.PUSHED_COUNT).append(gameId).append(periodId).toString();
    }

    public static String getAliPushedCountKey(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.ALI_PUSHED_COUNT).append(gameId).append(periodId).toString();
    }

    /* 推送Photo key game.getGameId() + RedisConstant.PUSH_PHOTO + .toString()*/
    public static String getPushPhotoKey(long gameId) {
        return new StringBuffer(REDIS_CLUSTER_PUSH).append(PUSH_SNAP).append(gameId).append(DateUtil.formatTime(DateUtil
                .getCurrentTimestamp(), DateUtil.DATE_FORMAT_YYYYMMDDHHMMSS)).toString();
    }

    // ali
    public static String getAliPushPhotoKey(long gameId) {
        return new StringBuffer(ALI_REDIS_CLUSTER_PUSH).append(PUSH_SNAP).append(gameId).append(DateUtil.formatTime
                (DateUtil.getCurrentTimestamp(), DateUtil.DATE_FORMAT_YYYYMMDDHHMMSS)).toString();
    }

    /* 推送缓存初始化 game.getGameId() + RedisConstant.PUSH_CLIENT_LIST*/
    public static String getPushClientList(long gameId) {
        return new StringBuffer(REDIS_CLUSTER_PUSH).append(PUSH_CLIENT_LIST).append(gameId).toString();
    }

    /* 阿里云推送*/
    public static String getAliPushClientList(long gameId) {
        return new StringBuffer(ALI_REDIS_CLUSTER_PUSH).append(ALI_PUSH_CLIENT_LIST).append(gameId).toString();
    }

    /* 推送缓存初始化 game.getGameId() + RedisConstant.PUSH_CLIENT_LIST*/
    public static String getDefaultClientSet() {
        return new StringBuffer(REDIS_CLUSTER_PUSH).append(DEFAULT_CLIENT_SET).toString();
    }

    public static String getAliDefaultClientSet() {
        return new StringBuffer(ALI_REDIS_CLUSTER_PUSH).append(DEFAULT_CLIENT_SET).toString();
    }

    /* 号码分析 号码近期表现存储*/
    public static String getBehaveKey(Long gameId) {
        return new StringBuffer().append(LAST_BEHAVE).append(gameId).toString();
    }

    /* 号码分析 号码近期表现存储*/
    public static String getShowTitleKey(String gameEn) {
        return new StringBuffer().append(SHOW_TITLE).append(gameEn).toString();
    }

    /* 号码分析 历史遗漏和连号*/
    public static String getHistoryOmit(String gameEn) {
        return new StringBuffer().append(HISTORY_OMIT).append(gameEn).toString();
    }

    public static String getEncircleTimesKey(long gameId, String periodId, Integer encircleType, Long userId) {
        return new StringBuffer().append(PREFIX_SOCIAL_ENCIRCLE_TIMES).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).append(CommonConstant.COMMON_COLON_STR).append(encircleType)
                .append(CommonConstant.COMMON_COLON_STR).append(userId).toString();
    }

    public static String getEncircleUserKillRankKey(long gameId, long encircleId) {
        return new StringBuffer().append(PREFIX_SOCIAL_ENCIRCLE_USER_RANK).append(CommonConstant.COMMON_COLON_STR)
                .append
                        (gameId).append(CommonConstant.COMMON_COLON_STR).append(encircleId).toString();
    }

    public static final Map<String, String> Hot_KEY_MAP = new HashMap<>();

    static {
        Hot_KEY_MAP.put("ssq:blue", BLUE_COLD_HOT);
        Hot_KEY_MAP.put("ssq:red", RED_COLD_HOT);
        Hot_KEY_MAP.put("dlt:blue", BACK_COLD_HOT);
        Hot_KEY_MAP.put("dlt:red", FRONT_COLD_HOT);
        Hot_KEY_MAP.put("fc3d:red", COLD_HOT);
        Hot_KEY_MAP.put("fc3d:hundred", HUNDRED_COLD_HOT);
        Hot_KEY_MAP.put("fc3d:ten", TEN_COLD_HOT);
        Hot_KEY_MAP.put("fc3d:one", ONE_COLD_HOT);
    }

    public static final Map<String, String> OMIT_KEY_MAP = new HashMap<>();

    static {
        OMIT_KEY_MAP.put("ssq:blue", BLUE_OMIT);
        OMIT_KEY_MAP.put("ssq:red", RED_OMIT);
        OMIT_KEY_MAP.put("dlt:blue", BACK_OMIT);
        OMIT_KEY_MAP.put("dlt:red", FRONT_OMIT);
        OMIT_KEY_MAP.put("fc3d:hundred", HUNDRED_OMIT);
        OMIT_KEY_MAP.put("fc3d:ten", TEN_OMIT);
        OMIT_KEY_MAP.put("fc3d:one", ONE_OMIT);
    }

    public static final Map<String, String> THEORY_VALUE_MAP = new HashMap<>();

    static {
        THEORY_VALUE_MAP.put("blue:ssq50", "3.12");
        THEORY_VALUE_MAP.put("red:ssq50", "9.09");

        THEORY_VALUE_MAP.put("blue:ssq100", "6.24");
        THEORY_VALUE_MAP.put("red:ssq100", "18.18");

        THEORY_VALUE_MAP.put("blue:dlt50", "8.33");
        THEORY_VALUE_MAP.put("red:dlt50", "7.14");

        THEORY_VALUE_MAP.put("blue:dlt100", "16.66");
        THEORY_VALUE_MAP.put("red:dlt100", "14.28");

        THEORY_VALUE_MAP.put("hundred:fc3d100", "10.00");
        THEORY_VALUE_MAP.put("hundred:fc3d50", "5.00");

        THEORY_VALUE_MAP.put("ten:fc3d100", "10.00");
        THEORY_VALUE_MAP.put("ten:fc3d50", "5.00");

        THEORY_VALUE_MAP.put("one:fc3d100", "10.00");
        THEORY_VALUE_MAP.put("one:fc3d50", "5.00");
    }

    /* 过滤*/
    /* 旋转矩阵*/
    public static String getMatrixListKey(String gameEn, Integer length, String matrixAction) {
        return new StringBuffer(MATRIX_LIST).append(gameEn).append(length).append(matrixAction).toString();
    }


    /* 条件*/
    public static String getMatrixActionKey(String gameEn, String redNumber, String matrixAction, String action) {
        return new StringBuffer(MATRIX_ACTION_RESULT).append(gameEn).append(CommonConstant.COMMON_COLON_STR).append
                (matrixAction).append(CommonConstant.COMMON_COLON_STR).append(action).append(CommonConstant
                .COMMON_COLON_STR).append(redNumber).toString();
    }

    public static String getMatrixActionShowTextKey(String gameEn, String redNumber, String matrixAction, String
            action) {
        return new StringBuffer(MATRIX_ACTION_RESULT_TEXT).append(gameEn).append(CommonConstant.COMMON_COLON_STR).append
                (matrixAction).append(CommonConstant.COMMON_COLON_STR).append(action).append(CommonConstant
                .COMMON_COLON_STR).append(redNumber).toString();
    }

    public static String getMatrixActionBlueKey(String matrixActionKey) {
        return new StringBuffer(MATRIX_ACTION_RESULT_BLUE).append(matrixActionKey).toString();
    }

    /* 社交杀号 */
    public static String getConcurrentUserKillLockKey(long gameId, long encircleId, Long userId) {
        return new StringBuffer(PREFIX_SOCIAL_USER_KILL_NUM_LOCK).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(encircleId).append(CommonConstant.COMMON_COLON_STR).append(userId).toString();
    }

    public static String getSocialKillNumListKey(long gameId, Integer killNumType) {
        return new StringBuffer(PREFIX_SOCIAL_KILL_NUM_LIST).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(killNumType).toString();
    }

    public static String getUserKillNumTotalTimesKey(long gameId, String periodId, Long userId) {
        return new StringBuffer().append(PREFIX_SOCIAL_KILL_NUM).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).append(userId).append(SOCIAL_PERIOD_KILL_NUM_TOTAL_TIMES)
                .toString();
    }

    public static String getSocialUserEncircleTempListKey(long gameId) {
        return new StringBuffer().append(PREFIX_SOCIAL_ENCIRCLE_NUM).append(gameId).append
                (SOCIAL_ENCIRCLE_NUM_TEMP_LIST).toString();
    }

    public static String getSocialUserKillTempListKey(long gameId) {
        return new StringBuffer().append(PREFIX_SOCIAL_ENCIRCLE_NUM).append(gameId).append(SOCIAL_KILL_NUM_TEMP_LIST)
                .toString();
    }

    public static String getPeriodEncircleListKey(long gameId, String periodId, Integer encircleType) {
        return new StringBuffer(PREFIX_SOCIAL_ENCIRCLE_NUM).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(periodId).append(CommonConstant.COMMON_COLON_STR).append(encircleType).append
                        (SOCIAL_PERIOD_ENCIRCLE_SORTED_SET).toString();
    }

    public static String getUpdateSocialKillNumListKey(long gameId, String periodId, Integer encircleType) {
        return new StringBuffer(PREFIX_SOCIAL_KILL_NUM).append(gameId).append(CommonConstant.COMMON_COLON_STR).append
                (encircleType).append(CommonConstant.COMMON_COLON_STR).append(periodId).append
                (SOCIAL_KILL_NUM_LIST_UPDATE_FLAG).toString();
    }

    public static String getPeriodEncircleListKeyTemp(long gameId, String periodId, Integer encircleType) {
        return new StringBuffer(PREFIX_SOCIAL_ENCIRCLE_NUM).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(periodId).append(CommonConstant.COMMON_COLON_STR).append(encircleType).append
                        (SOCIAL_PERIOD_ENCIRCLE_SORTED_SET_TEMP).toString();
    }

    public static String getSocialBigDataKey(long gameId, String periodId) {
        return new StringBuilder(SOCIAL_BIG_DATA_LIST).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(periodId).toString();
    }

    public static String getLastStatisticTimeKey(long gameId, Integer dataType) {
        return new StringBuilder(SOCIAL_LAST_STATISTIC_TIME).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(dataType).toString();
    }

    /* 管理后台身份识别*/
    public static String getAdminKey(String mobile) {
        return new StringBuffer(ADMIN_KEY).append(mobile).toString();
    }


    /* 预测类型*/
    public static String getPredictTypeKey(String gameEn, Integer predictType) {
        return new StringBuffer(PREDICT_TYPE).append(gameEn).append(CommonConstant.COMMON_COLON_STR).append(predictType)
                .toString();
    }

    /* 算奖标志位*/
    public static String getDisTributeFlag(long gameId) {
        return new StringBuffer().append(RedisConstant.DISTRIBUTE_FLAG).append(gameId).toString();
    }

    /* 成就标志位*/
    public static String getAchievementFlag(long gameId) {
        return new StringBuffer(ACHIEVEMENT_FLAG).append(gameId).toString();
    }

    /* 用户当期圈号*/
    public static String getUserCurrentEncircleVo(long gameId, String periodId, Integer socialType, Long userId) {
        return new StringBuilder(PREFIX_SOCIAL_CURRENT_ENCIRCLEVO).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).append(CommonConstant.COMMON_COLON_STR).append(socialType).append
                (CommonConstant.COMMON_COLON_STR).append(userId).toString();
    }

    /* 社交经典圈号*/
    public static String getClassicEncircleListKey(long gameId, Integer socialType) {
        return new StringBuilder(SOCIAL_CLASSIC_ENCIRCLE_LIST).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(socialType).toString();
    }

    /* 热门圈号key*/
    public static String getHotEncircleListKey(long gameId, String periodId, Integer socialType) {
        return new StringBuilder(SOCIAL_HOT_ENCIRCLE_LIST).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(periodId).append(CommonConstant.COMMON_COLON_STR).append(socialType).toString();
    }

    /* 签到*/
    public static String getUserSignKey(Long userId, String date, Integer signType) {
        return new StringBuilder(USER_DATE_SIGN).append(userId).append(CommonConstant.COMMON_COLON_STR).append(date)
                .append(signType).toString();
    }

    /*  机器人可以杀的圈号*/
    public static String getRobotKillEncircleKey(Long gameId, Integer socialType) {
        return new StringBuilder(SOCIAL_ENCIRCLE_LIST_ROBOT_KILL).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(socialType).toString();
    }

    public static String getPersionalAchieveKey(long gameId, Long userId) {
        return new StringBuffer().append(PERSIONAL_ACHIEVEMENT).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(userId).toString();
    }

    /* vip会员key*/
    public static String getUserVipRedisKey(Long userId, Integer vipType) {
        return new StringBuilder(USER_VIP_MEMBER_DETAIL).append(userId).append(CommonConstant.COMMON_COLON_STR)
                .append(vipType).toString();
    }

    public static String getUserPurchaseLazySecond(Long userId) {
        return new StringBuilder(USER_VIP_PURCHASE_LAZY_SECOND).append(userId).toString();
    }

    public static String getEncirclePeriodRank(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.ENCIRCLE_PERIOD_RANK).append(gameId).append(periodId).toString();
    }

    public static String getEncircleWeekRank(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.ENCIRCLE_WEEK_RANK).append(gameId).append(periodId).toString();
    }

    public static String getEncircleMonthRank(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.ENCIRCLE_MONTH_RANK).append(gameId).append(periodId).toString();
    }

    public static String getKillPeriodRank(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.KILL_PERIOD_RANK).append(gameId).append(periodId).toString();
    }

    public static String getKillWeekRank(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.KILL_WEEK_RANK).append(gameId).append(periodId).toString();
    }

    public static String getKillMonthRank(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.KILL_MONTH_RANK).append(gameId).append(periodId).toString();
    }

    public static String getEncircleRankFlag(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.ENCIRCLE_RANK_FLAG).append(gameId).append(periodId).toString();
    }

    public static String getKillRankFlag(long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.KILL_RANK_FLAG).append(gameId).append(periodId).toString();
    }

    public static String getUserDistributeFlag(Long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.USER_DISTRIBUTE_FLAG).append(gameId).append(periodId).toString();
    }

    public static String getUserDistributeKillFlag(Long gameId, String periodId) {
        return new StringBuffer().append(RedisConstant.USER_KILL_NUM_DISTRIBUTE_FLAG).append(gameId).append(periodId)
                .toString();
    }

    public static String getUserShareWx(Long gameId, Long userId) {
        return new StringBuffer().append(RedisConstant.USER_SHARE_WX).append(gameId).append(userId)
                .toString();
    }

    public static String getUserShareWxActivity(Integer activityId, Long userId) {
        return new StringBuffer().append(RedisConstant.USER_SHARE_WX).append(activityId).append(userId)
                .toString();
    }

    public static String getUserShareWxActivityPeriod(Integer activityId, Long userId, String periodId) {
        return new StringBuffer().append(RedisConstant.USER_SHARE_WX).append(activityId).append(userId).append(periodId)
                .toString();
    }

    public static String getAwardPopupFlag(Long gameId, String periodId, Long userId) {
        return new StringBuffer().append(RedisConstant.AWARD_POPUP_FLAG).append(gameId).append(periodId).append(userId)
                .toString();
    }

    public static String getSocialPopupFlag(Long gameId, String periodId, Long userId, Integer popType) {
        return new StringBuffer().append(RedisConstant.SOCIAL_POPUP_FLAG).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).append(userId).append(CommonConstant.COMMON_COLON_STR).append
                (popType).toString();
    }

    public static String getUserTrendSaveNumPopFlag(Long userId) {
        return new StringBuilder(USER_TREND_SAVE_NUM_POP_FLAG).append(userId).toString();
    }

    public static void refresh() {
    }

    public static String getUserOneDaySaveNumBooKTimesKey(Long userId, String date) {
        return new StringBuilder(USER_ONE_DAY_SAVE_NUM_BOOK_TIMES).append(userId).append(CommonConstant
                .COMMON_COLON_STR).append(date).toString();
    }

    // 预测号码集合
    public static String predictNumbers(Long gameId, String periodId) {
        return new StringBuilder(PREDICT_NUMBERS_SET).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).toString();
    }

    // 预测号码集合
    public static String predictNumbersCountFlag(Long gameId, String periodId) {
        return new StringBuilder(PREDICT_NUMBERS_SET_COUNT_FLAG).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).toString();
    }

    // 预测号码用户使用次数
    public static String userUseNums(Long gameId, String periodId, Long userId) {
        return new StringBuilder(USER_USE_NUMS).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).append(CommonConstant.COMMON_COLON_STR).append(userId).toString();
    }

    //marquee
    public static String getMarqueeIndex(Long gameId) {
        return new StringBuilder(MARQUEE_INDEX).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).toString();
    }

    // 福彩3d 预测定位杀3码flag
    public static String getPositionKillThreeFlag(Long gameId, String periodId) {
        return new StringBuilder(POSITION_KILL_THREE).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).toString();
    }

    // 福彩3d 预测3胆码flag
    public static String getThreeDanCodeFlag(Long gameId, String periodId) {
        return new StringBuilder(THREE_DAN_CODE).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).toString();
    }

    // 福彩3d 预测3胆码flag
    public static String getFcPredictFlag(Long gameId, String periodId) {
        return new StringBuilder(FC_PREDICT).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).toString();
    }

    //用户头衔
    public static String getUserTitleVoKey(long gameId, Long userId) {
        return new StringBuilder(USER_TITLE).append(gameId).append(CommonConstant.COMMON_COLON_STR).append(userId)
                .toString();
    }

    //用户头衔期次派发
    public static String getUserTitleDistrbuteFlag(long gameId, String dateStr) {
        return new StringBuilder(USER_TITLE_DISTRIBUTE_FLAG).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(dateStr).toString();
    }

    public static String getTestUserMoney() {
        return new StringBuilder(TEST_DB_USER_MONEY).toString();
    }

    //统计购买订单key
    public static String getStatisticPurchaseOrderKey() {
        return STATISTIC_PURCHASE_ORDER;
    }

    //娃娃机器活动用户key
    public static String getOthrePlateFormPrizeMoblie(Integer prizeType, Integer uniqueFlag) {
        return OTHRE_PLATEFORM_ACTIVITY_MOBILE + prizeType + CommonConstant.COMMON_COLON_STR + uniqueFlag;
    }

    // 用户等级积分派发flag
    public static String getSocialIntegralDistributeFlag(long gameId) {
        return SOCIAL_INTEGRAL_DISTRIBUTE_FALG + CommonConstant.COMMON_COLON_STR + gameId;
    }

    //用户积分
    public static String getUserIntegralKey(long gameId, Long userId) {
        return new StringBuilder(USER_SOCIAL_INTEGRAL).append(gameId).append(CommonConstant.COMMON_COLON_STR).append
                (userId).toString();
    }

    //用户积分
    public static String getGameProductProgramFlag(long gameId, String periodId) {
        return new StringBuilder(GAME_PRODUCT_PROGRAM).append(gameId).append(CommonConstant.COMMON_COLON_STR).append
                (periodId).toString();
    }

    //用户购买方案
    public static String getUserPurchaseProgramKey(long gameId, String periodId, Long userId, String programId) {
        return new StringBuilder(USER_PURCHASE_PROGRAM).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(periodId).append(CommonConstant.COMMON_COLON_STR).append(userId).append(CommonConstant
                        .COMMON_COLON_STR).append(programId).toString();
    }

    //方案列表
    public static String getSaleProgramList(Long gameId, String periodId, Integer programType) {
        return new StringBuilder(PROGRAM_TYPE_LIST).append(gameId).append(CommonConstant.COMMON_COLON_STR).append
                (periodId).append(CommonConstant.COMMON_COLON_STR).append(programType).toString();
    }

    //热态杀三标志为
    public static String getPredictRedBallFlagKey(String gameEn) {
        return new StringBuilder(PREDICT_RED_BALL_FLAG).append(gameEn).toString();
    }

    //热态及其它首页展示
    public static String getSubscribeIndexKey(long gameId, Integer programType) {
        return new StringBuilder(PREDICT_RED_BALL_SUBSCRIBE_INDEX_DATA).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(programType).toString();
    }

    //用户订购方案信息
    public static String getUserSubscribeTypeKey(long gameId, Long userId, Integer predictType) {
        return new StringBuilder(USER_SUBSCRIBE_PREDICT_LAST_PERIOD_ID).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(userId).append(CommonConstant.COMMON_COLON_STR).append(predictType)
                .toString();
    }

    //用户冷热态首购
    public static String getUserFirstBuyColdHotStatePredictKey(long gameId, Long userId, Integer programType) {
        return new StringBuilder(USER_FIRST_BUY_COLD_HOT_STATE_PREDICT).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(userId).append(CommonConstant.COMMON_COLON_STR).append(programType)
                .toString();
    }

    public static String getUpgradeLevelUserSet(long gameId, String periodId) {
        return new StringBuilder(SOCIAL_POP_USER_LEVEL_UPGRADE_LIST).append(gameId).append(CommonConstant
                .COMMON_COLON_STR).append(periodId).toString();
    }

    public static String getBecomeSportsMaster(Integer lotteryCode) {
        return SPORTS_SOCIAL_POP_BECOME_MASTER_LIST + lotteryCode;
    }

    public static String getUserAchievePopList(long gameId, String periodId, String socialType, Integer awardType) {
        return new StringBuilder(SOCIAL_POP_USER_ACHIEVE_LIST).append(gameId).append(CommonConstant.COMMON_COLON_STR)
                .append(periodId).append(CommonConstant.COMMON_COLON_STR).append(socialType).append(CommonConstant
                        .COMMON_COLON_STR).append(awardType).toString();
    }

    public static String getFootballMarqueeKey() {
        return FOOTBALL_INDEX_MARQUEE_KEY;
    }

    public static String getUserFollowKey(Long userId, Integer followType) {
        return new StringBuilder(RedisConstant.PREFIX_FOLLOW_INFO_VO).append(userId).append(CommonConstant
                .COMMON_COLON_STR).append(followType).toString();
    }

    public static String getUserPurchaseFootballProgramKey(Long userId, String programId) {
        return new StringBuilder(USER_PURCHASE_FOOTBALL_PROGRAM).append(userId).append(CommonConstant
                .COMMON_COLON_STR).append(programId).toString();
    }

    //首页热门推荐
    public static String getSportSocialIndexHotRecommend() {
        return SPORT_SOCIAL_INDEX_HOT_RECOMMEND;
    }

    //用户
    public static String getSportSocialRecommendTempStorageListKey() {
        return SPORT_SOCIAL_RECOMMEND_TEMP_STORAGE_LIST;
    }

    //热门推荐列表
    public static String getSportSocialRecommendListKey(Integer listType, Integer playType) {
        return SPORT_SOCIAL_RECOMMEND_LIST + listType + CommonConstant.COMMON_COLON_STR + playType;
    }

    //赛事推荐列表
    public static String getSportSocialOneMatchRecommendListKey(String matchId) {
        return SPORT_SOCIAL_ONE_MATCH_RECOMMEND_LIST + matchId;
    }

    // 排行榜
    public static String getSportSocialRankKey(Integer rankType, Integer playType) {
        return SPORT_SOCIAL_RANK + rankType + CommonConstant.COMMON_COLON_STR + playType;
    }

    // 排行榜
    public static String getSportSocialVoKey(Long userId) {
        return SPORT_SOCIAL_RANK_VO + String.valueOf(userId);
    }

    public static String getUserRecommendMatchsKey(Long userId) {
        return USER_RECOMMEND_MATCHS + String.valueOf(userId);
    }


    public static String getBlueMatrixTrendKey(String periodId) {
        return "vipPrivilegeBlueMatrixTrend:" + periodId;
    }

    public static String getAllMatchesKey(Integer lotteryCode) {
        return SPORT_ALL_CAN_PREDICT_MATCH_LIST + lotteryCode;
    }

    public static String getUserSignPopKey(String deviceId, String date) {
        return "userSignPopContent:" + deviceId + ":" + date;
    }

    public static String getInnerSitePopLimitKey(String deviceId, String date, Integer activityId) {
        return INNER_SITE_POP_LIMIT + deviceId + CommonConstant.COMMON_COLON_STR + date + CommonConstant
                .COMMON_COLON_STR + activityId;
    }

    public static String getHttpMatchInfoKey(String matchIds) {
        return "HTTP_MATCH_INFO:" + matchIds;
    }

    public static String getMatchListByDateKey(String lastDate, String lastMatchId) {
        return "HTTP_MATCH_LIST_DATE:" + lastDate + ":" + lastMatchId;
    }

    public static String getManualRecommendKey(Integer listType, Integer playType) {
        return "MANUAL_RECOMMEND_HOT_LIST:" + listType + ":" + playType;
    }

    public static String getStarUserRankKey(Integer activityId) {
        return START_USER_RANK + activityId;
    }

    public static String getStarUserIdsKey(Integer activityId) {
        return STAR_USER_ID_SET + activityId;
    }

    public static String getActivityStarUserInfoKey(Integer activityId, String orderType) {
        return "ADMIN_FILTER_STAR_USER_LIST:" + activityId + ":" + orderType;
    }

    public static String getInternetCelebrityLikeCount(String recommendId) {
        return "INTERNET_CELEBRITY_LIKE_COUNT" + recommendId;
    }

    public static String getIosReviewIpKey() {
        return "IOS_REVIEW_BLACK_IP";
    }

    public static String getIosReviewRangeIpKey() {
        return "IOS_REVIEW_RANGE_BLACK_IP";
    }

    public static String getHttpIpInfoKey(String visitIp) {
        return "THIRD_HTTP_IP_INFO:" + visitIp;
    }

    public static String getManualSportsHotRecommendIdsKey(Integer playType) {
        return "MANUAL_SET_SPORTS_HOT_RECOMMEND_IDS" + ":" + playType;
    }

    public static String getFocusMatchesKey() {
        return SPORT_INDEX_FOCUS_MATCHES;
    }

    public static String getMatchTagKey() {
        return SPORT_MATCH_TAG_LIST;
    }

    public static String getTagMatchListKey(Integer tagId) {
        return SPORTS_TAG_MATCH_LIST + tagId;
    }

    public static String getUserFollowMatchListKey(Integer tagId, Long userId) {
        return SPORTS_USER_FOLLOW_MATCH_LIST + tagId + ":" + userId;
    }

    public static String getMatchInfoKey(Integer matchId) {
        return SPORTS_MATCH_INFO + matchId;
    }

    public static String getHttpSportsInfoKey() {
        return HTTP_SPORTS_INFO_KEY;
    }

    public static String getWithdrawMerchantBalance(Integer code) {
        return WITHDRAW_MERCHANT_BALANCE + code;
    }

    public static String getWxJSAPICodeOpenIdMapKey(String wxCode) {
        return "WX_JSAPI_CODE_OPENID_MAP:" + wxCode;
    }

    public static String getUserWithdrawTimes(String currentDay, Long userId) {
        return new StringBuilder(USER_WITHDRAW_TIMES).append(currentDay).append(CommonConstant.COMMON_COLON_STR)
                .append(userId).toString();
    }

    public static String threePartyBillFileKey(String threePartName, String merchantNo) {
        return new StringBuilder(threePartName).append(CommonConstant.COMMON_COLON_STR).append(merchantNo).toString();
    }

    public static String getRedVipProgramKey() {
        return RED_VIP_PROGRAM;
    }
}
