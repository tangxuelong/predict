package com.mojieai.predict.enums.trend;

/**
 * Created by Singal
 */
public enum TrendPeriodEnum {
    PERIOD1000(1000), PERIOD500(500), PERIOD200(200), PERIOD100(100), PERIOD50(50), PERIOD30(30);

    private int num;

    TrendPeriodEnum(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public int getDefaultNum() {
        return PERIOD100.getNum();
    }

    public static TrendPeriodEnum getTrendPeriodEnumByNum(int num) {
        for (TrendPeriodEnum tpe : TrendPeriodEnum.values()) {
            if (tpe.getNum() == num) {
                return tpe;
            }
        }
        return null;
    }
}