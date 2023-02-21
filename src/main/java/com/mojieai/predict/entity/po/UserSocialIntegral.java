package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserSocialIntegral implements Serializable {
    private static final long serialVersionUID = 534598625113253066L;

    private Long gameId;
    private Long userId;
    private Long userScore;
    private Timestamp updateTime;

    public UserSocialIntegral(long gameId, Long userId) {
        this.gameId = gameId;
        this.userId = userId;
        this.userScore = 0l;
    }
}
