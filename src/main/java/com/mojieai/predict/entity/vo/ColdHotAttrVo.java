package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.criteria.CriteriaBuilder;

@Data
@NoArgsConstructor
public class ColdHotAttrVo {

    private String ballNum;

    private Integer omitNum;

    private String coldHeatName;//冷热号名称

    private String color;

    private Double coldHeatPercent;

    private Integer coldHeatVal;

    public ColdHotAttrVo(String ballNum, Integer omitNum, String coldHeatName, Double coldHeatPercent, Integer
            coldHeatVal) {
        this.ballNum = ballNum;
        this.omitNum = omitNum;
        this.coldHeatName = coldHeatName;
        this.coldHeatPercent = coldHeatPercent;
        this.coldHeatVal = coldHeatVal;
    }

}
