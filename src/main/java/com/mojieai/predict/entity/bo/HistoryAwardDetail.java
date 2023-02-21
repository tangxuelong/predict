package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 历史开奖信息统计dto
 */
@Data
public class HistoryAwardDetail implements Serializable {
    private String awardTime; //开奖时间
    private long winningNumber; //开奖号码,位图法表示
    private String periodId; //期次
}
