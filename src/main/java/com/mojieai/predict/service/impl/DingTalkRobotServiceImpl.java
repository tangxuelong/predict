package com.mojieai.predict.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.service.DingTalkRobotService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by tangxuelong on 2017/9/14.
 */
@Service
public class DingTalkRobotServiceImpl implements DingTalkRobotService {
    private static final Logger log = LogConstant.commonLog;
    @Override
    public void sendMassageToAll(String title, String text, List<String> at) {
        try {
            String WEBHOOK_TOKEN = IniCache.getIniValue(IniConstant.DING_TALK_URL);

            HttpClient httpclient = HttpClients.createDefault();

            HttpPost httppost = new HttpPost(WEBHOOK_TOKEN);
            httppost.addHeader("Content-Type", "application/json; charset=utf-8");

            Map<String, Object> textMsgMap = new HashMap<>();
            textMsgMap.put("msgtype", "markdown");
            Map<String, String> markdown = new HashMap<>();
            markdown.put("title", title);
            markdown.put("text", text);
            textMsgMap.put("markdown", markdown);
            Map<String, Object> atMap = new HashMap<>();
            atMap.put("atMobiles", at);
            atMap.put("isAtAll", false);
            textMsgMap.put("at", atMap);
            String textMsg = JSONUtils.toJSONString(textMsgMap);
            StringEntity se = new StringEntity(textMsg, "utf-8");
            httppost.setEntity(se);

            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                System.out.println(result);
            }
        } catch (Exception e) {
            log.error("Ding talk robot is error");
        }
    }
}
