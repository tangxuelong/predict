package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by ynght on 2018/8/27
 */
@Data
@NoArgsConstructor
public class CelebrityRecommendVo {

    private String recommendId;
    private String recommendUserId;
    private String payMemo;
    private Map<String, Object> userInfo;
    private Map<String, Object> matchInfo;
    private Map<String, Object> recommendInfo;
    private Integer popularIndex;
    private String btnMsg;
    private Integer buyStatus;
    private Integer programType;
    private Integer discount;


    public CelebrityRecommendVo(String recommendId, String recommendUserId, String payMemo, Map<String, Object>
            userInfo, Map<String, Object> recommendInfo, Map<String, Object> matchInfo, Integer popularIndex,
                                String btnMsg, Integer buyStatus, Integer programType, Integer discount) {
        this.recommendId = recommendId;
        this.recommendUserId = recommendUserId;
        this.payMemo = payMemo;
        this.userInfo = userInfo;
        this.matchInfo = matchInfo;
        this.recommendInfo = recommendInfo;
        this.popularIndex = popularIndex;
        this.btnMsg = btnMsg;
        this.buyStatus = buyStatus;
        this.programType = programType;
        this.discount = discount;
    }
}
