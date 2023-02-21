package com.mojieai.predict.constant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Singal
 */
public class CommonConstant {
    public static final ThreadLocal<HttpServletRequest> requestTL = new ThreadLocal<>(); //保存request的threadlocal
    public static final ThreadLocal<HttpServletResponse> responseTL = new ThreadLocal<>(); //保存response的threadlocal
    public static final ThreadLocal<HttpSession> sessionTL = new ThreadLocal<>(); //保存session的threadlocal

    public final static String SPACE_SPLIT_STR = " ";
    public final static String SPACE_NULL_STR = "";
    public final static String PERCENT_SPLIT_STR = "%";
    public final static String COMMON_SPLIT_STR = "_";
    public final static String COMMON_DASH_STR = "-";
    public final static String COMMA_SPLIT_STR = ",";
    public final static String SEMICOLON_SPLIT_STR = ";";
    public final static String COMMON_VERTICAL_STR = "|";
    public final static String URL_SPLIT_STR = "/";
    public final static String DOUBLE_SLASH_STR = "//";
    public final static String POUND_SPLIT_STR = "#";
    public final static String COMMON_ESCAPE_STR = "\\";
    public final static String COMMON_AT_STR = "@";
    public final static String COMMON_DOLLAR_STR = "$";
    public final static String COMMON_WAVE_STR = "~";
    public final static String COMMON_STAR_STR = "*";
    public final static String COMMON_COLON_STR = ":";
    public final static String COMMON_DOT_STR = ".";
    public final static String COMMON_EQUAL_STR = "=";
    public final static String COMMON_AND_STR = "&";
    public final static String UP_ARROW_STR = "^";
    public final static String COMMON_BRACKET_LEFT = "(";
    public final static String COMMON_BRACKET_RIGHT = ")";
    public final static String COMMON_SQUARE_BRACKET_LEFT = "[";
    public final static String DOUBLE_DASH_STR = "--";
    public final static String COMMON_ADD_STR = "+";
    public final static String COMMON_QUESTION_STR = "?";

    public final static String COMMON_DOT_STR_CN = "。";
    public final static String COMMON_COLON_STR_CN = "：";
    public final static String COMMON_PAUSE_STR_CN = "、";
    public final static String COMMON_LEFT_STR_CN = "“";
    public final static String COMMON_RIGHT_STR_CN = "”";
    public final static String COMMA_SPLIT_STR_CN = "，";

    public final static String COMMON_COLOR_BLUE_1 = "#5575B5";
    public final static String COMMON_COLOR_ORIGIN = "#FF8450";
    public final static String COMMON_COLOR_RED = "#FF5050";

    public final static String COMMON_COLOR_ORIGIN_1 = "#FF8150";

    public static final int ZERO = 0;
    public static final int ONE = 1;

    public static final int FC_HUNDRED_DIGIT = 0;
    public static final int FC_TEN_DIGIT = 1;
    public static final int FC_ONE_DIGIT = 2;//个位
    public static final int FC_ALL_DIGIT = 3;//所有中奖球

    public static final int BLUE_BALL_TYPE = 1;
    public static final int RED_BALL_TYPE = 0;

    public static final String SOCIAL_CODE_TYPE_ENCIRCLE = "encircle";
    public static final String SOCIAL_CODE_TYPE_KILL = "kill";

    public static final String SOCIAL_FOLLOW_TYPE_FOLLOW = "follow";
    public static final String SOCIAL_FOLLOW_TYPE_FANS = "fans";

    public static final Integer SOCIAL_FOLLOW_FANS_TYPE_DIGIT = 0;
    public static final Integer SOCIAL_FOLLOW_FANS_TYPE_SPORT = 1;


    public static final String SOCIAL_RANK_TYPE_PERIOD = "period";
    public static final String SOCIAL_RANK_TYPE_WEEK = "week";
    public static final String SOCIAL_RANK_TYPE_MONTH = "month";

    public static final String STR_MINUS_ONE = "-1";

    //分隔符
    public static final String SEPARATOR_LINE = System.getProperty("line.separator");
    public static final String SEPARATOR_FILE = System.getProperty("file.separator");

    //开关
    public final static String SWITCH_ON = "on";
    public final static String SWITCH_OFF = "off";

