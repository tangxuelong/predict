package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 用户关注比赛表
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class UserFollowMatches {
    private Long userId;
    private String matchId;
    private Integer followStatus;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserFollowMatches(Long userId,String matchId){
        this.userId = userId;
        this.matchId = matchId;
        this.followStatus = 1;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}