package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 会员专区
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class UserVipProgramDateTime {
    private Long userId;
    private String dateId;
    private Integer times;
    private Integer useTimes;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserVipProgramDateTime(Long userId, String dateId, Integer times, Integer useTimes,
                                  Timestamp createTime, Timestamp updateTime) {
        this.userId = userId;
        this.dateId = dateId;
        this.times = times;
        this.useTimes = useTimes;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}