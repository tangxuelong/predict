package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@Data
@NoArgsConstructor
public class User {
    private Long userId;
    private String mobile;
    private String password;
    private String oauthId;
    private Integer oauthType;
    private Timestamp createTime;
    private Timestamp updateTime;

    public User(Long userId, String mobile, String password, String oauthId, Integer oauthType, Timestamp createTime,
                Timestamp updateTime) {
        this.userId = userId;
        this.mobile = mobile;
        this.password = password;
        this.oauthId = oauthId;
        this.oauthType = oauthType;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
