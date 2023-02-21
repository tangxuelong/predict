package com.mojieai.predict.util;

import com.alibaba.fastjson.JSON;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Output Utils类
 * <p>
 * 实现获取Request/Response/Session与绕过jsp/freemaker直接输出文本的简化函数.
 *
 * @author Singal
 */
public class OutputUtils {
    private static Logger log = LogConstant.commonLog;

    //-- header 常量定义 --//
    private static final String ENCODING_PREFIX = "encoding";
    private static final String NOCACHE_PREFIX = "no-com.mojieai.predict.cache";
    private static final String ENCODING_DEFAULT = "UTF-8";
    private static final boolean NOCACHE_DEFAULT = true;

    //-- content-type 常量定义 --//
    private static final String TEXT_TYPE = "text/plain";
    private static final String JSON_TYPE = "application/json";
    private static final String XML_TYPE = "text/xml";
    private static final String HTML_TYPE = "text/html";
    private static final String JS_TYPE = "text/javascript";

    public static final String[] headers = new String[]
            {"encoding:UTF-8", "no-com.mojieai.predict.cache:true"};

    //-- 取得Request/Response/Session的简化函数 --//

    /**
     * 取得HttpSession的简化函数.
     */
    public static HttpSession getSession() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
    }

    /**
     * 取得HttpRequest的简化函数.
     */
    public static HttpServletRequest getRequest() {
        try {
            HttpServletRequest request = CommonConstant.requestTL.get();
            return request;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 取得HttpResponse的简化函数.
     */
    public static HttpServletResponse getResponse() {
        return CommonConstant.responseTL.get();
    }

    /**
     * 直接输出内容的简便函数.
     * <p>
     * eg.
     * render("text/plain", "hello", "encoding:GBK");
     * render("text/plain", "hello", "no-com.mojieai.predict.cache:false");
     * render("text/plain", "hello", "encoding:GBK", "no-com.mojieai.predict.cache:false");
     *
     * @param headers 可变的header数组，目前接受的值为"encoding:"或"no-com.mojieai.predict.cache:",默认值分别为UTF-8和true.
     */
    public static void render(final String contentType, final String content, final String... headers) {
        try {
            //分析headers参数
            String encoding = ENCODING_DEFAULT;
            boolean noCache = NOCACHE_DEFAULT;
            for (String header : headers) {
                String headerName = StringUtils.substringBefore(header, ":");
                String headerValue = StringUtils.substringAfter(header, ":");

                if (StringUtils.equalsIgnoreCase(headerName, ENCODING_PREFIX)) {
                    encoding = headerValue;
                } else if (StringUtils.equalsIgnoreCase(headerName, NOCACHE_PREFIX)) {
                    noCache = Boolean.parseBoolean(headerValue);
                } else
                    throw new IllegalArgumentException(headerName + "不是一个合法的header类型");
            }

            HttpServletResponse response = CommonConstant.responseTL.get();

            //设置headers参数
            String fullContentType = contentType + ";charset=" + encoding;
            response.setContentType(fullContentType);
            if (noCache) {
                setNoCacheHeader(response);
            }
            response.getWriter().write(content);
            response.getWriter().flush();
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 设置无缓存Header.
     */
    private static void setNoCacheHeader(HttpServletResponse response) {
        //Http 1.0 header
        response.setDateHeader("Expires", 0);
        //Http 1.1 header
        response.setHeader("Cache-Control", "no-com.mojieai.predict.cache");
    }

    /**
     * 直接输出JSON.
     *
     * @param map Map对象,将被转化为json字符串.
     * @see #render(String, String, String...)
     */
    @SuppressWarnings("unchecked")
    public static void renderJson(final Map map, final String... headers) {
        String jsonString = JSON.toJSONString(map).toString();
        render(JSON_TYPE, jsonString, headers);
    }

    @SuppressWarnings("unchecked")
    public static void renderJson(final Map map) {
        renderJson(map, headers);
    }
}
