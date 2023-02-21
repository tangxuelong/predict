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
public class ActivityAwardLevel {
    private Integer activityId;
    private Integer levelId;
    private String levelName;
    private String goodsName;
    private Integer dayAwardCount;
    private Integer dayLeftCount;
    private Timestamp lastAwardTime;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;
}