    //账户类型
    public final static Integer ACCOUNT_TYPE_GOLD = 0;
    public final static Integer ACCOUNT_TYPE_CASH = 1;
    public final static Integer ACCOUNT_TYPE_WISDOM_COIN = 2;
    public final static Integer ACCOUNT_TYPE_BALANCE = 3;

    //
    public final static Integer SIGN_IF_REWARD_NO = 0;
    public final static Integer SIGN_IF_REWARD_YES = 1;

    //tableShard
    public static final String SHARD_BY_USER_CODE = "userCode";
    public static final String SHARD_BY_MOBILE = "mobile";
    public static final String SHARD_BY_USER_ID = "userId";
    public static final String SHARD_BY_DEVICE_ID = "deviceId";
    public static final String SHARD_BY_TOKEN = "token";
    public static final String SHARD_BY_OAUTH_ID = "oauthId";
    public static final String SHARD_BY_GAME_ID = "gameId";
    public static final String SHARD_BY_PERIOD_ID = "periodId";
    public static final String SHARD_BY_VIP_OPERATE_CODE = "vipOperateCode";

    //下载大盘彩奖级信息
    public static final String AWARD_163_DOWNLOAD_URL_PREFIX = "http://caipiao.163.com/award/";
    public static final String AWARD_163_DOWNLOAD_ELEMENT_CLASS = "iSelectList";
    public static final int AWARD_163_DOWNLOAD_TIMEOUT_MSEC = 3000;

    public static final String AWARD_QQ_DOWNLOAD_URL_PREDIX = "https://888.qq" +
            ".com/static/mobile_app/app/public/jc/kaijiang/";
    public static final String QQ_OPEN_AWARD_PAGE = "kaijiang_history_public_data_";
    public static final String SUFFIX_JS = ".js";

    public static final String AWARD_WBAI_SSQ_DOWNLOAD_URL_PREDIX = "http://kaijiang.500.com/";

    //下载中奖地区地址
    public static final String AWARD_AREA_CWL_DOWNLOAD_URL = "http://www.cwl.gov" +
            ".cn/cwl_admin/kjxx/findDrawNotice?name=ssq&issueCount=1";
    public static final String AWARD_AREA_SPORT_LOTTERY_DOWNLOAD_URL = "http://www.lottery.gov.cn/kjdlt/";
    public static final String AWARD_AREA_SPORT_LOTTERY_DOWNLOAD_PAGE_URL = "http://www.lottery.gov" +
            ".cn/api/lottery_kj_detail_new.jspx?_ltype=4&_term=";
    public static final String SUFFIX_HTML = ".html";

    //500抓取数据
    public static final String GREP_500_URL_PREFIX = "http://kaijiang.500.com/shtml/";
    public static final String GREP_500_URL_SUFFIX = ".shtml";
    public static final String GREP_500_URL_SSQ = "ssq";
    public static final String GREP_500_URL_DLT = "dlt";
    public static final String GREP_500_URL_FC3D = "sd";

    public static final String NET_EASE_FC3D_EN = "3d";
    public static final String GREP_REMARK_TRY_NUM = "testNum";//试机号
    public static final String GREP_REMARK_TRY_SALE = "sale";//销量
    public static final String GREP_REMARK_TRY_POOL = "pool";//奖池

    public static final String DEFAULT_HEAD_IMG_URL = "https://ohduoklem.qnssl.com/headImg.png";
    public static final String DEFAULT_NICK_NAME_PART = "****";

    public static final String MOJIE_SPORTS_MATCH_DATA_URL = "https://sportsapi.mojieai.com/api";
//    public static final String MOJIE_SPORTS_MATCH_DATA_URL = "https://sports.caiqr.cn/api";

    //首页预测
    public static final Integer FREE_PREDICT_MAX_TIMS = 3;
    public static final Integer DEVICE_PREDICT_MAX_TIMES = 3;
    public static final Integer USERID_PREDICT_MAX_TIMES = 3;
    public static final Integer PREDICT_NUMS_FRONT_MAX = 5000;//预测号码前段最大值
    public static final Integer PREDICT_NUMS_BACK_MAX = 10000;//预测号码后段最大值
    public static final Integer PREDICT_GET_NUM_TIMES = 10;

    public static final Integer FESTIVAL_DELAY_DAY = 13;
    public static final Integer DEFAULT_DELAY_DAY = 5;

    public static final String SMS = "SMS";
    public static final String SMS_TYPE_LOGIN = "0";
    public static final String SMS_TYPE_FORGET_PASSWORD = "1";
    public static final String SMS_TYPE_BIND_BANK = "2";

