package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.ResultVo;

import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/7.
 */
public interface ActivityService {
    void share(Long gameId, Long userId, String openId);

    Integer shareResult(Long gameId, Long userId);

    Map<String, String> getJsApiTicket(String url);

    Map<String, Integer> shareUsersToday(Long gameId);

    ResultVo commonTakePartInActivity(Integer activityId, Long userId);

    /* 抽奖活动*/

    Map<String, Object> drawLottery(Integer activityId, Long userId);

    Map<String, Object> drawLotteryInfo(Integer activityId, Long userId);

    void drawLotteryShare(Integer activityId, Long userId, String openId);

    void drawNumberShare(Integer activityId, Long userId, String openId);

    void drawLotteryShareOut(Integer activityId, Long userId);

    void updateActivityInfo(ActivityAwardLevel awardLevel, Integer activityId, Long userId, ActivityDateUserInfo
            activityDateUserInfo, ActivityUserInfo activityUserInfo, String dateId, Boolean mustRight);


    // rebuild
    void rebuildLastDayTimes();

    Map<String, Object> drawNumberAward(Integer activityId, String periodId, Integer levelId, Integer awardAmount);

    // 新的活动分享接口
    Map<String, Object> newShare(Integer activityId, Long userId);

    Map<String, Object> shareUserRegister(Integer activityId, Long fromUserId, Long userId, Integer vipType);

    Map<String, Object> shareUserIndex(Integer activityId, Long userId);

    // 春节活动 首页
    Map<String, Object> festivalIndex(Integer activityId, Long userId);

    // 答题分奖金
    Map<String, Object> questionAward(Integer activityId, Long userId);

    Map<String, Object> questionRightWithOutSign(Integer activityId, Long userId, String questionId);

    Map<String, Object> questionWrongWithOutSign(Integer activityId, Long userId, String questionId);

    Map<String, Object> rankWithOutSign(Integer activityId, Long userId);

    Map<String, Object> rankDistributeWithOutSign();

    Map<String, Object> clearTimesWithOutSign(Integer activityId, String mobile);

    // 注册送5智慧币
    void registerGiveWisdomCoin(Long userId, Integer activityId);

    // 检查是否赠送智慧币和会员资格
    Map<String, Object> checkIsGivenWisdomCoin(Long userId, Integer activityId);

    // 检查活动是否可用
    Boolean checkActivityIsEnabled(Integer activityId);

    boolean checkUserTakepartActivity(Long userId, Integer activityId, Integer delayDay);

    // 新手活动短信推送
    void activitySmsPush();

    /* 领取方案活动*/
    Map<String, Object> getActivityInfo(Integer activityId, Long userId);

    // 生产每期需要的号码
    void productProgram();

    Map<String, Object> drawNumber(Integer activityId, Long userId, Integer levelId);

    Map<String, Object> updateDrawNumber(Integer activityId, Integer levelId, Long userId, GamePeriod
            currentPeriod, Map<String, Object> awardResult);

    Map<String, Object> activityGiveCoupon2User(Long userId);

    // 会员过期短信提醒
    void vipExpireSmsPush();

    /**
     * 单关方案 活动 start
     **/

    String productDanguanProgram(String matchId, String programInfo, Long price, Long vipPrice);

    // 单关方案列表
    List<Map<String, Object>> danguanProgramList(Long userId);

    void getRightAwardNums(Map<String,Object> resultMap);

    Integer userDanguanTimes(Long userId);

    Integer checkVip(Long userId);

    // 单关方案战绩
    List<Map<String, Object>> danguanProgramHistory();

    // 卡列表
    List<Map<String, Object>> danguanProgramCards();

    Integer privilegeBuyProgram(Long userId, String matchId);

    Integer transPrivilegeBuyProgram(Long userId, String matchId);

    // 购买卡
    Map<String, Object> buyDanguanCard(Long userId, Integer payChannelId, String memo, Integer bankId, Integer
            clientType);

    void buyDuanguanCardBusiness(Long userId, Integer cardId);

    Boolean buyDuanguanCardBusinessCall(String cardId, String flowId);

    // 购买方案
    Map<String, Object> buyDanguanProgram(Long userId, Integer payChannelId, String memo, Integer bankId, Integer
            clientType);

    void buyDuanguanProgramBusiness(Long userId, String matchId);

    Boolean buyDuanguanProgramBusinessCall(String flowId, String matchId);

    // 比赛结束更新状态 定时任务
    void updateDanguanProgramStatus();

    /** 单关方案 活动 end**/
}
