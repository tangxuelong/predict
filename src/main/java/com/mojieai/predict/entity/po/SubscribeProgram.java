package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class SubscribeProgram implements Serializable {
    private static final long serialVersionUID = -8236981892880373895L;

    private Integer programId;
    private long gameId;
    private String programName;
    private Integer programType;
    private Integer predictType;
    private Integer subscribeNum;
    private Long amount;
    private Long vipAmount;
    private Integer vipDiscount;
    private Integer payType;
    private Integer buyType;
    private Timestamp updateTime;
}
