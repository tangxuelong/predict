package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tangxuelong on 2017/7/4.
 */
@Data
@NoArgsConstructor
public class UserLoginVo {
    private String token;      // 令牌
    private String nickName;   // 昵称
    private String headImgUrl; // 头像
    private Long userId;     // ID
    private String mobile;    //手机号
    private Boolean realNameAuthentication;

    public UserLoginVo(String token, Long userId, String nickName, String headImgUrl, String mobile, Boolean
            realNameAuthentication) {
        this.token = token;
        this.userId = userId;
        this.nickName = nickName;
        this.headImgUrl = headImgUrl;
        this.mobile = mobile;
        this.realNameAuthentication = realNameAuthentication;
    }

    public String getHeadImgUrl() {
        if (headImgUrl.contains("ovqsyejql.bkt.clouddn.com")) {
            headImgUrl = headImgUrl.replace("ovqsyejql.bkt.clouddn.com", "sportsimg.mojieai.com");
        }
        return headImgUrl;
    }
}
