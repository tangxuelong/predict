package com.mojieai.predict.controller;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.UserNumberBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@RequestMapping("/numberBook")
@Controller
public class NumberBookController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private UserNumberBookService userNumberBookService;
    @Autowired
    private RedisService redisService;

    @RequestMapping("/userNumberBooks")
    @ResponseBody
    public Object getUserNumberBooks(@RequestParam String token, @RequestParam String gameEn, String lastNumId) {

        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登录");
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常");
        }

        Map<String, Object> res = userNumberBookService.getUserNumbers(game.getGameId(), userToken.getUserId(),
                lastNumId);
        return buildSuccJson(res);
    }

    @RequestMapping("/saveNumToCloud")
    @ResponseBody
    public Object saveNum2Cloud(@RequestParam String token, @RequestParam String gameEn, @RequestParam String
            periodId, @RequestParam String nums, @RequestParam Integer numType, Integer ifPopFlag) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登录");
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常");
        }
        Map<String, Object> res = userNumberBookService.saveUserNumber(game.getGameId(), periodId, userToken.getUserId
                (), nums, numType);
        if (ifPopFlag != null) {
            String key = RedisConstant.getUserTrendSaveNumPopFlag(userToken.getUserId());
            redisService.kryoSetEx(key, 2592000, ifPopFlag);
        }
        return buildSuccJson(res);
    }

    @RequestMapping("/deleteUserNum")
    @ResponseBody
    public Object deleteUserNum(@RequestParam String token, @RequestParam String numId) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登录");
        }
        Map<String, Object> result = userNumberBookService.deleteUserNum(userToken.getUserId(), numId);
        return buildSuccJson(result);
    }
}
