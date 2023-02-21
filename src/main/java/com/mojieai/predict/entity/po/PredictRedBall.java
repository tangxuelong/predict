package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PredictRedBall {
    private long gameId;
    private String periodId;
    private Integer strType;
    private String numStr;
    private Timestamp createTime;
    private Timestamp updateTime;

    public PredictRedBall(long gameId, String periodId, int strType, String numStr){
        this.gameId = gameId;
        this.periodId = periodId;
        this.strType = strType;
        this.numStr = numStr;
    }
}