    public static final String COMMON_YUAN_STR = "￥";

    public static final String CASH_MONETARY_UNIT_FEN = "分";
    public static final String CASH_MONETARY_UNIT_YUAN = "元";
    public static final String GOLD_COIN_MONETARY_UNIT = "金币";
    public static final String GOLD_WISDOM_COIN_MONETARY_UNIT = "智慧币";

    public static final String TASK_LIST_TYPE_SIGN = "0";
    public static final String TASK_LIST_TYPE_KILL = "1";
    public static final String TASK_LIST_TYPE_ENCIRCLE = "2";


    public static final String PASSWORD = "PASSWORD";

    //sign
    public static final String SIGN_STR = "moJieSignatureStr";
    public static final String SIGN_CODE = "moJieSignatureCod";

    public static final String WX_APP_ID = "wx9fea09e960df08c0";
    public static final String WX_APP_SECRET = "cbfee692c3ca210391df2708fdf8069d";

    // 交易
    public static final Integer PAY_TYPE_GOLD_COIN = 0;//金币
    public static final Integer PAY_TYPE_CASH = 1;//现金
    public static final Integer PAY_TYPE_WISDOM_COIN = 2;//智慧币
    public static final Integer PAY_TYPE_BALANCE = 3;//余额
    public static final Integer PAY_TYPE_COUPON = 4;//优惠券

    public static final Integer PAY_STATUS_START = 0;
    public static final Integer PAY_STATUS_FINISH = 1;
    public static final Integer PAY_STATUS_HANDLED = 2;
    public static final Integer PAY_OPERATE_TYPE_ADD = 0;
    public static final Integer PAY_OPERATE_TYPE_DEC = 1;

    public final static String[] levelName =
            {"一等奖", "二等奖", "三等奖", "四等奖", "五等奖", "六等奖"};
    public static final String DLT_EIGHT_AWARD_LEVEL_LAST_PEIROD_ID = "14051";

    public static final int AWARD_INFO_MAX_NUM = 100000;

    public static final int MAX_COMPRESS_TIME = 1000;//Gzip压缩和解压缩时间限制MS

    public static final BigDecimal BONUS_MAX_500_LIMIT = new BigDecimal(5000000);
    public static final BigDecimal BONUS_MAX_1000_LIMIT = new BigDecimal(10000000);
    public static final BigDecimal BONUS_10000_LIMIT = new BigDecimal(100000000);
    public static final BigDecimal BONUS_30000_LIMIT = new BigDecimal(300000000);

    public static final Map<Integer, Integer> TEST_AWARD_NUM_MAP = new HashMap<>();

    public static final Integer NO_ENCRYPT_VERSION = 1;
    public static final long NUMBER_BOOK_MAX_COUNT = 10000;
    public static final String WEI_XIN_SERVICE_CHARGE = "0.006";

    public static final String WISDOM_COIN_FLOW_ID_SQE = "WISDOMFLOW";
    public static final String CHARSET_UTF_8 = "utf-8";
    public static final String SIGN_TYPE_RSA2 = "RSA2";

    public static final Integer HAS_PERMISSION_NO = 0;//没有权限
    public static final Integer HAS_PERMISSION_YES = 1;//有权限

    public static final Integer VIP_DISCOUNT_WAY_DISCOUNT = 0;//优惠方式 打折
    public static final Integer VIP_DISCOUNT_WAY_LOW_PAY = 1;//优惠方式 独享优惠价

    public static final String KILL_PROGRAM_IOS_MALL_ID = "zhcpyc.kill.18yuan";
    public static final String KILL_PROGRAM_IOS_MALL_ID_VIP = "zhcpyc.kill.18yuan";

    public static final String PLATE_WEI_XIN_CODE = "zhihuicp";

    public static final Integer PUSH_CENTER_NOTICE_TYPE_DIGIT = 0;
    public static final Integer PUSH_CENTER_NOTICE_TYPE_DIGIT_SOCIAL = 1;
    public static final Integer PUSH_CENTER_NOTICE_TYPE_FOOTBALL_GOLD = 2;

    public static final Integer IF_PURCHASE_LOG_INIT = 0;
    public static final Integer IF_PURCHASE_LOG_ACCOUNT = 1;//已加入待取款
    public static final Integer IF_PURCHASE_LOG_REFUND = 2;//已经入等待退款

