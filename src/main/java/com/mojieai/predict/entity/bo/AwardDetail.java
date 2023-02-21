package com.mojieai.predict.entity.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by bowu on 2017/7/16.
 */
@Data
@NoArgsConstructor
public class AwardDetail implements Cloneable{
    private Long gameId;
    private String periodId;
    private String periodName;
    private BigDecimal singleBonus;//单注奖金
    private BigDecimal bonus;//总奖金
    private int[] awardLevel;//中奖注数详情
    private String[] awardLevelStr;//中奖注数详情
    private int maxAwardLevel;
    private int historyPredictAwardSum;//历史预测累计中奖金额
    private String historyPredictAwardLevelSum;//历史预测中奖累计


    public AwardDetail(Long gameId, String periodId, BigDecimal bonus, int[] awardLevel) {
        this.gameId = gameId;
        this.periodId = periodId;
        this.bonus = bonus;
        this.awardLevel = awardLevel;
    }

    @Override
    public Object clone(){
        AwardDetail awardDetail = null;
        try{
            awardDetail = (AwardDetail) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return awardDetail;
    }
}
