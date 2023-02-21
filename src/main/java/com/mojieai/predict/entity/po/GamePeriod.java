package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by Singal
 */
@Data
@NoArgsConstructor
public class GamePeriod implements java.io.Serializable {
    private Long gameId;
    private String periodId;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp awardTime;
    private String winningNumbers;
    private String remark;
    private Timestamp openTime;
    private Timestamp createTime;
    private Timestamp updateTime;

    public GamePeriod(Long gameId) {
        this.gameId = gameId;
    }

    public GamePeriod(Long gameId, String periodId, Timestamp startTime, Timestamp endTime, Timestamp
            awardTime, Timestamp createTime) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.awardTime = awardTime;
        this.createTime = createTime;
    }
}