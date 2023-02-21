package com.mojieai.predict.entity.po;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 活动用户配置表
 *
 * @author Singal
 */
@Data
public class ActivityUserInfo {
    private Integer activityId;
    private Long userId;
    private Integer totalTimes;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public ActivityUserInfo(Integer activityId, Long userId, Integer totalTimes, String remark, Timestamp
            createTime, Timestamp updateTime) {
        this.activityId = activityId;
        this.userId = userId;
        this.totalTimes = totalTimes;
        this.remark = remark;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}