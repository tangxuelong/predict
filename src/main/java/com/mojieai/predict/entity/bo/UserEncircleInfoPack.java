package com.mojieai.predict.entity.bo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
public class UserEncircleInfoPack {
    private String encircleCodeId;
    private Integer encircleCount;
    private String encircleDesc;
    private String encircleHeadImg;
    private Integer encircleKillCount;
    private String encircleNum;
    private String encircleTime;
    private Timestamp encircleTimeBak;
    private Long encircleUserId;
    private String encircleUserName;
    private String killListAwardAdMsg;
    private String killNumAwardAdMsg;
    private String killNumBtnAdMsg;
    private Integer killNumStatus;
    private Integer partakeCount;
    private String periodId;
    private boolean isVip;

    private String encircleUserIdStr;
    private List<String> godList;

    public UserEncircleInfoPack(String encircleUserIdStr, UserEncircleInfo userEncircleInfo, List<String> godList) {
        this.encircleUserIdStr = encircleUserIdStr;
        this.godList = godList;
        BeanUtils.copyProperties(userEncircleInfo, this);
    }
}
