package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class CouponConfig implements Serializable {
    private static final long serialVersionUID = -4028165933050739755L;

    private Long couponId;
    private Integer couponType;
    public static final int COUPON_TYPE_CELEBRITY_RECOMMEND_CARD  = 2;//大咖推荐特权卡

    private String couponName;
    private String couponDesc;
    private Integer validDay;
    private Integer status;
    public static final int COUPON_CONFIG_STATUS_UNUSABLE = 0;
    public static final int COUPON_CONFIG_STATUS_USABLE = 1;

    private Integer accessType;
    public static final int ACCESS_TYPE_DIGITAL_GAME = 0;//数字彩
    public static final int ACCESS_TYPE_SPORT_GAME = 1;//竞彩
    public static final int ACCESS_TYPE_CELEBRITY_RECOMMEND = 2;//大咖推荐卡

    private Integer distributeCount;
    private Timestamp createTime;

    private String remark;
}
