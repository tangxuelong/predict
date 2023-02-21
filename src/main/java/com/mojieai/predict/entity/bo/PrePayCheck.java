package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrePayCheck implements Serializable {
    private static final long serialVersionUID = 6685442990963390892L;

    private Integer code;
    private String msg;

    public PrePayCheck(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
