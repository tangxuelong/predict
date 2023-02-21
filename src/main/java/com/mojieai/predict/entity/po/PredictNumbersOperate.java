package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PredictNumbersOperate {
    private long gameId;
    private String periodId;
    private byte[] operateNums;
    private String ruleStr;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
}
