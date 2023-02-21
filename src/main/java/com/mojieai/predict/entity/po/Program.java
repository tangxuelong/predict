package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 活动奖级配置表
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class Program {
    private String programId;
    private Long gameId;
    private String periodId;
    private String redNumber;
    private String blueNumber;
    private String wisdomScore;
    private Integer programType;
    private Integer buyType;
    private Long price;
    private Long vipPrice;
    private Integer vipDiscount;
    private Integer saleCount;
    private Integer totalCount;
    private Integer isAward;
    private Integer refundStatus;
    private String iosMallId;
    private String vipIosMallId;
    private Timestamp createTime;
    private Timestamp updateTime;
}