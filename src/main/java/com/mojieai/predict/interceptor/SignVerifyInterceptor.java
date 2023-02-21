package com.mojieai.predict.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by tangxuelong on 2017/7/6.
 */
public class SignVerifyInterceptor implements HandlerInterceptor {
    private static final Logger log = LogConstant.commonLog;

    @SuppressWarnings("rawtypes")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
            Exception {
        Map<String, Object> data;
        String plainTextSignKey = null;

        String signFlag = IniCache.getIniValue(IniConstant.COMPATIBLE_SIGN_FLAG, IniConstant.COMPATIBLE_SIGN_YES);
        if (signFlag.equals(IniConstant.COMPATIBLE_SIGN_NO)) {
            return true;
        }
        String secretKey = request.getHeader(CommonConstant.SIGN_CODE);
        if (StringUtils.isBlank(secretKey)) {
            log.info(String.format("暗码为空，params is [%s]", request.getParameterMap().toString()));
            data = CommonUtil.asMap("code", 3001, "msg", "签名错误", "resp", "");
        } else {
            String aesDecrypt = AESUtil.decrypt(secretKey, PropertyUtils.getProperty("signPublicKey"));
            try {
                Map signMap = JSONObject.parseObject(aesDecrypt, HashMap.class);
                plainTextSignKey = (String) signMap.get("plainTextSignKey");
                if (StringUtils.isBlank(plainTextSignKey)) {
                    log.info(String.format("解析暗码失败，params is [%s]", request.getParameterMap().toString()));
                    data = CommonUtil.asMap("code", 3001, "msg", "签名错误", "resp", "");
                } else {
                    Map<String, String[]> sortedParams = new TreeMap<>(request.getParameterMap());
                    StringBuffer sb = new StringBuffer();

                    for (Map.Entry<String, String[]> entry : sortedParams.entrySet()) {
                        if (StringUtils.isNotBlank(entry.getValue()[0])) {
                            sb.append(entry.getKey()).append(CommonConstant.COMMON_EQUAL_STR).append(entry.getValue()
                                    [0]).append(CommonConstant.COMMON_AND_STR);
                        }
                    }
                    sb.append(plainTextSignKey);
                    if (Md5Util.getMD5String(sb.toString()).equals(request.getHeader(CommonConstant.SIGN_STR))) {
                        return true;
                    } else {
                        data = CommonUtil.asMap("code", 3002, "msg", "验签失败", "resp", "");
                    }
                }
            } catch (Exception e) {
                if (IniConstant.COMPATIBLE_SIGN_YES.equals(IniCache.getIniValue(IniConstant.UPGRADE_SIGN_FLAG,
                        IniConstant.COMPATIBLE_SIGN_NO))) {
                    log.info(String.format("暗码解析异常--密钥升级过程中，params is [%s]", request.getParameterMap().toString()) + e);
                    data = CommonUtil.asMap("code", 3003, "msg", "网络异常，请重试", "resp", "");
                } else {
                    log.info(String.format("暗码解析异常，params is [%s]", request.getParameterMap().toString()) + e);
                    data = CommonUtil.asMap("code", 3001, "msg", "签名错误", "resp", "");
                }
            }
        }
        OutputUtils.renderJson(data);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView
            modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception
            ex) throws Exception {

    }
}
