package com.mojieai.predict.service.impl;

import com.mojieai.predict.service.SendEmailService;
import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;


/**
 * Created by tangxuelong on 2017/8/24.
 */
@Service
public class SendEmailServiceImpl implements SendEmailService {

    private final static String ACCOUNT = "notice@caiqr.com";//登录用户名
    private final static String PASSWORD = "caiqiu502";        //登录密码
    private final static String FROM = "notice@caiqr.com";        //发件地址
    private final static String HOST = "smtp.exmail.qq.com";        //服务器地址
    private final static String PORT = "465";        //端口
    private final static String PROTOCOL = "smtp"; //协议
    private final static String TO = "mojiecaipiao@caiqr.com"; //协议


    @Override
    public void SendEmail(String title, String content) {
        Properties prop = new Properties();
        //协议
        prop.setProperty("mail.transport.protocol", PROTOCOL);
        //服务器
        prop.setProperty("mail.smtp.host", HOST);
        //端口
        prop.setProperty("mail.smtp.port", PORT);
        //使用smtp身份验证
        prop.setProperty("mail.smtp.auth", "true");
        //使用SSL，企业邮箱必需！
        //开启安全协议
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);
        //
        Session session = Session.getDefaultInstance(prop, new MyAuthenricator(ACCOUNT, PASSWORD));
        session.setDebug(true);
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(FROM, "智慧彩票后台服务"));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
            mimeMessage.setSubject(title);
            mimeMessage.setSentDate(new Date());
            mimeMessage.setContent(content, "text/html;charset = gbk");
            mimeMessage.saveChanges();
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //用户名密码验证，需要实现抽象类Authenticator的抽象方法PasswordAuthentication
    static class MyAuthenricator extends Authenticator {
        String u = null;
        String p = null;

        public MyAuthenricator(String u, String p) {
            this.u = u;
            this.p = p;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(u, p);
        }
    }

}
