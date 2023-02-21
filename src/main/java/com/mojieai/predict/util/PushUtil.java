package com.mojieai.predict.util;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.service.PushService;
import com.mojieai.predict.thread.GodPredictTask;
import com.mojieai.predict.thread.ThreadPool;

import java.util.HashMap;
import java.util.Map;

public class PushUtil {

    public static void godPredictPush(Long godUserId, String pushText, String pushUrl, PushService pushService) {
        String url = "";
        Map<String, String> content = new HashMap<>();
        content.put("pushUrl", pushUrl);
        PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, pushText, url, content);
        GodPredictTask task = new GodPredictTask(godUserId, CommonConstant.PUSH_CENTER_NOTICE_TYPE_FOOTBALL_GOLD,
                pushDto, pushService);
        ThreadPool.getInstance().getGodPredictTaskExec().submit(task);
    }
}
