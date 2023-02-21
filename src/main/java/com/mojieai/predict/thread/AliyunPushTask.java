package com.mojieai.predict.thread;

import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.service.aliyunpush.ALiYunPush;

import java.util.concurrent.Callable;

/**
 * Created by tangxuelong on 2017/11/15.
 */
public class AliyunPushTask implements Callable {
    private PushDto pushDto;
    private String pushType;
    private String targetValue;
    private String pushFromType;

    public AliyunPushTask(PushDto pushDto, String pushType, String targetValue, String pushFromType) {
        this.pushDto = pushDto;
        this.pushType = pushType;
        this.targetValue = targetValue;
        this.pushFromType = pushFromType;
    }

    @Override
    public Object call() throws Exception {
        try {
            ALiYunPush aLiYunPush = new ALiYunPush();
            aLiYunPush.pushNoticeToAll(pushDto, pushType, targetValue, pushFromType);
        } catch (Exception e) {
            throw new BusinessException("AliyunPushTask error" + e.getStackTrace());
        }
        return 0;
    }
}
