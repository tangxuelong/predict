package com.mojieai.predict.entity.dto;

/**
 * 基本走势的数据返回模版
 */
public class TrendChartRtnDto {
    private String period_num;

    private String period_name;

    private int [] red_ball;

    /*红球初始化*/
    public TrendChartRtnDto(String period_num, String period_name, int[] red_ball) {
        this.period_num = period_num;
        this.period_name = period_name;
        this.red_ball = red_ball;
    }

    public String getPeriod_num() {
        return period_num;
    }

    public void setPeriod_num(String period_num) {
        this.period_num = period_num;
    }

    public String getPeriod_name() {
        return period_name;
    }

    public void setPeriod_name(String period_name) {
        this.period_name = period_name;
    }

    public int[] getRed_ball() {
        return red_ball;
    }

    public void setRed_ball(int[] red_ball) {
        this.red_ball = red_ball;
    }

}
