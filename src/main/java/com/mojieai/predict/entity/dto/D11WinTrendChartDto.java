package com.mojieai.predict.entity.dto;

import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.D11GameEnum;

import java.util.HashSet;
import java.util.Set;

public class D11WinTrendChartDto {
    private String period_num;

    private String period_name;

    private String bonus_code;//

    private String sum_val;//和值

    private String spans;//跨度

    private String repeat_num;//重复数

    public D11WinTrendChartDto(){}

    public D11WinTrendChartDto(String period_num, String period_name,
                               String bonus_code, String sum_val, String spans,
                               String repeat_num) {
        this.period_num = period_num;
        this.period_name = period_name;
        this.bonus_code = bonus_code;
        this.sum_val = sum_val;
        this.spans = spans;
        this.repeat_num = repeat_num;
    }

    public static D11WinTrendChartDto convert2D11WinTrendDto(GamePeriod gamePeriod, GamePeriod oldGamePeriod, String type){
        int periodLen = gamePeriod.getPeriodId().length();
        String period_name = gamePeriod.getPeriodId().substring(periodLen-2, periodLen) + "期";
        String [] winNums = gamePeriod.getWinningNumbers().split(" ");
        String [] oldWinNums;
        int sum = 0;
        String bonus_code = gamePeriod.getWinningNumbers().replace(" ", ",");
        int firstNum = Integer.valueOf(winNums[0]);
        int secondNum = Integer.valueOf(winNums[1]);
        int thirdNum = Integer.valueOf(winNums[2]);
        int max_num = 0;
        int min_num = 0;
        int repeat_num = 0;

        if(oldGamePeriod != null){
//            oldGamePeriod = getLastPeriod(gameId, gamePeriod.getPeriodId());//
        }
        oldWinNums = oldGamePeriod.getWinningNumbers().split(" ");
        //
        if (type == D11GameEnum.QIAN_2_ZHIXUAN.getExtra()){
            sum = firstNum + secondNum;
            max_num = Math.max(firstNum, secondNum);
            min_num = Math.min(firstNum, secondNum);

        }else if(type == D11GameEnum.QIAN_3_ZHIXUAN.getExtra()){
            sum = firstNum + secondNum + thirdNum;
            max_num = Math.max(Math.max(firstNum, secondNum), thirdNum);
            min_num = Math.min(Math.min(firstNum, secondNum), thirdNum);

        }else{
            for(String winCode : winNums){
                sum += Integer.valueOf(winCode);
                max_num = Math.max(Integer.valueOf(winCode),max_num);
                min_num = Math.min(Integer.valueOf(winCode),min_num);
            }
        }
        String sum_val = sum +"";
        String span = (max_num - min_num) + "";
        repeat_num = analyRepeatNum(winNums, oldWinNums);
        String repeatNumStr= repeat_num + "";
        return new D11WinTrendChartDto(gamePeriod.getPeriodId().toString(), period_name,
                bonus_code, sum_val, span, repeatNumStr);
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

    public String getSpans() {
        return spans;
    }

    public void setSpans(String spans) {
        this.spans = spans;
    }

    public String getRepeat_num() {
        return repeat_num;
    }

    public void setRepeat_num(String repeat_num) {
        this.repeat_num = repeat_num;
    }

    @Override
    public String toString() {
        return "D11WinTrendChartDto{" +
                "period_num='" + period_num + '\'' +
                ", period_name='" + period_name + '\'' +
                ", bonus_code='" + bonus_code + '\'' +
                ", sum_val='" + sum_val + '\'' +
                ", spans='" + spans + '\'' +
                ", repeat_num='" + repeat_num + '\'' +
                '}';
    }

    public static int analyRepeatNum(String [] arr1, String [] arr2){
        int repeatNum = 0;
        Set arrSet = new HashSet();
        for(String temp : arr1){
            arrSet.add(temp);
        }
        for(String temp : arr2){
            arrSet.add(temp);
        }
        repeatNum = arr2.length + arr1.length - arrSet.size();
        return repeatNum;
    }
}
