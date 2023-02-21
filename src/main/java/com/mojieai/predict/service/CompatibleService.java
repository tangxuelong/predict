package com.mojieai.predict.service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/9/6.
 */
public interface CompatibleService {

    /* 红球杀三码修改*/
    void killThreeRed(Map<String, Object> resultMap, HttpServletRequest request);

    /*三连号版本控制*/
    void continueNum(Map<String, Object> periodList, HttpServletRequest request);

    /* 杀号列表版本控制*/
    void getKillNumListByPeriodId(Map<String, Object> result, long gameId, String periodId, String versionCode);

    /* ios杀号列表userid转换*/
    void killNumListCompateUserId(Map<String, Object> result, String versionCode, Integer clientType, Long gameId);

    /* 关注人杀号列表版本控制*/
    void followsKillNumList(Map<String, Object> result, Long gameId, String versionCode);

    void followsKillNumListCompateUserId(long gameId, Map<String, Object> result, String versionCode, Integer
            clientType);

    /* 我的围号最新期标记红*/
    void myEncirclesV2_3(Map result, long gameId, String versionCode);

    /* 经典围号兼容*/
    void classicEncircleListCompateUserId(long gameId, Map<String, Object> result, String versionCode);

    /* 杀号详情*/
    void killNumDetail(Long gameId, String periodId, Map<String, Object> result, String versionCode);

    void killNumDetailCompateUserId(long gameId, Map<String, Object> result, String versionCode);

    /* 我的杀号最新期标红*/
    void myKillNumsV2_3(Map result, Long gameId, String versionCode);

    /* 关注和粉丝列表userId 加str*/
    void followListCompate(Map<String, Object> resultMap, String versionCode, Integer clientType);

    /* 支付列表*/
    Map<String, Object> exchangeDefaultPayChannel(Long userId, Integer clientType, Integer versionCode, String
            payAmount, Map<String, Object> res);

    List<Map<String, Object>> setDefaultChannel(Long userId, List<Map<String, Object>> paymentList, Long price);

    /* 预测首页方案顺序*/
    Map<String, Object> programChangeOrderBug(Map<String, Object> res, Integer clientId, String versionCode);

    void temporaryIosSignControl(Map<String, Object> result, Integer clientType, Integer versionCode, Long userId);

    void sportsRecommendRemunerationControl(Map<String, Object> result, Integer versionCode);

    Map<String, Object> iosReviewNotShowWorldCup(Map<String, Object> result, Integer versionCode, Integer clientType);

    void recommendListWorldCup(Map<String, Object> res, Integer versionCode, Integer clientType);

    void iosReviewMatchPredict(Map<String, Object> res, Integer versionCode, Integer clientType);

    void userPurchaseSportsIosReview(Map<String, Object> res, Integer versionCode, Integer clientType);

    void sportsSocialPersonCenterIosReview(Map<String, Object> res, Integer versionCode, Integer clientType);
}
