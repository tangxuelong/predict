package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SMSService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.HttpServiceUtils;
import com.mojieai.predict.util.Md5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Random;

@Service
public class SMSServiceImpl implements SMSService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    public RedisService redisService;

    /*private final String USER = "zhyc";
    private final String PASSWORD = "zhyc123";*/
    private final String MsgContentPre = "您的验证码为";
    private final String MsgContent = "，有效时间5分钟，请尽快验证";

    @Override
    public void sendVerifyCode(String mobile, String msg, Timestamp sendTime) {
        String sendTimeStr = DateUtil.formatTime(sendTime, DateUtil.DATE_FORMAT_YYYYMMDDHHMMSS);
        NeShiKuaiSendSMS(mobile, msg, sendTimeStr.substring(0, sendTimeStr.length() - 2));
    }

    @Override
    public void sendVerifyCodePushOnly(String mobile, String msg, Timestamp sendTime) {
        String sendTimeStr = DateUtil.formatTime(sendTime, DateUtil.DATE_FORMAT_YYYYMMDDHHMMSS);
        String user = ActivityIniConstant.SMS_USER;
        String password = ActivityIniConstant.SMS_PWD;
        NaShiKuaiSendSMS(mobile, msg, sendTimeStr.substring(0, sendTimeStr.length() - 2), user, password);
    }

    @Override
    public Boolean sendVerifyCode(String mobile, String redisPrefix) {
        if (StringUtils.isBlank(redisPrefix)) {
            redisPrefix = RedisConstant.PREFIX_SEND_VERIFY_CODE;
        }
        if (Strings.isNotBlank(redisService.kryoGet(redisPrefix + mobile, String.class))) {
            return Boolean.FALSE;
        }
        /* 生成验证码*/
        Long random = new Long(new Random().nextInt(9999));
        String verifyCode = CommonUtil.formatSequence(random).substring(4, 8);
        // String verifyCode = "1234";
        /* 立即发送验证码*/
        sendVerifyCode(mobile, MsgContentPre + verifyCode + MsgContent, new Timestamp(System.currentTimeMillis()));
        redisService.kryoSetEx(redisPrefix + mobile, IniCache.getIniIntValue(IniConstant.SMS_EXPIRE_TIME, 60 * 5),
                verifyCode);
        return Boolean.TRUE;
    }

    @Override
    public void sendMsgByCustomAccount(String mobile, String msg, Timestamp sendTime, String user, String password) {
        String sendTimeStr = DateUtil.formatTime(sendTime, DateUtil.DATE_FORMAT_YYYYMMDDHHMMSS);
        NaShiKuaiSendSMS(mobile, msg, sendTimeStr, user, password);
    }

    /* 那时快 发送验证码*/
    private void NeShiKuaiSendSMS(String mobile, String msg, String sendTime) {
        String user = IniCache.getIniValue(IniConstant.SMS_USER, "zhyc");
        String password = IniCache.getIniValue(IniConstant.SMS_PASSWORD, "zhyc123");
        NaShiKuaiSendSMS(mobile, msg, sendTime, user, password);
    }

    private void NaShiKuaiSendSMS(String mobile, String msg, String sendTime, String user, String password) {
        String smsChannel = IniCache.getIniValue(IniConstant.SMS_CHANNEL, "sd_sms_channel");
        if (smsChannel.equals("sd_sms_channel")) {
            //顺达短信渠道
            StringBuffer sb = new StringBuffer();
            String uId = "4566";
            String pw = "657643";
            String message = "【智慧预测】 " + msg;
            sb.append("uid").append(CommonConstant.COMMON_EQUAL_STR).append(uId).append(CommonConstant.COMMON_AND_STR)
                    .append("pw").append(CommonConstant.COMMON_EQUAL_STR).append(pw).append(CommonConstant
                    .COMMON_AND_STR).append("mb").append(CommonConstant.COMMON_EQUAL_STR).append(mobile).append
                    (CommonConstant.COMMON_AND_STR).append("ms").append(CommonConstant.COMMON_EQUAL_STR).append(message)
                    .append(CommonConstant.COMMON_AND_STR).append("ex").append(CommonConstant.COMMON_EQUAL_STR).append
                    ("77");
            String result = HttpServiceUtils.sendPostRequest("http://47.101.58.161:18002/send.do", sb.toString(), "UTF-8");
            if (!result.substring(0, 1).equals("0")) {
                log.error("send message error ,channel sd ,errMsg is " + CommonUtil.mergeUnionKey(mobile, msg,
                        sendTime, result));
                throw new BusinessException("send message error ,channel sd ,errMsg is " + result);
            }
        } else if (smsChannel.equals("nsk_sms_channel")){
            //那时快短信渠道
            StringBuffer sb = new StringBuffer();
            String passwd = Md5Util.getMD5String(user + password);
            sb.append("user").append(CommonConstant.COMMON_EQUAL_STR).append(user).append(CommonConstant.COMMON_AND_STR)
                    .append("passwd").append(CommonConstant.COMMON_EQUAL_STR).append(passwd).append(CommonConstant
                    .COMMON_AND_STR).append("msg").append(CommonConstant.COMMON_EQUAL_STR).append(msg).append
                    (CommonConstant.COMMON_AND_STR).append("mobs").append(CommonConstant.COMMON_EQUAL_STR).append(mobile)
                    .append(CommonConstant.COMMON_AND_STR).append("ts").append(CommonConstant.COMMON_EQUAL_STR).append
                    (sendTime).append(CommonConstant.COMMON_AND_STR).append("dtype").append(CommonConstant
                    .COMMON_EQUAL_STR).append(0).append(CommonConstant.COMMON_AND_STR);
            String result = HttpServiceUtils.sendPostRequest("http://webapi2.didisms.com/SendSms.aspx", sb.toString(), "UTF-8");
            if (!result.substring(0, 1).equals("0")) {
                log.error("send message error ,channel neshikuai ,errMsg is " + CommonUtil.mergeUnionKey(mobile, msg,
                        sendTime, result));
                throw new BusinessException("send message error ,channel neshikuai ,errMsg is " + result);
            }
        } else {
            throw new BusinessException("send message error ,channel not exist");
        }
    }
}
