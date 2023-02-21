package com.mojieai.predict.service;

import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PushTrigger;

import java.util.List;

/**
 * Created by tangxuelong on 2017/7/20.
 */

public interface PushService {
    void pushToSingle(PushDto pushDto, String clientId);

    void pushToList(PushDto pushDto, GamePeriod period);

    void pushToListPart(List<String> clientIdList, PushDto pushDto, String pushType);

    void pushToList(PushDto pushDto);

    /* 开奖推送*/
    void winningNumberPush();

    /* 复制名单缓存*/
    void copyPushUsers();

    void userOperate(String deviceId, String gameEn, String type);

    void rebuildClientIdList();

    Boolean checkPush(String clientId, Long gameId);

    void createPushTask(PushTrigger pushTrigger);

    void triggerPush();

    Integer godPredictPush(Long userId, Integer pushType, PushDto pushDto);
}
