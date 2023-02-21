package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ColdHotNumVo {
    private String periodNum;
    private Integer redMaxShowTime = 0;
    private Integer blueMaxShowTime = 0;
    private List<ColdHotAttrVo> redColdHotAttrVoList = new ArrayList<>();
    private List<ColdHotAttrVo> blueColdHotAttrVoList = new ArrayList<>();

    public ColdHotNumVo(String periodNum) {
        this.periodNum = periodNum;
    }

    public ColdHotNumVo(String periodNum, List<ColdHotAttrVo> redColdHotAttrVoList, List<ColdHotAttrVo>
            blueColdHotAttrVoList) {
        this.periodNum = periodNum;
        this.redColdHotAttrVoList = redColdHotAttrVoList;
        this.blueColdHotAttrVoList = blueColdHotAttrVoList;
    }

    public List<ColdHotAttrVo> getColdHotAttrVoList(String ballType) {
        if (ballType.equals("RED")) {
            return this.redColdHotAttrVoList;
        } else {
            return this.blueColdHotAttrVoList;
        }
    }

    public void setMaxShowTime(Integer maxShowTime, String ballType) {
        if (ballType.equals("RED")) {
            this.setRedMaxShowTime(maxShowTime);
        } else {
            this.setBlueMaxShowTime(maxShowTime);
        }
    }

    public Integer getMaxShowTime(String ballType) {
        if (ballType.equals("RED")) {
            return this.getRedMaxShowTime();
        } else {
            return this.getBlueMaxShowTime();
        }
    }

    public void addRedColdHostAttrVo(ColdHotAttrVo redColdHostAttrVo) {
        if (!redColdHotAttrVoList.contains(redColdHostAttrVo)) {
            this.redColdHotAttrVoList.add(redColdHostAttrVo);
        }
    }

    public void addBlueColdHostAttrVo(ColdHotAttrVo redColdHostAttrVo) {
        if (!blueColdHotAttrVoList.contains(redColdHostAttrVo)) {
            this.blueColdHotAttrVoList.add(redColdHostAttrVo);
        }
    }

    public void addColdHostAttrVo(ColdHotAttrVo coldHotAttrVo, String ballType) {
        if (ballType.equals("RED")) {
            addRedColdHostAttrVo(coldHotAttrVo);
        } else {
            addBlueColdHostAttrVo(coldHotAttrVo);
        }
    }
}
