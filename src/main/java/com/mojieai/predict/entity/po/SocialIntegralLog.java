package com.mojieai.predict.entity.po;

import com.mojieai.predict.enums.CommonStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class SocialIntegralLog implements Serializable {
    private static final long serialVersionUID = -5011901533498230119L;

    private Integer socialType;
    private Long socialCode;
    private Long userId;
    private Long gameId;
    private String periodId;
    private String name;
    private Long score;
    private Integer isDistribute;
    private Timestamp updateTime;

    public SocialIntegralLog(Integer socialType, Long socialCode, Long userId, Long gameId, String periodId, String
            name, Long score) {
        this.socialType = socialType;
        this.socialCode = socialCode;
        this.userId = userId;
        this.gameId = gameId;
        this.periodId = periodId;
        this.name = name;
        this.score = score;
        this.isDistribute = CommonStatusEnum.NO.getStatus();
    }
}
