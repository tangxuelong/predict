package com.mojieai.predict.entity.vo;

import com.mojieai.predict.entity.po.ActivityProgram;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 活动方案号码表
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class ActivityProgramVo {
    private Integer programId;
    private String periodId;
    private String lotteryNumber;
    private String numberType;
    private Timestamp startTime;
    private Integer awardCount;
    private Integer leftCount;
    private Timestamp lastAwardTime;
    private String remark;
    private Integer isSingleFlag; //添加字段 是否是单式
    private String firstNumber; //添加字段 第一行显示
    private String secondNumber; //添加字段 第二行显示
    private Integer status; //添加字段 状态 按钮状态
    private String statusText; //添加字段 状态 按钮状态
    private Timestamp createTime;
    private Timestamp updateTime;

    public ActivityProgramVo(ActivityProgram activityProgram) {
        this.programId = activityProgram.getProgramId();
        this.periodId = activityProgram.getPeriodId();
        this.lotteryNumber = activityProgram.getLotteryNumber();
        this.numberType = activityProgram.getNumberType();
        this.startTime = activityProgram.getStartTime();
        this.awardCount = activityProgram.getAwardCount();
        this.leftCount = activityProgram.getLeftCount();
        this.lastAwardTime = activityProgram.getLastAwardTime();
        this.remark = activityProgram.getRemark();
        this.isSingleFlag = 1;
        this.createTime = activityProgram.getCreateTime();
        this.updateTime = activityProgram.getUpdateTime();
    }

}
