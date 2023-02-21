package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThirdPartBillOrderInfo implements Serializable {
    private static final long serialVersionUID = 2766165014568765045L;
    private Double totalAmount;
    private Double totalPoundage;

    public ThirdPartBillOrderInfo() {
        this.totalAmount = 0d;
        this.totalPoundage = 0d;
    }
}
