package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tangxuelong on 2017/10/18.
 */
@Data
@NoArgsConstructor
public class SocialRankVo {
    private String userId;
    private String nickName;
    private String headImgUrl;
    private Integer userScore;
    private String userAwardDesc;
    private Integer rank;
    private Integer isCurrentUser;
    private boolean isVip;
}
