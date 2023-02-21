package com.mojieai.predict.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.util.AESUtil;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.OutputUtils;
import com.mojieai.predict.util.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by tangxuelong on 2017/7/6.
 */
public class DecipherInterceptor extends HttpServletRequestWrapper implements HandlerInterceptor {
    private static final Logger log = LogConstant.commonLog;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     * @throws IllegalArgumentException if the request is null
     */
    public DecipherInterceptor(HttpServletRequest request) {
        super(request);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
            Exception {
        Map<String, Object> resp;
        String plainTextSignKey = null;

        String secretKey = request.getHeader(CommonConstant.SIGN_CODE);
        if (StringUtils.isBlank(secretKey) || Strings.isBlank(request.getParameter("data"))) {
            log.info(String.format("解密失败，params is [%s]", request.getParameter("data")));
            resp = CommonUtil.asMap("code", 3001, "msg", "签名错误", "resp", "");
        } else {
            String aesDecrypt = AESUtil.decrypt(secretKey, PropertyUtils.getProperty("signPublicKey"));
            try {
                Map signMap = JSONObject.parseObject(aesDecrypt, HashMap.class);
                plainTextSignKey = (String) signMap.get("plainTextSignKey");
                if (StringUtils.isBlank(plainTextSignKey)) {
                    log.info(String.format("解析暗码失败，params is [%s]", request.getParameterMap().toString()));
                    resp = CommonUtil.asMap("code", 3001, "msg", "签名错误", "resp", "");
                } else {
                    String data = AESUtil.decrypt(request.getParameter("data"), plainTextSignKey);
                    if (StringUtils.isBlank(data)) {
                        log.info(String.format("解析暗码失败，params is [%s]", request.getParameterMap().toString()));
                        resp = CommonUtil.asMap("code", 3002, "msg", "验签失败", "resp", "");
                    } else {
                        for (String param : data.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                                .COMMON_AND_STR)) {
                            String[] paramArr = param.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                                    .COMMON_EQUAL_STR);
                            if (paramArr.length >= 2) {
                                request.setAttribute(paramArr[0], paramArr[1]);
                            }
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                if (IniConstant.COMPATIBLE_SIGN_YES.equals(IniCache.getIniValue(IniConstant.UPGRADE_SIGN_FLAG,
                        IniConstant.COMPATIBLE_SIGN_NO))) {
                    log.info(String.format("暗码解析异常--密钥升级过程中，params is [%s]", request.getParameterMap().toString()) + e);
                    resp = CommonUtil.asMap("code", 3003, "msg", "网络异常，请重试", "resp", "");
                } else {
                    log.error("解析暗码异常" + e);
                    resp = CommonUtil.asMap("code", 3001, "msg", "签名错误", "resp", "");
                }
            }
        }
        OutputUtils.renderJson(resp);
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


    public static void main(String[] args) {
        System.out.println("start=".split("\\=").length);
    }
}
