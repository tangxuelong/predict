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
public class SocialUserFans {
    private Long userId;
    private Long fansUserId;
    private Integer fansType;
    private Integer isFans;
    private Timestamp createTime;
    private Timestamp updateTime;

    public SocialUserFans(Long userId, Long fansUserId, Integer fansType, Integer isFans) {
        this.userId = userId;
        this.fansUserId = fansUserId;
        this.isFans = isFans;
        this.fansType = fansType;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}
