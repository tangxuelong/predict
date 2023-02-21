package com.mojieai.predict.entity.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserEncircleInfo {
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
}
