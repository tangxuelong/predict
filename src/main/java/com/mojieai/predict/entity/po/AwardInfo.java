package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class AwardInfo implements java.io.Serializable {
    private Long gameId;
    private String periodId; // 期次
    private String awardLevel; // 奖金等级
    private String levelName;
    private BigDecimal bonus; // 奖金金额
    private Integer awardCount;//中奖注数

    public static final int FIVE_MILLION = 5000000;

    public AwardInfo(String awardLevel, String levelName, BigDecimal bonus) {
        this.awardLevel = awardLevel;
        this.levelName = levelName;
        this.bonus = bonus;
    }

    public AwardInfo(Long gameId, String periodId, String awardLevel, String levelName, BigDecimal bonus, Integer
            awardCount) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.awardLevel = awardLevel;
        this.levelName = levelName;
        this.bonus = bonus;
        this.awardCount = awardCount;
    }
}
