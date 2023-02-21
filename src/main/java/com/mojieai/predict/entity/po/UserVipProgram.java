package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 会员专区
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class UserVipProgram {
    private Long userId;
    private String programId;
    private String prePayId;
    private Integer isPay;
    private Integer payType;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserVipProgram(Long userId, String programId, String prePayId, Integer isPay, Integer payType) {
        this.userId = userId;
        this.programId = programId;
        this.prePayId = prePayId;
        this.isPay = isPay;
        this.payType = payType;
    }
}