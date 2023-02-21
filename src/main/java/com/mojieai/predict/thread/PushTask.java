package com.mojieai.predict.thread;

import com.alibaba.druid.support.json.JSONUtils;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.ListMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.base.payload.APNPayload;
import com.gexin.rp.sdk.base.payload.MultiMedia;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.dto.PushDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by tangxuelong on 2017/7/20.
 */
public class PushTask implements Callable {
    private PushDto pushDto;
    private List<String> clientIdList;

    private static final Logger log = LogConstant.commonLog;

    public PushTask(PushDto pushDto, List<String> clientIdList) {
        this.pushDto = pushDto;
        this.clientIdList = clientIdList;
    }

    @Override
    public Integer call() {
        String hostAndroid = IniCache.getIniValue(IniConstant.PUSH_HOST);

        String appKeyAndroid = IniCache.getIniValue(IniConstant.PUSH_APP_KEY);

        String masterSecretAndroid = IniCache.getIniValue(IniConstant.PUSH_MASTER_SECRET);

        String appIdAndroid = IniCache.getIniValue(IniConstant.PUSH_APP_ID);

        pushToList(hostAndroid, appKeyAndroid, masterSecretAndroid, appIdAndroid);

        String hostIos = IniCache.getIniValue(IniConstant.PUSH_HOST_IOS);

        String appKeyIos = IniCache.getIniValue(IniConstant.PUSH_APP_KEY_IOS);

        String masterSecretIos = IniCache.getIniValue(IniConstant.PUSH_MASTER_SECRET_IOS);

        String appIdIos = IniCache.getIniValue(IniConstant.PUSH_APP_ID_IOS);

        pushToList(hostIos, appKeyIos, masterSecretIos, appIdIos);

        return 0;
    }

    public void pushToList(String host, String appKey, String masterSecret, String appId) {
        // 配置返回每个用户返回用户状态，可选
        System.setProperty("gexin_pushList_needDetails", "true");
        // 配置返回每个别名及其对应cid的用户状态，可选
        // System.setProperty("gexin_pushList_needAliasDetails", "true");
        IGtPush push = new IGtPush(host, appKey, masterSecret);
        // 通知透传模板
        TransmissionTemplate template = getTemplate(appId, appKey);
        ListMessage message = new ListMessage();
        message.setData(template);
        // 设置消息离线，并设置离线时间
        message.setOffline(true);
        // 离线有效时间，单位为毫秒，可选
        message.setOfflineExpireTime(24 * 1000 * 3600);
        // 配置推送目标

        List targets = new ArrayList();
        for (String clientId : clientIdList) {
            Target target = new Target();
            target.setAppId(appId);
            target.setClientId(clientId);
            targets.add(target);
        }
        // taskId用于在推送时去查找对应的message
        String taskId = push.getContentId(message);
        IPushResult ret = push.pushMessageToList(taskId, targets);
        // 判断异常情况添加报警
        String resp = ret.getResponse().toString();
        log.info(resp);
        if (StringUtils.isNotBlank(resp) && resp.split(CommonConstant.COMMA_SPLIT_STR).length > 0) {
            if (!resp.split(CommonConstant.COMMA_SPLIT_STR)[0].equals(ResultConstant.SUCCESS)) {
                log.info("推送已结束，推送结果：" + ret.getResponse().toString());
            }
        }
    }

    public TransmissionTemplate getTemplate(String appId, String appKey) {
        TransmissionTemplate template = new TransmissionTemplate();
        template.setAppId(appId);
        template.setAppkey(appKey);
        template.setTransmissionContent(JSONUtils.toJSONString(pushDto.getContent()));
        template.setTransmissionType(2);
        APNPayload payload = new APNPayload();
        //在已有数字基础上加1显示，设置为-1时，在已有数字上减1显示，设置为数字时，显示指定数字
        payload.setAutoBadge("+1");
        payload.setContentAvailable(1);
        payload.setSound("default");
        payload.setCategory("$由客户端定义");
        payload.addCustomMsg("content", JSONUtils.toJSONString(pushDto.getContent()));

        //简单模式APNPayload.SimpleMsg
        //payload.setAlertMsg(new APNPayload.SimpleAlertMsg("hello"));

        //字典模式使用APNPayload.DictionaryAlertMsg
        payload.setAlertMsg(getDictionaryAlertMsg());

        // 添加多媒体资源
        payload.addMultiMedia(new MultiMedia().setResType(MultiMedia.MediaType.video)
                .setResUrl("http://ol5mrj259.bkt.clouddn.com/test2.mp4")
                .setOnlyWifi(true));

        template.setAPNInfo(payload);
        return template;
    }

    private APNPayload.DictionaryAlertMsg getDictionaryAlertMsg() {
        APNPayload.DictionaryAlertMsg alertMsg = new APNPayload.DictionaryAlertMsg();
        alertMsg.setBody(pushDto.getText());
        alertMsg.setActionLocKey("滑动来解锁");
        alertMsg.setLocKey(pushDto.getText());//这个是显示的内容字段
        alertMsg.addLocArg("loc-args");
        alertMsg.setLaunchImage("launch-image");
        // iOS8.2以上版本支持
        alertMsg.setTitle(pushDto.getTitle());
        alertMsg.setTitleLocKey("智慧彩票预测");
        alertMsg.addTitleLocArg("TitleLocArg");
        return alertMsg;
    }

}
