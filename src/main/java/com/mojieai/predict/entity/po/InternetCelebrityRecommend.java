package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class InternetCelebrityRecommend implements Serializable {
    private static final long serialVersionUID = 357228196136457795L;

    private Long userId;
    private String recommendId;
    private Long price;
    private Integer matchId;
    private Long likeCount;
    private String remark;
    private Integer status;
    private Timestamp matchTime;
    private Timestamp createTime;
    private Timestamp updateTime;

    //新增字段08.28 热门指数
    private Integer popularIndex = 0;

    public static final int CELEBRITY_RECOMMEND_PAY_TYPE_COIN = 0;//智慧币解锁
    public static final int CELEBRITY_RECOMMEND_PAY_TYPE_PRIVILEGED_CARD = 1;//特权卡解锁
}
