package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class StarUserMatch implements Serializable {
    private static final long serialVersionUID = -6074993062998958312L;

    private Integer activityId;
    private Long userId;
    private Integer matchId;
    private Integer isRight;
    private Integer award;
    private Timestamp matchTime;

    public StarUserMatch(Integer activityId, Long userId, Integer matchId, Integer isRight, Integer award, Timestamp
            matchTime) {
        this.activityId = activityId;
        this.userId = userId;
        this.matchId = matchId;
        this.isRight = isRight;
        this.award = award;
        this.matchTime = matchTime;
    }
}
