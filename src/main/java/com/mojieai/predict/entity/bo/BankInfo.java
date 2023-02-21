package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BankInfo implements Serializable {
    private static final long serialVersionUID = 3865677137108866502L;

    private String bankName;
    private String bankEn;
    private String bankImg;

    public BankInfo(String bankEn, String bankName, String bankImg) {
        this.bankEn = bankEn;
        this.bankImg = bankImg;
        this.bankName = bankName;
    }
}
