package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 活动奖级配置表
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class IndexMatchRecommend {
    private String matchId;
    private Long userId;
    private String recommendId;
    private Integer isRank;
    private Timestamp rankTime;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public void initIndexMatchRecommend(String matchId, Long userId, String recommendId) {
        this.matchId = matchId;
        this.userId = userId;
        this.recommendId = recommendId;
        this.createTime = DateUtil.getCurrentTimestamp();
    }
}