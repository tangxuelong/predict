package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@Data
@NoArgsConstructor
public class ThirdUser {
    private Long userId;
    private String oauth_id;
    private Integer oauth_type;
    private Timestamp createTime;

    public ThirdUser(Long userId, String oauth_id, Integer oauth_type, Timestamp createTime) {
        this.userId = userId;
        this.oauth_id = oauth_id;
        this.oauth_type = oauth_type;
        this.createTime = createTime;
    }
}
