package com.mojieai.predict.interceptor;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.NetUtil;
import com.mojieai.predict.util.OutputUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 带有通配符功能的ip拦截器 例如 batch_* 可以匹配 batch_a、batch_b 等字符串
 */
public class WildcardIpInterceptor implements HandlerInterceptor {
    private static Logger log = LogConstant.commonLog;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception
            exception) throws Exception {

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mav)
            throws Exception {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
            Exception {
        String visitorIp = NetUtil.getCurrentLoginUserIp(request);
        String interfaceName = request.getContextPath();
        String ipsStr = IniCache.getIniValue(IniConstant.WHITE_IP_LIST, "127.0.0.1,0:0:0:0:0:0:0:1");
        if (ipsStr.indexOf(visitorIp) < 0) {
            log.info("interfaceName:" + interfaceName + ", visitorIp:" + visitorIp);
            Map<String, Object> resultMap = CommonUtil.asMap("msg", "无效请求！");
            OutputUtils.renderJson(resultMap);
            return false;
        }
        return true;
    }
}
