package com.mojieai.predict.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WithdrawStatusVo implements Serializable {
    private String statusName;
    private Integer status;
    private Integer lineStatus;//1是亮，0是暗色，-1无线

    public WithdrawStatusVo(String statusName, Integer status, Integer lineStatus) {
        this.statusName = statusName;
        this.status = status;
        this.lineStatus = lineStatus;
    }
}
