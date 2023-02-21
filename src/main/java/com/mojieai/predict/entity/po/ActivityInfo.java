package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 活动信息配置表
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class ActivityInfo {
    private Integer activityId;
    private String activityName;
    private String imgUrl;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer isEnable;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;
}