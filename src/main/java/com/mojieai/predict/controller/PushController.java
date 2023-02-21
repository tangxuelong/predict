package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.dao.UserInfoDao;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.UserInfo;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.PushService;
import com.mojieai.predict.service.aliyunpush.ALiYunPush;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/7/20.
 */
@RequestMapping("/push")
@Controller
public class PushController extends BaseController {

    @Autowired
    private PushService pushService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserInfoDao userInfoDao;

    @RequestMapping("/pushToSingle")
    @ResponseBody
    public Object pushToSingle(@RequestParam String clientId) {
        Map<String, String> pushContent = new HashMap<>();
        Game game = GameCache.getGame(GameConstant.DLT);
        GamePeriod openingPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        pushContent.put("title", CommonConstant.getWinningNumberTxt(openingPeriod.getPeriodId(), game
                .getGameName()));
        AbstractGame abstractGame = GameFactory.getInstance().getGameBean(game.getGameId());
        pushContent.put("winNum", openingPeriod.getWinningNumbers());
        pushContent.put("type", "1");
        pushContent.put("pushUrl", abstractGame.getWinningNumberPushUrl());
        pushContent.put("text", "");

        PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, CommonConstant.getWinningNumberTxt(openingPeriod
                .getPeriodId(), game.getGameName()) + openingPeriod.getWinningNumbers().replace(CommonConstant
                        .COMMON_COLON_STR,
                CommonConstant.COMMON_VERTICAL_STR), game.getGameEn(), pushContent);
        pushService.pushToSingle(pushDto, clientId);
        return buildSuccJson();
    }

    @RequestMapping("/pushToAll")
    @ResponseBody
    public Object pushToAll(@RequestParam String title, @RequestParam String text, @RequestParam String url) {
        /*PushDto pushDto = new PushDto(title, text, url);
        pushService.pushToList(pushDto);*/
        return buildSuccJson();
    }

    @RequestMapping("/pushTest")
    @ResponseBody
    public Object pushToAll(@RequestParam String pushUrl, @RequestParam String deviceId) {
        Map<String, String> pushContent = new HashMap<>();
        Game game = GameCache.getGame(GameConstant.DLT);
        GamePeriod openingPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        pushContent.put("title", CommonConstant.getWinningNumberTxt(openingPeriod.getPeriodId(), game
                .getGameName()));
        AbstractGame abstractGame = GameFactory.getInstance().getGameBean(game.getGameId());
        pushContent.put("winNum", openingPeriod.getWinningNumbers());
        pushContent.put("type", "1");
        pushContent.put("pushUrl", pushUrl);
        pushContent.put("text", "");

        PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, CommonConstant.getWinningNumberTxt(openingPeriod
                .getPeriodId(), game.getGameName()) + openingPeriod.getWinningNumbers().replace(CommonConstant
                        .COMMON_COLON_STR,
                CommonConstant.COMMON_VERTICAL_STR), game.getGameEn(), pushContent);
        ALiYunPush aLiYunPush = new ALiYunPush();
        try {
            aLiYunPush.pushNoticeToIos(pushDto, "DEVICE", deviceId, "default");
        } catch (Exception e) {
            throw new BusinessException("push error");
        }

        return buildSuccJson();
    }


    @RequestMapping("/userOperate")
    @ResponseBody
    public Object userOperate(@RequestParam String deviceId, @RequestParam String gameEn, @RequestParam String type) {
        /* 开启或者关闭推送*/
        pushService.userOperate(deviceId, gameEn, type);
        Map<String, String> resultMap = new HashMap<>();
        String showText = Integer.valueOf(type) == CommonStatusEnum.YES.getStatus() ? "推送已开启" : "推送已关闭";
        resultMap.put("showText", showText);
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/check")
    @ResponseBody
    public Object check(@RequestParam(required = false) String clientId) {
        List<Map<String, String>> resultMapList = new ArrayList<>();
        /* 开启或者关闭推送*/
        List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIdList) {
            Map<String, String> resultMap = new HashMap<>();
            Game game = GameCache.getGame(gameId);
            if (null == game) {
                return buildErrJson("参数校验错误");
            }
            int isPush = pushService.checkPush(clientId, game.getGameId()) ? CommonStatusEnum.YES.getStatus()
                    : CommonStatusEnum.NO.getStatus();
            resultMap.put("gameEn", game.getGameEn());
            resultMap.put("gameName", game.getGameName());
            resultMap.put("isPush", String.valueOf(isPush));
            resultMapList.add(resultMap);
        }
        return buildSuccJson(resultMapList);
    }

    @RequestMapping("/pushCenter")
    @ResponseBody
    public Object pushCenter(String token, @RequestParam(required = false) String clientId, @RequestAttribute String
            clientType) {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> pushList = new ArrayList<>();
        //1.数字彩开奖通知
        Map<String, Object> digitMap = new HashMap<>();
        List<Map<String, String>> digitPushInfos = new ArrayList<>();
        List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIdList) {
            Map<String, String> resultMap = new HashMap<>();
            Game game = GameCache.getGame(gameId);
            if (null == game) {
                return buildErrJson("参数校验错误");
            }
            int isPush = pushService.checkPush(clientId, game.getGameId()) ? CommonStatusEnum.YES.getStatus()
                    : CommonStatusEnum.NO.getStatus();
            resultMap.put("pushType", game.getGameEn());
            resultMap.put("pushName", game.getGameName());
            resultMap.put("isPush", String.valueOf(isPush));
            resultMap.put("loginFlag", "0");
            digitPushInfos.add(resultMap);
        }

        digitMap.put("title", "数字彩开奖通知");
        digitMap.put("type", CommonConstant.PUSH_CENTER_NOTICE_TYPE_DIGIT);
        digitMap.put("pushInfo", digitPushInfos);
        pushList.add(digitMap);

        UserToken userToken = loginService.checkToken(token);
        String pushInfoStr = "";
        if (userToken != null) {
            UserInfo userInfo = userInfoDao.getUserInfo(userToken.getUserId());
            pushInfoStr = userInfo.getPushInfo();
        }
        Map<Integer, Integer> pushInfo = CommonUtil.getUserPushMap(pushInfoStr);
        //2.数字彩社区
        Map<String, Object> digitSocialMap = new HashMap<>();
        List<Map<String, String>> digitSocialPushInfos = new ArrayList<>();

        //2.1杀号提醒
        Map<String, String> killNum = getUserPushMap(pushInfo, CommonConstant.PUSH_TYPE_DIGIT_SOCIAL_KILL_NUM, "杀号提醒");
        digitSocialPushInfos.add(killNum);

        //2.2围号提醒
        Map<String, String> encircleNum = getUserPushMap(pushInfo, CommonConstant
                .PUSH_TYPE_DIGIT_SOCIAL_GOD_ENCIRCLE_NUM, "围号提醒");
        digitSocialPushInfos.add(encircleNum);
        digitSocialMap.put("title", "数字彩社区通知");
        digitSocialMap.put("type", CommonConstant.PUSH_CENTER_NOTICE_TYPE_DIGIT_SOCIAL);
        digitSocialMap.put("pushInfo", digitSocialPushInfos);
        pushList.add(digitSocialMap);

        //3.足彩通知
        Map<String, Object> sportSocialMap = new HashMap<>();
        List<Map<String, String>> sportSocialPushInfos = new ArrayList<>();

        //3.1大神发单提醒
        Map<String, String> godRecommend = getUserPushMap(pushInfo, CommonConstant
                .PUSH_TYPE_SPORTS_SOCIAL_GOD_RECOMMEND, "大神发单提醒");
        sportSocialPushInfos.add(godRecommend);

        sportSocialMap.put("title", "足彩通知");
        sportSocialMap.put("type", CommonConstant.PUSH_CENTER_NOTICE_TYPE_FOOTBALL_GOLD);
        sportSocialMap.put("pushInfo", sportSocialPushInfos);
        pushList.add(sportSocialMap);

        res.put("pushList", pushList);
        return buildSuccJson(res);
    }

    @RequestMapping("/operatePushSwitch")
    @ResponseBody
    public Object operatePushSwitch(@RequestParam String deviceId, @RequestParam Integer noticeType, @RequestParam
            String pushType, @RequestParam Integer operate, String token) {
        if (noticeType.equals(CommonConstant.PUSH_CENTER_NOTICE_TYPE_DIGIT)) {
            /* 开启或者关闭推送*/
            pushService.userOperate(deviceId, pushType, String.valueOf(operate));
        } else {
            UserToken userToken = loginService.checkToken(token);
            UserInfo userInfo = userInfoDao.getUserInfo(userToken.getUserId());
            String pushInfoStr = userInfo.getPushInfo();
            Map<Integer, Integer> pushInfo = CommonUtil.getUserPushMap(pushInfoStr);
            pushInfo.put(Integer.valueOf(pushType), operate);
            userInfo.setPushInfo(JSONObject.toJSONString(pushInfo));
            userInfoDao.update(userInfo);
        }

        Map<String, String> resultMap = new HashMap<>();
        String showText = Integer.valueOf(operate) == CommonStatusEnum.YES.getStatus() ? "推送已开启" : "推送已关闭";
        resultMap.put("showText", showText);
        return buildSuccJson(resultMap);
    }

    private Map<String, String> getUserPushMap(Map<Integer, Integer> pushInfo, Integer pushType, String pushName) {
        Map<String, String> result = new HashMap<>();
        Integer isPush = 0;
        if (pushInfo.get(pushType) > 0) {
            isPush = 1;
        }
        result.put("pushType", String.valueOf(pushType));
        result.put("pushName", pushName);
        result.put("isPush", String.valueOf(isPush));
        result.put("loginFlag", "1");
        return result;
    }
}
