package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

/**
 * Created by tangxuelong on 2017/7/4.
 */
@Data
@NoArgsConstructor
public class UserLoginVoPack {
    private String token;      // 令牌
    private String nickName;   // 昵称
    private String headImgUrl; // 头像
    private Long userId;     // ID
    private String mobile;    //手机号
    private String userIdStr;

    public UserLoginVoPack(UserLoginVo userLoginVo) {
        this.userIdStr = userLoginVo.getUserId() + "";
        BeanUtils.copyProperties(userLoginVo, this);
    }
}
