package com.mojieai.predict.entity.bo;

import lombok.Data;

@Data
public class RecommendCheckBo {

    private Integer code;
    private String msg;

    public RecommendCheckBo(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
