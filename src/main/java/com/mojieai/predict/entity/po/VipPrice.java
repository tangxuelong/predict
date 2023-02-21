package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class VipPrice {
    private Integer vipPriceId;
    private Long originPrice;//分
    private Long price;//分
    private Integer vipDate;
    private String discountDesc;
    private String discountImg;
    private Integer payType;
    private Integer enable;
    private String iosMallId;
    private Integer clientType;
    private Integer vipType;
    private Timestamp createTime;
    private Timestamp updateTime;
}
