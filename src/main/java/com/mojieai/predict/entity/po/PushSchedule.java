package com.mojieai.predict.entity.po;

import com.mojieai.predict.enums.CommonStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PushSchedule {
    private Long gameId;
    private String periodId;
    private Integer ifWinningNumberPush;
    private Timestamp ifWinningNumberPushTime;
    private Timestamp createTime;

    public PushSchedule(long gameId, String periodId, Timestamp createTime) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.ifWinningNumberPush = CommonStatusEnum.NO.getStatus();
        this.createTime = createTime;
    }
}
