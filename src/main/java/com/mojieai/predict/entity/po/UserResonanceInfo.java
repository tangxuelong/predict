package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserResonanceInfo implements Serializable {
    private static final long serialVersionUID = 8755488212138167867L;

    private Long userId;
    private long gameId;
    private Integer lastPeriod;
    private Timestamp createTime;
    private Timestamp updateTime;

    public void initUserResonanceInfo(Long userId, long gameId) {
        this.userId = userId;
        this.gameId = gameId;
    }
}
