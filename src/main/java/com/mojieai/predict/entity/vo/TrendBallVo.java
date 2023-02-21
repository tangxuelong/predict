package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class TrendBallVo implements Serializable {
    private static final long serialVersionUID = 2542392447540516747L;

    private String omitNum;
    private Integer color = 0;
    private Integer floatNum = 0;//上浮号码
    private Integer form = 0;//绘制方圆

    public TrendBallVo(String omitNum) {
        this.omitNum = omitNum;
    }

    public TrendBallVo(String omitNum, Integer color) {
        this.omitNum = omitNum;
        this.color = color;
    }

    public TrendBallVo(String omitNum, Integer color, Integer floatNum, Integer form) {
        this.omitNum = omitNum;
        this.color = color;
        this.floatNum = floatNum;
        this.form = form;
    }
}
