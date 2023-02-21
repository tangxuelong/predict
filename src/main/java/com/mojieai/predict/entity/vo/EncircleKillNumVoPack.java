package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class EncircleKillNumVoPack {
    private String headImg;
    private String userName;
    private String userKillCode;
    private String userKillCode_3_2;
    private String userTitle;
    private String rewardScore;
    private Long killUserId;
    private Integer isMe;
    private boolean isVip;
    private Integer numType;
    private List<String> godList;
    private Boolean isBestKill;
    private String scoreMsg;

    private String killUserIdStr;

    public EncircleKillNumVoPack(EncircleKillNumVo encircleKillNumVo) {
        this.killUserIdStr = String.valueOf(encircleKillNumVo.getKillUserId());
        BeanUtils.copyProperties(encircleKillNumVo, this);

    }
}