    public static final Integer IN_RANK_INIT = 0;//初始化
    public static final Integer IN_RANK_YES = 1;//进入排行榜
    public static final Integer IN_RANK_NOT_NEED = 2;//不用进入排行榜

    public static final Integer IOS_REVIEW_STATUS_PASSED = 1;
    public static final Integer IOS_REVIEW_STATUS_WAIT = 0;

    //提现状态
    public static final Integer WITHDRAW_STATUS_INIT = 0;
    public static final Integer WITHDRAW_STATUS_ACCEPT = 1;
    public static final Integer WITHDRAW_STATUS_FINISH = 2;
    public static final Integer WITHDRAW_STATUS_EXCEPTION = 3;
    public static final Integer WITHDRAW_STATUS_FAIL = 4;
    public static final Integer WITHDRAW_STATUS_WAIT_CONFIRM = 5;
    public static final Integer WITHDRAW_STATUS_CONFIRM_THROUGH = 6;

    //三方代付订单状态
    public static final Integer THREE_PARTY_WITHDRAW_ORDER_PROCESSING = 1;
    public static final Integer THREE_PARTY_WITHDRAW_ORDER_ACCEPT = 2;
    public static final Integer THREE_PARTY_WITHDRAW_ORDER_SUCCESS = 3;
    public static final Integer THREE_PARTY_WITHDRAW_ORDER_FAIL = 4;
    public static final Integer THREE_PARTY_WITHDRAW_ORDER_UNKNOWN = 5;

    public static final Integer BANK_CARD_TYPE_DEBIT = 1;
    public static final Integer BANK_CARD_TYPE_CREDIT = 2;
    public static final Integer BANK_CARD_TYPE_CREDIT_1 = 3;

    //会员专区支付类型
    public static final Integer USER_VIP_PROGRAM_PAY_TYPE_VIP = 0;
    public static final Integer USER_VIP_PROGRAM_PAY_TYPE_PAY = 1;

    public static final Integer VIP_PROGRAM_IS_RIGHT_INIT = 0;
    public static final Integer VIP_PROGRAM_IS_RIGHT_PART_OPEN_AWARD = 1;
    public static final Integer VIP_PROGRAM_IS_RIGHT_RIGHT = 2;
    public static final Integer VIP_PROGRAM_IS_RIGHT_LOSE = 3;

    public static final Integer VIP_PROGRAM_STATUS_INIT = 0;
    public static final Integer VIP_PROGRAM_STATUS_PARTY = 1;
    public static final Integer VIP_PROGRAM_STATUS_OPEN_AWARD = 2;
    public static final Integer VIP_PROGRAM_STATUS_RED = 3;

    public static final Integer VIP_PROGRAM_PROVILEGE_DISABLE = 0;
    public static final Integer VIP_PROGRAM_PROVILEGE_ENABLE = 1;
    public static final Integer VIP_PROGRAM_PROVILEGE_USEED = 2;

    // 财物对账
    public static Integer BILL_PRODUCT_TYPE_VIP = 0;
    public static Integer BILL_PRODUCT_TYPE_WISDOM = 1;
    public static Integer BILL_PRODUCT_TYPE_PROGRAM = 2;
    public static Integer BILL_PRODUCT_TYPE_SUBSCRIBE_KILL = 3;
    public static Integer BILL_PRODUCT_TYPE_RESONANCE_DATA = 4;
    public static Integer BILL_PRODUCT_TYPE_FOOTBALL_RECOMMEND = 5;
    public static Integer BILL_PRODUCT_TYPE_COUPON_CARD = 6;
    public static Integer BILL_PRODUCT_TYPE_VIP_PROGRAM = 7;
    public static Integer BILL_PRODUCT_TYPE_ROBOT_FOOTBALL_RECOMMEND = 8;
    public static Integer BILL_PRODUCT_TYPE_USER_FOOTBALL_RECOMMEND = 9;
    public static Integer BILL_PRODUCT_TYPE_USER_FOOTBALL_RECOMMEND_INCOME = 10;
    public static Integer BILL_PRODUCT_TYPE_DEFAULT_ACTIVITY_INCOME = 11;

    static {
        TEST_AWARD_NUM_MAP.put(0, 10000);
        TEST_AWARD_NUM_MAP.put(1, 10000);
        TEST_AWARD_NUM_MAP.put(2, 18621);
        TEST_AWARD_NUM_MAP.put(3, 10978);
        TEST_AWARD_NUM_MAP.put(4, 38760);
        TEST_AWARD_NUM_MAP.put(5, 8008);
    }

