package com.mojieai.predict.constant;

public class SportsProgramConstant {

    public static final Integer PROGRAM_LOG_WITHDRAW_STATUS_WAIT = 0;//提现金额等待转入现金账户
    public static final Integer PROGRAM_LOG_WITHDRAW_STATUS_FINISH = 1;//转入成功
    public static final Integer PROGRAM_LOG_WITHDRAW_STATUS_QUIT = 2;//取消

    public static final Integer SPORT_WITHDRAW_MAX_OCCUPY_RATIO = 70;
    public static final Integer SPORT_WITHDRAW_DEFAULT_OCCUPY_RATIO = 70;

    public static final Integer LOTTERY_LOTTERY_CODE_FOOTBALL = 200;//足球
    public static final Integer LOTTERY_LOTTERY_CODE_BASKETBALL = 300;

    public static final Integer FOOTBALL_PLAY_TYPE_ALL = 3;
    //赛事玩法类型
    public static final Integer FOOTBALL_PLAY_TYPE_SPF = 0;
    public static final Integer FOOTBALL_PLAY_TYPE_RQSPF = 1;
    public static final Integer FOOTBALL_PLAY_TYPE_ASIA = 2;
    public static final Integer FOOTBALL_PLAY_TYPE_SCORE = 4;
    public static final Integer FOOTBALL_PLAY_TYPE_GOAL = 5;
    public static final Integer FOOTBALL_PLAY_TYPE_BQC = 6;

    public static final Integer SPORT_SOCIAL_RANK_PLAY_TYPE_MULTIPLE = 3;

    //是否为用户推荐项
    public static final Integer MATCH_COMPETITION_ITEM_RECOMMEND_NO = 0;
    public static final Integer MATCH_COMPETITION_ITEM_RECOMMEND_YES = 1;

    //选项是否命中
    public static final Integer MATCH_COMPETITION_ITEM_BET_RESULT_NO = 0;
    public static final Integer MATCH_COMPETITION_ITEM_BET_RESULT_YES = 1;

    public static final Integer SPORT_RECOMMEND_TYPE_FREE = 0;
    public static final Integer SPORT_RECOMMEND_TYPE_PAY = 1;

    //赛事状态
    public static final Integer SPORT_MATCH_STATUS_INIT = 0;//未开赛
    public static final Integer SPORT_MATCH_STATUS_GOING = 1;//进行中
    public static final Integer SPORT_MATCH_STATUS_END = 2;//已结束
    public static final Integer SPORT_MATCH_STATUS_QUIT = 3;//取消
    public static final Integer SPORT_MATCH_STATUS_DELAY = 4;//比赛延期
    public static final Integer SPORT_MATCH_STATUS_MIDFIELD = 5;//中场 （休息）

    //推荐列表type
    public static final Integer SPORT_RECOMMEND_LIST_FREE = 0;//免费
    public static final Integer SPORT_RECOMMEND_LIST_HOT = 1;//热门
    public static final Integer SPORT_RECOMMEND_LIST_FOLLOW = 2;//关注

    //排行榜类型
    public static final Integer SPORT_SOCIAL_RANK_TYPE_PROFIT = 0;//收益榜
    public static final Integer SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM = 1;//命中榜
    public static final Integer SPORT_SOCIAL_RANK_TYPE_CONTINUE = 2;//连中榜

    //用户推荐方案状态
    public static final Integer RECOMMEND_STATUS_INIT = 0;//等待开奖
    public static final Integer RECOMMEND_STATUS_LOST = 1;//未中
    public static final Integer RECOMMEND_STATUS_WINNING = 2;//已中
    public static final Integer RECOMMEND_STATUS_GOES = 3;//走
    public static final Integer RECOMMEND_STATUS_CANCEL = 4;//取消

    //足彩
    public static final Integer MATCH_JUMP_TYPE_PREDICT = 0;
    public static final Integer MATCH_JUMP_TYPE_MATCH = 1;
    //联赛标签
    public static final Integer MATCH_TAG_WISDOM_RECOMMEND = 1;//智慧推荐
    public static final Integer MATCH_TAG_PREMIER_LEAGUE = 2;//英超
    public static final Integer MATCH_TAG_SERIE_A = 3;//意甲
    public static final Integer MATCH_TAG_BUNDESLIGA = 4;//德甲
    public static final Integer MATCH_TAG_LA_LIGA = 5;//西甲
    public static final Integer MATCH_TAG_LIGUE_1 = 6;//法甲
    public static final Integer MATCH_TAG_FIFA_WORLD_CUP = 7;//世界杯
    public static final Integer MATCH_TAG_EUROPEAN_NATIONS_CUP = 8;//欧洲杯
    public static final Integer MATCH_TAG_EUROPEAN_CHAMPION_CLUBS_CUP = 9;//欧冠
    public static final Integer MATCH_TAG_ALL_MATCH = 10;//全部
    public static final Integer MATCH_TAG_UEFA = 11;//欧罗巴
    public static final Integer MATCH_TAG_SPORTTERY = 12;//竞彩
    public static final Integer MATCH_TAG_USER_FOLLOW = 999;//用户关注

    public static final Integer MATCH_TAG_OPERATE_TYPE_DESC = 0;
    public static final Integer MATCH_TAG_OPERATE_TYPE_ADD = 1;


    public static final String DEFAULT_TEAM_IMG = "http://sportsimg.mojieai.com/default_team_head_img.png";
    public static final String DEFAULT_OTHER_PLATE_TEAM_IMG = "http://sportsimg.mojieai.com/default_mojieai_team_icon.png";

    public static final String SPORTTERY_SINGLE_CN = "竞彩单关";
    //赛事底层页
    public static final Integer MATCH_BOTTOM_PAGE_PREDICT = 0;//预测
    public static final Integer MATCH_BOTTOM_PAGE_BASE_DATA = 1;//基本面
    public static final Integer MATCH_BOTTOM_PAGE_ODDS = 2;//赔率

    public static final Integer SPROT_MATCH_USER_FOLLOW_STATUS_NO = 0;
    public static final Integer SPROT_MATCH_USER_FOLLOW_STATUS_YES = 1;
    public static final Integer SPROT_MATCH_USER_FOLLOW_STATUS_STOP = 2;
}
