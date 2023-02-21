package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class SocialKillCode {
    private Long killCodeId;
    private Long encircleCodeId;
    private long gameId;
    private String periodId;
    private long userId;
    private String userKillCode;
    private Integer codeType;
    private Integer killNums;
    private Integer userRank;
    private Integer rightNums;
    private Integer isDistribute;
    private Integer userAwardScore;
    private Timestamp createTime;
    private Timestamp updateTime;
}
