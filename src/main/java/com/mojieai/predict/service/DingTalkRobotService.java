package com.mojieai.predict.service;

import java.util.List;

/**
 * Created by tangxuelong on 2017/9/14.
 */
public interface DingTalkRobotService {
    void sendMassageToAll(String title, String text, List<String> at);
}
