package com.mojieai.predict.interceptor;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.OutputUtils;
import com.mojieai.predict.util.PropertyUtils;
import com.mojieai.predict.util.RSAUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author Singal
 */
public class SignCheckInterceptor extends HandlerInterceptorAdapter {
    private static final Logger log = LogConstant.commonLog;

    @SuppressWarnings("rawtypes")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientType = request.getParameter("client");
        Map<String, Object> data;
        String bodyStr = null;
        String sign = null;
        try {
            sign = request.getHeader("caiqr-signature");
            StringBuffer sb = new StringBuffer();
            InputStream is = request.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            bodyStr = sb.toString();

            //验签
            if (StringUtils.isBlank(bodyStr) || RSAUtil.doCheck(bodyStr, sign, PropertyUtils.getProperty
                    ("signPublicKey"))) {
                return true;
            }
            log.error("验签有误，bodyStr=" + bodyStr + " sign=" + sign);
            data = CommonUtil.asMap("code", ResultConstant.SIGN_ERROR, "msg", "认证失败", "resp", "认证失败");
        } catch (Exception e) {
            log.info("验签失败，bodyStr=" + bodyStr + " sign=" + sign, e);
            data = CommonUtil.asMap("code", ResultConstant.ERROR, "msg", "系统繁忙", "resp", "系统繁忙");
        }
        OutputUtils.renderJson(data);
        return false;
    }
}
