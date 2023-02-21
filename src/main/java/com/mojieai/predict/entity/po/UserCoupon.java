package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserCoupon implements Serializable {
    private static final long serialVersionUID = 1974886561198382976L;

    private String couponId;
    private Long userId;
    private Integer couponType;
    private Long couponConfigId;
    private String couponName;
    private String couponDesc;
    private Timestamp beginTime;
    private Timestamp endTime;
    private Integer accessType;
    public static final int ACCESS_TYPE_DIGITAL_GAME = 0;//数字彩
    public static final int ACCESS_TYPE_SPORT_GAME = 1;//竞彩
    public static final int ACCESS_TYPE_CELEBRITY_RECOMMEND = 2;//大咖推荐卡

    private Integer useStatus;
    public static final int USE_STATUS_INIT = 0;//初始
    public static final int USE_STATUS_USABLE = 1;
    public static final int USE_STATUS_UNUSABLE = 2;

    private Timestamp createTime;
    private Timestamp updateTime;
    //新增字段， 可用次数
    private Integer availableTimes;

    public UserCoupon(Long userId, Integer couponType, Long couponConfigId, String couponName, String couponDesc,
                      Timestamp beginTime, long validDay) {
        this.userId = userId;
        this.couponType = couponType;
        this.couponConfigId = couponConfigId;
        this.couponName = couponName;
        this.couponDesc = couponDesc;
        this.beginTime = DateUtil.getBeginOfOneDay(beginTime);
        this.endTime = DateUtil.getBeginOfOneDay(DateUtil.getIntervalDays(beginTime, validDay));
        this.accessType = 1;
        this.useStatus = 1;
        this.availableTimes = 1;
    }

    public UserCoupon(String couponId, Long userId, Integer couponType, Long couponConfigId, String couponName,
                      String couponDesc, Timestamp beginTime, Timestamp endTime, Integer accessType, Integer useStatus,
                      Timestamp createTime, Integer availableTimes) {
        this.couponId = couponId;
        this.userId = userId;
        this.couponType = couponType;
        this.couponConfigId = couponConfigId;
        this.couponName = couponName;
        this.couponDesc = couponDesc;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.accessType = accessType;
        this.useStatus = useStatus;
        this.createTime = createTime;
        this.availableTimes = availableTimes;
    }
}
