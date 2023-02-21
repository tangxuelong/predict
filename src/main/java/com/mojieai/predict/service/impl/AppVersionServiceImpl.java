package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.AppChannelDao;
import com.mojieai.predict.dao.AppChannelVersionDao;
import com.mojieai.predict.dao.AppVersionDao;
import com.mojieai.predict.entity.po.AppChannel;
import com.mojieai.predict.entity.po.AppChannelVersion;
import com.mojieai.predict.entity.po.AppVersion;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.AppVersionService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.HttpServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class AppVersionServiceImpl implements AppVersionService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private AppVersionDao appVersionDao;
    @Autowired
    private AppChannelDao appChannelDao;
    @Autowired
    private AppChannelVersionDao appChannelVersionDao;
    @Autowired
    private RedisService redisService;

    @Override
    public Map<String, Object> versionControl(String clientType, String versionCode, String channelName) {
        Map<String, Object> resultData = new HashMap<>();
        String versionName = "";
        Integer isUpdate = 0;
        String versionCodeRes = "";
        String versionUpdateUrlAd = "";
        String versionUpdateTextAd = "";
        String isForceUpdateAd = "0";
        if (clientType != null && Integer.valueOf(clientType).equals(CommonConstant.CLIENT_TYPE_ANDRIOD) && Integer
                .valueOf(versionCode) < CommonConstant.VERSION_CODE_4_1) {
            String versionCodeIni = ActivityIniConstant.NEW_VERSION;

            String versionUpdateUrlIni = ActivityIniConstant.VERSION_UPDATE_URL;
            String versionUpdateText = ActivityIniConstant.VERSION_UPDATE_TEXT;
            String isForceUpdate = ActivityIniConstant.IS_FORCE_UPDATE;
            if (StringUtils.isNotBlank(clientType)) {
                versionUpdateUrlIni += clientType;
                versionUpdateText += clientType;
                isForceUpdate += clientType;
                versionCodeIni += clientType;
            }
            if (Integer.valueOf(versionCode) < Integer.valueOf(ActivityIniCache.getActivityIniValue(versionCodeIni,
                    "1"))) {
                isUpdate = 1;
                versionUpdateUrlAd = ActivityIniCache.getActivityIniValue(versionUpdateUrlIni);
                versionUpdateTextAd = ActivityIniCache.getActivityIniValue(versionUpdateText);
                isForceUpdateAd = ActivityIniCache.getActivityIniValue(isForceUpdate, "0");
                versionName = "V4.1";
            }

            isForceUpdateAd = isForceUpdateAd == null ? "0" : isForceUpdateAd;
            versionCodeRes = versionCode;
        } else {
            versionCodeRes = versionCode;
            if (appVersionCheck(Integer.valueOf(versionCode), clientType)) {
                AppVersion appVersion = appVersionDao.getLatestAppVersionByClientId(Integer.valueOf(clientType));
                AppChannel appChannel = appChannelDao.getAppChannelByChannelName(channelName);
                if (appVersion != null && appChannel != null) {
                    AppChannelVersion appChannelVersion = appChannelVersionDao.getAppChannelVersionByUniqueKey(appChannel
                            .getChannelId(), appVersion.getVersionId());
                    if (appChannelVersion != null && appVersion.getVersionCode() > Integer.valueOf(versionCode)) {
                        isUpdate = 1;
                    }
                    versionName = "V" + appVersion.getVersionCodeName();
                    versionCodeRes = String.valueOf(appVersion.getVersionCode());
//                    versionUpdateUrlAd = appChannelVersion == null ? "" : appChannelVersion.getAppUrl();
                    if (null != appChannelVersion){
                        versionUpdateUrlAd = appChannelVersion.getAppUrl();
                    }else {
                        versionUpdateUrlAd = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                                .VERSION_UPDATE_URL);
                    }

                    versionUpdateTextAd = appChannelVersion == null ? "" : appChannelVersion.getUpgradeDesc();
                    isForceUpdateAd = appChannelVersion == null ? "0" : appChannelVersion.getForceUpgrade() + "";
                }
            }
        }

        resultData.put("versionUpdateUrl", versionUpdateUrlAd);
        resultData.put("versionUpdateText", versionUpdateTextAd);
        resultData.put("isForceUpdate", isForceUpdateAd);
        resultData.put("isUpdate", isUpdate);
        resultData.put("versionName", versionName);
        resultData.put("versionCode", versionCodeRes);
        return resultData;
    }

    private Boolean appVersionCheck(Integer versionCode, String clientType) {
        if (StringUtils.isBlank(clientType)) {
            return Boolean.FALSE;
        }
        if (Integer.valueOf(clientType) % 2 == 0) {
            return Boolean.TRUE;
        }
        Map<String, Object> iosReviewMap = getIosReview(versionCode, Integer.valueOf(clientType), "127.0.0.1");
        if (iosReviewMap != null && iosReviewMap.containsKey("iosReview")) {
            if (iosReviewMap.get("iosReview").toString().equals(CommonConstant.IOS_REVIEW_STATUS_WAIT + "")) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Map<String, Object> getAllVersion(Integer clientId) {
        Map<String, Object> result = new HashMap<>();
        List<AppVersion> versions = appVersionDao.getAllAppVersion(clientId);

        result.put("versions", versions);
        return result;
    }

    @Override
    public Map<String, Object> getIosReview(Integer versionCode, Integer clientType, String visitorIp) {
        Map<String, Object> iosReviewMap = new HashMap<>();
        if (clientType != null && clientType % 2 == 0) {
            iosReviewMap.put("iosReview", CommonConstant.IOS_REVIEW_STATUS_PASSED);
            return iosReviewMap;
        }

        Integer iosReview = CommonConstant.IOS_REVIEW_STATUS_WAIT;

        if (!checkBlackIp(visitorIp)) {
            AppVersion appVersion = appVersionDao.getLatestAppVersionByClientId(clientType);
            if (appVersion == null) {
                log.error("ios review clientId:" + clientType + " 未配置请及时配置");
            } else {
                iosReview = CommonConstant.IOS_REVIEW_STATUS_PASSED;
                if (appVersion.getVersionCode() <= versionCode) {
                    iosReview = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.IOS_REVIEW_FLAG + ":" +
                            clientType, CommonConstant.IOS_REVIEW_STATUS_WAIT);
                }
            }
        }
        iosReviewMap.put("iosReview", iosReview);
        return iosReviewMap;
    }

    @Override
    public Map<String, Object> addAppVersion(AppVersion appVersion) {
        Map<String, Object> res = new HashMap<>();
        AppVersion tempAppVersion = appVersionDao.getAppVersionByUnikey(appVersion.getClientId(), appVersion
                .getVersionCode());
        if (tempAppVersion == null) {
            appVersionDao.insert(appVersion);
        } else {
            appVersionDao.update(appVersion);
        }

        res.put("msg", "成功");
        return res;
    }

    @Override
    public Map<String, Object> updateForceUpgrade(Integer versionId, Integer forceUpgrade) {
        Map<String, Object> res = new HashMap<>();
        String msg = "保存失败";
        AppVersion tempAppVersion = appVersionDao.getAppVersionByPk(versionId);
        if (tempAppVersion != null) {
            tempAppVersion.setForceUpgrade(forceUpgrade);
            appVersionDao.update(tempAppVersion);
            msg = "保存成功";
        }
        res.put("msg", msg);
        return res;
    }

    private boolean checkBlackIp(String visitorIp) {
        if (StringUtils.isNotBlank(visitorIp)) {
            if (!redisService.isKeyByteExist(RedisConstant.getIosReviewIpKey())) {
                rebuildBlackIp();
            }
            if (redisService.kryoSismemberSet(RedisConstant.getIosReviewIpKey(), visitorIp)) {
                return true;
            }

            String httpRes = httpGetIpInfo(visitorIp);
            if (StringUtils.isNotBlank(httpRes)) {
                String httpStr = httpRes.substring(httpRes.indexOf("{"), httpRes.lastIndexOf("}") + 1);
                Map<String, Object> ipMap = JSONObject.parseObject(httpStr, HashMap.class);
                if (ipMap.containsKey("data") && ipMap.get("data") != null) {
                    List<Map<String, Object>> dataMap = (List<Map<String, Object>>) ipMap.get("data");
                    if (dataMap.size() > 0 && dataMap.get(0).containsKey("location") && dataMap.get(0).get
                            ("location").toString().contains("美国")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String httpGetIpInfo(String visitorIp) {
        String thirdHttpKey = RedisConstant.getHttpIpInfoKey(visitorIp);
        String result = redisService.kryoGet(thirdHttpKey, String.class);
        if (StringUtils.isBlank(result)) {
            String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api" + ".php?query=" + visitorIp +
                    "&co=&resource_id=6006&t=1530771424184&ie=utf8&oe=utf-8&cb=op_aladdin_callback&format=json&tn" +
                    "=baidu&cb=jQuery1102040881587526272245_1530763379134&_=1530763379160";
            result = HttpServiceUtils.sendRequest(url);
            if (StringUtils.isNotBlank(result)) {
                redisService.kryoSetEx(thirdHttpKey, 604800, result);
            }
        }
        return result;
    }

    private Set<String> rebuildBlackIp() {
        Set<String> result = new HashSet<>();
        String url = "/data/mojiecp/predict/src/main/resources/black_ip.txt";

        File file = new File(url);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            if (result.size() > 0) {
                redisService.kryoSAddSets(RedisConstant.getIosReviewIpKey(), result);
                redisService.expire(RedisConstant.getIosReviewIpKey(), 604800);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
