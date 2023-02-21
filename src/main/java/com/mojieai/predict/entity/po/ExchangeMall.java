package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 账户余额
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class ExchangeMall {
    private Integer itemId;
    private String itemName;
    private String itemImg;
    private Long itemPrice;
    private Long itemOriginPrice;
    private Integer itemType;
    private Long itemCount;
    private Integer accountType;
    private Long gameId;
    private Integer status;
    private String iosMallId;
    private Integer isDefault;
    private Integer vipDiscount;
    private Integer clientId;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;
}