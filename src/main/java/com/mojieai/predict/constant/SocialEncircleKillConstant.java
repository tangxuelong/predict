package com.mojieai.predict.constant;

import java.util.HashMap;
import java.util.Map;

public class SocialEncircleKillConstant {

    public static final Map<Integer, Map<String, Integer>> SOCIAL_LEVEL = new HashMap<>();


    public static final Integer ACHIEVE_POP_AWARD_TYPE_WEEK_ENCIRCLE = 0;
    public static final Integer ACHIEVE_POP_AWARD_TYPE_WEEK_KILL = 1;
    public static final Integer ACHIEVE_POP_AWARD_TYPE_MONTH_ENCIRCLE = 2;
    public static final Integer ACHIEVE_POP_AWARD_TYPE_MONTH_KILL = 3;
    //圈号类型
    public static Integer ENCIRCLE_CODE_TYPE_RED = 0;//围红球

    //杀号类型
    public static Integer SOCIAL_KILL_CODE_TYPE_RED = 0;//杀红球

    //社交类型
    public static Integer SOCIAL_OPERATE_NUM_ENCIRCLE_RED = 0;//圈号
    public static Integer SOCIAL_OPERATE_NUM_KILL_RED = 1;//杀号

    //用户社区积分类型
    public static Integer SOCIAL_ENCIRCLE_RED_INTEGRAL_TYPE = 0;//圈号积分
    public static Integer SOCIAL_KILL_RED_INTEGRAL_TYPE = 1;//杀号积分
    public static Integer SOCIAL_NEW_USER_INTEGRAL_TYPE = 66;//新用户积分

    public static Integer SOCIAL_ENCIRCLE_MAX_COUNT = 2;
//    public static Integer SOCIAL_ENCIRCLE_TASK_MAX_COUNT = 1;

    //杀号状态
    public static Integer SOCIAL_KILL_NUM_STATUS_INI = 0;//未参与
    public static Integer SOCIAL_KILL_NUM_STATUS_TAKEPART = 1;//已参与
    public static Integer SOCIAL_KILL_NUM_STATUS_END = 2;//已结束
    public static Integer SOCIAL_KILL_NUM_STATUS_NOT_RIGHT = 3;//未中
    public static Integer SOCIAL_KILL_NUM_STATUS_ALL_RIGHT = 4;//全中

    //圈号状态
    public static Integer SOCIAL_ENCIRCLE_STATUS_ENABLE = 0;//可以圈号
    public static Integer SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD = 1;//开奖中
    public static Integer SOCIAL_ENCIRCLE_STATUS_END = 2;//已结束
    public static Integer SOCIAL_ENCIRCLE_STATUS_FUTURE = 3;//未开始

    //杀号中奖状态
    public static Integer SOCIAL_KILL_NUM_AWARD_STATUS_WAIT_OPEN = 0;//待开奖
    public static Integer SOCIAL_KILL_NUM_AWARD_STATUS_WIN = 1;//全中
    public static Integer SOCIAL_KILL_NUM_AWARD_STATUS_LOSS = 2;//未中

    public static Integer SOCIAL_KILL_NUM_MAX_COUNT = 10;//杀号次数
    public static Long SOCIAL_ENCIRCLE_REDIS_EXPIRE_BUFFER_TIME = 30L;
    public static Integer SOCIAL_ENCIRCLE_REDIS_EXPIRE_BUFFER_TIME_INTEGER = 30;

    public static Integer SOCIAL_KILL_NUM_LIST_PAGE_SIZE = 50;
    public static Integer SOCIAL_MY_KILL_NUM_LIST_PAGE_SIZE = 10;
    public static Integer SOCIAL_MY_KILL_NUM_LIST_PERIOD_SIZE = 6;
    public static Integer SOCIAL_MY_ENCIRCLE_LIST_PERIOD_SIZE = 5;
    public static Integer SOCIAL_OTHER_KILL_NUM_LIST_PERIOD_SIZE = 7;
    public static Integer SOCIAL_CLASSIC_ENCIRCLE_LIST_PERIOD_SIZE = 6;


    public static final Integer USER_SOCIAL_INTEGRAL_DETAIL_PAGE_SIZE = 5;//用户等级积分明细

    public static Integer SOCIAL_MYENCIRCLE_LIST_TYPE_ENCIRCLE = 0;//围号列表
    public static Integer SOCIAL_MYENCIRCLE_LIST_TYPE_KILL = 1;//杀号列表

