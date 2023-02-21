package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.sql.Timestamp;

/**
 * 活动方案号码表
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class ActivityProgram {
    private Integer programId;
    private String periodId;
    private String lotteryNumber;
    private String numberType;
    private Timestamp startTime;
    private Integer awardCount;
    private Integer leftCount;
    private Timestamp lastAwardTime;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public ActivityProgram(String periodId, String lotteryNumber, String numberType, Timestamp startTime, Integer
            leftCount, Integer awardCount) {
        this.periodId = periodId;
        this.lotteryNumber = lotteryNumber;
        this.numberType = numberType;
        this.startTime = startTime;
        this.leftCount = leftCount;
        this.awardCount = awardCount;
    }

}
