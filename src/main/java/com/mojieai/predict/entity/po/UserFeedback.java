package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/8/24.
 */
@Data
@NoArgsConstructor
public class UserFeedback {
    private Integer feedbackId;
    private String content;
    private String userToken;
    private String contact;
    private Integer isSend;
    private Timestamp createTime;
    private Timestamp updateTime;
}
