package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@Data
@NoArgsConstructor
public class UserToken {
    private Long userId;
    private String token;
    private Timestamp expireTime;
    private Timestamp createTime;

    public UserToken(Long userId, String token, Timestamp expireTime, Timestamp createTime) {
        this.userId = userId;
        this.token = token;
        this.expireTime = expireTime;
        this.createTime = createTime;
    }
}
