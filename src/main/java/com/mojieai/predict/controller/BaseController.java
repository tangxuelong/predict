package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BaseController {
    protected static Logger log = LogConstant.commonLog;

    public Object buildSuccJson() {
        return buildJson(ResultConstant.SUCCESS, "success");
    }

    public Object buildSuccJson(Object data) {
        return buildJson(ResultConstant.SUCCESS, "success", data);
    }

    public Object buildJson(int code, String msg) {
        return buildJson(code, msg, null);
    }

    public Object buildErrJson(String msg) {
        return buildJson(ResultConstant.ERROR, msg, null);
    }

    public Object buildSyserrJson() {
        return buildErrJson("system error");
    }

    public Object buildJson(int code, String msg, Object data) {
        String time = DateUtil.getCurrentDate();
        if (data == null) {
            return CommonUtil.asMap("code", code, "msg", msg, "serverTime", time, "data", "");
        } else {
            return CommonUtil.asMap("code", code, "msg", msg, "serverTime", time, "data", data);
        }
    }

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e) {
        long now = System.currentTimeMillis();//统一时间戳
        HttpServletResponse response = CommonConstant.responseTL.get();
        response.setCharacterEncoding("UTF-8");
        try {
            String msg = e.getMessage();
            if (ResultConstant.NEED_RELOGIN.equals(msg)) {
                response.getWriter().print(JSONObject.toJSONString(buildJson(ResultConstant.SIGN_ERROR, "需要重新登录")));
            } else if (e instanceof BadSqlGrammarException) {
                response.getWriter().print(JSONObject.toJSONString(buildErrJson("网络异常")));
            } else {
                response.getWriter().print(JSONObject.toJSONString(buildErrJson(e.getMessage())));
            }
        } catch (IOException ex) {
            log.warn("Error" + now + ":", ex);
        }
        log.warn("Error" + now + ":", e);
    }
}
