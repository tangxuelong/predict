package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ynght on 2016/10/20.
 */
@Data
@NoArgsConstructor
public class AwardInfoVo {
    private String periodId;
    private String winningNumbers;
    private Map<String, Object> resultMap = new HashMap<>();
    private String gameName;
    private String gameEn;
    private Integer awardStatus;
    private String awardTime;
    private List<String> playTypeAndBonus;
    private String periodSale;//本期销量  大盘彩
    private String poolBonus;//奖池奖金   大盘彩
    private String testNum;//奖池奖金   大盘彩
    private String showTitle;//标题

    public AwardInfoVo(String periodId, String winningNumbers, Map<String, Object> resultMap, String gameName, String
            gameEn, Integer awardStatus, String awardTime) {
        this.periodId = periodId;
        this.winningNumbers = winningNumbers;
        this.resultMap = resultMap;
        this.gameName = gameName;
        this.gameEn = gameEn;
        this.awardStatus = awardStatus;
        this.awardTime = awardTime;
    }

    public void put(String key, Object value) {
        resultMap.put(key, value);
    }
}
