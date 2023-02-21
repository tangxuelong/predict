package com.mojieai.predict.enums;

import com.mojieai.predict.constant.WordConstant;
import com.mojieai.predict.entity.po.AwardInfo;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Singal
 */
public enum Kuai3GameEnum {
    HEZHI(WordConstant.HEZHI), SAME_3_ALL("SAME_3_ALL"), SAME_3_SINGLE("SAME_3_SINGLE"), DIFF_3("DIFF_3"),
    ABC_3_ALL("ABC_3_ALL"), SAME_2_ALL("SAME_2_ALL"), SAME_2_SINGLE("SAME_2_SINGLE"), DIFF_2("DIFF_2");

    public static final Set<String> basicNumberSet = new LinkedHashSet<>();
    public static final Set<String> heZhiSet = new LinkedHashSet<>();
    public static final Set<String> same3AllSet = new LinkedHashSet<>();
    public static final Set<String> same3SingleSet = new LinkedHashSet<>();
    public static final Set<String> abc3AllSet = new LinkedHashSet<>();
    public static final Set<String> same2AllSet = new LinkedHashSet<>();
    public static final Set<String> same2SingleSet = new LinkedHashSet<>();
    public final static List<AwardInfo> KUAI3_AWARD_INFO_LIST = new ArrayList<>();
    public final static Map<String, String> extraCnMap = new HashMap<>();
    public final static List<String> KUAI3_PLAY_TYPE_BONUS = new ArrayList<>();

    public final static Integer SHKUAI3_DAILY_PERIOD = 82;
    public final static Integer JXKUAI3_DAILY_PERIOD = 84;
    public final static String SHKUAI3_INIT_PERIOD_FORMAT = "-1,22:28:00,08:58:00,08:58:20_08:58:00," +
            "09:08:00,09:08:20_yyyyMMdd,000";
    public final static String JXKUAI3_INIT_PERIOD_FORMAT = "-1,22:52:03,09:04:49,09:04:49_09:04:49," +
            "09:14:47,09:14:47_yyyyMMdd,000";
    public final static Integer SHKUAI3_TIME_INTERVAL = 600;
    public final static Integer JXKUAI3_TIME_INTERVAL = 598;


    static {
        extraCnMap.put(WordConstant.HEZHI, "和值");
        extraCnMap.put(SAME_3_ALL.getExtra(), "三同号通选");
        extraCnMap.put(SAME_3_SINGLE.getExtra(), "三同号单选");
        extraCnMap.put(DIFF_3.getExtra(), "三不同号");
        extraCnMap.put(ABC_3_ALL.getExtra(), "三连号通选");
        extraCnMap.put(SAME_2_ALL.getExtra(), "二同号复选");
        extraCnMap.put(SAME_2_SINGLE.getExtra(), "二同号单选");
        extraCnMap.put(DIFF_2.getExtra(), "二不同号");

        //基本选项
        basicNumberSet.add("1");
        basicNumberSet.add("2");
        basicNumberSet.add("3");
        basicNumberSet.add("4");
        basicNumberSet.add("5");
        basicNumberSet.add("6");
        //和值所有投注选项
        heZhiSet.add("3");
        heZhiSet.add("4");
        heZhiSet.add("5");
        heZhiSet.add("6");
        heZhiSet.add("7");
        heZhiSet.add("8");
        heZhiSet.add("9");
        heZhiSet.add("10");
        heZhiSet.add("11");
        heZhiSet.add("12");
        heZhiSet.add("13");
        heZhiSet.add("14");
        heZhiSet.add("15");
        heZhiSet.add("16");
        heZhiSet.add("17");
        heZhiSet.add("18");
        //三同号通选
        same3AllSet.add("SAME_3_ALL");
        //三同号单选
        same3SingleSet.add("1 1 1");
        same3SingleSet.add("2 2 2");
        same3SingleSet.add("3 3 3");
        same3SingleSet.add("4 4 4");
        same3SingleSet.add("5 5 5");
        same3SingleSet.add("6 6 6");

        //"三连号通选"
        abc3AllSet.add("ABC_3_ALL");
        //"二同号复选"
        same2AllSet.add("11*");
        same2AllSet.add("22*");
        same2AllSet.add("33*");
        same2AllSet.add("44*");
        same2AllSet.add("55*");
        same2AllSet.add("66*");
        //二同号单选
        same2SingleSet.add("11");
        same2SingleSet.add("22");
        same2SingleSet.add("33");
        same2SingleSet.add("44");
        same2SingleSet.add("55");
        same2SingleSet.add("66");
        //一等奖到21等奖 快3
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("1", "和值4", new BigDecimal(80)));//和值4
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("2", "和值5", new BigDecimal(40)));//和值5
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("3", "和值6", new BigDecimal(25)));//和值6
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("4", "和值7", new BigDecimal(16)));//和值7
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("5", "和值8", new BigDecimal(12)));//和值8
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("6", "和值9", new BigDecimal(10)));//和值9
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("7", "和值10", new BigDecimal(9)));//和值10
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("8", "和值11", new BigDecimal(9)));//和值11
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("9", "和值12", new BigDecimal(10)));//和值12
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("10", "和值13", new BigDecimal(12)));//和值13
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("11", "和值14", new BigDecimal(16)));//和值14
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("12", "和值15", new BigDecimal(25)));//和值15
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("13", "和值16", new BigDecimal(40)));//和值16
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("14", "和值17", new BigDecimal(80)));//和值17
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("15", "三同号通选", new BigDecimal(40)));//三同号通选
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("16", "三同号单选", new BigDecimal(240)));//三同号单选
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("17", "三不同号", new BigDecimal(40)));//三不同号
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("18", "三连号通选", new BigDecimal(10)));//三连号通选
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("19", "二同号复选", new BigDecimal(15)));//二同号复选
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("20", "二同号单选", new BigDecimal(80)));//二同号单选
        KUAI3_AWARD_INFO_LIST.add(new AwardInfo("21", "二不同号", new BigDecimal(8)));//二不同号

        //玩法对应的奖金
        KUAI3_PLAY_TYPE_BONUS.add("和值,9-240");
        KUAI3_PLAY_TYPE_BONUS.add("三同号通选,40");
        KUAI3_PLAY_TYPE_BONUS.add("三同号单选,240");
        KUAI3_PLAY_TYPE_BONUS.add("三不同号,40");
        KUAI3_PLAY_TYPE_BONUS.add("三连号通选,10");
        KUAI3_PLAY_TYPE_BONUS.add("二同号复选,15");
        KUAI3_PLAY_TYPE_BONUS.add("二同号单选,80");
        KUAI3_PLAY_TYPE_BONUS.add("二不同号,8");
    }

    private String extra;

    Kuai3GameEnum(String extra) {
        this.extra = extra;
    }

    public String getExtra() {
        return extra;
    }
}