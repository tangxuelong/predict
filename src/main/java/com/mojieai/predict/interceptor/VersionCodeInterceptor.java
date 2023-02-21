package com.mojieai.predict.interceptor;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.NetUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VersionCodeInterceptor implements HandlerInterceptor {

    @SuppressWarnings("rawtypes")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
            Exception {
        String visitorIp = NetUtil.getCurrentLoginUserIp(request);
        String versionCode = request.getHeader(CommonConstant.VERSION_CODE);
        String clientInfo = request.getHeader(CommonConstant.CLIENT_INFOS);
        Integer clientType = null;
        String channelId = "";
        if (StringUtils.isBlank(versionCode)) {
            versionCode = "1";
        }
        if (StringUtils.isNotBlank(clientInfo)) {
            String[] clientArr = clientInfo.split(CommonConstant.COMMON_COLON_STR);
            if (clientArr.length >= 2) {
                clientType = Integer.valueOf(clientArr[0]);
                channelId = clientArr[1];
            }
        }
        request.setAttribute("versionCode", versionCode);
        request.setAttribute("clientType", clientType);
        request.setAttribute("channelId", channelId);
        request.setAttribute("visitorIp", visitorIp);
        return true;
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
