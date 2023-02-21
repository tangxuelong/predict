package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserSignReward {
    private Integer signType;
    private Integer signReward;
    private Integer rewardType;
    private Integer signCount;
    private Timestamp createTime;
}
