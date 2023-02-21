package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SocialBigDataVo {
    private String hotEncircleData;
    private String hotKillData;
    private String statisticDate;
    private String statisticHour;
    private String periodId;

    public SocialBigDataVo(String hotEncircleData, String hotKillData, String statisticDate, String statisticHour,
                           String periodId) {
        this.hotEncircleData = hotEncircleData;
        this.hotKillData = hotKillData;
        this.statisticDate = statisticDate;
        this.statisticHour = statisticHour;
        this.periodId = periodId;
    }
}
