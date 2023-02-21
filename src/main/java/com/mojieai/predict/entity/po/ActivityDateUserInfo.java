package com.mojieai.predict.entity.po;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 活动日期配置表
 *
 * @author Singal
 */
@Data
public class ActivityDateUserInfo {
    private Integer activityId;
    private Long userId;
    private String dateId;
    private Integer times;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public ActivityDateUserInfo(Integer activityId, Long userId, String dateId, Integer times, String remark, Timestamp
            createTime, Timestamp updateTime) {
        this.activityId = activityId;
        this.userId = userId;
        this.dateId = dateId;
        this.times = times;
        this.remark = remark;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}