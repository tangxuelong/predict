package com.mojieai.predict.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static String toJsonStr(String nameArray, String value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(nameArray, value);
        return jsonObject.toString();
    }

    public static <T> String toJsonStr(Object... args) {
        Map<String, T> map = CommonUtil.asMap(args);
        return JSONObject.toJSONString(map);
    }

    public static <T> String addJsonStr(String str, Object... args) {
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isNotBlank(str)) {
            jsonObject = JSONObject.parseObject(str);
        }
        Map<String, T> map = CommonUtil.asMap(args);
        jsonObject.putAll(map);
        return JSONObject.toJSONString(jsonObject);
    }

    public static String addJsonStr(String str, String addStr) {
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isNotBlank(str)) {
            jsonObject = JSONObject.parseObject(str);
        }
        if (StringUtils.isNotBlank(addStr)) {
            Map<String, Object> addStrMap = JSONObject.parseObject(addStr, HashMap.class);
            jsonObject.putAll(addStrMap);
        }
        return JSONObject.toJSONString(jsonObject);
    }
}
