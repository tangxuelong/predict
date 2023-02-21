package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.UserAccountDao;
import com.mojieai.predict.dao.UserNumberBookDao;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/independPop")
public class IndependPopController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private PayService payServer;
    @Autowired
    private UserNumberBookService userNumberBookService;
    @Autowired
    private UserNumberBookDao userNumberBookDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private IndependPopService independPopService;

    @RequestMapping("/socialBigDataPop")
    @ResponseBody
    public Object socialBigData(@RequestParam String token, @RequestParam String gameEn) {

        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("游戏不存在");
        }
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登陆");
        }
        Map result = new HashMap();
        GamePeriod gamePeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        gamePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), gamePeriod.getPeriodId());
        boolean isVip = vipMemberService.checkUserIsVip(userToken.getUserId(), VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);

        long goldBalance = getGoldBalance(userToken);
        String goldBalanceMsg = "剩余:<font color=\"#FF5050\">" + goldBalance + "</font>" + CommonConstant
                .GOLD_COIN_MONETARY_UNIT;

        //check是否已经金币购买
        Map<String, Object> itemInfo = payServer.getAccessIdByType(CommonConstant.ACCESS_BIG_DATA, game.getGameId
                ());
        Integer itemId = (Integer) itemInfo.get("itemId");
        Long goldPrice = (Long) itemInfo.get("itemPrice");

        boolean isPayed = payServer.checkUserAccess(userToken.getUserId(), game.getGameId(), gamePeriod.getPeriodId()
                , itemId);
        boolean isPermission = false;
        if (isPayed || isVip) {
            isPermission = true;
        }

        result.put("isVip", isVip);
        result.put("itemId", itemId);
        result.put("isPermission", isPermission);
        result.put("goldPrice", goldPrice);
        result.put("goldBalance", goldBalance);
        result.put("goldBalanceMsg", goldBalanceMsg);
        result.put("cashVipBtnMsg", "成为VIP");
        result.put("goldExchangeBtnMsg", "消耗" + goldPrice + CommonConstant.GOLD_COIN_MONETARY_UNIT);
        result.put("popTitle", gamePeriod.getPeriodId() + "期社区大数据");
        return buildSuccJson(result);
    }

    private long getGoldBalance(UserToken userToken) {
        long goldBalance = 0;
        UserAccount userAccount = userAccountDao.getUserAccountBalance(userToken.getUserId(), CommonConstant
                .PAY_TYPE_GOLD_COIN, false);
        if (userAccount != null) {
            goldBalance = userAccount.getAccountBalance();
        }
        return goldBalance;
    }

    @RequestMapping("/trend1000Pop")
    @ResponseBody
    public Object trendThousandPop(@RequestParam String token, String gameEn) {
        Map result = new HashMap();
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登陆");
        }
        boolean isVip = vipMemberService.checkUserIsVip(userToken.getUserId(), VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        String popTitle = "提示";
        String popContent = "此功能为会员专享";
        if (isVip) {
            popContent = "";
        }
        result.put("isVip", isVip);
        result.put("popTitle", popTitle);
        result.put("popContent", popContent);
        return buildSuccJson(result);
    }

    @RequestMapping("/lastKillCodePop")
    @ResponseBody
    public Object lastKillCodePop(@RequestParam String token, @RequestParam String gameEn) {
        Map result = new HashMap();
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登陆");
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常");
        }

        boolean isVip = vipMemberService.checkUserIsVip(userToken.getUserId(), VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        //check是否已经金币购买
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        GamePeriod currentLastCodePeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(),
                lastOpenPeriod.getPeriodId());
        Map<String, Object> itemInfo = payServer.getAccessIdByType(CommonConstant.ACCESS_LAST_KILL_CODE, game.getGameId
                ());
        Integer itemId = (Integer) itemInfo.get("itemId");
        Long goldPrice = (Long) itemInfo.get("itemPrice");

        boolean isPayed = payServer.checkUserAccess(userToken.getUserId(), game.getGameId(), currentLastCodePeriod
                .getPeriodId(), itemId);//todo
        boolean isPermission = false;
        if (isPayed || isVip) {
            isPermission = true;
        }

        long goldBalance = getGoldBalance(userToken);
        String goldBalanceMsg = "剩余:<font color=\"#FF5050\">" + goldBalance + "</font>" + CommonConstant
                .GOLD_COIN_MONETARY_UNIT;

        result.put("isVip", isVip);
        result.put("itemId", itemId);
        result.put("isPermission", isPermission);
        result.put("goldPrice", goldPrice);
        result.put("goldBalance", goldBalance);
        result.put("goldBalanceMsg", goldBalanceMsg);
        result.put("cashVipBtnMsg", "成为VIP");
        result.put("goldExchangeBtnMsg", "消耗" + goldPrice + CommonConstant.GOLD_COIN_MONETARY_UNIT);
        result.put("popTitle", "查看方法");
        return buildSuccJson(result);
    }

    @RequestMapping("/numberBookPop")
    @ResponseBody
    public Object numberBookPop(@RequestParam String token, @RequestParam Integer type) {
        Map<String, Object> result = new HashMap<>();
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登陆");
        }
        boolean isVip = vipMemberService.checkUserIsVip(userToken.getUserId(), VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        String titleFrontAdMsg = "";
        String titleBackAdMsg = "";
        String vipValidateMsg = "您还不是VIP会员";
        String purchaseVipBtnMsg = "开通会员";
        if (type.equals(CommonConstant.NUMBER_BOOK_VIP_POP_TYPE)) {
            titleFrontAdMsg = "号码本已使用云端存储技术(数据不丢失)";
            titleBackAdMsg = "成为会员免费升级" + CommonConstant.NUMBER_BOOK_MAX_COUNT + "条。";
        } else {
            titleFrontAdMsg = "号码本已使用云端存储技术(数据不丢失)";
            titleBackAdMsg = "成为会员免费升级" + CommonConstant.NUMBER_BOOK_MAX_COUNT + "条。";
        }

        if (isVip) {
            VipMember vipMember = vipMemberService.getUserVipMemberRedisAndDb(userToken.getUserId(),
                    VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
            titleFrontAdMsg = "尊贵的会员，您正在";
            titleBackAdMsg = "享受10000条号码云存储服务（数据不丢失）";
            purchaseVipBtnMsg = "续费会员";
            vipValidateMsg = "您的会员有效期截止到 " + DateUtil.formatTime(vipMember.getEndTime(), "yyyy-MM-dd");
        }

        //双色球大乐透
        List<Map<String, Object>> cloudInfo = generateCloudInfo();

        result.put("isVip", isVip);
        result.put("title", "VIP专享");
        result.put("titleFrontAdMsg", titleFrontAdMsg);
        result.put("titleBackAdMsg", titleBackAdMsg);
        result.put("vipValidateMsg", vipValidateMsg);
        result.put("cloudInfo", cloudInfo);
        result.put("purchaseVipBtnMsg", purchaseVipBtnMsg);
        return buildSuccJson(result);
    }

    private List<Map<String, Object>> generateCloudInfo() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (GameEnum ge : GameEnum.values()) {
            Game game = ge.getGame();
            if (game != null && game.getGameType().equals(game.GAME_TYPE_COMMON)) {
                Map temp = new HashMap();
                temp.put("gameName", game.getGameName());
                temp.put("userMsg", "普通用户");
                temp.put("userCount", VipMemberConstant.NOT_VIP_MAX_CLOUD_NUMBER_BOOK + "条");
                temp.put("vipCount", VipMemberConstant.VIP_MAX_CLOUD_NUMBER_BOOK + "条");
                temp.put("vipMsg", "VIP用户");
                result.add(temp);
            }
        }
        return result;
    }

    @RequestMapping("/saveNumToBookPop")
    @ResponseBody
    public Object saveNumToBookPop(@RequestParam String token, @RequestParam String gameEn) {
        Map<String, Object> result = new HashMap<>();
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登陆");
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常");
        }
        //1.从缓存中拿用户标识
        String key = RedisConstant.getUserTrendSaveNumPopFlag(userToken.getUserId());
        Integer redisPopFlag = redisService.kryoGet(key, Integer.class);
        //2.如果用户历史设置了不弹或这次不弹就不弹
        boolean ifShowNumBookPop = true;
        if (redisPopFlag != null && redisPopFlag == 0) {
            ifShowNumBookPop = false;
        }
        //3.判断用户是否是vip
        String msg = "";
        boolean isVip = vipMemberService.checkUserIsVip(userToken.getUserId(), VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        if (isVip) {
            ifShowNumBookPop = false;
        } else {
            Integer total = userNumberBookDao.getUserNumBookCount(game.getGameId(), userToken.getUserId());
            if (total < VipMemberConstant.NOT_VIP_MAX_CLOUD_NUMBER_BOOK) {
                ifShowNumBookPop = false;
            } else {
                msg = "号码本已满" + VipMemberConstant.NOT_VIP_MAX_CLOUD_NUMBER_BOOK + "条，继续保存将删除一条历史号码，是否继续";
            }
        }
        result.put("msg", msg);
        result.put("ifShowNumBookPop", ifShowNumBookPop);
        return buildSuccJson(result);
    }

    @RequestMapping("/predictNumLeadBuyVipPop")
    @ResponseBody
    public Object predictNumLeadBuyVipPop(@RequestParam String token, String gameEn) {
        Map<String, Object> result = new HashMap<>();
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("请重新登陆");
        }

        result.put("predictTimesAd", "每期智慧次数＋" + VipMemberConstant.VIP_PREDICT_NUM_MORE_TIMES);
        result.put("vipPriceAd", "每月最低<font color='#ff5050'>12</font>元");
        result.put("btnMsg", "成为会员");
        return buildSuccJson(result);
    }

    @RequestMapping("/socialPopup")
    @ResponseBody
    public Object socialPopup(@RequestParam String token) {
        if (null == token) {
            return buildSuccJson();
        }

        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("参数异常，用户不存在");
        }
        List<Map<String, Object>> resultList = independPopService.getSocialPopup(userToken.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("popupList", resultList);
        return buildSuccJson(result);
    }

    @RequestMapping("/sportsSocialPopup")
    @ResponseBody
    public Object sportsSocialPopup(@RequestParam String token) {
        if (null == token) {
            return buildSuccJson();
        }
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("参数异常，用户不存在");
        }

        List<Map<String, Object>> resultList = independPopService.getSportsSocialPopup(userToken.getUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("popupList", resultList);
        return buildSuccJson(result);
    }
}
