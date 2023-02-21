package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class SocialEncircle {

    private long encircleCodeId;
    private long userId;
    private long gameId;
    private String periodId;
    private String userEncircleCode;
    private Integer codeType;
    private Integer followKillNums;
    private Integer encircleNums;
    private String killNums;
    private Integer rightNums;
    private Integer isDistribute;
    private Integer userAwardScore;
    private Integer isHot = 0;
    private Timestamp createTime;
    private Timestamp updateTime;
}