    public static final String APP_TITLE = "智慧彩票预测";

    public static final String RANDOM_CODE = "zhcpyc";

    public static final String VERSION_CODE = "versionCode";
    public static final String CLIENT_INFOS = "clientType";

    public static final Integer VERSION_CODE_2_3 = 13;
    public static final Integer VERSION_CODE_3_0 = 14;
    public static final Integer VERSION_CODE_3_2 = 16;
    public static final Integer VERSION_CODE_3_3 = 17;
    public static final Integer VERSION_CODE_3_4 = 18;
    public static final Integer VERSION_CODE_3_5 = 19;
    public static final Integer VERSION_CODE_3_6 = 20;
    public static final Integer VERSION_CODE_4_0 = 21;
    public static final Integer VERSION_CODE_4_0_1 = 22;
    public static final Integer VERSION_CODE_4_1 = 23;
    public static final Integer VERSION_CODE_4_2 = 24;
    public static final Integer VERSION_CODE_4_3 = 25;
    public static final Integer VERSION_CODE_4_4 = 26;
    public static final Integer VERSION_CODE_4_4_1 = 27;
    public static final Integer VERSION_CODE_4_6 = 29;
    public static final Integer VERSION_CODE_4_6_1 = 30;
    public static final Integer VERSION_CODE_4_6_3 = 33;
    public static final Integer VERSION_CODE_4_6_4 = 34;

    public static String getWinningNumberTxt(String periodId, String gameName) {
        return gameName + periodId + "期" + "开奖号：";
    }

    public static Integer DEFAULT_PAGE_SIZE = 20;
    //号码类型
    public static Integer USER_NUMBER_TYPE_SELF = 0;//自选
    public static String USER_NUMBER_TYPE_SELF_MSG = "自选";//自选
    public static Integer USER_NUMBER_TYPE_PREDICT = 1;//智慧一注
    public static String USER_NUMBER_TYPE_PREDICT_MSG = "智慧";//智慧一注
    //号码算奖状态
    public static Integer USER_NUMBER_IF_AWARD_YES = 1;//已算奖
    public static Integer USER_NUMBER_IF_AWARD_NO = 0;//未算奖
    public static Integer USER_NUMBER_BOOK_ENABLE = 1;
    public static Integer USER_NUMBER_BOOK_DISENABLE = 0;

    // 支付
    public static final String WX_QUERY_TYPE_QUERY = "query";
    public static final String WX_PAY_ENUM_NAME = "wxPay";
    public static final String ALI_PAY_ENUM_NAME = "aliPay";
    public static final String YOP_PAY_ENUM_NAME = "yopPay";
    public static final String JD_PAY_ENUM_NAME = "jdPay";
    public static final String WX_JSAPI_PAY_ENUM_NAME = "wxJsapiPay";
    public static final String HAO_DIAN_PAY_ENUM_NAME = "haoDianPay";
    public static final String WX_QUERY_TYPE_CLOSE = "close";

    public static final String APPLY_PAY_NAME = "苹果支付";
    public static final String WISDOM_COIN_PAY_NAME = "智慧币";
    public static final String ALI_PAY_NAME = "支付宝";

    public static final Integer WX_PAY_CHANNEL_ID = 1001;
    public static final Integer APPLE_PAY_CHANNEL_ID = 1002;
    public static final Integer WISDOM_COIN_CHANNEL_ID = 1003;
    public static final Integer ALI_PAY_CHANNEL_ID = 1004;
    public static final Integer YOP_PAY_CHANNEL_ID = 1006;
    public static final Integer JD_PAY_CHANNEL_ID = 1005;
    public static final Integer WX_PAY_CHANNEL_ID_1 = 1007;
    public static final Integer WX_PAY_CHANNEL_WX_JSAPI = 1008;
    public static final Integer HAO_DIAN_PAY_CHANNEL_ID = 1009;


    public static final String ACCESS_LAST_KILL_CODE = "lastKillCode";
    public static final String ACCESS_BIG_DATA = "bigData";

    public static final Integer USER_NUMBER_BOOK_PERIOD_COUNT = 11;

    public static final Integer NUMBER_BOOK_VIP_POP_TYPE = 0;//号码本
    public static final Integer NUMBER_BOOK_VIP_POP_TYPE_TREND = 1;//走势图

