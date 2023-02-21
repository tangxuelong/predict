package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.SerializeUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserSocialTaskAward implements Serializable {
    private static final long serialVersionUID = -7784051686846911187L;
    private String taskId;
    private Long userId;
    private Long gameId;
    private String periodId;
    private Integer taskType;
    private Integer taskTimes;
    private Integer isAward;
    private Timestamp updateTime;
}
