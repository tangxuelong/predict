package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserSign {
    private Long signCode;
    private Long userId;
    private String signDate;
    private Integer ifReward;
    private Integer signType;
    private Timestamp createTime;
}
