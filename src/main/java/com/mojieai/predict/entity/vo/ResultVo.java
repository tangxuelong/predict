package com.mojieai.predict.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultVo implements Serializable {
    private static final long serialVersionUID = 17525936021854289L;

    private Integer code;
    private String msg;

    public ResultVo(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultVo(Integer code) {
        this.code = code;
    }
}
