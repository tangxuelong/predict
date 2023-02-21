package com.mojieai.predict.entity.dto;

import com.mojieai.predict.entity.po.GamePeriod;

import java.util.HashSet;
import java.util.Set;

/**
 * 快3和值开奖走势
 */
public class K3WinTrendChartDto {
    private String period_num;//期号

    private String period_name;//

    private String bonus_code;//中奖号码

    private String sum_val;//和值

    private String big_small;//大小

    private String dan_shuang;//单双

    private String forms;//形态

    public K3WinTrendChartDto(){}

    /**
     * 和值构造函数
     * @param period_num
     * @param period_name
     * @param bonus_code
     * @param sum_val
     * @param big_small
     * @param dan_shuang
     */
    public K3WinTrendChartDto(String period_num, String period_name,
                              String bonus_code, String sum_val,
                              String big_small, String dan_shuang) {
        this.period_num = period_num;
        this.period_name = period_name;
        this.bonus_code = bonus_code;
        this.sum_val = sum_val;
        this.big_small = big_small;
        this.dan_shuang = dan_shuang;
    }

    /**
     * 同号构造函数
     * @param period_num
     * @param period_name
     * @param bonus_code
     * @param forms
     */
    public K3WinTrendChartDto(String period_num, String period_name,
                              String bonus_code, String forms) {
        this.period_num = period_num;
        this.period_name = period_name;
        this.bonus_code = bonus_code;
        this.forms = forms;
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

    public String getBonus_code() {
        return bonus_code;
    }

    public void setBonus_code(String bonus_code) {
        this.bonus_code = bonus_code;
    }

    public String getSum_val() {
        return sum_val;
    }

    public void setSum_val(String sum_val) {
        this.sum_val = sum_val;
    }

    public String getBig_small() {
        return big_small;
    }

    public void setBig_small(String big_small) {
        this.big_small = big_small;
    }

    public String getDan_shuang() {
        return dan_shuang;
    }

    public void setDan_shuang(String dan_shuang) {
        this.dan_shuang = dan_shuang;
    }

    public String getForms() {
        return forms;
    }

    public void setForms(String forms) {
        this.forms = forms;
    }

    @Override
    public String toString() {
        return "K3WinTrendChartDto{" +
                "period_num='" + period_num + '\'' +
                ", period_name='" + period_name + '\'' +
                ", bonus_code='" + bonus_code + '\'' +
                ", sum_val='" + sum_val + '\'' +
                ", big_small='" + big_small + '\'' +
                ", dan_shuang='" + dan_shuang + '\'' +
                '}';
    }

    /**
     * 和值开奖走势
     * @param gamePeriod
     * @return
     */
    public static K3WinTrendChartDto convert2K3SumValWinTrendDto(GamePeriod gamePeriod){
        int periodLen = gamePeriod.getPeriodId().length();
        String period_name = gamePeriod.getPeriodId().substring(periodLen-2, periodLen) + "期";
        String [] winNums = gamePeriod.getWinningNumbers().split(" ");
        String bonus_code = gamePeriod.getWinningNumbers().replace(" ", ",");
        int sum = 0;
        String big_small = "大";
        String dan_shuang = "单";
        for(String num : winNums){
            sum += Integer.valueOf(num);
        }
        if(sum < 11) big_small = "小";
        if(sum%2==0) dan_shuang = "双";
        String sumVal = sum+"";
        return new K3WinTrendChartDto(gamePeriod.getPeriodId(), period_name, bonus_code,
                sumVal, big_small, dan_shuang);
    }

    /**
     * 同号不同号开奖走势
     * @param gamePeriod
     * @return
     */
    public static K3WinTrendChartDto convert2K3TongHaoWinTrendDto(GamePeriod gamePeriod){
        int periodLen = gamePeriod.getPeriodId().length();
        String period_name = gamePeriod.getPeriodId().substring(periodLen-2, periodLen) + "期";
        String [] winNums = gamePeriod.getWinningNumbers().split(" ");
        String bonus_code = gamePeriod.getWinningNumbers().replace(" ", ",");
        String forms = "三不同号";
        Set winNumSet = new HashSet();
        for(String num : winNums){
            winNumSet.add(num);
        }
        if(winNumSet.size() == 1) forms = "三同号";
        if(winNumSet.size() == 2) forms = "二同号";

        return new K3WinTrendChartDto(gamePeriod.getPeriodId(), period_name, bonus_code, forms);
    }

}
