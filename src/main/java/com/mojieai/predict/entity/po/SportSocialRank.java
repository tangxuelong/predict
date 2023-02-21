package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 体育社区排行榜
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class SportSocialRank {
    private Long userId;
    private Integer rankType;
    private Integer playType;
    private Integer matchCount;
    private Integer userScore;
    private String remark;
    private Integer inRank;
    private Timestamp createTime;
    private Timestamp updateTime;

    public SportSocialRank(Long userId, Integer rankType, Integer playType, Integer matchCount, Integer userScore,
                           String
            remark, Integer inRank) {
        this.userId = userId;
        this.rankType = rankType;
        this.playType = playType;
        this.matchCount = matchCount;
        this.userScore = userScore;
        this.remark = remark;
        this.inRank = inRank;
    }
}