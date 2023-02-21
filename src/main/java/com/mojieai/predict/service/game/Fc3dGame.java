package com.mojieai.predict.service.game;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Fc3dGame extends AbstractGame {

    private static final String FC3D_BEGIN_TIME = "20:00:00";
    private static final List<AwardInfo> FC3D_AWARD_INFO_LIST = new ArrayList<>();
    public static final List<Integer> FC3D_OMIT_WIN_NUM_COLOR_LIST = new ArrayList<>();//福彩基本走势图中奖颜色
    public static final List<Integer> FC3D_OMIT_WIN_NUM_CONSECUTE_COLOR_LIST = new ArrayList<>();//福彩基本走势图中奖连号颜色
    public static final List<String> FC3D_BASE_TREND_TITLE = Arrays.asList(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "0:3", "1:2", "2:1", "3:0",
            "0:3", "1:2", "2:1", "3:0",
            "0:3", "1:2", "2:1", "3:0"
    );
    //福彩基本走势图title

    //0:没有底色 1:红色(百位) 2:百位深红 3:十位黄色 4:十位深黄 5:个位红色 6:个位深红 7:文字颜色
    public static final Integer BASE_COLOR_NO = 0;
    public static final Integer BASE_COLOR_HUNDRED_RED = 1;
    public static final Integer BASE_COLOR_HUNDRED_DEEP_RED = 2;
    public static final Integer BASE_COLOR_TEN_YELLOW = 3;
    public static final Integer BASE_COLOR_TEN_DEEP_YELLOW = 4;
    public static final Integer BASE_COLOR_ONE_RED = 5;
    public static final Integer BASE_COLOR_ONE_DEEP_RED = 6;
    public static final Integer BASE_COLOR_TEXT_RED = 7;

    private static final String PUSH_URL_WINNING_NUMBER = "mjLottery://mjNative?page=kjlb&gameName=福彩3D&gameEn=fc3d";

    static {
        FC3D_AWARD_INFO_LIST.add(new AwardInfo("1", "单选", new BigDecimal(1040)));
        FC3D_AWARD_INFO_LIST.add(new AwardInfo("2", "组三", new BigDecimal(346)));
        FC3D_AWARD_INFO_LIST.add(new AwardInfo("3", "组六", new BigDecimal(173)));

        addList(10, BASE_COLOR_HUNDRED_RED, BASE_COLOR_HUNDRED_DEEP_RED);//百位
        addList(10, BASE_COLOR_TEN_YELLOW, BASE_COLOR_TEN_DEEP_YELLOW);//十位
        addList(10, BASE_COLOR_ONE_RED, BASE_COLOR_ONE_DEEP_RED);//个位
        addList(10, BASE_COLOR_HUNDRED_RED, BASE_COLOR_HUNDRED_DEEP_RED);//不分位
        addList(4, BASE_COLOR_TEN_YELLOW, BASE_COLOR_TEN_DEEP_YELLOW);//奇偶比
        addList(4, BASE_COLOR_ONE_RED, BASE_COLOR_ONE_DEEP_RED);//大小
        addList(4, BASE_COLOR_TEN_YELLOW, BASE_COLOR_TEN_DEEP_YELLOW);//质合比
    }

    private static void addList(int count, int baseColor, int consecuteColor) {
        for (int i = 0; i < count; i++) {
            FC3D_OMIT_WIN_NUM_COLOR_LIST.add(baseColor);
            FC3D_OMIT_WIN_NUM_CONSECUTE_COLOR_LIST.add(consecuteColor);
        }
    }

    @Override
    public Game getGame() {
        return GameEnum.FC3D.getGame();
    }

    /* 期次格式*/
    @Override
    public String getPeriodDateFormat() {
        return DateUtil.DATE_FORMAT_YYYY;
    }

    @Override
    public String getWinningNumberPushUrl() {
        return PUSH_URL_WINNING_NUMBER;
    }

    @Override
    public String[] getAllRedNums() {
        return new String[0];
    }

    @Override
    protected int getWinningNumberLength() {
        return 3;
    }

    @Override
    protected String getWinningNumberRegexp() {
        return "((0[1-9]|1\\d|2\\d|3[0-5]) ){4}(0[1-9]|1\\d|2\\d|3[0-5]):((0[1-9]|1[0-2]) (0[1-9]|1[0-2]))";
    }

    @Override
    public Integer getDailyPeriod() {
        return 1;
    }

    @Override
    public Timestamp getOfficialStartTime(GamePeriod gamePeriod) {
        return getOfficialStartTime(gamePeriod, FC3D_BEGIN_TIME);
    }

    @Override
    public List<AwardInfo> getDefaultAwardInfoList() {
        return FC3D_AWARD_INFO_LIST;
    }

    @Override
    public int[] analyseBidAwardLevels(String bidBalls, GamePeriod period) {
        return new int[0];
    }

    /* 号码用｜隔开  1,2,3:1,2:1,3*/
    @Override
    public Integer checkLotteryNumberTypeIfValid(String lotteryNumber) {
        lotteryNumber = lotteryNumber.trim();
        if (StringUtils.isBlank(lotteryNumber) || !lotteryNumber.contains(CommonConstant.COMMON_COLON_STR)) {
            return ResultConstant.ERROR;
        }
        String[] numArr = lotteryNumber.split(CommonConstant.COMMON_COLON_STR);
        if (numArr.length < 3) {
            return ResultConstant.ERROR;
        }
        int sum = 1;
        for (String num : numArr) {
            String[] tempArr = num.split(CommonConstant.COMMA_SPLIT_STR);
            sum *= tempArr.length;
        }
        if (sum != 1) {
            return ResultConstant.LOTTERY_NUMBER_TYPE_MULTIPLE;
        }
        return ResultConstant.LOTTERY_NUMBER_TYPE_SINGLE;

    }
}
