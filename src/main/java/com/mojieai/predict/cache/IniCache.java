package com.mojieai.predict.cache;


import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.IniDao;
import com.mojieai.predict.entity.po.Ini;
import com.mojieai.predict.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IniCache {
    private static final Logger log = LogConstant.commonLog;

    private static Map<String, String> iniValueMap = new HashMap<>();

    @Autowired
    private IniDao iniDao;

    private IniCache() {
    }

    public void init() {
        log.info("init ini");
        refresh();
    }

    public void refresh() {
        Map<String, Ini> tempIniMap = new HashMap<>();
        Map<String, String> tempIniValueMap = new HashMap<String, String>();
        List<Ini> iniList = iniDao.getAllIni();
        if (iniList != null) {
            for (Ini ini : iniList) {
                tempIniMap.put(ini.getIniName(), ini);
                tempIniValueMap
                        .put(ini.getIniName(), ini.getIniValue());
            }
        }
        iniValueMap = tempIniValueMap;
        log.info("refresh " + (iniList == null ? 0 : iniList.size()) + " inis");
    }

    public static String getIniValue(String key) {
        if (!iniValueMap.containsKey(key)) {
            throw new BusinessException("no ini found for :" + key);
        }
        return iniValueMap.get(key);
    }

    public static String getIniValue(String key, String defaultValue) {
        if (iniValueMap.containsKey(key)) {
            return iniValueMap.get(key);
        } else if (StringUtils.isNotBlank(defaultValue)) {
            return defaultValue;
        } else {
            throw new BusinessException("no ini found for :" + key);
        }
    }

    public static int getIniIntValue(String key, String defaultValue) {
        String iniValue = getIniValue(key, defaultValue);
        int result;
        try {
            result = Integer.parseInt(iniValue);
        } catch (NumberFormatException e) {
            throw new BusinessException("not found an int for ini :" + key);
        }
        return result;
    }

    public static int getIniIntValue(String key, int defaultValue) {
        int result = defaultValue;
        if (iniValueMap.containsKey(key)) {
            String iniValue = getIniValue(key);
            try {
                result = Integer.parseInt(iniValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return result;
    }

    public static long getIniLongValue(String key, String defaultValue) {
        String iniValue = getIniValue(key, defaultValue);
        long result;
        try {
            result = Long.parseLong(iniValue);
        } catch (NumberFormatException e) {
            throw new BusinessException("not found an int for ini :" + key);
        }
        return result;
    }

    public static long getIniLongValue(String key, long defaultValue) {
        long result = defaultValue;
        if (iniValueMap.containsKey(key)) {
            String iniValue = getIniValue(key);
            try {
                result = Long.parseLong(iniValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return result;
    }
}