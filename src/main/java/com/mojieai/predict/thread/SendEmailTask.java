package com.mojieai.predict.thread;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.bo.Email;
import com.mojieai.predict.service.SendEmailService;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by tangxuelong on 2017/8/25.
 */
public class SendEmailTask implements Callable {

    private static final Logger log = LogConstant.commonLog;

    private SendEmailService sendEmailService;
    private Email email;

    public SendEmailTask(SendEmailService sendEmailService, Email email) {
        this.sendEmailService = sendEmailService;
        this.email = email;
    }

    @Override
    public Object call() throws Exception {
        try {
            // 发送邮件
            sendEmailService.SendEmail(email.getTitle(), email.getContent());
        } catch (Exception e) {
            log.error("send email error:" + email.getContent());
        }
        return null;
    }


}
