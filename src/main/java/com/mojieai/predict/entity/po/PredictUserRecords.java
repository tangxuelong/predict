package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PredictUserRecords {
    private String recordId;
    private Long gameId;
    private String periodId;
    private Long userId;
    private String numStr;
    private String timeSpan;
    private Integer predictType;
    private Integer isAward;
    private Timestamp createTime;
    private Timestamp updateTime;

    public PredictUserRecords(String recordId, Long gameId, String periodId, Long userId, String numStr, String
            timeSpan, Integer predictType) {
        this.recordId = recordId;
        this.gameId = gameId;
        this.periodId = periodId;
        this.userId = userId;
        this.numStr = numStr;
        this.timeSpan = timeSpan;
        this.predictType = predictType;
    }
}
