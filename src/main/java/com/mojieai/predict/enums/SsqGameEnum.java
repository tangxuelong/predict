package com.mojieai.predict.enums;

import java.util.LinkedHashSet;
import java.util.Set;

public enum SsqGameEnum {
    RED("TYPE_RED"), BLUE("TYPE_BLUE");

    private String extra;

    SsqGameEnum(String extra) {
        this.extra = extra;
    }

    public static final int RED_BALL_NUM = 6; // 每注红球的数量
    public static final int BLUE_BALL_NUM = 1; // 每注蓝球的数量

    public static final Set<String> SSQ_RED_NUMBERS = new LinkedHashSet<>();
    public static final Set<String> SSQ_BLUE_NUMBERS = new LinkedHashSet<>();

    static {
        SSQ_RED_NUMBERS.add("01");
        SSQ_RED_NUMBERS.add("02");
        SSQ_RED_NUMBERS.add("03");
        SSQ_RED_NUMBERS.add("04");
        SSQ_RED_NUMBERS.add("05");
        SSQ_RED_NUMBERS.add("06");
        SSQ_RED_NUMBERS.add("07");
        SSQ_RED_NUMBERS.add("08");
        SSQ_RED_NUMBERS.add("09");
        SSQ_RED_NUMBERS.add("10");
        SSQ_RED_NUMBERS.add("11");
        SSQ_RED_NUMBERS.add("12");
        SSQ_RED_NUMBERS.add("13");
        SSQ_RED_NUMBERS.add("14");
        SSQ_RED_NUMBERS.add("15");
        SSQ_RED_NUMBERS.add("16");
        SSQ_RED_NUMBERS.add("17");
        SSQ_RED_NUMBERS.add("18");
        SSQ_RED_NUMBERS.add("19");
        SSQ_RED_NUMBERS.add("20");
        SSQ_RED_NUMBERS.add("21");
        SSQ_RED_NUMBERS.add("22");
        SSQ_RED_NUMBERS.add("23");
        SSQ_RED_NUMBERS.add("24");
        SSQ_RED_NUMBERS.add("25");
        SSQ_RED_NUMBERS.add("26");
        SSQ_RED_NUMBERS.add("27");
        SSQ_RED_NUMBERS.add("28");
        SSQ_RED_NUMBERS.add("29");
        SSQ_RED_NUMBERS.add("30");
        SSQ_RED_NUMBERS.add("31");
        SSQ_RED_NUMBERS.add("32");
        SSQ_RED_NUMBERS.add("33");

        SSQ_BLUE_NUMBERS.add("01");
        SSQ_BLUE_NUMBERS.add("02");
        SSQ_BLUE_NUMBERS.add("03");
        SSQ_BLUE_NUMBERS.add("04");
        SSQ_BLUE_NUMBERS.add("05");
        SSQ_BLUE_NUMBERS.add("06");
        SSQ_BLUE_NUMBERS.add("07");
        SSQ_BLUE_NUMBERS.add("08");
        SSQ_BLUE_NUMBERS.add("09");
        SSQ_BLUE_NUMBERS.add("10");
        SSQ_BLUE_NUMBERS.add("11");
        SSQ_BLUE_NUMBERS.add("12");
        SSQ_BLUE_NUMBERS.add("13");
        SSQ_BLUE_NUMBERS.add("14");
        SSQ_BLUE_NUMBERS.add("15");
        SSQ_BLUE_NUMBERS.add("16");
    }

    // 各级的中奖规则，每行为奖级、红球数、篮球数
    public static int[][] LEVEL_INFO = {
            {1, 6, 1},
            {2, 6, 0},
            {3, 5, 1},
            {4, 5, 0},
            {4, 4, 1},
            {5, 4, 0},
            {5, 3, 1},
            {6, 2, 1},
            {6, 1, 1},
            {6, 0, 1}
    };
}