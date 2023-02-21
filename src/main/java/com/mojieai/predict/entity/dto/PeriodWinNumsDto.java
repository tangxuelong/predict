package com.mojieai.predict.entity.dto;


import lombok.Data;

@Data
public class PeriodWinNumsDto {
    private String periodId;
    private String winningNumbers;
    private int awardStatus;
    private String awardTime;//期次开奖时间
}
