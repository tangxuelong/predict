package com.mojieai.predict.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Singal
 */
public class TrendConstant {
    public static final String KEY_TREND_PERIOD = "periodList";
    public static final String KEY_TREND_STATISTICS = "statisticsList";
    public static final String KEY_TREND_STATISTICS_TEMP = "statisticsListTemp";
    public static final String KEY_TREND_OPEN_AWARD_TEMP = "openAwardTemp";
    public static final String KEY_TREND_PERIOD_NUM = "periodNum";
    public static final String KEY_TREND_PERIOD_NAME = "periodName";
    public static final String KEY_TREND_RED_BALL = "redBall";
    public static final String KEY_TREND_BLUE_BALL = "blueBall";
    public static final String KEY_TREND_FRONT_AREA = "frontArea";
    public static final String KEY_TREND_BACK_AREA = "backArea";
    public static final String KEY_TREND_OMIT_NUM = "omitNum";
    public static final String KEY_TREND_CODE_NUM = "codeNum";
    public static final String KEY_TREND_STAT_NAME = "statName";

    public static final String CHART_TYPE_RED = "1";
    public static final String CHART_TYPE_BLUE = "2";
    //统计
    public static final String TREND_STATISTICS_APPEAR = "出现次数";
    public static final String TREND_STATISTICS_AVERAGE_OMIT = "平均遗漏";
    public static final String TREND_STATISTICS_MAX_OMIT = "最大遗漏";
    public static final String TREND_STATISTICS_MAX_CONTINUOUS = "最大连出";

    public static final String TREND_CONTINUE_NUMBERS_DIRECTION_LEFT_UP = "leftUp"; //左上
    public static final String TREND_CONTINUE_NUMBERS_DIRECTION_RIGHT_UP = "rightUp"; //右上
    public static final String TREND_CONTINUE_NUMBERS_DIRECTION_UP = "up"; //上
    public static final String TREND_CONTINUE_NUMBERS_DIRECTION_DOWN = "down"; //下
    public static final String TREND_CONTINUE_NUMBERS_DIRECTION_LEFT_DOWN = "leftDown"; //左下
    public static final String TREND_CONTINUE_NUMBERS_DIRECTION_RIGHT_DOWN = "rightDown"; //右下

    public static final String TREND_COLD_HEAT_NUMBERS_HEAT = "热号";
    public static final String TREND_COLD_HEAT_NUMBERS_WARM = "温号";
    public static final String TREND_COLD_HEAT_NUMBERS_COLD = "冷号";

    public static final String TREND_COLOR_RED_VAL = "#ff5050";//红色
    public static final String TREND_COLOR_ORANGE_VAL = "#ffa850";//橙色
    public static final String TREND_COLOR_GREEN_VAL = "#4596ff";//绿色

    public static final Integer TREND_CHART_SPECIAL_TEMP = -1;
    public static final Integer TREND_CHART_SPECIAL_CONSEUTIVE_NUMBERS = -2;
    public static final Integer TREND_CHART_SPECIAL_NOT_SHOW = -3;


    public static final String TREND_TYPE_JIOU = "TREND_TYPE_JIOU"; //奇偶
    public static final String TREND_TYPE_BIG_SMALL = "TREND_TYPE_BIG_SMALL"; //大小
    public static final String TREND_TYPE_PRIME = "TREND_TYPE_PRIME"; //质合
    public static final String TREND_TYPE_ZERO_ONE_TWO = "TREND_TYPE_ZERO_ONE_TWO"; //012
    public static final String TREND_TYPE_AC_VALUE = "TREND_TYPE_AC_VALUE"; //AC_VALUE
    public static final String TREND_TYPE_SPAN_VALUE = "TREND_TYPE_SPAN_VALUE"; //012

    public static final String TREND_COLUMN_RATIO_PREFIX = "RATIO_";

    public static final String FC3D_WIN_NUM_TYPE_3_SAME = "豹子";
    public static final String FC3D_WIN_NUM_TYPE_2_SAME = "组三";
    public static final String FC3D_WIN_NUM_TYPE_NO_SAME = "组六";

    public static final Integer FC3D_BASE_TREND_FORM_TYPE_NONE = 0;
    public static final Integer FC3D_BASE_TREND_FORM_TYPE_CIRCLE = 1;
    public static final Integer FC3D_BASE_TREND_FORM_TYPE_SQURE = 2;

    public static final Map<String, String> TREND_TYPE_MAP = new HashMap<>();

    static {
        TREND_TYPE_MAP.put("TREND_TYPE_JIOU_FIRST", "ODD");
        TREND_TYPE_MAP.put("TREND_TYPE_JIOU_SECOND", "EVEN");
        TREND_TYPE_MAP.put("TREND_TYPE_BIG_SMALL_FIRST", "BIG");
        TREND_TYPE_MAP.put("TREND_TYPE_BIG_SMALL_SECOND", "SMALL");
        TREND_TYPE_MAP.put("TREND_TYPE_PRIME_FIRST", "PRIME");
        TREND_TYPE_MAP.put("TREND_TYPE_PRIME_SECOND", "COMPOSITE");
        TREND_TYPE_MAP.put("TREND_TYPE_ZERO_ONE_TWO_FIRST", "ROUTE_0");
        TREND_TYPE_MAP.put("TREND_TYPE_ZERO_ONE_TWO_SECOND", "ROUTE_1");
        TREND_TYPE_MAP.put("TREND_TYPE_ZERO_ONE_TWO_THIRD", "ROUTE_2");
        TREND_TYPE_MAP.put("TREND_TYPE_AC_VALUE_FIRST", "ODD");
        TREND_TYPE_MAP.put("TREND_TYPE_AC_VALUE_SECOND", "EVEN");
        TREND_TYPE_MAP.put("TREND_TYPE_AC_VALUE_THIRD", "PRIME");
        TREND_TYPE_MAP.put("TREND_TYPE_AC_VALUE_FOURTH", "COMPOSITE");
        TREND_TYPE_MAP.put("TREND_TYPE_SPAN_VALUE_FIRST", "ODD");
        TREND_TYPE_MAP.put("TREND_TYPE_SPAN_VALUE_SECOND", "EVEN");
        TREND_TYPE_MAP.put("TREND_TYPE_SPAN_VALUE_THIRD", "PRIME");
        TREND_TYPE_MAP.put("TREND_TYPE_SPAN_VALUE_FOURTH", "COMPOSITE");
    }
}