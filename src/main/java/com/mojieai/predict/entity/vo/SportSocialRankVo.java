package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class SportSocialRankVo {
    private Long userId;
    private Map<Integer, Integer> userAwardAmountRank; //收益
    private Map<Integer, Integer> userRightNumsRank;   // 命中
    private Map<Integer, Integer> userMaxNumsRank;    // 连中

    public SportSocialRankVo(Long userId, Map<Integer, Integer> userAwardAmountRank, Map<Integer, Integer>
            userRightNumsRank, Map<Integer, Integer> userMaxNumsRank) {
        this.userId = userId;
        this.userAwardAmountRank = userAwardAmountRank;
        this.userRightNumsRank = userRightNumsRank;
        this.userMaxNumsRank = userMaxNumsRank;
    }

    public Map<Integer, Integer> getUserRankMapByType(Integer rankType) {
        if (rankType == 0) {
            return userAwardAmountRank;
        }
        if (rankType == 1) {
            return userRightNumsRank;
        }
        if (rankType == 2) {
            return userMaxNumsRank;
        }
        return null;
    }
}
