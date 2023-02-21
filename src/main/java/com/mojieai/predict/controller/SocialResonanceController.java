package com.mojieai.predict.controller;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.SocialResonanceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by tangxuelong on 2017/8/28.
 */

@RequestMapping("/resonance")
@Controller
public class SocialResonanceController extends BaseController {

    @Autowired
    private SocialResonanceService socialResonanceService;
    @Autowired
    private LoginService loginService;

    // 共振数据
    @RequestMapping("/data")
    @ResponseBody
    public Object data(@RequestParam String gameEn, String token, @RequestParam Integer resonanceType,
                       @RequestAttribute Integer clientType) {
        Long userId = exchangeUserId(token);
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("彩种不存在");
        }

        Map<String, Object> resultMap = socialResonanceService.getResonanceData(game, resonanceType, userId,
                clientType);
        return buildSuccJson(resultMap);
    }

    private Long exchangeUserId(String token) {
        Long userIdStr = null;
        if (StringUtils.isNotBlank(token)) {
            UserToken userToken = loginService.checkToken(token);
            if (userToken != null) {
                userIdStr = userToken.getUserId();
            }
        }
        return userIdStr;
    }
}

