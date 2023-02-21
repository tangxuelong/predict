package com.mojieai.predict.service.aliyunpush;

import com.alibaba.druid.support.json.JSONUtils;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.push.model.v20160801.PushRequest;
import com.aliyuncs.push.model.v20160801.PushResponse;
import com.aliyuncs.utils.ParameterHelper;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.exception.BusinessException;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * Created by tangxuelong on 2017/11/9.
 */
public class ALiYunPush extends BasePush {

    protected Logger log = LogConstant.commonLog;

    @Override
    public void init() {
        super.init();
    }

    public ALiYunPush() {
        init();
    }

    /**
     * 推送高级接口
     * <p>
     * 参见文档 https://help.aliyun.com/document_detail/48089.html
     * //
     */
    public void advancedPush(PushDto pushDto, Long appKey, String messageType, String pushType, String targetValue,
                             String pushFromType, String deviceType)
            throws Exception {

        PushRequest pushRequest = new PushRequest();
        pushRequest.setProtocol(ProtocolType.HTTPS);
        pushRequest.setMethod(MethodType.POST);
        pushRequest.setAppKey(appKey);
        pushRequest.setPushType(messageType); // 消息类型 MESSAGE NOTICE
        pushRequest.setDeviceType(deviceType); // 设备类型 ANDROID iOS ALL.
        //pushRequest.setTarget("DEVICE"); //推送目标: DEVICE:按设备推送 ALIAS : 按别名推送 ACCOUNT:按帐号推送  TAG:按标签推送; ALL: 广播推送
        //pushRequest.setTargetValue(deviceIds); //根据Target来设定，如Target=DEVICE, 则对应的值为 设备id1,设备id2. 多个值使用逗号分隔.
        // (帐号与设备有一次最多100个的限制)
        pushRequest.setTarget(pushType);
        //推送目标: device:推送给设备; account:推送给指定帐号,tag:推送给自定义标签; all: 推送给全部
        pushRequest.setTargetValue(targetValue); //根据Target来设定，如Target=device, 则对应的值为 设备id1,设备id2. 多个值使用逗号分隔.
        // (帐号与设备有一次最多100个的限制)

        // 推送配置
        pushRequest.setTitle(pushDto.getTitle()); // 消息的标题
        pushRequest.setBody(pushDto.getText()); // 消息的内容

        // 推送配置: iOS
        if (deviceType.equals("iOS")) {
            //pushRequest.setIOSBadge(0); // iOS应用图标右上角角标
            pushRequest.setIOSBadgeAutoIncrement(true);
            pushRequest.setIOSMusic("default"); // iOS通知声音
            if (pushFromType.equals("killPush")) {
                pushRequest.setIOSMusic("push.wav"); // iOS通知声音
            }
            pushRequest.setIOSApnsEnv(IniCache.getIniValue(IniConstant.ALIYUN_PUSH_IOS_DEV, "DEV"));
            //iOS的通知是通过APNs中心来发送的，需要填写对应的环境信息。'DEV': 表示开发环境 // 'PRODUCT': // 表示生产环境
            pushRequest.setIOSRemind(true); //
            // 消息推送时设备不在线（既与移动推送的服务端的长连接通道不通），则这条推送会做为通知，通过苹果的APNs通道送达一次。注意：**离线消息转通知仅适用于`生产环境`**
            //pushRequest.setIOSRemindBody("PushRequest summary"); // iOS消息转通知时使用的iOS通知内容，仅当iOSApnsEnv=`PRODUCT` &&
            // iOSRemind为true时有效
            pushRequest.setIOSExtParameters(JSONUtils.toJSONString(pushDto.getContent())); //通知的扩展属性(注意 : 该参数要以json
            // map的格式传入,否则会解析出错)
        }


        // 推送配置: Android
        if (deviceType.equals("ANDROID")) {
            pushRequest.setAndroidOpenType("ACTIVITY");
            pushRequest.setAndroidNotifyType("SOUND");
            pushRequest.setAndroidMusic("default");
            //pushRequest.setAndroidActivity("com.mjanroidlottery.business.main.SplashActivity");
            pushRequest.setAndroidPopupActivity("com.mjanroidlottery.push.PopupPushActivity");
            pushRequest.setAndroidPopupTitle(pushDto.getTitle());
            pushRequest.setAndroidPopupBody(pushDto.getText());
            pushRequest.setAndroidNotificationBarType(50);
            pushRequest.setAndroidNotificationBarPriority(2);
            pushRequest.setAndroidExtParameters(JSONUtils.toJSONString(pushDto.getContent()));
        }

        // 推送控制
        Date pushDate = new Date(System.currentTimeMillis()); // 30秒之间的时间点, 也可以设置成你指定固定时间
        String pushTime = ParameterHelper.getISO8601Time(pushDate);
        //pushRequest.setPushTime(pushTime); // 延后推送。可选，如果不设置表示立即推送
        String expireTime = ParameterHelper.getISO8601Time(new Date(System.currentTimeMillis() + 12 * 3600 * 1000));
        // 12小时后消息失效, 不会再发送
        pushRequest.setExpireTime(expireTime);
        pushRequest.setStoreOffline(true); // 离线消息是否保存,若保存, 在推送时候，用户即使不在线，下一次上线则会收到


        PushResponse pushResponse = client.getAcsResponse(pushRequest);
        System.out.printf("RequestId: %s, MessageID: %s\n", pushResponse.getRequestId(), pushResponse.getMessageId());


    }

    /* android推送*/
    public void pushNoticeToAndroid(PushDto pushDto, String pushType, String targetValue, String pushFromType) {
        try {
            String androidAppKey = IniCache.getIniValue(IniConstant.ALIYUN_PUSH_APP_KEY_ANDROID, "24689647");
            advancedPush(pushDto, Long.parseLong(androidAppKey), IniConstant.ALIYUN_PUSH_TYPE_NOTICE, pushType,
                    targetValue, pushFromType, "ANDROID");
        } catch (Exception e) {
            log.error("pushToAll error" + e.getStackTrace());
            throw new BusinessException("pushToAll error" + e.getStackTrace());
        }
    }

    /* ios推送*/
    public void pushNoticeToIos(PushDto pushDto, String pushType, String targetValue, String pushFromType) {
        try {
            String iosAppKey = IniCache.getIniValue(IniConstant.ALIYUN_PUSH_APP_KEY_IOS, "24689714");
            advancedPush(pushDto, Long.parseLong(iosAppKey), IniConstant.ALIYUN_PUSH_TYPE_NOTICE, pushType,
                    targetValue, pushFromType, "iOS");
        } catch (Exception e) {
            log.error("pushToAll error" + e.getStackTrace());
            throw new BusinessException("pushToAll error" + e.getStackTrace());
        }
    }

    /* 全员推送通知*/
    public void pushNoticeToAll(PushDto pushDto, String pushType, String targetValue, String pushFromType) {
        try {
            pushNoticeToAndroid(pushDto, pushType, targetValue, pushFromType);
            pushNoticeToIos(pushDto, pushType, targetValue, pushFromType);
        } catch (Exception e) {
            log.error("pushToAll error" + e.getStackTrace());
            throw new BusinessException("pushToAll error" + e.getStackTrace());
        }
    }
}
