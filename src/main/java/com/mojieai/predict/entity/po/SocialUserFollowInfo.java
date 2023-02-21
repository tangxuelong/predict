package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/11/28.
 */
@Data
@NoArgsConstructor
public class SocialUserFollowInfo {
    private Long userId;
    private Integer followType;
    private Integer followCount;
    private Integer fansCount;
    private Timestamp createTime;
    private Timestamp updateTime;

    public SocialUserFollowInfo(Long userId, Integer followCount, Integer followType, Integer fansCount) {
        this.userId = userId;
        this.followType = followType;
        this.followCount = followCount;
        this.fansCount = fansCount;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}
