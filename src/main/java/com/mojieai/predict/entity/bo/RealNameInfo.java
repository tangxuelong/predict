package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RealNameInfo implements Serializable {
    private static final long serialVersionUID = 5563409632313332337L;

    private String realName;
    private String idCard;
    private Integer authenticateStatus;

    public RealNameInfo(String realName, String idCard, Integer authenticateStatus) {
        this.realName = realName;
        this.idCard = idCard;
        this.authenticateStatus = authenticateStatus;
    }
}
