package com.mojieai.predict.controller;

import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.UserSignService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/userSign")
public class UserSignController extends BaseController {

    @Autowired
    private UserSignService userSignService;
    @Autowired
    private LoginService loginService;

    //4.2之前
    @RequestMapping("/addSign")
    @ResponseBody
    public Object addSign(@RequestParam String token, @RequestAttribute String visitorIp, @RequestAttribute Integer
            clientType) {
        UserToken userToken = exchangeUserId(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请登录后重试");
        }
        Map result = userSignService.dailySigned(userToken.getUserId(), visitorIp, clientType);
        return buildSuccJson(result);
    }

    @RequestMapping("/add_sign_version_4_2")
    @ResponseBody
    public Object addSignV42(@RequestParam String token, @RequestAttribute String visitorIp, @RequestAttribute Integer
            clientType, @RequestAttribute Integer versionCode) {
        UserToken userToken = exchangeUserId(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请登录后重试");
        }
        Map result = userSignService.cycleSigned(userToken.getUserId(), visitorIp, clientType, versionCode);
        return buildSuccJson(result);
    }

    private UserToken exchangeUserId(String token) {
        UserToken userToken = null;
        if (StringUtils.isNotBlank(token)) {
            userToken = loginService.checkToken(token);
        }
        return userToken;
    }
}
