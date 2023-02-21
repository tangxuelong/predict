package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.SignRewardCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.bo.DigitNavParams;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.entity.vo.IndexShowVo;
import com.mojieai.predict.enums.spider.SpiderSportInformationEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.HttpServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tangxuelong on 2017/7/22.
 */

@RequestMapping("/attach")
@Controller
public class AttachController extends BaseController {
    @Autowired
    private AttachService attachService;
    @Autowired
    private AppVersionService appVersionService;

    @Autowired
    private SendEmailService sendEmailService;
    @Autowired
    private UserSignService userSignService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private CompatibleService compatibleService;
    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("/versionCheck")
    @ResponseBody
    public Object versionCheck(@RequestParam String versionCode, @RequestParam(required = false) String clientType,
                               @RequestAttribute String channelId) {
        if (Strings.isBlank(versionCode)) {
            return buildErrJson(ResultConstant.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(clientType) && Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_4_1) {
            clientType = CommonConstant.CLIENT_TYPE_ANDRIOD + "";
        }
        Map<String, Object> resultData = appVersionService.versionControl(clientType, versionCode, channelId);
        return buildSuccJson(resultData);
    }

    @RequestMapping("/newVersionCheck")
    @ResponseBody
    public Object newVersionCheck(@RequestParam String versionCode, @RequestParam(required = false) String
            clientType, @RequestAttribute String channelId) {
        if (Strings.isBlank(versionCode)) {
            return buildErrJson(ResultConstant.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(clientType) && Integer.valueOf(versionCode) >= CommonConstant.VERSION_CODE_4_1) {
            clientType = CommonConstant.CLIENT_TYPE_ANDRIOD + "";
        }

        Map<String, Object> resultData = appVersionService.versionControl(clientType, versionCode, channelId);
        return buildSuccJson(resultData);
    }

//    private void versionControl(String clientType, String versionCode, Map<String, Object> resultData, String
//            versionCodeIni) {
//        Integer isUpdate = 0;
//        String versionUpdateUrlIni = ActivityIniConstant.VERSION_UPDATE_URL;
//        String versionUpdateText = ActivityIniConstant.VERSION_UPDATE_TEXT;
//        String isForceUpdate = ActivityIniConstant.IS_FORCE_UPDATE;
//        if (StringUtils.isNotBlank(clientType)) {
//            versionUpdateUrlIni += clientType;
//            versionUpdateText += clientType;
//            isForceUpdate += clientType;
//            versionCodeIni += clientType;
//        }
//        if (Integer.valueOf(versionCode) < Integer.valueOf(ActivityIniCache.getActivityIniValue(versionCodeIni, "1")
//        )) {
//            isUpdate = 1;
//
//            resultData.put("versionUpdateUrl", ActivityIniCache.getActivityIniValue(versionUpdateUrlIni));
//            resultData.put("versionUpdateText", ActivityIniCache.getActivityIniValue(versionUpdateText));
//            resultData.put("isForceUpdate", ActivityIniCache.getActivityIniValue(isForceUpdate,
//                    "0"));
//        }
//        resultData.put("isUpdate", isUpdate);
//        resultData.put("versionName", "V4.0");
//        resultData.put("versionCode", "22");
//    }

    @RequestMapping("/indexShow")
    @ResponseBody
    public Object indexShow(Integer type) {
        IndexShowVo indexShowVo = attachService.getIndexShow(type);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("indexShow", indexShowVo);
        return buildSuccJson(resultData);
    }

    @RequestMapping("/userFeedback")
    @ResponseBody
    public Object userFeedback(@RequestParam String content, @RequestParam(required = false) String contact,
                               @RequestParam(required = false) String token) {
        attachService.userFeedback(content, contact, token);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("showText", "提交成功！");
        return buildSuccJson(resultData);
    }

    @RequestMapping("/communication")
    @ResponseBody
    public Object communication(Integer type) {
        Map<String, Object> resultData = attachService.getCommunications(type);
        return buildSuccJson(resultData);
    }

    @RequestMapping("/communicationWithOutSign")
    @ResponseBody
    public Object communicationWithOutSign(Integer type) {
        return communication(1);
    }

    /* 闪屏*/
    @RequestMapping("/splashScreen")
    @ResponseBody
    public Object splashScreen(@RequestAttribute Integer clientType, @RequestAttribute Integer versionCode, String
            deviceId, String token, @RequestAttribute String visitorIp) {
        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            UserToken userToken = loginService.checkToken(token);
            if (userToken != null) {
                userId = userToken.getUserId();
            }
        }
        return buildSuccJson(getActivityPopContentByUserType(deviceId, userId, clientType, versionCode, visitorIp));
    }

    /* 首页弹窗*/
    @RequestMapping("/activityPopConfig")
    @ResponseBody
    public Object activityPopConfig(@RequestAttribute Integer clientType, @RequestAttribute Integer versionCode,
                                    String token, String deviceId) {
        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            UserToken userToken = loginService.checkToken(token);
            if (userToken != null) {
                userId = userToken.getUserId();
            }
        }

        return buildSuccJson(getActivityPopContent(deviceId, userId, ActivityIniConstant.INNER_SITE_POP_CONFIG,
                clientType, versionCode));
    }