    //圈号失败文案
    public static String SOCIAL_ADD_ENCIRCLE_MSG_SUCC = "可到“我的围号”里查看";
    public static String SOCIAL_ADD_ENCIRCLE_MSG_PERIOD_ERR = "本期围号已结束，请等待开奖";
    public static String SOCIAL_ADD_ENCIRCLE_MSG_REENCIRCLE_ERR = "请重新围号";
    public static String SOCIAL_ADD_ENCIRCLE_MSG_PERIOD_UESED_ERR = "本期2次围号已用完";
    public static String SOCIAL_ADD_ENCIRCLE_SUCC_TITLE = "发布成功";
    public static String SOCIAL_ADD_ENCIRCLE_ERROR_TITLE = "发布失败";
    public static Integer SOCIAL_ADD_ENCIRCLE_SUCC_FLAG = 1;//圈号成功表示
    public static Integer SOCIAL_ADD_ENCIRCLE_ERROR_FLAG = 0;//圈号失败表示

    //杀号开始期次
    public static String SOCIAL_ENCIRCLE_BEGIN_PERIODID_SSQ = "2017128";// TODO: 17/11/14  线上是2017129 测试是2017120
    public static String SOCIAL_ENCIRCLE_BEGIN_PERIODID_DLT = "18013";// TODO: 17/11/14  线上是2017129 测试是18013

    //成就类型围号是已1开头杀号是以2开头
    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_10_6 = 10;//围10中6
    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_5_5 = 11;
    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_15_6 = 12;
    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_5_4 = 13;
    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_10_5 = 14;
    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_20_6 = 15;

    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_1 = 20;
    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_3 = 21;
    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_5 = 22;
    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_8 = 23;
    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_10 = 24;

    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_10 = 25;
    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_9 = 26;
    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_8 = 27;
    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_7 = 28;
    public static Integer SOCIAL_KILL_ACHIEVEMENT_KILL_NEW_6 = 29;

    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_15_5 = 30;
    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_10_4 = 31;
    public static Integer SOCIAL_ENCIRCLE_ACHIEVEMENT_20_5 = 32;

    //用户围号杀号分割前缀
    public static String INDEX_SOCIAL_SPLIT_PREFIX = "indexSocialSplit";

    public static Integer SOCIAL_ACHIEVEMENT_HIGHLIGHT_YES = 1;
    public static Integer SOCIAL_ACHIEVEMENT_HIGHLIGHT_NO = 0;

    //排行榜
    public static Integer SOCIAL_KILL_NUM_RANK_PAGE_SIZE = 100;
    public static Integer SOCIAL_ENCIRCLE_RANK_NUM_PAGE_SIZE = 100;

    //是否可以关注

    public static Integer SOCIAL_FOLLOW_STATUS_NO = 0;
    public static Integer SOCIAL_FOLLOW_STATUS_YES = 1;

    public static Integer SOCIAL_FOLLOW_PAGE_SIZE = 10;

    //热门号码
    public static Integer SOCIAL_HOT_ENCIRCLE_TYPE = 1;
    public static Integer SOCIAL_GENERATE_ENCIRCLE_TYPE = 0;

    //社区大数据
    public static Integer SOCIAL_BIG_DATA_HOT_ENCIRCLE_NUMBERS = 0;//热门围号
    public static Integer SOCIAL_BIG_DATA_HOT_KILL_NUMBERS = 1;//热门杀号

    public static String SOCIAL_BIG_DATA_STATISTIC_BEFOR_TIME = "before";
    public static String SOCIAL_BIG_DATA_STATISTIC_CURRENT_TIME = "current";
    public static String SOCIAL_BIG_DATA_STATISTIC_AFTER_TIME = "after";//暂时没有写这块逻辑

    //机器人状态
    public static Integer SOCIAL_ROBOT_ENABLE = 1;
    public static Integer SOCIAL_ROBOT_DISENABLE = 0;
    public static String SOCIAL_ROBOT_PASSWORD = "123456";

    public static Integer SOCIAL_ROBOT_TYPE_DIGIT = 0;
    public static Integer SOCIAL_ROBOT_TYPE_SPORT = 1;
    public static Integer SOCIAL_ROBOT_TYPE_CELEBRITY = 2;

