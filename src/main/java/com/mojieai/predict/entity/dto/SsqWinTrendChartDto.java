package com.mojieai.predict.entity.dto;

import com.mojieai.predict.entity.po.GamePeriod;

/**
 *
 * 红球中奖走势图
 */
public class SsqWinTrendChartDto {
    private String period_num; //期号

    private String period_name;//展示名称

    private String red_ball;//红球中奖号

    private String blue_ball;//篮球中奖号

    public SsqWinTrendChartDto(){}

    public SsqWinTrendChartDto(String period_num, String period_name, String red_ball, String blue_ball){
        this.period_num = period_num;
        this.period_name = period_name;
        this.red_ball = red_ball;
        this.blue_ball = blue_ball;
    }

    /**
     * 将期次信息转为Dto
     * @param gamePeriod
     * @return
     */
    public static SsqWinTrendChartDto convert2SsqWinTrendDto(GamePeriod gamePeriod){
        int periodLen = gamePeriod.getPeriodId().length();
        String period_name = gamePeriod.getPeriodId().substring(periodLen-3, periodLen) + "期";
        String [] winNums = gamePeriod.getWinningNumbers().split(":");
        String red_ball = winNums[0].replace(" ", ",");

        return new SsqWinTrendChartDto(gamePeriod.getPeriodId(),
                period_name, red_ball, winNums[1]);
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

    public String getRed_ball() {
        return red_ball;
    }

    public void setRed_ball(String red_ball) {
        this.red_ball = red_ball;
    }

    public String getBlue_ball() {
        return blue_ball;
    }

    public void setBlue_ball(String blue_ball) {
        this.blue_ball = blue_ball;
    }

    @Override
    public String toString() {
        return "SsqWinTrendChartDto{" +
                "period_num='" + period_num + '\'' +
                ", period_name='" + period_name + '\'' +
                ", red_ball='" + red_ball + '\'' +
                ", blue_ball='" + blue_ball + '\'' +
                '}';
    }
}
