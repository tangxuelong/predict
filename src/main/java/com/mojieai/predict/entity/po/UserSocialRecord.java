package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserSocialRecord {
    private Long gameId;
    private Long recordId;
    private Long userId;
    private Integer recordType;
    private Integer socialType;
    private Integer totalCount;
    private Integer maxContinueTimes;
    private Integer currentContinueTimes;
    private String periodId;
    private Timestamp createTime;
    private Timestamp updateTime;
}
