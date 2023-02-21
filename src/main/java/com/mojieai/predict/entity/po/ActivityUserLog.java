package com.mojieai.predict.entity.po;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 活动用户抽奖记录表
 *
 * @author tangxuelong
 */
@Data
public class ActivityUserLog {
    private Integer userLogId;
    private Integer activityId;
    private Long userId;
    private String dateId;
    private Integer levelId;
    private Timestamp createTime;

    public ActivityUserLog(Integer userLogId, Integer activityId, Long userId, Integer levelId, String dateId, Timestamp
            createTime) {
        this.userLogId = userLogId;
        this.activityId = activityId;
        this.userId = userId;
        this.levelId = levelId;
        this.dateId = dateId;
        this.createTime = createTime;
    }
}