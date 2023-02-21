package com.mojieai.predict.controller;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.MobileUserDao;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.bo.SocialKillNumFilter;
import com.mojieai.predict.entity.dto.PushDto;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.MyEncircleVo;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.thread.AliyunPushTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tangxuelong on 2017/8/28.
 */

@RequestMapping("/admin")
@Controller
public class AdminController extends BaseController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private SocialClassicEncircleCodeService socialClassicEncircleCodeService;
    @Autowired
    private SocialService socialService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RobotEncircleService robotEncircleService;
    @Autowired
    private PayService payService;
    @Autowired
    private MobileUserDao mobileUserDao;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private PushService pushService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private AppVersionService appVersionService;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private StarUserMatchService starUserMatchService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private InternetCelebrityRecommendService internetCelebrityRecommendService;
    @Autowired
    private MatchInfoService matchInfoService;
    @Autowired
    private MatchTagService matchTagService;
    @Autowired
    private UserWithdrawFlowService userWithdrawFlowService;
    @Autowired
    private UserAccountService userAccountService;

    /* 登录*/
    @RequestMapping("/login")
    @ResponseBody
    public Object login(@RequestParam String mobile, @RequestParam String password) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("mobile", adminService.login(mobile, password));
        return buildSuccJson(resultMap);
    }

    /* 设置管理员*/
    @RequestMapping("/setAdmin")
    @ResponseBody
    public Object setAdmin(@RequestParam String mobile) {
        adminService.setAdmin(mobile);
        return buildSuccJson();
    }

    /* 查询banner*/
    @RequestMapping("/banner")
    @ResponseBody
    public Object queryBanner() {
        return buildSuccJson(adminService.queryBanner());
    }

    /* 添加banner*/
    @RequestMapping("/banner/add")
    @ResponseBody
    public Object bannerAdd(@RequestParam Integer bannerId, @RequestParam String title, @RequestParam String
            imgUrl, @RequestParam String detailUrl, @RequestParam String startTime, @RequestParam String endTime,
                            @RequestParam Integer isDel, @RequestParam Integer weight, @RequestParam Long gameId,
                            @RequestParam Integer actionType, @RequestParam(required = false) String isNew,
                            @RequestParam(required = false) Integer positionType, String exclusiveClientId) {
        Timestamp startTimeA = new Timestamp(Long.parseLong(startTime));
        Timestamp endTimeB = new Timestamp(Long.parseLong(endTime));
        Banner banner = new Banner(bannerId, title, imgUrl, detailUrl, startTimeA,
                endTimeB, isDel, weight, gameId, actionType, positionType, exclusiveClientId);
        adminService.bannerAdd(banner, isNew);
        return buildSuccJson();
    }

    @RequestMapping("/activityIni")
    @ResponseBody
    public Object queryAllIni() {
        List<ActivityIni> inis = adminService.getAllActivityInis();
        return buildSuccJson(inis);
    }

    @RequestMapping("/activityIni/addOrUpdate")
    @ResponseBody
    public Object addIni(ActivityIni activityIni) {
        adminService.activityIniAddOrUpdate(activityIni);
        return buildSuccJson();
    }

    @RequestMapping("/activityIni/androidVersion")
    @ResponseBody
    public Object androidVersion(String androidDownloadUrl, String androidUpdateText, String androidVersionCode,
                                 String androidNewVersionCode) {
        Map<String, String> map = new HashMap<>();
        map.put("versionUpdateUrl", androidDownloadUrl);
        map.put("versionUpdateText", androidUpdateText);
        map.put("version", androidVersionCode);
        map.put("newVersion", androidNewVersionCode);
        adminService.updateActivityIni(map);
        return buildSuccJson();
    }

    @RequestMapping("/activityIni/iosVersion")
    @ResponseBody
    public Object iosVersion(String iosDownloadUrl, String iosUpdateText, String iosVersionCode, String
            iosNewVersionCode) {
        Map<String, String> map = new HashMap<>();
        map.put("versionUpdateUrl1001", iosDownloadUrl);
        map.put("versionUpdateText1001", iosUpdateText);
        map.put("version1001", iosVersionCode);
        map.put("newVersion1001", iosNewVersionCode);
        adminService.updateActivityIni(map);
        return buildSuccJson();
    }

    @RequestMapping("/setPeriodManualRule")
    @ResponseBody
    public Object setPeriodManualRule(@RequestParam String gameEn, @RequestParam String ruleStr) {
        Map res = adminService.setPeriodManualRule(gameEn, ruleStr);
        return buildSuccJson(res);
    }

    @RequestMapping("/updatePeriodManualStatus")
    @ResponseBody
    public Object updatePeriodManualStatus(@RequestParam long gameId, @RequestParam String periodId, @RequestParam
            Integer status) {
        Map res = adminService.updateOperateStatus(gameId, periodId, status);
        return buildSuccJson(res);
    }

    @RequestMapping("/manualPredictList")
    @ResponseBody
    public Object operateNumList(String gameId, String minPeriodId, String maxPeriodId, String manualFlag) {
        Long gameIdP = null;
        if (StringUtils.isNotBlank(gameId)) {
            gameIdP = Long.valueOf(gameId);
        }
        List<PredictNumbersOperate> result = adminService.getOpertePredict(gameIdP, minPeriodId, maxPeriodId,
                manualFlag);
        return buildSuccJson(result);
    }

    @RequestMapping("/getLastManualPredictNum")
    @ResponseBody
    public Object getLastManualNum() {
        List<Map> result = new ArrayList<>();
        Map ssqMap = new HashMap();
        Map dltMap = new HashMap();
        Game ssqGame = GameCache.getGame(GameConstant.SSQ);
        Game dltGame = GameCache.getGame(GameConstant.DLT);

        GamePeriod ssqGamePeriod = PeriodRedis.getAwardCurrentPeriod(ssqGame.getGameId());
        GamePeriod dltGamePeriod = PeriodRedis.getAwardCurrentPeriod(dltGame.getGameId());

        List<PredictNumbersOperate> ssqNum = adminService.getOpertePredict(ssqGame.getGameId(), ssqGamePeriod
                .getPeriodId(), null, null);
        List<PredictNumbersOperate> dltNum = adminService.getOpertePredict(dltGame.getGameId(), dltGamePeriod
                .getPeriodId(), null, null);

        PredictNumbersOperate ssqOperateNum = ssqNum != null && ssqNum.size() > 0 ? ssqNum.get(0) : null;
        PredictNumbersOperate dltOperateNum = dltNum != null && dltNum.size() > 0 ? dltNum.get(0) : null;

        ssqMap.put("gameEn", ssqGame.getGameEn());
        ssqMap.put("gameId", ssqGame.getGameId());
        ssqMap.put("ruleStr", ssqOperateNum == null ? null : ssqOperateNum.getRuleStr());
        ssqMap.put("status", ssqOperateNum == null ? null : ssqOperateNum.getStatus());
        ssqMap.put("periodId", ssqGamePeriod.getPeriodId());

        dltMap.put("gameEn", dltGame.getGameEn());
        dltMap.put("gameId", dltGame.getGameId());
        dltMap.put("ruleStr", dltOperateNum == null ? null : dltOperateNum.getRuleStr());
        dltMap.put("status", dltOperateNum == null ? null : dltOperateNum.getStatus());
        dltMap.put("periodId", dltGamePeriod.getPeriodId());
        dltMap.put("operateRuleData", dltOperateNum);

        result.add(ssqMap);
        result.add(dltMap);
        return buildSuccJson(result);
    }

    /* 用户添加次数*/
    @RequestMapping("/updateUserPredictMaxNums")
    @ResponseBody
    public Object updateUserPredictMaxNums(@RequestParam String mobile, @RequestParam String gameEn, @RequestParam
            Integer
            addNums) {
        Game game = GameCache.getGame(gameEn);
        String msg = adminService.updateUserPredictNums(mobile, game.getGameId(), addNums);
        if (StringUtils.isBlank(msg)) {
            return buildSuccJson();
        }
        return buildErrJson(msg);
    }

    /* 查询用户次数*/
    @RequestMapping("/getUserPredictMaxNums")
    @ResponseBody
    public Object getUserPredictMaxNums(@RequestParam String mobile, @RequestParam String gameEn) {
        Game game = GameCache.getGame(gameEn);
        Integer nums = adminService.getUserPredictNums(mobile, game.getGameId());
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("nums", nums);
        return buildSuccJson(resultMap);
    }

    /*手动计算圈号索引正确数*/
    @RequestMapping("/rebuildIndexSocial")
    @ResponseBody
    public Object rebuildIndexSocial(@RequestParam long gameId, @RequestParam String beginPeriod, @RequestParam String
            endPeriod) {
        socialEncircleCodeService.reCalculateIndexUserSocialRightNums(gameId, beginPeriod, endPeriod);
        return buildSuccJson("success");
    }

    @RequestMapping("/importClassicEncirle")
    @ResponseBody
    public Object manualSaveSocialEncircle2Classic(@RequestParam long gameId) {
        socialClassicEncircleCodeService.saveSocialEncircle2ClassicDb(gameId);
        return buildSuccJson("success");
    }

    @RequestMapping("/rebuildSocialClassicRedis")
    @ResponseBody
    public Object rebuildSocialClassicRedis(long gameId) {
        socialClassicEncircleCodeService.rebuildSocialClassicRedis(gameId);
        return buildSuccJson("success");
    }

    @RequestMapping("/updateEncircleHotType")
    @ResponseBody
    public Object updateEncircleHotType(@RequestParam String gameEn, @RequestParam String periodId, @RequestParam Long
            encircleId, @RequestParam Integer isHot) {
        Game game = GameCache.getGame(gameEn);

        Integer result = socialEncircleCodeService.updateHotEncircleType(game.getGameId(), periodId, encircleId, isHot);
        if (result == 0) {
            return buildErrJson("更新失败");
        }
        return buildSuccJson("更新成功");
    }

    @RequestMapping("/getEncircleList")
    @ResponseBody
    public Object getEncircleList(@RequestParam String gameEn, @RequestParam Integer page, String encircleCount,
                                  String killNumCount, Integer takepartCount) {
        Game game = GameCache.getGame(gameEn);
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        SocialKillNumFilter socialKillNumFilter = new SocialKillNumFilter(takepartCount, encircleCount, killNumCount);

        PaginationList<SocialEncircle> paginationList = adminService.getSocialEncircleList(game.getGameId(),
                gamePeriod.getPeriodId(), page, socialKillNumFilter);
        boolean hasNext = false;
        int total = paginationList.getPaginationInfo().getTotalPage();
        int current = paginationList.getPaginationInfo().getCurrentPage();
        if (total > current) {
            hasNext = true;
        }

        Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(game.getGameId(), CommonConstant
                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);
        List<SocialEncircle> socialEncircls = paginationList.getList();
        List<MyEncircleVo> userEncircleInfos = new ArrayList<>();
        for (SocialEncircle socialEncircle : socialEncircls) {
            MyEncircleVo userEncircleInfo = socialEncircleCodeService.packageMyEncircleVo(socialEncircle,
                    socialKillAwardLevel);
            userEncircleInfos.add(userEncircleInfo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("encircle", userEncircleInfos);
        result.put("hasNext", hasNext);
        result.put("page", paginationList.getPaginationInfo().getCurrentPage());
        return buildSuccJson(result);
    }

    @RequestMapping("/addHotEncircle")
    @ResponseBody
    public Object addHotEncircle(@RequestParam String gameEn, @RequestParam Long encircleId, @RequestParam Integer
            isHot) {
        Game game = GameCache.getGame(gameEn);
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        Integer periodStatus = socialEncircleCodeService.getPeriodEncircleStatus(game.getGameId(), gamePeriod
                .getPeriodId());
        if (periodStatus != SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_ENABLE) {
            return buildErrJson(gamePeriod.getPeriodId() + "期次已过期");
        }
        SocialEncircle socialEncircle = socialEncircleCodeService.getSocialEncircleByEncircleId(game.getGameId(),
                gamePeriod.getPeriodId(), encircleId);
        Integer res = 0;
        if (socialEncircle != null) {
            res = socialEncircleCodeService.updateHotEncircleType(game.getGameId(), gamePeriod.getPeriodId(),
                    encircleId, isHot);
            socialEncircleCodeService.rebuildPeriodHotEncircle(game.getGameId(), gamePeriod.getPeriodId());//todo 考虑单独添加
        }
        if (res > 0) {
            return buildSuccJson("置顶成功");
        } else {
            return buildErrJson("置顶失败");
        }
    }

    @RequestMapping("/rebuildUserEncircleInfo")
    @ResponseBody
    public Object rebuildUserEncircleInfo(long gameId) {

        GamePeriod current = PeriodRedis.getCurrentPeriod(gameId);
        Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(gameId, CommonConstant
                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);
        List<SocialEncircle> socialEncircles = socialEncircleCodeService.getAllEncircle(gameId, current.getPeriodId());
        for (SocialEncircle socialEncircle : socialEncircles) {
            String userEncircleInfoKey = RedisConstant.getUserCurrentEncircleVo(socialEncircle.getGameId(),
                    socialEncircle.getPeriodId(), SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED,
                    socialEncircle.getUserId());
            redisService.del(userEncircleInfoKey);
        }
        for (SocialEncircle socialEncircle : socialEncircles) {
            socialEncircleCodeService.saveUserEncircleInfo2Redis(socialEncircle, socialKillAwardLevel);
        }
        return buildSuccJson("success");
    }

    @RequestMapping("/registerRobot")
    @ResponseBody
    public Object registerRobot(@RequestParam Integer count, @RequestParam Integer robotType) {
        Map<String, Object> res = robotEncircleService.registerRobot(count, robotType);
        return buildSuccJson(res);
    }

    @RequestMapping("/updateRobotName")
    @ResponseBody
    public Object updateRobotName(@RequestParam Integer robotType) {
        Map<String, Object> res = robotEncircleService.modifyRobotName(robotType);
        return buildSuccJson(res);
    }

    @RequestMapping("/divideRobotRecommend")
    @ResponseBody
    public Object divideRobotRecommend(@RequestParam Integer robotType) {
        Boolean res = robotEncircleService.divideRobotRecommend(robotType);
        return buildSuccJson(res);
    }

    // 给用户送金币 用户，金币，文案
    @RequestMapping("/giveUserGoldCoin")
    @ResponseBody
    public Object giveUserGoldCoin(@RequestParam String mobile, @RequestParam Integer goldCount, @RequestParam String
            flowText) {
        // userId
        Long userId = mobileUserDao.getUserIdByMobile(mobile);
        if (userId == null) {
            return "用户不存在";
        }
        // payId null

        Map<String, Object> res = payService.fillAccount(userId, null, Long.parseLong(String.valueOf(goldCount)),
                CommonConstant.PAY_TYPE_GOLD_COIN, null, Long.parseLong(String.valueOf(goldCount)), flowText,
                "127.0.0.1", null);
        return buildSuccJson(res);
    }

    // 给用户送vip 用户，天数，文案记录
    @RequestMapping("/giveUserVip")
    @ResponseBody
    public Object giveUserVip(@RequestParam String mobile, @RequestParam Integer dateCount, @RequestParam(required =
            false, defaultValue = "0") Integer vipType) {
        Long userId = mobileUserDao.getUserIdByMobile(mobile);
        if (userId == null) {
            return "用户不存在";
        }
        Map<String, Object> res = vipMemberService.adminGiftVip(userId, Long.valueOf(dateCount), VipMemberConstant
                .VIP_SOURCE_TYPE_ADMIN, vipType);
        return buildSuccJson(res);
    }


    /* 单独的clientId推送*/
    @RequestMapping("/pushToSingleClientId")
    @ResponseBody
    public Object pushToSingleClientId(@RequestParam String clientId, String text, String pushUrl) {
        String url = "";
        Map<String, String> content = new HashMap<>();
        content.put("pushUrl", pushUrl);
        PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, text, url, content);
        AliyunPushTask pushTask = new AliyunPushTask(pushDto, "DEVICE", String.valueOf(clientId), "");
        ThreadPool.getInstance().getPushExec().submit(pushTask);
        return buildSuccJson();
    }

    /* 单独的moible推送*/
    @RequestMapping("/pushToSingleMobile")
    @ResponseBody
    public Object pushToSingleMobile(@RequestParam String mobile, String text, String pushUrl) {
        Long userId = loginService.getUserId(mobile);
        String url = "";
        Map<String, String> content = new HashMap<>();
        content.put("pushUrl", pushUrl);
        PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, text, url, content);
        AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf(userId), "");
        ThreadPool.getInstance().getPushExec().submit(pushTask);
        return buildSuccJson();
    }

    /* 多个clientId推送*/
    @RequestMapping("/pushToListClientId")
    @ResponseBody
    public Object pushToListClientId(@RequestParam String clientIds, String text, String pushUrl) {
        String[] clientIdList = clientIds.split(CommonConstant.COMMA_SPLIT_STR);
        String url = "";
        Map<String, String> content = new HashMap<>();
        content.put("pushUrl", pushUrl);
        PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, text, url, content);
        pushService.pushToListPart(Arrays.asList(clientIdList), pushDto, "1");
        return buildSuccJson();
    }

    /* 多个moible推送*/
    @RequestMapping("/pushToListMobile")
    @ResponseBody
    public Object pushToListMobile(@RequestParam String mobiles, String text, String pushUrl) {
        String[] mobileList = mobiles.split(CommonConstant.COMMA_SPLIT_STR);
        for (String mobileSingle : mobileList) {
            Long userId = loginService.getUserId(mobileSingle);
            String url = "";
            Map<String, String> content = new HashMap<>();
            content.put("pushUrl", pushUrl);
            PushDto pushDto = new PushDto(CommonConstant.APP_TITLE, text, url, content);
            AliyunPushTask pushTask = new AliyunPushTask(pushDto, "ACCOUNT", String.valueOf(userId), "");
            ThreadPool.getInstance().getPushExec().submit(pushTask);
        }
        return buildSuccJson();
    }

    // 创建定时推送任务
    @RequestMapping("/createPushTask")
    @ResponseBody
    public Object createPushTask(@RequestParam Integer pushType, String pushTitle, String pushText, String pushUrl,
                                 String pushTime, String pushTarget) {
        PushTrigger pushTrigger = new PushTrigger();
        pushTrigger.setPushTitle(pushTitle);
        pushTrigger.setPushText(pushText);
        pushTrigger.setPushType(pushType);
        pushTrigger.setPushUrl(pushUrl);
        pushTrigger.setPushTarget(pushTarget);
        Timestamp startTimeA = new Timestamp(Long.parseLong(pushTime));
        pushTrigger.setPushTime(startTimeA);
        pushTrigger.setIsPushed(0);
        pushTrigger.setCreateTime(DateUtil.getCurrentTimestamp());
        pushTrigger.setUpdateTime(DateUtil.getCurrentTimestamp());
        pushService.createPushTask(pushTrigger);
        return buildSuccJson();
    }

    @RequestMapping("/get_user_balance")
    @ResponseBody
    public Object getUserBalance(@RequestParam String mobile) {

        Long userId = loginService.getUserId(mobile);
        if (userId == null) {
            return buildErrJson("用户不存在");
        }
        UserAccount userAccount = payService.getUserAccount(userId, CommonConstant.PAY_TYPE_BALANCE, false);
        Map<String, Object> result = new HashMap<>();
        result.put("userBalance", CommonUtil.convertFen2Yuan(userAccount.getAccountBalance()) + "元");
        return buildSuccJson(result);
    }

    @RequestMapping("/manualWithdraw")
    @ResponseBody
    public Object manualWithdraw(@RequestParam String withdrawMobile, @RequestParam String withdrawAmount,
                                 Long operateUserId) {
        Long withdrawUserId = loginService.getUserId(withdrawMobile);
        if (withdrawUserId == null) {
            return buildErrJson("用户不存在，请核对后在操作");
        }
        if (StringUtils.isBlank(withdrawAmount)) {
            return buildErrJson("操作金额不能为空");
        }

        Long amount = CommonUtil.convertYuan2Fen(withdrawAmount).longValue();
        if (amount.equals(0L)) {
            return buildErrJson("操作金额不能小于0");
        }
//        if (amount <= 10000) {
//            return buildErrJson("操作金额不能小于100");
//        }
        Map res = payService.manualWithdraw(withdrawUserId, amount, operateUserId);
        return buildSuccJson(res);
    }

    @RequestMapping("/upgrade")
    @ResponseBody
    public Object appUpgrade(Integer clientId) {
        Map res = appVersionService.getAllVersion(clientId);
        return buildSuccJson(res);
    }

    @RequestMapping("/addAppVersion")
    @ResponseBody
    public Object addAppVersion(Integer versionId, Integer clientId, String clientName, Integer versionCode, String
            versionCodeName, String upgradeDesc, Integer forceUpgrade) {
        AppVersion appVersion = new AppVersion(versionId, clientId, clientName, versionCode, versionCodeName,
                upgradeDesc, forceUpgrade);
        return buildSuccJson(appVersionService.addAppVersion(appVersion));
    }

    @RequestMapping("/forceUpgrade")
    @ResponseBody
    public Object forceUpgrade(Integer versionId, Integer forceUpgrade) {
        Map res = appVersionService.updateForceUpgrade(versionId, forceUpgrade);
        return buildSuccJson(res);
    }

    @RequestMapping("/manual_operate_hot_recommend")
    @ResponseBody
    public Object operateHotRecommend(@RequestParam String recommendId, @RequestParam Integer operateType,
                                      @RequestParam Long weight) {
        return buildSuccJson(userSportSocialRecommendService.operateHotRecommend(recommendId, weight, operateType));
    }

    @RequestMapping("/get_manual_operate_hot_recommend")
    @ResponseBody
    public Object getManualOperateHotRecommend(@RequestParam Integer playType) {
        return buildSuccJson(userSportSocialRecommendService.getHotRecommendInfo(playType));
    }

    @RequestMapping("/clear_hot_recommend")
    @ResponseBody
    public Object clearManualHotRecommend(@RequestParam Integer playType) {
        return buildSuccJson(userSportSocialRecommendService.clearManualHotRecommend(playType));
    }

    @RequestMapping("/get_user_recent_recommend")
    @ResponseBody
    public Object getUserRecentRecommend(@RequestParam String mobile) {
        Long userId = loginService.getUserId(mobile);
        if (userId == null) {
            return buildErrJson("用户不存在");
        }

        return buildSuccJson(userSportSocialRecommendService.getUserRecentRecommend(userId));
    }

    @RequestMapping("/refresh_match_predict")
    @ResponseBody
    public Object refreshMatchPredict(String matchId) {
        userSportSocialRecommendService.rebuildSportOneMatchRecommend(matchId);
        return buildSuccJson("刷新成功");
    }

    @RequestMapping("/manual_set_star_user")
    @ResponseBody
    public Object manualSetStarUser(@RequestParam Long userId, @RequestParam Integer operateType, Long weight) {

        //1.设置用户权重
        starUserMatchService.manualSetStarUserWeight(userId, weight, operateType);
        //2.构建
        starUserMatchService.buildStarUserList();
        return buildSuccJson("成功");
    }

    @RequestMapping("/get_top_star_users")
    @ResponseBody
    public Object getTopStarUsers(@RequestParam String orderType) {
        return buildSuccJson(starUserMatchService.getTopStarUsers(orderType));
    }

    @RequestMapping("/get_user_info")
    @ResponseBody
    public Object getUserInfo(String nickName, String mobile) {
        if (StringUtils.isNotBlank(nickName) && StringUtils.isNotBlank(mobile)) {
            return buildErrJson("查询信息不能为空");
        }
        return buildSuccJson(userInfoService.getUserInfoByNickNameOrMobileFromOtter(nickName, mobile));
    }

    @RequestMapping("/add_internet_celebrity_recommend")
    @ResponseBody
    public Object addInternetCelebrityRecommend(@RequestParam String mobile, @RequestParam Integer matchId,
                                                @RequestParam Integer goodsPriceId, @RequestParam String reason,
                                                @RequestParam String rewardDesc, @RequestParam String recommendInfo,
                                                @RequestParam(required = false) Integer programType, @RequestParam
                                                        (required = false) String originPrice,@RequestParam
                                                        (required = false) Integer index,@RequestParam
                                                        (required = false) String tips) {
        Long userId = loginService.getUserId(mobile);
        if (userId == null) {
            return buildErrJson("用户不存在");
        }
        Map<String, Object> res = internetCelebrityRecommendService.celebrityAddRecommend(userId, matchId, goodsPriceId,
                recommendInfo, reason, rewardDesc, "127.0.0.1", 1000, programType, originPrice,index,tips);
        return buildSuccJson(res);
    }

    @RequestMapping("/get_tag_matches")
    @ResponseBody
    public Object getTagMatches(Integer tagId) {
        return buildSuccJson(matchInfoService.getTagMatches(tagId));
    }

    @RequestMapping("/set_wisdom_recommend_match")
    @ResponseBody
    public Object setWisdomRecommendMatch(@RequestParam Integer matchId, @RequestParam Integer operateType) {
        Boolean saveRes = matchInfoService.addTag2MatchInfo(matchId, SportsProgramConstant.MATCH_TAG_WISDOM_RECOMMEND
                + "", operateType);
        Map<String, Object> result = new HashMap<>();
        Integer code = ResultConstant.ERROR;
        String msg = "";
        if (saveRes) {
            code = ResultConstant.SUCCESS;
            msg = "设置成功";
        }
        result.put("code", code);
        result.put("msg", msg);
        return buildSuccJson(result);
    }

    /* 发送推送短信**/
    @RequestMapping("/sendPushSms")
    @ResponseBody
    public Object sendPushSms(@RequestParam String typeStr, @RequestParam String msg) {
        adminService.sendPushSms(typeStr, msg);
        return buildSuccJson();
    }

    @RequestMapping("/set_index_focus_match")
    @ResponseBody
    public Object setIndexFocusMatch(@RequestParam Integer matchId, @RequestParam Integer weight, @RequestParam
            Integer ifFocus) {
        return buildSuccJson(matchInfoService.focusMatchHandler(matchId, weight, ifFocus));
    }

    @RequestMapping("/add_match_tag")
    @ResponseBody
    public Object addMatchTag(MatchTag matchTag) {
        return buildSuccJson(matchTagService.addMatchTag(matchTag));
    }

    @RequestMapping("/get_match_tag_list")
    @ResponseBody
    public Object getMatchTagList(Integer status) {
        return buildSuccJson(matchTagService.getAllMatchTagsForAdmin(status));
    }

    @RequestMapping("/manual_confirm_big_withdraw_order")
    @ResponseBody
    public Object manualConfirmBigWithdrawOrder(@RequestParam String withdrawId, Integer orderStatus, String errorMsg) {
        if (!orderStatus.equals(CommonConstant.WITHDRAW_STATUS_CONFIRM_THROUGH) && !orderStatus.equals(CommonConstant
                .WITHDRAW_STATUS_FAIL)) {
            return buildErrJson("人工干预提现订单只能为通过或拒绝");
        }
        return buildSuccJson(userWithdrawFlowService.adminManualConfirmLargeWithdrawOrder(withdrawId, orderStatus,
                errorMsg));
    }

    @RequestMapping("/get_wait_confirm_withdraw_order")
    @ResponseBody
    public Object getWaitConfirmWithdrawOrder() {

        return buildSuccJson(userWithdrawFlowService.adminGetAllWaitConfirmWithdrawOrder());
    }

    @RequestMapping("/fill_wisdom_to_user_account")
    @ResponseBody
    public Object fillWisdom2UserAccount(@RequestParam String mobile, @RequestParam String amount, @RequestParam
            String wisdomAmount, @RequestParam Integer exchangeType,@RequestParam String mobileOperate) {
        Long userId = loginService.getUserId(mobile);
        if (userId == null) {
            return buildErrJson("用户不存在");
        }
        if (!CommonUtil.isNumericeFloat(amount) || !CommonUtil.isNumericeFloat(wisdomAmount)) {
            return buildErrJson("输入的数字不正确");
        }
        Long amountLong = CommonUtil.multiply(amount, "100").longValue();
        Long wisdomAmountLong = CommonUtil.multiply(wisdomAmount, "100").longValue();
        if (amountLong > 200000 || wisdomAmountLong > 200000) {
            buildErrJson("请联系后台解除充值2千元限制");
        }
        if (!exchangeType.equals(UserAccountConstant.WISDOM_COIN_EXCHANGE_TYPE_PROGRAM_OUT_LINE_TRANSFER) &&
                !exchangeType.equals(UserAccountConstant.WISDOM_COIN_EXCHANGE_TYPE_PROGRAM_COMPENSATE)) {
            return buildErrJson("目前充值接口仅支持人工充值和方案赔付");
        }
        if (!mobileOperate.equals("18513671642")){
            return buildErrJson("非超级管理员");
        }

        return userAccountService.fillWisdom2UserAccount(userId, wisdomAmountLong, amountLong, exchangeType,mobileOperate);
    }

}