    public static final Integer CLIENT_TYPE_ANDRIOD = 1000;
    public static final Integer CLIENT_TYPE_IOS = 1001;
    public static final Integer CLIENT_TYPE_IOS_1 = 1021;
    public static final Integer CLIENT_TYPE_IOS_WISDOM_PREDICT = 1031;
    public static final Integer CLIENT_TYPE_IOS_WISDOM_ENTERPRISE = 1101;

    public static final Integer SOCIAL_INTEGERAL_CURRENT_LEVEL = 0;
    public static final Integer SOCIAL_INTEGERAL_NEXT_LEVEL = 1;

    public static final Integer SOCIAL_POP_UP_TYPE_REWARD = 0;
    public static final Integer SOCIAL_POP_UP_TYPE_ACHIEVE = 1;
    public static final Integer SOCIAL_POP_UP_TYPE_LEVEL = 2;
    public static final Integer SOCIAL_POP_UP_TYPE_MASTER = 2;

    public static final String SOCIAL_REWARD_POPUP_AWARD_TYPE_PERIOD = "period";
    public static final String SOCIAL_REWARD_POPUP_AWARD_TYPE_WEEK = "week";
    public static final String SOCIAL_REWARD_POPUP_AWARD_TYPE_MONTH = "month";

    public static final Integer PROGRAM_TYPE_15_5 = 0;//15红5蓝
    public static final Integer PROGRAM_TYPE_12_3 = 1;//12红3蓝
    public static final Integer PROGRAM_TYPE_9_3 = 2;//9红3蓝
    public static final Integer PROGRAM_TYPE_8_3 = 3;//8红3蓝

    public static final Integer PROGRAM_BUY_TYPE_LIMIT = 0;//限购
    public static final Integer PROGRAM_BUY_TYPE_COMPENSATE = 1;//不中包赔
    public static final Integer PROGRAM_BUY_TYPE_COMMON = 2;//普通

    public static final Integer PROGRAM_IS_RETURN_COIN_INIT = 0;//初始
    public static final Integer PROGRAM_IS_RETURN_COIN_WAIT = 1;//等待退款
    public static final Integer PROGRAM_IS_RETURN_COIN_NO = 2;//不需要赔付
    public static final Integer PROGRAM_IS_RETURN_COIN_YES = 3;//已赔付

    public static final Integer PROGRAM_IS_AWARD_NO = 0;//未中奖
    public static final Integer PROGRAM_IS_AWARD_YES = 1;//中奖
    public static final Integer PROGRAM_IS_AWARD_WAIT = 2;//待开奖

    public static final Integer PROGRAM_BUY_STATUS_NO_PURCHASE = 0;//0 未购买  1 已购买  2 已售完  3已过期
    public static final Integer PROGRAM_BUY_STATUS_PAYED = 1;
    public static final Integer PROGRAM_BUY_STATUS_SALE_END = 2;
    public static final Integer PROGRAM_BUY_STATUS_EXPORED = 3;

    public static final Integer PROGRAM_IS_PAY_NO = 0;
    public static final Integer PROGRAM_IS_PAY_YES = 1;//已支付

    public static final Integer FOOTBALL_PROGRAM_TYPE_FREE = 0;
    public static final Integer FOOTBALL_PROGRAM_TYPE_CASH = 1;

    public static final Integer FOOTBALL_PROGRAM_STATUS_NO_PAY = 0;
    public static final Integer FOOTBALL_PROGRAM_STATUS_PAYED = 1;

    public static final Integer FOOTBALL_SPF_ITEM_S = 3;
    public static final Integer FOOTBALL_SPF_ITEM_P = 1;
    public static final Integer FOOTBALL_SPF_ITEM_F = 0;

    public static final Integer FOOTBALL_RQ_SPF_ITEM_S = 3;
    public static final Integer FOOTBALL_RQ_SPF_ITEM_P = 1;
    public static final Integer FOOTBALL_RQ_SPF_ITEM_F = 0;

    public static final Integer FOOTBALL_RQ_ASIA_ITEM_S = 3;
    public static final Integer FOOTBALL_RQ_ASIA_ITEM_P = 1;
    public static final Integer FOOTBALL_RQ_ASIA_ITEM_F = 0;

    public static final Integer LOTTERY_CODE_FOOTBALL = 200;

