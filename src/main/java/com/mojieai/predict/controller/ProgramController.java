package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.PayChannelInfoCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.CompatibleService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.ProgramService;
import com.mojieai.predict.service.UserProgramService;
import com.mojieai.predict.service.goods.AbstractGoods;
import com.mojieai.predict.service.goods.GoodsFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.ProgramUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/8/28.
 */

@RequestMapping("/program")
@Controller
public class ProgramController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private ProgramService programService;
    @Autowired
    private UserProgramService userProgramService;
    @Autowired
    private CompatibleService compatibleService;

    // 方案列表
    @RequestMapping("/programTab")
    @ResponseBody
    public Object programTab(@RequestParam String gameEn) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> programTabList = new ArrayList<>();
        Game game = GameCache.getGame(gameEn);
        if (game == null || game.getGameId() == null) {
            return buildErrJson("彩种不存在");
        }
        List<Map<String, Object>> programList = ProgramUtil.getProgramListFromActivityIni(game.getGameId());
        for (Map<String, Object> temp : programList) {
            Map<String, Object> programTab = new HashMap<>();
            programTab.put("programText", temp.get("programText"));
            programTab.put("programType", temp.get("programType"));
            programTabList.add(programTab);
        }
        result.put("programTabList", programTabList);
        result.put("programAd", "以下号码预测仅供投注参考，购彩需到彩票店");
        return buildSuccJson(result);
    }

    // 方案列表
    @RequestMapping("/programList")
    @ResponseBody
    public Object programList(@RequestParam String gameEn, @RequestParam Integer programType, String token) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            buildErrJson("彩种不存在");
        }

        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            userId = exchangeUserId(token);
        }

        Map<String, Object> res = programService.getCurrentSalePrograms(game.getGameId(), userId, programType);
        return buildSuccJson(res);
    }

    @RequestMapping("/awardProgramList")
    @ResponseBody
    public Object awardProgramList(String token, @RequestParam String gameEn, String lastPeriodId) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            buildErrJson("彩种不存在");
        }

        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            userId = exchangeUserId(token);
        }

        Map res = programService.getHistoryAwardProgramList(game.getGameId(), lastPeriodId, userId, CommonConstant
                .PROGRAM_IS_AWARD_YES);
        return buildSuccJson(res);
    }

    @RequestMapping("/userProgram")
    @ResponseBody
    public Object userProgram(@RequestParam String token, @RequestParam String gameEn, String lastPeriodId) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            buildErrJson("彩种不存在");
        }

        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            userId = exchangeUserId(token);
        }

        Map<String, Object> res = userProgramService.getUserProgram(game.getGameId(), lastPeriodId, userId);
        return buildSuccJson(res);
    }

    @RequestMapping("/programPayChannel")
    @ResponseBody
    public Object programPayChannel(@RequestParam String token, @RequestParam String programId, @RequestAttribute
            Integer clientType, @RequestAttribute Integer versionCode) {

        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            userId = exchangeUserId(token);
        }
        if (userId == null) {
            buildErrJson("用户不存在");
        }

        Map<String, Object> res = programService.getPurchaseProgramInfo(userId, programId, clientType, versionCode);
        res = compatibleService.exchangeDefaultPayChannel(userId, clientType, versionCode, res.get("price").toString
                (), res);
        return buildSuccJson(res);
    }

    @RequestMapping("/purchaseProgram")
    @ResponseBody
    public Object purchaseProgram(@RequestParam String token, @RequestParam String programId, @RequestParam Integer
            payChannelId, Integer bankId, @RequestAttribute Integer clientType, @RequestAttribute String visitorIp) {
        Long userId = exchangeUserId(token);
        if (userId == null) {
            return buildErrJson("用户不存在");
        }

        PayChannelInfo payChannelInfo = PayChannelInfoCache.getChannelInfo(payChannelId);
        if (payChannelInfo == null) {
            return buildErrJson("支付渠道不存在");
        }

        //check 方案
        Map<String, Object> checkRes = programService.checkProgram(userId, programId);
        if (Integer.valueOf(checkRes.get("flag").toString()) == -1) {
            return buildErrJson(checkRes.get("msg").toString());
        }

        Integer accountType = CommonConstant.ACCOUNT_TYPE_CASH;
        Map<String, Object> res = null;
        //智慧币支付
        if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            res = userProgramService.wisdomCoinPurchaseProgram(userId, programId, payChannelId);
            accountType = CommonConstant.ACCOUNT_TYPE_WISDOM_COIN;
        } else {
            //现金支付
            res = userProgramService.cashPurchaseProgram(userId, programId, payChannelId, bankId, visitorIp, clientType);
        }
        //错误信息
        if (res.containsKey("flag") && Integer.valueOf(res.get("flag").toString()) == ResultConstant.PAY_FAILED_CODE) {
            return buildErrJson(res.get("msg").toString());
        }
        res.put("accountType", accountType);
        res.put("channel", payChannelId);
        res.put("webPay", CommonUtil.getWebPayStatus(payChannelId));
        return buildSuccJson(res);
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