    @RequestMapping("/userInfoPopup")
    @ResponseBody
    public Object userInfoPopup(@RequestParam(required = false) String token, @RequestParam(required = false) String
            deviceId, @RequestAttribute Integer clientType, @RequestAttribute Integer versionCode) {
        Long userId = null;
        UserToken userToken = loginService.checkToken(token);
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(getActivityPopContent(deviceId, userId, ActivityIniConstant.USER_INFO_POP_CONFIG,
                clientType, versionCode));
    }

    @RequestMapping("/indexFooterPopup")
    @ResponseBody
    public Object indexFooterPopup(@RequestParam(required = false) String token, @RequestParam(required = false)
            String deviceId, @RequestAttribute Integer clientType, @RequestAttribute Integer versionCode) {
        Long userId = null;
        UserToken userToken = loginService.checkToken(token);
        if (userToken != null) {
            userId = userToken.getUserId();
        }

        return buildSuccJson(getActivityPopContent(deviceId, userId, ActivityIniConstant.INDEX_FOOTER_POP_CONFIG,
                clientType, versionCode));
    }

    @RequestMapping("/iosReview")
    @ResponseBody
    public Object iosReview(@RequestAttribute Integer versionCode, @RequestAttribute Integer clientType,
                            @RequestAttribute String visitorIp) {
        return buildSuccJson(appVersionService.getIosReview(versionCode, clientType, visitorIp));
    }

    @RequestMapping("/agreement")
    @ResponseBody
    public Object agreement(@RequestParam String agreementType) {
        Map<String, Object> res = new HashMap<>();

        String agreementUrl = ActivityIniCache.getActivityIniValue(agreementType);
        res.put("agreementUrl", agreementUrl);
        return super.buildSuccJson(res);
    }

    @RequestMapping("/index_sign_pop")
    @ResponseBody
    public Object indexSignPop(String token, @RequestParam Integer manual, @RequestParam String deviceId,
                               @RequestAttribute Integer clientType, @RequestAttribute Integer versionCode) {

        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        Integer signType = SignRewardCache.SIGN_TYPE_CYCLE;
        Boolean activityStatus = activityService.checkActivityIsEnabled(201809002);
        if (activityStatus) {
            signType = SignRewardCache.SIGN_TYPE_CYCLE_ACTIVITY;
        }

        Map<String, Object> result = userSignService.getUserSignPop(userId, manual, deviceId, signType);
        compatibleService.temporaryIosSignControl(result, clientType, versionCode, userId);
        return buildSuccJson(result);
    }

    @RequestMapping("/digital_lottery_home_page")
    @ResponseBody
    public Object digitLotteryHomePage(@RequestAttribute Integer clientType, @RequestAttribute Integer versionCode,
                                       DigitNavParams digitNavParams) {
        return buildSuccJson(attachService.getDigitalLotteryHomePage(clientType, versionCode, digitNavParams));
    }

