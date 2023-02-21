package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class SportsRecommendOrderReport implements Serializable {
    private static final long serialVersionUID = -4742513641126910420L;

    private Integer reportDate;
    private Integer wisdomOrderCount;
    private Long wisdomAmount;
    private Integer cashOrderCount;
    private Long cashAmount;
    private Integer couponOrderCount;
    private Long couponAmount;
    private Integer statisticFlag;
    private Timestamp createTime;
    private Timestamp updateTime;
}
