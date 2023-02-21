package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class AllProductBill implements Serializable{
    private static final long serialVersionUID = -5199708076319939063L;

    private Integer dateNum;
    private Integer orderType;
    private Integer payPersonNum;
    private Long amount;
    private Integer cumulatePayPersonNum;
    private Long cumulateAmount;
    private Timestamp createTime;
    private Timestamp updateTime;
}
