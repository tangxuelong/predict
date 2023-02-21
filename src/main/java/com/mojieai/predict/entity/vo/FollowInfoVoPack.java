package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * Created by tangxuelong on 2017/11/29.
 */
@Data
public class FollowInfoVoPack {
    private Long userId;
    private Integer followCount;
    private Integer fansCount;
    private String nickName;
    private String headImgUrl;
    private Integer isFollow;
    private Boolean isMe;
    private Boolean isVip;

    private String userIdStr;
    private List<String> godList;

    public FollowInfoVoPack(String userIdStr, FollowInfoVo followInfoVo){
        this.userIdStr = userIdStr;
        BeanUtils.copyProperties(followInfoVo, this);
    }
}
