package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserWithdrawFlow implements Serializable {
    private static final long serialVersionUID = -8017892750665202838L;

    private String withdrawId;
    private Long userId;
    private String bankCard;
    private String userName;
    private Long withdrawAmount;
    private Integer withdrawStatus;
    private Long serviceCharge;
    private String remark;
    private Timestamp withdrawTime;
    private Timestamp createTime;
    private Timestamp updateTime;
}
