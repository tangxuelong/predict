package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class SocialStatistic {
    private Long statisticId;
    private long gameId;
    private String periodId;
    private String socialData;
    private Integer dataType;
    private Timestamp statisticTime;
    private Timestamp nextTime;
    private Timestamp createTime;
}
