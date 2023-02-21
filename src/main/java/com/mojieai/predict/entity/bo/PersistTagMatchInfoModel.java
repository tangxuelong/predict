package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class PersistTagMatchInfoModel {
    private Integer matchId;
    private String matchName;
    private Timestamp matchTime;
    private String matchDate;

    public PersistTagMatchInfoModel(Integer matchId, String matchName, Timestamp matchTime, String matchDate) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.matchTime = matchTime;
        this.matchDate = matchDate;
    }
}
