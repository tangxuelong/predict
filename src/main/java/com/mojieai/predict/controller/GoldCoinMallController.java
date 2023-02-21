package com.mojieai.predict.controller;

import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.service.ExchangeMallService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.UserSocialTaskAwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/gold_coin_mall")
public class GoldCoinMallController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private ExchangeMallService exchangeMallService;
    @Autowired
    private UserSocialTaskAwardService userSocialTaskAwardService;

    @RequestMapping("/get_good_list")
    @ResponseBody
    public Object getGoodList(@RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);

        return buildSuccJson(exchangeMallService.getGoldCoinGoods(userToken.getUserId()));
    }

    @RequestMapping("/get_earn_gold_coin_tasks")
    @ResponseBody
    public Object getEarnGoldCoinTasks(@RequestParam String token, @RequestAttribute Integer versionCode,
                                       @RequestAttribute Integer clientType) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson(ResultConstant.PARAMS_ERR_MSG);
        }

        return buildSuccJson(userSocialTaskAwardService.getEarnGoldCoinTaskList(userToken.getUserId(), versionCode,
                clientType));
    }

}
