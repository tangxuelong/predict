package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@Data
@NoArgsConstructor
public class MobileUser {
    private String mobile;
    private Long userId;
    private Timestamp createTime;

    public MobileUser(String mobile, Long userId, Timestamp createTime) {
        this.mobile = mobile;
        this.userId = userId;
        this.createTime = createTime;
    }
}
