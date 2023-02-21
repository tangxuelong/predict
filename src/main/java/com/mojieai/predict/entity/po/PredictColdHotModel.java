package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PredictColdHotModel implements Serializable {
    private static final long serialVersionUID = 7473446599964004742L;

    private long gameId;
    private String periodId;
    private Integer periodCount;
    private Integer numType;
    private String nums;
    private Timestamp createTime;

    public PredictColdHotModel(long gameId, String periodId, Integer periodCount, Integer numType, String nums) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.periodCount = periodCount;
        this.nums = nums;
        this.numType = numType;
    }
}
