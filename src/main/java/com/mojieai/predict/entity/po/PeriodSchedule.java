package com.mojieai.predict.entity.po;

import com.mojieai.predict.enums.CommonStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by Singal
 */
@Data
@NoArgsConstructor
public class PeriodSchedule {
    private Long gameId;
    private String periodId;
    private Integer ifAward;
    private Timestamp awardTime;
    private Integer ifAwardInfo;
    private Timestamp awardInfoTime;
    private Integer ifAwardArea;
    private Timestamp awardAreaTime;
    private Integer ifTrendCache;
    private Timestamp cacheTime;
    private Integer ifTrendDB;
    private Timestamp dbTime;
    private Timestamp createTime;
    private Timestamp updateTime;

    public PeriodSchedule(Long gameId, String periodId, Timestamp createTime) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.ifAward = CommonStatusEnum.NO.getStatus();
        this.ifAwardInfo = CommonStatusEnum.NO.getStatus();
        this.ifTrendCache = CommonStatusEnum.NO.getStatus();
        this.ifTrendDB = CommonStatusEnum.NO.getStatus();
        this.createTime = createTime;
        this.ifAwardArea = CommonStatusEnum.NO.getStatus();
    }
}
