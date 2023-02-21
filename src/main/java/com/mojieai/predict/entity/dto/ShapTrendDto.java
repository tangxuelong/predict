package com.mojieai.predict.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/*形态走势dto*/
@Data
@NoArgsConstructor
public class ShapTrendDto {
    private int gameId;
    private String periodId;
    private int bigSmall;
    private int oddEven;
    private int premeComposite;
    private Timestamp createTime;
    private Timestamp updateTime;

    public ShapTrendDto(int gameId, String periodId, int bigSmall, int oddEven, int premeComposite) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.bigSmall = bigSmall;
        this.oddEven = oddEven;
        this.premeComposite = premeComposite;
    }

    public ShapTrendDto(int gameId, String periodId, int bigSmall, int oddEven, int premeComposite, Timestamp createTime, Timestamp updateTime) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.bigSmall = bigSmall;
        this.oddEven = oddEven;
        this.premeComposite = premeComposite;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
