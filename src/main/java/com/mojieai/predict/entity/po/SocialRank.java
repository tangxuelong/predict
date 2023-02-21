package com.mojieai.predict.entity.po;

import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Data
public class SocialRank {
    private Long userId;
    private Long gameId;
    private String periodId;
    private String weekId;
    private String monthId;
    private Integer userScore;
    private String lastPeriodId;
    private Timestamp createTime;
    private Timestamp updateTime;

    public SocialRank(Long userId, Long gameId, String periodId, String weekId, String monthId, Integer userScore,
                      Timestamp createTime, Timestamp updateTime) {
        this.userId = userId;
        this.gameId = gameId;
        this.periodId = periodId;
        this.weekId = weekId;
        this.monthId = monthId;
        this.userScore = userScore;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public SocialRank(Long userId, Long gameId, String periodId, String weekId, String monthId, Integer userScore,
                      String lastPeriodId, Timestamp createTime, Timestamp updateTime) {
        this.userId = userId;
        this.gameId = gameId;
        this.periodId = periodId;
        this.weekId = weekId;
        this.monthId = monthId;
        this.userScore = userScore;
        this.lastPeriodId = lastPeriodId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public SocialRank(Long userId, Long gameId, String weekId, String monthId, Integer userScore,
                      String lastPeriodId, Timestamp createTime, Timestamp updateTime) {
        this.userId = userId;
        this.gameId = gameId;
        this.weekId = weekId;
        this.monthId = monthId;
        this.userScore = userScore;
        this.lastPeriodId = lastPeriodId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public SocialRank(Long userId, Long gameId, String monthId, String lastPeriodId, Integer userScore,
                      Timestamp createTime, Timestamp updateTime) {
        this.userId = userId;
        this.gameId = gameId;
        this.monthId = monthId;
        this.userScore = userScore;
        this.lastPeriodId = lastPeriodId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

}
