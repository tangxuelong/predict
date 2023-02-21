package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RobotEncircle {

    private Integer robotId;

    private Long gameId;

    private String periodId;

    private Integer encircleTimes;

    private Integer killNumTimes;

    private String encircleCode;

    private String killCode;
}