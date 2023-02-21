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
public class SocialUserFollow {
    private Long userId;
    private Integer followType;
    private Long followUserId;
    private Integer isFollow;
    private Timestamp createTime;
    private Timestamp updateTime;

    public SocialUserFollow(Long userId, Long followUserId, Integer followType, Integer isFollow) {
        this.userId = userId;
        this.followUserId = followUserId;
        this.followType = followType;
        this.isFollow = isFollow;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}