    //个人杀号中心号吗展示类型
    public static Integer SOCIAL_PERSON_KILL_LIST_CODE_TYPE = 0;//数字号吗
    public static Integer SOCIAL_PERSON_KILL_LIST_TEXT_TYPE = 1;//文字

    //杀号后奖励任务
//    public static final Integer SOCIAL_TASK_AWARD_TYPE_ENCIRCLE = 0;//圈号任务
//    public static final Integer SOCIAL_TASK_AWARD_TYPE_KILL = 1;//杀号任务

    public static final Integer SOCIAL_TASK_IS_AWARD_INIT = 0;
    public static final Integer SOCIAL_TASK_IS_AWARD_WAIT = 1;
    public static final Integer SOCIAL_TASK_IS_AWARD_YES = 2;

    //圈号杀号智慧＋1奖励发放
    public static final Integer SOCIAL_ENCIRCLE_KILL_IS_DISTRIBUTE_NO = 0;
    public static final Integer SOCIAL_ENCIRCLE_KILL_IS_DISTRIBUTE_YES = 1;

    //用户头衔类型
    public static final Integer USER_TITLE_IMG_TYPE_GOD_ENCIRCLE = 0;
    public static final Integer USER_TITLE_IMG_TYPE_GOD_KILL = 1;
    public static final Integer USER_TITLE_IMG_TYPE_ACHIEVE_ENCIRCLE = 2;
    public static final Integer USER_TITLE_IMG_TYPE_ACHIEVE_KILL = 3;

    public static final String USER_SOCIAL_LEVEL_1 = "http://sportsimg.mojieai.com/user_social_level_1_shixi.png";


    static {
        Map<String, Integer> bound1 = new HashMap<>();
        bound1.put("begin", 6000);
        bound1.put("end", 8000);
        Map<String, Integer> bound2 = new HashMap<>();
        bound2.put("begin", 8000);
        bound2.put("end", 10000);
        Map<String, Integer> bound3 = new HashMap<>();
        bound3.put("begin", 10000);
        bound3.put("end", 12000);
        Map<String, Integer> bound4 = new HashMap<>();
        bound4.put("begin", 10000);
        bound4.put("end", 12000);
        Map<String, Integer> bound5 = new HashMap<>();
        bound5.put("begin", 12000);
        bound5.put("end", 14000);
        Map<String, Integer> bound6 = new HashMap<>();
        bound6.put("begin", 12000);
        bound6.put("end", 14000);
        Map<String, Integer> bound7 = new HashMap<>();
        bound7.put("begin", 12000);
        bound7.put("end", 14000);
        Map<String, Integer> bound8 = new HashMap<>();
        bound8.put("begin", 6000);
        bound8.put("end", 8000);
        Map<String, Integer> bound9 = new HashMap<>();
        bound9.put("begin", 3000);
        bound9.put("end", 4000);
        Map<String, Integer> bound10 = new HashMap<>();
        bound10.put("begin", 2000);
        bound10.put("end", 2200);
        Map<String, Integer> bound11 = new HashMap<>();
        bound11.put("begin", 500);
        bound11.put("end", 600);
        Map<String, Integer> bound12 = new HashMap<>();
        bound12.put("begin", 200);
        bound12.put("end", 250);
        Map<String, Integer> bound13 = new HashMap<>();
        bound13.put("begin", 100);
        bound13.put("end", 110);
        Map<String, Integer> bound14 = new HashMap<>();
        bound14.put("begin", 50);
        bound14.put("end", 55);
        Map<String, Integer> bound15 = new HashMap<>();
        bound15.put("begin", 20);
        bound15.put("end", 25);

        SOCIAL_LEVEL.put(1, bound1);
        SOCIAL_LEVEL.put(2, bound2);
        SOCIAL_LEVEL.put(3, bound3);
        SOCIAL_LEVEL.put(4, bound4);
        SOCIAL_LEVEL.put(5, bound5);
        SOCIAL_LEVEL.put(6, bound6);
        SOCIAL_LEVEL.put(7, bound7);
        SOCIAL_LEVEL.put(8, bound8);
        SOCIAL_LEVEL.put(9, bound9);
        SOCIAL_LEVEL.put(10, bound10);
        SOCIAL_LEVEL.put(11, bound11);
        SOCIAL_LEVEL.put(12, bound12);
        SOCIAL_LEVEL.put(13, bound13);
        SOCIAL_LEVEL.put(14, bound14);
        SOCIAL_LEVEL.put(15, bound15);
    }

}
