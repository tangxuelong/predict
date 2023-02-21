package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 单关方案 卡
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class DanguanProgramCards {
    private Integer cardId;
    private String cardName;
    private String cardDesc;
    private String imgUrl;
    private Long originPrice;
    private Long price;
    private Integer times;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;
}