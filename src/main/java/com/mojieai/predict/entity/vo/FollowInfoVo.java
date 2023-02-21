package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by tangxuelong on 2017/11/29.
 */
@Data
@NoArgsConstructor
public class FollowInfoVo {
    private Long userId;
    private Integer followCount;
    private Integer fansCount;
    private String nickName;
    private String headImgUrl;
    private Integer isFollow;
    private Boolean isMe;
    private Boolean isVip;
    private List<String> godList;
}
