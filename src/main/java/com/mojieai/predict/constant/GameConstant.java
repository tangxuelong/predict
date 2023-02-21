package com.mojieai.predict.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiwang
 */
public class GameConstant {
    public final static String SSQ = "ssq";
    public final static String DLT = "dlt";
    public final static String FC3D = "fc3d";
    public final static String SHKUAI3 = "shKuai3";
    public final static String HBD11 = "hbD11";
    public final static String JXD11 = "jxD11";
    public final static String SDD11 = "sdD11";
    public final static String GDD11 = "gdD11";
    public final static String XJD11 = "xjD11";
    public final static String HLJD11 = "hljD11";
    public final static String JXKUAI3 = "jxKuai3";
    public final static String SXD11 = "sxD11";

    public final static String LOTTERY_WIN_NUMS_URL = "http://lottery.caiqr.com/bet/";

    /*走势图前台参数*/
    public final static int TREND_LOTTERY_CLASS_SSQ = 0;
    public final static int TREND_LOTTERY_CLASS_DLT = 1;
    public final static int TREND_LOTTERY_CLASS_KUAI3 = 2;
    public final static int TREND_LOTTERY_CLASS_D11 = 3;

    public final static int TREND_PALY_TYPE_SSQ_COM = 1;
    public final static int TREND_PALY_TYPE_SSQ_DANTUO = 2;
    public final static int TREND_PALY_TYPE_DLT_COM = 1;
    public final static int TREND_PALY_TYPE_DLT_DANTUO = 2;

    public final static int TREND_CHART_TYPE_SSQ_AWARD = 0;
    public final static int TREND_CHART_TYPE_SSQ_RED = 1;
    public final static int TREND_CHART_TYPE_SSQ_BLUE = 2;
    public final static int TREND_CHART_TYPE_SSQ_RED_HOT = 3;
    public final static int TREND_CHART_TYPE_SSQ_BLUE_HOT = 4;
    public final static int TREND_CHART_TYPE_DLT_AWARD = 0;
    public final static int TREND_CHART_TYPE_DLT_FRONT = 1;
    public final static int TREND_CHART_TYPE_DLT_BACK = 2;
    public final static int TREND_CHART_TYPE_DLT_FORNT_HOT = 3;
    public final static int TREND_CHART_TYPE_DLT_BACK_HOT = 4;

    //预测号码四种不同时段
    public final static int PERIOD_TIME_AREA_TYPE_1 = 1;
    public final static int PERIOD_TIME_AREA_TYPE_2 = 2;//20:30~21:15
    public final static int PERIOD_TIME_AREA_TYPE_3 = 3;//过了21:15未预测时状态
    public final static int PERIOD_TIME_AREA_TYPE_4 = 4;

    /*public final static Map<String, String> gameEnTitleMap = new HashMap<>();

    static {
        gameEnTitleMap.put(SSQ, "每周二、四、日开奖");
    }*/

    public final static Map<String, String> PERIOD_NAME_MAP = new HashMap();

    static {
        PERIOD_NAME_MAP.put(SSQ, "双色球 ");
        PERIOD_NAME_MAP.put(DLT, "大乐透 ");
    }

}
