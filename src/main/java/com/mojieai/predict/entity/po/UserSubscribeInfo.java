package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserSubscribeInfo implements Serializable {
    private static final long serialVersionUID = 3233883353514961416L;

    private Long userId;
    private long gameId;
    private Integer predictType;
    private Integer periodId;
    private Integer programType;
    private Timestamp createTime;
    private Timestamp updateTime;

    public void initUserSubscribeInfo(Long userId, long gameId, Integer predictType, Integer programType) {
        this.userId = userId;
        this.gameId = gameId;
        this.predictType = predictType;
        this.programType = programType;
    }
}
