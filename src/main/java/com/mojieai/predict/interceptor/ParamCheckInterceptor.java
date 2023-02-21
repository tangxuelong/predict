package com.mojieai.predict.interceptor;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.OutputUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 参数过滤拦截器
 *
 * @author Singal
 */
public class ParamCheckInterceptor implements HandlerInterceptor {
    private static final Logger log = LogConstant.commonLog;

    private static final String XSS_PATTERN = "((<.+>)|(\\{.+\\})|(\\(.+\\).*>)|(/\\*.*/))+";
    private static final String XSS_PATTERN2 = "<|>|&gt|&lt|&#|/\\*.*\\*/|vbscript:|javascript:|=\\s*[\\[{\"']";
    private static Pattern pattern = Pattern.compile(XSS_PATTERN);
    private static Pattern pattern2 = Pattern.compile(XSS_PATTERN2);

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception
            exception) throws Exception {

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView
			mav) throws Exception {

    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String paramValue = request.getParameter(paramName) == null ? null : StringUtils.trimToEmpty(request
                    .getParameter(paramName));
            if (paramValue != null) {
//                if (pattern.matcher(paramValue).find() || pattern2.matcher(paramValue).find()) {
//                    log.error("maybe risky param value:" + paramValue);
//                    /**导向一个参数校验非法出错页面*/
//                    Map<String, Object> data = CommonUtil.asMap("code", -1, "msg", "param error");
//                    OutputUtils.renderJson(data);
//                    throw new BusinessException("param error");
//                }
            }
        }
        return true;
    }

}
