package com.mojieai.predict.entity.vo;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class MyEncircleVo implements Comparable {
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

    //我的杀号使用
    private String myKillNumAwardFrontAdMsg;
    private String myKillNumAwardBackAdMsg;

    @Override
    public int compareTo(Object o) {
        MyEncircleVo comParatorVo = (MyEncircleVo) o;
        Timestamp myEncircleVoTime = DateUtil.convertMonthStr2Time(comParatorVo.getEncircleTimeBak());
        Timestamp comparatorPeriodTime = DateUtil.convertMonthStr2Time(this.getEncircleTimeBak());
        if (StringUtils.isNotBlank(this.encircleTimeBak) && StringUtils.isNotBlank(comParatorVo.getEncircleTimeBak())) {
            if (DateUtil.compareDate(comparatorPeriodTime, myEncircleVoTime)) {
                return -1;
            }
            if (DateUtil.compareDate(myEncircleVoTime, comparatorPeriodTime)) {
                return 1;
            }
        }
        return (-1 * this.encircleCodeId.compareTo(comParatorVo.getEncircleCodeId()));
    }
}
