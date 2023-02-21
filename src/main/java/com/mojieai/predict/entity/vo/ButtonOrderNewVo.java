package com.mojieai.predict.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ButtonOrderNewVo implements Serializable {
    private static final long serialVersionUID = -2317574422700110171L;
    private String toolName;
    private String toolType;
    private String toolImgUrl;
    private String toolIcon;
    private Integer toolWeight;
    private Boolean checkVip;
    private String jumpUrl;
    private Integer jumpFlag;

    public ButtonOrderNewVo() {
        this.checkVip = false;
    }
}
