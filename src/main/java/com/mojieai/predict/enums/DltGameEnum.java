package com.mojieai.predict.enums;

import java.util.LinkedHashSet;
import java.util.Set;

public enum DltGameEnum {
    FRONT("FRONT"), BACK("BACK");

    private String extra;

    DltGameEnum(String extra) {
        this.extra = extra;
    }

    public String getExtra() {
        return extra;
    }

    public static final int FRONT_BALL_NUM = 5; // 每注前区的数量
    public static final int BACK_BALL_NUM = 2; // 每注后区的数量

    public static final Set<String> DLT_FRONT_NUMBERS = new LinkedHashSet<>();
    public static final Set<String> DLT_BACK_NUMBERS = new LinkedHashSet<>();

    static {
        DLT_FRONT_NUMBERS.add("01");
        DLT_FRONT_NUMBERS.add("02");
        DLT_FRONT_NUMBERS.add("03");
        DLT_FRONT_NUMBERS.add("04");
        DLT_FRONT_NUMBERS.add("05");
        DLT_FRONT_NUMBERS.add("06");
        DLT_FRONT_NUMBERS.add("07");
        DLT_FRONT_NUMBERS.add("08");
        DLT_FRONT_NUMBERS.add("09");
        DLT_FRONT_NUMBERS.add("10");
        DLT_FRONT_NUMBERS.add("11");
        DLT_FRONT_NUMBERS.add("12");
        DLT_FRONT_NUMBERS.add("13");
        DLT_FRONT_NUMBERS.add("14");
        DLT_FRONT_NUMBERS.add("15");
        DLT_FRONT_NUMBERS.add("16");
        DLT_FRONT_NUMBERS.add("17");
        DLT_FRONT_NUMBERS.add("18");
        DLT_FRONT_NUMBERS.add("19");
        DLT_FRONT_NUMBERS.add("20");
        DLT_FRONT_NUMBERS.add("21");
        DLT_FRONT_NUMBERS.add("22");
        DLT_FRONT_NUMBERS.add("23");
        DLT_FRONT_NUMBERS.add("24");
        DLT_FRONT_NUMBERS.add("25");
        DLT_FRONT_NUMBERS.add("26");
        DLT_FRONT_NUMBERS.add("27");
        DLT_FRONT_NUMBERS.add("28");
        DLT_FRONT_NUMBERS.add("29");
        DLT_FRONT_NUMBERS.add("30");
        DLT_FRONT_NUMBERS.add("31");
        DLT_FRONT_NUMBERS.add("32");
        DLT_FRONT_NUMBERS.add("33");
        DLT_FRONT_NUMBERS.add("34");
        DLT_FRONT_NUMBERS.add("35");

        DLT_BACK_NUMBERS.add("01");
        DLT_BACK_NUMBERS.add("02");
        DLT_BACK_NUMBERS.add("03");
        DLT_BACK_NUMBERS.add("04");
        DLT_BACK_NUMBERS.add("05");
        DLT_BACK_NUMBERS.add("06");
        DLT_BACK_NUMBERS.add("07");
        DLT_BACK_NUMBERS.add("08");
        DLT_BACK_NUMBERS.add("09");
        DLT_BACK_NUMBERS.add("10");
        DLT_BACK_NUMBERS.add("11");
        DLT_BACK_NUMBERS.add("12");
    }

    // 各级的中奖规则，每行为奖级、红球数、篮球数
    public static int[][] LEVEL_INFO = {
            {1, 5, 2},
            {2, 5, 1},
            {3, 5, 0},
            {3, 4, 2},
            {4, 4, 1},
            {4, 3, 2},
            {5, 4, 0},
            {5, 3, 1},
            {5, 2, 2},
            {6, 3, 0},
            {6, 2, 1},
            {6, 1, 2},
            {6, 0, 2}
    };

    public static int[][] LEVEL_INFO_OLD = {
            {1, 5, 2},
            {2, 5, 1},
            {3, 5, 0},
            {4, 4, 2},
            {5, 4, 1},
            {6, 4, 0},
            {6, 3, 2},
            {7, 3, 1},
            {7, 2, 2},
            {8, 3, 0},
            {8, 2, 1},
            {8, 1, 2},
            {8, 0, 2}
    };
}