    public static final Integer LOTTERY_TYPE_DIGIT = 0;
    public static final Integer LOTTERY_TYPE_SPORTS = 1;

    public static final Integer FOOTBALL_MATCH_STATUS_NOT_START = 0;
    public static final Integer FOOTBALL_MATCH_STATUS_PLAYING = 1;
    public static final Integer FOOTBALL_MATCH_STATUS_END = 2;
    public static final Integer FOOTBALL_MATCH_STATUS_CANCEL = 3;

    public static final Integer PUSH_TYPE_DIGIT_SOCIAL_KILL_NUM = 0;//杀号提醒
    public static final Integer PUSH_TYPE_DIGIT_SOCIAL_GOD_ENCIRCLE_NUM = 1;//大神围号提醒
    public static final Integer PUSH_TYPE_SPORTS_SOCIAL_GOD_RECOMMEND = 2;//大神推单提醒

    public static final Integer USER_SIGN_TYPE_DAILY = 0;
    public static final Integer USER_SIGN_TYPE_CYCLE = 1;
    public static final Integer USER_SIGN_TYPE_CYCLE_ACTIVITY = 2;

    public static final Integer PAYMENT_STATISTIC_FLAG_NO = 0;
    public static final Integer PAYMENT_STATISTIC_FLAG_YES = 1;

    public static final Integer ORDER_STATISTIC_FLAG_NO = 0;
    public static final Integer ORDER_STATISTIC_FLAG_YES = 1;

    public static final Integer DEVICEID_ALLOW_MAX_MOBILE_LOGIN = 10;

    public static final Integer RONG_SHU_ID_CHECK_PASS = 1000;//一致
    public static final Integer RONG_SHU_ID_CHECK_REFUSE = 1001;//不一致
    public static final Integer RONG_SHU_ID_CHECK_NOT_EXIST = 1002;//库无
    public static final Integer RONG_SHU_ID_CHECK_ERROR = 2005;//不匹配

    public static final Integer RONG_SHU_CHECK_USER = 0;
    public static final Integer RONG_SHU_CHECK_BANK = 1;

    public static final Integer BANK_AUTHENTICATE_RONG_SHU = 3001;

    public static final String PROGRAM_PURCHASE_CALL_BACK_METHOD = "userProgramServiceImpl" +
            ".callBackMakeProgramEffective";

    public static final String SUBSCRIBE_PURCHASE_CALL_BACK_METHOD = "userSubscribeInfoLogServiceImpl" +
            ".callBackMakeSubscribeEffective";

    public static final String OUT_TRADE_GOODS_NAME = "智慧彩票商品";

    public static final String MAIN_RECOMMEND_IMG_URL = "";
    public static final String MAIN_RECOMMEND_IMG_REDIO = "";

    public static final String SINGLE_RECOMMEND_IMG_URL = "https://ojhwh2s98.qnssl.com/%E5%8D%95%E9%80%89@3x.png";
    public static final String SINGLE_RECOMMEND_IMG_REDIO = "45:69";

    public static final String ANY_RECOMMEND_IMG_URL = "https://ojhwh2s98.qnssl.com/%E5%88%86%E6%9E%90@3x.png";
    public static final String ANY_RECOMMEND_IMG_REDIO = "45:69";

    public static final String SOCIAL_RESONANCE_DATA_CALL_BACK_METHOD = "userResonanceLogServiceImpl" +
            ".callBackMakeResonanceEffective";

    public static final String FOOTBALL_RECOMMEND_PROGRAM_CALL_BACK_METHOD = "userBuyRecommendServiceImpl" +
            ".callBackMakeUserFootballRecommendEffective";

    public static final String SPORT_VIP_PROGRAM_CALL_BACK_METHOD = "userVipProgramServiceImpl" +
            ".callBackMakeUserVipProgramEffective";

    //获取对账类型
    public static final String TRADEDAYDOWNLOAD = "tradedaydownload";
    public static final String TRADEMONTHDOWNLOAD = "trademonthdownload";
    public static final String REMITDAYDOWNLOAD = "remitdaydownload";

    public static Integer factorial(int base, int count) {
        Integer res = base;
        Integer divisor = 1;

        for (int i = 1; i < count; i++) {
            int temp = base - i;
            res = res * temp;
        }
        for (int i = 1; i <= count; i++) {
            divisor = divisor * i;
        }
        return res / divisor;
    }
}