package com.mojieai.predict.service.timedtask;

import com.mojieai.predict.service.SMSService;
import com.mojieai.predict.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StationPromotionTimer {

    @Autowired
    private SMSService smsService;

    public void sendMsg2NotUseNewUserRedUsers() {
        List<String> userIdMobile = getNotConsumeNewUser();
        if (userIdMobile == null || userIdMobile.size() == 0) {
            return;
        }
        for (String mobile : userIdMobile) {
            sendMsg2User(mobile);
        }
    }

    private void sendMsg2User(String mobile) {
        String user = "";
        String password = "";
        String msg = "您的25元礼包已到账，3天内失效！礼包直抵现金，请确认收钱 https://t.mojieai.com/99/";
        try {
            smsService.sendMsgByCustomAccount(mobile, msg, DateUtil.getCurrentTimestamp(), user, password);
        } catch (Exception e) {

        }
    }

    private List<String> getNotConsumeNewUser() {
        List<String> userIds = new ArrayList<>();


        return userIds;
    }
}
