package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PredictNumbers {
    private long gameId;
    private String periodId;
    private byte[] predictNumbers;
    private Timestamp createTime;
    private BigDecimal historyAwardSum;
    private String historyAwardLevelSum;
    private String awardLevel;
}
