package com.mojieai.predict.cache;


import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.ActivityIniDao;
import com.mojieai.predict.entity.po.ActivityIni;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.*;

public class ActivityIniCache {
    private static final Logger log = LogConstant.commonLog;

    private static Map<String, String> activityIniValueMap = new HashMap<>();

    @Autowired
    private ActivityIniDao activityIniDao;

    private ActivityIniCache() {
    }

    public void init() {
        log.info("init activityIni");
        refresh();
    }

    public void refresh() {
        List<ActivityIni> activityIniList = activityIniDao.getAllIni();
        if (activityIniList != null) {
            for (ActivityIni activityIni : activityIniList) {
                activityIniValueMap.put(activityIni.getIniName(), activityIni.getIniValue());
            }
        }
        log.info("refresh " + (activityIniList == null ? 0 : activityIniList.size()) + " inis");
    }

    public static String getActivityIniValue(String key) {
        if (!activityIniValueMap.containsKey(key)) {
            throw new BusinessException("no ini found for :" + key);
        }
        return activityIniValueMap.get(key);
    }

    public static String getActivityIniValue(String key, String defaultValue) {
        if (activityIniValueMap.containsKey(key)) {
            return activityIniValueMap.get(key);
        }
        if (StringUtils.isNotBlank(defaultValue)) {
            return defaultValue;
        }
        {
            throw new BusinessException("no ini found for :" + key);
        }
    }

    public static Integer getActivityIniIntValue(String key, Integer defaultValue) {
        if (activityIniValueMap.containsKey(key)) {
            return Integer.valueOf(activityIniValueMap.get(key));
        }
        return defaultValue;
    }

    public static Integer getActivityIniIntValue(String key) {
        if (activityIniValueMap.containsKey(key)) {
            return Integer.valueOf(activityIniValueMap.get(key));
        }
        return null;
    }

    public static Boolean getActivityIniBooleanValue(String key, boolean defaultValue) {
        if (activityIniValueMap.containsKey(key)) {
            try {
                return Boolean.valueOf(activityIniValueMap.get(key));
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static List<Map<String, Object>> getActivityIniListValueByWeight(String key) {
        if (!activityIniValueMap.containsKey(key)) {
            return null;
        }
        String str = activityIniValueMap.get(key);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        List<Map<String, Object>> result = null;
        try {
            result = JSONObject.parseObject(str, ArrayList.class);
            if (result == null) {
                return result;
            }
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).containsKey("endTime") && result.get(i).get("endTime") != null) {
                    Timestamp endTime = DateUtil.formatString(result.get(i).get("endTime").toString(), "yyyy-MM-dd HH:mm:ss");
                    if (endTime != null && DateUtil.compareDate(endTime, DateUtil.getCurrentTimestamp())) {
                        result.remove(i);
                    }
                }
            }
            if (result.size() == 0) {
                return null;
            }

            if (!result.get(0).containsKey("weight")) {
                throw new BusinessException("获取权重失败");
            }
            Collections.sort(result, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Integer weight1 = o1 == null ? 0 : Integer.valueOf(o1.get("weight").toString());
                    Integer weight2 = o2 == null ? 0 : Integer.valueOf(o2.get("weight").toString());
                    return (weight2 - weight1);
                }
            });
        } catch (Exception e) {
            log.error("activity ini 获取 key:" + key + "异常", e);
        }

        return result;
    }

}