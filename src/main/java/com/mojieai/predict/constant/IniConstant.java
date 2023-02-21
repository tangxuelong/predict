package com.mojieai.predict.constant;

/**
 * Created by Ynght on 2017/1/12.
 */
public class IniConstant {
    public static final String REDIS_DEFAULT_CHARSET = "redisDefaultCharset";
    public static final String TOKEN_CACHE_TIME = "tokenCacheTime";
    public static final String SPRING_FESTIVAL_DAYS = "springFestivalDays";
    public static final String BANNER_COUNT = "bannerCount";
    public static final String DEFAULT_GAME_PERIOD_CRON = "defaultGamePeriodCron";
    public static final String WHITE_IP_LIST = "whiteIpList";
    public static final String REDIS_SUB_AND_PUB_ONLINE_CONFIRM_IP = "redisSubAndPubOnLineConfirmIp";
    public static final String CLUSTER_CRON_IP = "clusterCronIp";
    public static final String EXECUTOR_MONITOR_TIME_OUT = "executorMonitorTimeout";
    public static final String ONCE_SCHEDULER_PERIOD_SIZE = "onceSchedulerPeriodSize";
    public static final String TASK_THREAD_POOL_SIZE = "taskThreadPoolSize";
    public static final String PERIOD_DETAILS_EXPIRE_TIME = "periodDetailsExpireTime";
    public static final String PERIOD_EXPIRE = "periodExpire";
    public static final String PERIOD_THRESHOLD_MILLIS = "periodThresholdMillis";

    //大盘彩初始化加载awardInfo数量
    public static final String COMMON_GAME_INIT_LOAD_AWARD_INFO_COUNT = "commonGameInitLoadAwardInfoCount";
    public static final String COMMON_GAME_AWARD_INFO_DELAY = "commonGameAwardInfoDelay";

    public static final String NESHIKUAI_REQUEST_URL = "neshikuaiRequestUrl";
    public static final String HISTORY_AWARD_MAX_BET_NUMBER = "maxBetNumberHistoryAward";
    public static final String HISTORY_AWARD_DISPLAY_NUM = "historyAwardDisplayNum";
    /* redis时间*/
    public static final String SMS_EXPIRE_TIME = "smsExpireTime";
    public static final String SMS_VERIFY_TIMES = "smsVerifyTimes";
    public static final String PASSWORD_TIMES = "passwordTimes";
    public static final String MAX_TIMES_EXPIRE_TIME = "maxTimesExpireTime";
    public static final String DEFAULT_PUSH_GAMES = "defaultPushGames";

    /* 推送相关*/
    public static final String PUSH_APP_ID = "pushAppId";
    public static final String PUSH_APP_KEY = "pushAppKey";
    public static final String PUSH_MASTER_SECRET = "pushMasterSecret";
    public static final String PUSH_HOST = "pushHost";

    public static final String PUSH_APP_ID_IOS = "pushAppIdIos";
    public static final String PUSH_APP_KEY_IOS = "pushAppKeyIos";
    public static final String PUSH_MASTER_SECRET_IOS = "pushMasterSecretIos";
    public static final String PUSH_HOST_IOS = "pushHostIos";

    /* 短信 */
    public static final String SMS_USER = "smsUser";
    public static final String SMS_PASSWORD = "smsPassword";
    public static final String SMS_CHANNEL = "smsChannel";

    public static final String COMPATIBLE_SIGN_FLAG = "compatibleSignFlag";//兼容1.1无验签问题
    public static final String COMPATIBLE_SIGN_YES = "yes";
    public static final String COMPATIBLE_SIGN_NO = "no";

    public static final String UPGRADE_SIGN_FLAG = "updateGradeSignFlag"; //签名升级

    public static final String RANDOM_CODE = "randomCode";

    public static final String DING_TALK_URL = "dingTalkUrl";

    public static final String ALIYUN_PUSH_APPKEY = "aliyunpushkey";

    public static final String ALIYUN_PUSH_APP_KEY_IOS = "aliyunpushkeyios";
    public static final String ALIYUN_PUSH_APP_KEY_ANDROID = "aliyunpushkeyandroid";
    public static final String ALIYUN_PUSH_TYPE_NOTICE = "NOTICE";
    public static final String ALIYUN_PUSH_TYPE_MESSAGE = "MESSAGE";
    public static final String ALIYUN_PUSH_IOS_DEV = "aliyunpushiosdev";
    public static final String ALIYUN_PUSH_WINNING_SWITCH = "aliyunpushWinningSwitch";

    // 微信支付
    public static final String WX_PAY_APP_ID = "wxPayAppId";
    public static final String WX_PAY_MCH_ID = "wxPayMchId";
    public static final String WX_PAY_KEY = "wxPayKey";

    // 支付宝支付
    public static final String ALI_PAY_APP_ID = "aliPayAppId";
    public static final String ALI_PRIVATE_KEY = "aliPrivateKey";
    public static final String ALI_PUBLIC_KEY = "aliPublicKey";

    // 京东支付
    public static final String JD_PAY_MCH_ID = "jdPayMchId";
    public static final String JD_PRIVATE_KEY = "jdPrivateKey";
    public static final String JD_PRIVATE_DES_KEY = "jdPrivateDesKey";
    public static final String JD_NOTIFY_URL = "notifyUrl";

    //京东代付
    public static final String JD_DEFRAY_CONFIG = "jdDefrayConfig";
    // 微信公众号支付
    public static final String WX_JSAPI_PAY_APP_ID = "wxPayAppId";
    public static final String WX_JSAPI_PAY_MCH_ID = "wxPayMchId";
    public static final String WX_JSAPI_PAY_KEY = "wxPayKey";
    public static final String WX_JSAPI_SECRET = "secret";

    //好店支付
    public static final String HAO_DIAN_PAY_DS_ID = "dsId";
    public static final String HAO_DIAN_PAY_MCH_ID = "mchId";
    public static final String HAO_DIAN_PAY_MD5_KEY = "md5Key";
    public static final String HAO_DIAN_PAY_NOTIFY_URL = "notifyUrl";

    public static final String APPLE_PAY_VALIDATE_URL = "applePayUrl";

    public static final String QI_NIU_DOMAIN_NAME = "qiNiuDomainName";

    public static final String RONG_SHU_ID_CHECK = "rongShuIdCheck";


    /*public static final String PUSH_APP_ID = "KwTwpqqtk6AYPokdnrtVJA";
    public static final String PUSH_APP_KEY = "rkVJYhgPQJ7Ix1d3LSPye9";
    public static final String PUSH_MASTER_SECRET = "GlwNUy5EPj7f1dv8QzklT4";
    public static final String PUSH_HOST = "http://sdk.open.api.igexin.com/apiex.htm";*/

}
