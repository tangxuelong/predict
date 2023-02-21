package com.mojieai.predict.entity.vo;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class MyEncircleVoPack {
    private String periodId;
    private String encircleCodeId;
    private String encircleName;
    private Integer encircleCount;
    private String encircleNum;
    private String encircleTime;
    private String encircleTimeBak;
    private String encircleDesc;
    private Integer partakeCount;
    private String killNumDesc;
    private String encircleAwardAdMsg;

    //杀号列表使用
    private String encircleUserName;
    private String encircleHeadImg;
    private Long encircleUserId;
    private Integer killNumStatus;
    private Integer killNumAwardStatus;
    private String encircleKillCount;
    private String killNumBtnAdMsg;
    private String killNumAwardAdMsg;
    private String killListAwardAdMsg;
    private Integer isHot;
    private boolean isVip;
    private List<String> godList;

    //我的杀号使用
    private String myKillNumAwardFrontAdMsg;
    private String myKillNumAwardBackAdMsg;
    private String encircleUserIdStr;

    public MyEncircleVoPack(String encircleUserIdStr, MyEncircleVo myEncircleVo) {
        this.encircleUserIdStr = encircleUserIdStr;
        BeanUtils.copyProperties(myEncircleVo, this);
    }

    public MyEncircleVoPack(List<String> godList, MyEncircleVo myEncircleVo) {
        this.godList = godList;
        BeanUtils.copyProperties(myEncircleVo, this);
    }
}
