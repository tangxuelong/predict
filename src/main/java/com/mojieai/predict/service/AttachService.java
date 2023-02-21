package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.DigitNavParams;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.vo.IndexShowVo;

import java.util.Map;

/**
 * Created by tangxuelong on 2017/7/24.
 */
public interface AttachService {
    Boolean versionCheck(String versionCode);

    IndexShowVo getIndexShow(Integer type);

    Map<String, Object> getCommunications(Integer type);

    void userFeedback(String content, String contact, String token);

    void userFeedBackCheck();

    void getThirdWinningNumberUpdate();

    Map<String, Object> getDigitalLotteryHomePage(Integer clientType, Integer versionCode, DigitNavParams digitNavParams);

    Map<String,Object> getAllDigitNav(Game game, String navIds);
}