    @RequestMapping("/get_all_digit_nav")
    @ResponseBody
    public Object digitLotteryAll(@RequestParam String gameEn, String navIds) {

        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("彩种不存在");
        }
        return buildSuccJson(attachService.getAllDigitNav(game, navIds));
    }

    @RequestMapping("/get_http_sports_data")
    @ResponseBody
    public Object getHttpSportsData() {
        String key = RedisConstant.getHttpSportsInfoKey();
        Map<String, Object> result = redisService.kryoGet(key, HashMap.class);
        if (result == null || result.isEmpty()) {
            result = SpiderSportInformationEnum.NET_EASE.getSportInformation();
            redisService.kryoSetEx(key, 86400, result);
        }
        return buildSuccJson(result);
    }

    @RequestMapping("/loginAd")
    @ResponseBody
    public Object loginAd(@RequestAttribute Integer clientType, @RequestAttribute Integer versionCode) {

        Map<String, Object> result = new HashMap<>();
        result.put("img", "https://ojhwh2s98.qnssl.com/%E6%96%87%E6%A1%88%E6%8F%90%E7%A4%BA@3x.png");
        result.put("url", "");
        return buildSuccJson(result);
    }

    private Map<String, Object> getActivityPopContent(String deviceId, Long userId, String popKey, Integer
            clientType, Integer versionCode) {
        String img = "";
        Integer popCount = 0;
        String jumpUrl = "";
        int isShow = 0;
        Integer activityId = 0;
        Integer isActivity = null;
        Map<String, Object> result = new HashMap<>();

        String activityPopStr = ActivityIniCache.getActivityIniValue(popKey, ActivityIniConstant
                .INNER_SITE_POP_CONFIG_DEFAULT);
        Boolean iosVersionControl = Boolean.TRUE;
        Set<String> setKey = new HashSet<>();
        setKey.add(ActivityIniConstant.INDEX_FOOTER_POP_CONFIG);
        setKey.add(ActivityIniConstant.USER_INFO_POP_CONFIG);
        // 注释 IOS放出弹窗
//        if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && !setKey.contains(popKey)) {
//            iosVersionControl = Boolean.FALSE;
//        }
//        if (clientType.equals(1021) || clientType.equals(1031)) {
//            iosVersionControl = Boolean.FALSE;
//        }
        if (StringUtils.isNotBlank(activityPopStr) && iosVersionControl) {
            try {
                Map<String, Object> activityPopMap = JSONObject.parseObject(activityPopStr, HashMap.class);
                if (activityPopMap != null) {
                    isShow = activityPopMap.containsKey("isShow") ? Integer.valueOf(activityPopMap.get("isShow")
                            .toString()) : 0;
                    isActivity = activityPopMap.containsKey("isActivity") ? Integer.valueOf(activityPopMap.get
                            ("isActivity").toString()) : 0;
                    if (isActivity == 1) {
                        activityId = activityPopMap.containsKey("activityId") ? Integer.valueOf(activityPopMap.get
                                ("activityId").toString()) : 0;
                        Integer delayDay = activityPopMap.containsKey("delayDay") ? Integer.valueOf(activityPopMap.get
                                ("delayDay").toString()) : -1;
                        if (activityService.checkUserTakepartActivity(userId, activityId, delayDay)) {
                            isShow = 0;
                        }
                    }

                    img = activityPopMap.get("img").toString();
                    popCount = activityPopMap.containsKey("popCount") ? Integer.valueOf(activityPopMap.get("popCount")
                            .toString()) : 1;
                    jumpUrl = activityPopMap.get("jumpUrl").toString();
                }
            } catch (Exception e) {
                log.error("站内弹窗异常", e);
            }
        }

        //2.弹窗是否已经弹过
        if (isShow == 1 && StringUtils.isNotBlank(deviceId) && isActivity != null && isActivity != 0) {
            String innerSitePopLimitKey = RedisConstant.getInnerSitePopLimitKey(deviceId, DateUtil.formatDate(new
                    Date(), "yyyyMMdd"), activityId);
            if (redisService.isKeyByteExist(innerSitePopLimitKey)) {
                isShow = 0;
            } else {
                redisService.kryoSetEx(innerSitePopLimitKey, 172800, 1);
            }
        }

        if (isShow == 1 && StringUtils.isBlank(img)) {
            isShow = 0;
        }

        result.put("imgUrl", img);
        result.put("popCount", popCount);
        result.put("jumpUrl", jumpUrl);
        result.put("isShow", isShow);
        result.put("delaySecond", 90);
        return result;
    }

    private Map<String, Object> getActivityPopContentByUserType(String deviceId, Long userId, Integer clientType,
                                                                Integer versionCode, String visitorIp) {
        String img = "";
        Integer popCount = 0;
        String jumpUrl = "";
        int isShow = 0;
        Integer activityId = 0;
        Integer isActivity = null;
        Map<String, Object> result = new HashMap<>();
        Integer userLotteryType = userInfoService.getUserLotteryType(userId);
        String userType = "digit";
        if (userLotteryType != null && userLotteryType.equals(CommonConstant.LOTTERY_TYPE_SPORTS)) {
            userType = "sports";
        }
        String activityPopStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant.SPLASH_SCREEN_CONFIG,
                ActivityIniConstant.INNER_SITE_POP_CONFIG_DEFAULT);

        if (StringUtils.isNotBlank(activityPopStr) && getIosShow(clientType, versionCode, visitorIp)) {
            try {
                Map<String, Object> activityPopInfo = JSONObject.parseObject(activityPopStr, HashMap.class);
                if (activityPopInfo != null) {
                    isShow = activityPopInfo.containsKey("isShow") ? Integer.valueOf(activityPopInfo.get("isShow")
                            .toString()) : 0;

                    List<Map<String, Object>> activityPopList = (List<Map<String, Object>>) activityPopInfo.get
                            (userType);
                    Map<String, Object> activityPopMap = getActivityPopMap(activityPopList);

                    if (activityPopMap != null) {
                        isActivity = activityPopMap.containsKey("isActivity") ? Integer.valueOf(activityPopMap.get
                                ("isActivity").toString()) : 0;
                        if (isActivity == 1) {
                            activityId = activityPopMap.containsKey("activityId") ? Integer.valueOf(activityPopMap.get
                                    ("activityId").toString()) : 0;
                            Integer delayDay = activityPopMap.containsKey("delayDay") ? Integer.valueOf
                                    (activityPopMap.get
                                            ("delayDay").toString()) : -1;
                            if (activityService.checkUserTakepartActivity(userId, activityId, delayDay)) {
                                isShow = 0;
                            }
                        }

                        img = activityPopMap.get("img").toString();
                        popCount = activityPopMap.containsKey("popCount") ? Integer.valueOf(activityPopMap.get
                                ("popCount")
                                .toString()) : 1;
                        jumpUrl = activityPopMap.get("jumpUrl").toString();
                    }
                }
            } catch (Exception e) {
                log.error("站内弹窗异常", e);
            }
        }

        //2.弹窗是否已经弹过
        if (isShow == 1 && StringUtils.isNotBlank(deviceId) && isActivity != null && isActivity != 0) {
            String innerSitePopLimitKey = RedisConstant.getInnerSitePopLimitKey(deviceId, DateUtil.formatDate(new
                    Date(), "yyyyMMdd"), activityId);
            if (redisService.isKeyByteExist(innerSitePopLimitKey)) {
                isShow = 0;
            } else {
                redisService.kryoSetEx(innerSitePopLimitKey, 172800, 1);
            }
        }

        if (isShow == 1 && StringUtils.isBlank(img)) {
            isShow = 0;
        }


        result.put("imgUrl", img);
        result.put("popCount", popCount);
        result.put("jumpUrl", jumpUrl);
        result.put("isShow", isShow);
        result.put("delaySecond", 90);
        return result;
    }

    private Boolean getIosShow(Integer clientType, Integer versionCode, String visitorIp) {
        Boolean iosControl = Boolean.TRUE;
        Map<String, Object> iosReview = appVersionService.getIosReview(versionCode, clientType, visitorIp);
        if (iosReview != null && iosReview.containsKey("iosReview")) {
            Integer flag = Integer.valueOf(iosReview.get("iosReview").toString());
            if (flag.equals(CommonConstant.IOS_REVIEW_STATUS_WAIT)) {
                iosControl = Boolean.FALSE;
            }
        }
        return iosControl;
    }

    private Map<String, Object> getActivityPopMap(List<Map<String, Object>> activityPopList) {
        if (activityPopList == null || activityPopList.size() == 0) {
            return null;
        }
        Map<String, Object> result = null;
        Integer weight = 0;
        for (Map<String, Object> popMap : activityPopList) {
            if (!Integer.valueOf(popMap.get("status").toString()).equals(1)) {
                continue;
            }
            Timestamp endTime = DateUtil.formatString(popMap.get("endTime").toString(), "yyyy-MM-dd HH:mm:ss");
            if (endTime != null && DateUtil.compareDate(endTime, DateUtil.getCurrentTimestamp())) {
                continue;
            }
            Integer tempWeight = Integer.valueOf(popMap.get("weight").toString());
            if (result == null || weight < tempWeight) {
                result = popMap;
                weight = tempWeight;
            }
        }
        return result;
    }

//    private List<Map<String, Long>> rebuildRankBlackIp() {
//        List<Map<String, Long>> result = new ArrayList<>();
//        String url = "/data/mojiecp/predict/src/main/resources/black_range_ip.txt";
//
//        File file = new File(url);
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(file));
//            String line = "";
//            while ((line = reader.readLine()) != null) {
//                Map<String, Long> temp = new HashMap<>();
//                if (StringUtils.isBlank(line)) {
//                    continue;
//                }
//                String[] ipArr = line.split("-");
//                temp.put("min", Long.valueOf(ipArr[0]));
//                temp.put("max", Long.valueOf(ipArr[1]));
//                result.add(temp);
//
//            }
//            redisService.kryoSetEx(RedisConstant.getIosReviewRangeIpKey(), 604800, result);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
}
