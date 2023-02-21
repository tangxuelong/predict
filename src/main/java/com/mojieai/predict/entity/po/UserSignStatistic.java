package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserSignStatistic {
    private Long userId;
    private Integer signType;
    private Integer totalSignCount;
    private Integer maxCountinueSignCount;
    private Integer continueSignCount;
    private Timestamp lastSignTime;
    private Timestamp createTime;
}
