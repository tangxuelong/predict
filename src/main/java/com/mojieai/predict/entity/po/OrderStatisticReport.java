package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class OrderStatisticReport implements Serializable {
    private static final long serialVersionUID = 1245379591309836175L;

    private Integer statisticDate;
    private Integer totalOrderNum;
    private Integer totalUserNum;
    private Integer oldUserNum;
    private Integer newUserNum;
    private Long totalAmount;
    private Long oldUserAmount;
    private Long newUserAmount;
    private Integer realPayNum;
    private Long realPayAmount;
    private Integer statisticFlag;
    private Timestamp createTime;
    private Timestamp updateTime;
}
