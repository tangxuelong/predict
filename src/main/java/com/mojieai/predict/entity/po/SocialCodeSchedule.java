package com.mojieai.predict.entity.po;

import com.mojieai.predict.enums.CommonStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by Singal
 */
@Data
@NoArgsConstructor
public class SocialCodeSchedule {
    private Long gameId;
    private String periodId;
    private Integer ifDistribute;
    private Timestamp ifDistributeTime;
    private Integer ifRank;
    private Timestamp ifRankTime;
    private Timestamp createTime;
    private Timestamp updateTime;

    public SocialCodeSchedule(Long gameId, String periodId, Timestamp createTime) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.ifDistribute = CommonStatusEnum.NO.getStatus();
        this.ifDistributeTime = createTime;
        this.ifRank = CommonStatusEnum.NO.getStatus();
        this.ifRankTime = createTime;
        this.createTime = createTime;
    }
}
