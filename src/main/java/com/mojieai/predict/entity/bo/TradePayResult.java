package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class TradePayResult implements Serializable {
    private static final long serialVersionUID = 1190017188555597360L;

    private Integer code;
    private String msg;
    private Map<String, Object> payInfo;
}
