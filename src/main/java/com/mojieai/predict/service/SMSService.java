package com.mojieai.predict.service;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/7/8.
 */
public interface SMSService {
    void sendVerifyCode(String mobile, String msg, Timestamp sendTime);

    void sendVerifyCodePushOnly(String mobile, String msg, Timestamp sendTime);

    Boolean sendVerifyCode(String mobile, String redisPrefix);

    void sendMsgByCustomAccount(String mobile, String msg, Timestamp sendTime, String user, String password);
}
