package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSON;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.BannerCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.SocialLevelIntegralCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.SocialUserFansDao;
import com.mojieai.predict.entity.bo.SocialKillNumFilter;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.*;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import com.mojieai.predict.util.TrendUtil;
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
 * Created by tangxuelong on 2017/10/11.
 */

@RequestMapping("/social")
@Controller
public class SocialController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private SocialKillCodeService socialKillCodeService;
    @Autowired
    private SocialService socialService;
    @Autowired
    private SocialUserFansDao socialUserFansDao;
    @Autowired
    private SocialUserFollowService socialUserFollowService;
    @Autowired
    private SocialClassicEncircleCodeService socialClassicEncircleCodeService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private PayService payService;
    @Autowired
    private CompatibleService compatibleService;
    @Autowired
    private UserSocialRecordService userSocialRecordService;
    @Autowired
    private UserTitleService userTitleService;
    @Autowired
    private SocialLevelIntegralService socialLevelIntegralService;
    @Autowired
    private UserSocialIntegralService userSocialIntegralService;
    @Autowired
    private SocialIntegralLogService socialIntegralLogService;

    @RequestMapping("/addEncircle")
    @ResponseBody
    public Object addEncircle(@RequestParam String gameEn, @RequestParam String periodId, @RequestParam String
            encircleNums, @RequestParam Integer encircleCount, @RequestParam String killNum, @RequestParam String
                                      token, Integer encircleType, @RequestAttribute String versionCode,
                              @RequestAttribute String visitorIp, @RequestAttribute(required = false) Integer
                                      clientType) {
        Map<String, Object> result = new HashMap<>();
        //1.用户校验
        Long userId = exchangeUserId(token);
        boolean errorFlag = false;
        if (userId == null) {
            errorFlag = true;
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            errorFlag = true;
        }
        if (errorFlag) {
            result.put("successFlag", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_FLAG);
            result.put("title", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_TITLE);
            result.put("msg", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_REENCIRCLE_ERR);
            return buildSuccJson(result);
        }
        //2.期次校验
        Integer status = socialEncircleCodeService.getPeriodEncircleStatus(game.getGameId(), periodId);
        if (status == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_OPEN_AWARD) {
            result.put("successFlag", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_FLAG);
            result.put("title", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_TITLE);
            result.put("msg", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_PERIOD_ERR);
            return buildSuccJson(result);
        } else if (status == SocialEncircleKillConstant.SOCIAL_ENCIRCLE_STATUS_END) {
            result.put("successFlag", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_FLAG);
            result.put("title", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_TITLE);
            result.put("msg", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_PERIOD_ERR);
            return buildSuccJson(result);
        }
        //3.圈号数据正确性校验
        String[] encircleNumArr = SocialEncircleKillCodeUtil.removeStrOfNumArr(encircleNums.split(CommonConstant
                .COMMA_SPLIT_STR));
        if (encircleNumArr.length != encircleCount) {
            result.put("successFlag", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_FLAG);
            result.put("title", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_TITLE);
            result.put("msg", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_REENCIRCLE_ERR);
            return buildSuccJson(result);
        }
        //4.圈号次数限制
        if (encircleType == null) {
            encircleType = SocialEncircleKillConstant.ENCIRCLE_CODE_TYPE_RED;
        }
        GamePeriod currentPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(game.getGameId(), periodId);
        String userEncircleCountKey = RedisConstant.getEncircleTimesKey(game.getGameId(), periodId, encircleType,
                userId);
        Long encircleTimes = redisService.incr(userEncircleCountKey);
        if (encircleTimes > SocialEncircleKillConstant.SOCIAL_ENCIRCLE_MAX_COUNT) {
            result.put("successFlag", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_FLAG);
            result.put("title", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_ERROR_TITLE);
            result.put("msg", SocialEncircleKillConstant.SOCIAL_ADD_ENCIRCLE_MSG_PERIOD_UESED_ERR);
            return buildSuccJson(result);
        }
        int expireTime = TrendUtil.getExprieSecond(currentPeriod.getAwardTime(), 3600);
        redisService.expire(userEncircleCountKey, expireTime);
        //5.添加圈号
        result = socialEncircleCodeService.addEncircleCode(game.getGameId(), periodId, userId,
                encircleNums, encircleCount, killNum, versionCode, visitorIp, clientType);

        return buildSuccJson(result);
    }

    @RequestMapping("/addKillNum")
    @ResponseBody
    public Object addKillNum(@RequestParam String gameEn, @RequestParam String periodId, @RequestParam Long
            encircleCodeId, @RequestParam String killNums, @RequestParam String token, @RequestAttribute String
                                     visitorIp, @RequestAttribute(required = false) Integer clientType) {

        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        if (userId == null) {
            return buildErrJson("用户校验失败,请重新登录");
        }
        //1.总次数限制
        String userKillNumTotalTimesKey = RedisConstant.getUserKillNumTotalTimesKey(game.getGameId(), periodId, userId);
        Integer userKillTimes = StringUtils.isBlank(redisService.get(userKillNumTotalTimesKey)) ? 0 : Integer.valueOf
                (redisService.get(userKillNumTotalTimesKey));
        if (userKillTimes >= SocialEncircleKillConstant.SOCIAL_KILL_NUM_MAX_COUNT) {
            return buildErrJson("本期10次杀号机会已用完");
        }
        //2.并发限制
        String concurrentUserKillLockKey = RedisConstant.getConcurrentUserKillLockKey(game.getGameId(), encircleCodeId,
                userId);
        Long concurrentRes = redisService.kryoSetNx(concurrentUserKillLockKey, 1);

        if (concurrentRes.intValue() == 0) {
            return buildErrJson("您已经参与过该次杀号");
        }

        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        int expireTime = TrendUtil.getExprieSecond(gamePeriod.getAwardTime(), 120);
        redisService.expire(concurrentUserKillLockKey, expireTime);

        Map result = socialKillCodeService.addKillCode(game.getGameId(), gamePeriod, userId, encircleCodeId,
                killNums, visitorIp, clientType);
        if (result.get("successFlag").equals(IniConstant.COMPATIBLE_SIGN_NO)) {
            return buildErrJson(result.get("errorMsg").toString());
        }
        return buildSuccJson(result);
    }

    @RequestMapping("/getKillNumByEncircleId")
    @ResponseBody
    public Object getKillNumByEncircleId(@RequestParam String gameEn, @RequestParam String periodId, @RequestParam Long
            encircleCodeId, @RequestParam String token, Integer page, @RequestParam Integer killNumDetaillType,
                                         @RequestAttribute(required = false) String versionCode) {

        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        if (userId == null) {
            return buildErrJson("用户校验失败,请重新登录");
        }
        SocialEncircle socialEncircle = socialEncircleCodeService.getSocialEncircleByEncircleId(game.getGameId(),
                periodId, encircleCodeId);

        Map<String, Integer> socialKillAwardLevel = socialService.getAwardLevelMap(game.getGameId(), CommonConstant
                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);

        Integer periodEncircleStatus = socialEncircleCodeService.getPeriodEncircleStatus(game.getGameId(), periodId);
        Map<String, Object> result = socialKillCodeService.getKillNumsInfoByEncircleId(game.getGameId(), periodId,
                encircleCodeId, userId, page, socialKillAwardLevel, periodEncircleStatus, killNumDetaillType,
                socialEncircle.getUserId(), versionCode);
        return buildSuccJson(result);
    }

    @RequestMapping("/killNumDetail")
    @ResponseBody
    public Object getkillNumDetail(@RequestParam String gameEn, @RequestParam String periodId, @RequestParam Long
            encircleCodeId, @RequestParam String token, @RequestParam Integer page, @RequestParam Integer
                                           killNumDetailType, @RequestAttribute String versionCode) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        if (userId == null) {
            return buildErrJson("用户校验失败,请重新登录");
        }
        Map<String, Object> result = socialKillCodeService.getKillNumDetailByEncircleId(game.getGameId(), periodId,
                encircleCodeId, userId, page, killNumDetailType, versionCode);
        compatibleService.killNumDetail(game.getGameId(), periodId, result, versionCode);
        compatibleService.killNumDetailCompateUserId(game.getGameId(), result, versionCode);

        // 添加积分列表
        Map<String, Integer> awardLevelMap = socialService.getAwardLevelMap(game.getGameId(), CommonConstant
                .RED_BALL_TYPE, CommonConstant.SOCIAL_CODE_TYPE_KILL);
        List<String> scoreMap = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            scoreMap.add("预计奖励" + awardLevelMap.get(String.valueOf(i) + CommonConstant
                    .COMMON_COLON_STR + String.valueOf(i)) + "积分");
        }
        result.put("scoreMap", scoreMap);
        return buildSuccJson(result);
    }

    @RequestMapping("/myEncircle")
    @ResponseBody
    public Object getMyEncircle(@RequestParam String gameEn, @RequestParam String token, @RequestParam Integer page,
                                Integer socialCodeType, Integer lastIndex) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        if (userId == null) {
            return buildErrJson("用户校验失败,请重新登录");
        }
        Map<String, Object> result = socialEncircleCodeService.getMyEncircle(game.getGameId(), userId, page,
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, lastIndex);
        return buildSuccJson(result);
    }

    @RequestMapping("/encircleIndex")
    @ResponseBody
    public Object getEncircleIndex(@RequestParam String gameEn, @RequestParam Integer encircleType, String token) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        Map<String, Object> result = socialEncircleCodeService.getEncircleIndex(game.getGameId(), encircleType, userId);
        return buildSuccJson(result);
    }

    @RequestMapping("/rank")
    @ResponseBody
    public Object rank(@RequestParam String gameEn, @RequestParam String socialType, @RequestParam String rankType,
                       @RequestParam(required = false) String token, @RequestParam(required = false) Integer
                               pageIndex) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        Map<String, Object> result = socialService.getSocialRankList(game.getGameId(), userId, socialType, rankType,
                pageIndex);
        return buildSuccJson(result);
    }

    /* 关注和取消关注*/
    @RequestMapping("/follow")
    @ResponseBody
    public Object follow(@RequestParam String token, @RequestParam Long followUserId, Integer followType) {
        if (!loginService.checkUser(followUserId)) {
            return buildErrJson("关注失败，用户不存在");
        }
        Long userId = exchangeUserId(token);
        if (null == userId) {
            return buildErrJson("参数异常，用户不存在");
        }
        if (userId.equals(followUserId)) {
            return buildErrJson("不可以关注自己哦");
        }
        if (followType == null) {
            followType = CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_DIGIT;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isFollow", socialService.follow(userId, followUserId, followType));
        return buildSuccJson(resultMap);
    }


    /* 关注列表和粉丝列表*/
    @RequestMapping("/followList")
    @ResponseBody
    public Object followList(@RequestParam String token, @RequestParam Long followListUserId, @RequestParam String
            followType, @RequestParam(required = false) Integer pageIndex, @RequestAttribute String versionCode,
                             @RequestAttribute(required = false) Integer clientType) {
        // 用户ID校验
        if (!loginService.checkUser(followListUserId)) {
            return buildErrJson("查看用户不存在");
        }
        Long userId = exchangeUserId(token);

        if (null == userId) {
            return buildErrJson("参数异常，用户不存在");
        }
        Map<String, Object> resultMap = socialService.getFollowList(userId, followListUserId, followType, pageIndex);

        // 2.3苹果版本兼容代码 苹果的粉丝列表字段读取错误
        if (null != versionCode && versionCode.equals(String.valueOf(CommonConstant.VERSION_CODE_2_3))) {
            for (Map.Entry entry : resultMap.entrySet()) {
                if (entry.getKey().equals("followList")) {
                    List<FollowInfoVo> followInfoVos = (List<FollowInfoVo>) entry.getValue();
                    for (FollowInfoVo followInfoVo : followInfoVos) {
                        // 如果是关注列表 把粉丝数量改为关注数量
                        if (followType.equals(CommonConstant.SOCIAL_FOLLOW_TYPE_FOLLOW)) {
                            followInfoVo.setFollowCount(followInfoVo.getFansCount());
                        }
                        // 如果是粉丝列表 把关注数量改为粉丝数量
                        if (followType.equals(CommonConstant.SOCIAL_FOLLOW_TYPE_FANS)) {
                            followInfoVo.setFollowCount(followInfoVo.getFansCount());
                        }
                    }
                }
            }
        }

        compatibleService.followListCompate(resultMap, versionCode, clientType);
        return buildSuccJson(resultMap);
    }

    /* 关注人数详情*/
    @RequestMapping("/followInfo")
    @ResponseBody
    public Object followInfo(@RequestParam Long userId) {
        if (!loginService.checkUser(userId)) {
            return buildErrJson("查看用户不存在");
        }
        if (null == userId) {
            return buildErrJson("参数异常，用户不存在");
        }
        return buildSuccJson(socialService.getFollowInfo(userId));
    }

    @RequestMapping("/awardPopup")
    @ResponseBody
    public Object rank(@RequestParam String gameEn, @RequestParam(required = false) String token) {
        /* 兼容IOS2.3调用awardPopup未传递token*/
        if (null == token) {
            return buildSuccJson();
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        if (null == userId) {
            return buildErrJson("参数异常，用户不存在");
        }
        List<Map<String, String>> resultList = socialService.awardPopup(game, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("popupList", resultList);
        return buildSuccJson(result);
    }

    @RequestMapping("/killNumList")
    @ResponseBody
    public Object killNumList(@RequestParam String gameEn, @RequestParam Integer page, @RequestParam Integer
            killListType, String token, Integer partakeCount, String encircleCount, String killNumCount, Integer
                                      lastIndex) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        Map<String, Object> result = null;
        if (killListType == 0) {//杀号列表
            result = socialEncircleCodeService.getKillNumList(game.getGameId(), page, partakeCount,
                    encircleCount, killNumCount, userId, "all");
        } else {
            if (userId == null) {
                return buildErrJson("请登陆后查看个人杀号");
            }
            result = socialEncircleCodeService.getMyEncircle(game.getGameId(), userId, page,
                    SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED, lastIndex);
        }
        return buildSuccJson(result);
    }

    @RequestMapping("/getKillNumListByPeriodId")
    @ResponseBody
    public Object getKillNumListByPeriodId(@RequestParam String gameEn, Integer lastIndex, String
            token, Integer partakeCount, String encircleCount, String killNumCount, String periodId,
                                           @RequestAttribute String versionCode, @RequestAttribute(required = false)
                                                   Integer clientType) {
        Map<String, Object> result = new HashMap<>();
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        SocialKillNumFilter socialKillNumFilter = new SocialKillNumFilter(partakeCount, encircleCount, killNumCount);

        result = socialEncircleCodeService.getKillNumListByPeriodId(game.getGameId(), lastIndex, periodId, userId,
                socialKillNumFilter, versionCode);
        compatibleService.getKillNumListByPeriodId(result, game.getGameId(), periodId, versionCode);
        compatibleService.killNumListCompateUserId(result, versionCode, clientType, game.getGameId());

        List<Map<String, Object>> filterList = (List<Map<String, Object>>) JSON.parseObject(ActivityIniCache
                .getActivityIniValue(ActivityIniConstant.KILL_NUM_LIST_FILTER_LIST), ArrayList.class);
        result.put("filterList", filterList);
        //数字彩社区banner
        List<BannerVo> banners = BannerCache.getBannerVosV2(BannerCache.POSITION_TYPE_DIGIT_SOCIAL_INDEX, Integer
                .valueOf(versionCode), clientType);
        result.put("banner", banners);
        return buildSuccJson(result);
    }

    @RequestMapping("/followsKillNumList")
    @ResponseBody
    public Object getFollwsKillNumList(@RequestParam String gameEn, @RequestParam String token, Integer page, Integer
            followType, @RequestAttribute String versionCode, Long lastUserId, @RequestAttribute(required = false)
                                               Integer clientType) {
        //1.参数校验
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        Long userId = exchangeUserId(token);
        if (userId == null) {
            return buildErrJson("请登陆后查看个人杀号");
        }
        if (page == null || page <= 0) {
            page = 1;
        }
        if (followType == null) {
            followType = CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_DIGIT;
        }
        Map<String, Object> result = null;
        if (Integer.valueOf(versionCode) < CommonConstant.VERSION_CODE_3_0) {
            result = socialUserFollowService.getUserFollowByPage(game.getGameId(), userId, followType, page);
        } else {
            result = socialUserFollowService.getUserFollowKillNumList(game.getGameId(), userId, lastUserId, followType);
        }
        compatibleService.followsKillNumList(result, game.getGameId(), versionCode);
        compatibleService.followsKillNumListCompateUserId(game.getGameId(), result, versionCode, clientType);
        return buildSuccJson(result);
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

    @RequestMapping("/clearEncircleTimes")
    @ResponseBody
    public Object clearEncircleTimes(String mobile) {

        Long userId = loginService.getUserId(mobile);
        if (userId == null) {
            return buildErrJson("手机号码未注册");
        }

        GameEnum gameEnum = GameEnum.getGameEnumByEn(GameConstant.SSQ);
        Game game = gameEnum.getGame();
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        String userEncircleCountKey = RedisConstant.getEncircleTimesKey(game.getGameId(), gamePeriod.getPeriodId(), 0,
                userId);

        redisService.del(userEncircleCountKey);
        return buildSuccJson("清楚次数成功");
    }

    @RequestMapping("/persionalAchievement")
    @ResponseBody
    public Object persionalAchievement(@RequestParam Long userId, String gameEn, String token) {
        boolean isMe = false;
        Map<String, Object> result = new HashMap<>();

        if (StringUtils.isBlank(gameEn)) {
            gameEn = GameConstant.SSQ;
        }
        Game game = GameCache.getGame(gameEn);
        //1.判断是否是自己
        Long lookUpUserId = exchangeUserId(token);
        if (StringUtils.isNotBlank(token) && lookUpUserId != null) {
            if (userId.equals(lookUpUserId)) {
                isMe = true;
            }
        }
        //2.关注状态
        Integer followStatus = SocialEncircleKillConstant.SOCIAL_FOLLOW_STATUS_NO;
        SocialUserFans socialUserFans = socialUserFansDao.getUserFans(userId, lookUpUserId, CommonConstant
                .SOCIAL_FOLLOW_FANS_TYPE_DIGIT);
        if (socialUserFans != null && socialUserFans.getIsFans() == SocialEncircleKillConstant
                .SOCIAL_FOLLOW_STATUS_YES) {
            followStatus = SocialEncircleKillConstant.SOCIAL_FOLLOW_STATUS_YES;
        }
        result.put("followStatus", followStatus);
        result.put("isMe", isMe);
        result.put("isVip", vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));

        //3.获取用户战绩标签
        Map<String, List<AchievementVo>> userAchieveMap = userSocialRecordService.getUserAchievementVo(game.getGameId
                (), userId);
        result.putAll(userAchieveMap);
        return buildSuccJson(result);
    }

    @RequestMapping("/socialPersonCenterTitle")
    @ResponseBody
    public Object socialPersonCenterTitle(@RequestParam(required = false) String gameEn, @RequestParam Long userId,
                                          @RequestParam String token) {
        Long lookUpUserId = exchangeUserId(token);
        if (lookUpUserId == null) {
            return buildErrJson("请登陆后查看");
        }

        Map result = socialService.getSocialPersonTitle(userId, lookUpUserId);
        return buildSuccJson(result);
    }

    @RequestMapping("/myEncirclesV2-3")
    @ResponseBody
    public Object socialMyKillNumListV2_3(@RequestParam String gameEn, @RequestParam Long userId, @RequestParam
            String token, Integer lastIndex, @RequestAttribute String versionCode) {

        Game game = GameCache.getGame(gameEn);
        Long lookUpUserId = exchangeUserId(token);
        if (lookUpUserId == null) {
            return buildErrJson("请登陆后查看");
        }

        Map result = socialEncircleCodeService.getMyEncirclesV2_3(game.getGameId(), userId, lookUpUserId, lastIndex);
        compatibleService.myEncirclesV2_3(result, game.getGameId(), versionCode);
        return buildSuccJson(result);
    }

    @RequestMapping("/myKillNumsV2-3")
    @ResponseBody
    public Object socialMyEncirclesV2_3(@RequestParam String gameEn, @RequestParam Long userId, @RequestParam String
            token, Integer lastIndex, @RequestAttribute String versionCode) {

        Game game = GameCache.getGame(gameEn);
        Long lookUpUserId = exchangeUserId(token);
        if (lookUpUserId == null) {
            return buildErrJson("请登陆后查看");
        }
        Map result = socialKillCodeService.getMyKillNumsV2_3(game.getGameId(), userId, lookUpUserId, lastIndex,
                versionCode);
        compatibleService.myKillNumsV2_3(result, game.getGameId(), versionCode);
        return buildSuccJson(result);
    }


    // v3.2 围号列表和杀号列表
    //围号列表
    @RequestMapping("/userSocialRecords")
    @ResponseBody
    public Object userSocialRecords(@RequestParam String gameEn, @RequestParam Long userId, @RequestParam
            String token, @RequestAttribute String versionCode, @RequestParam Integer type,
                                    @RequestParam(required = false) Boolean enHasNext, @RequestParam(required =
            false) Boolean killHasNext, @RequestParam(required = false) Integer enLastIndex,
                                    @RequestParam(required = false) Integer killLastIndex) {

        Game game = GameCache.getGame(gameEn);
        Long lookUpUserId = exchangeUserId(token);
        if (lookUpUserId == null) {
            return buildErrJson("请登陆后查看");
        }

        Map result = socialEncircleCodeService.userSocialRecords(game.getGameId(), userId, lookUpUserId,
                versionCode, type, enHasNext, killHasNext, enLastIndex, killLastIndex);

        return buildSuccJson(result);
    }

    // v3.0 荣耀成就
    //围号列表
    @RequestMapping("/userSocialAchieves")
    @ResponseBody
    public Object userSocialAchieves(@RequestParam String gameEn, @RequestParam Long userId, @RequestParam String
            token) {
        Long gameId = GameCache.getGame(gameEn).getGameId();
        Long chuserId = exchangeUserId(token);
        if (chuserId == null) {
            return buildErrJson("请登陆后查看");
        }

        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> userTitleDetail = userTitleService.getUserTitleDetail(gameId, userId);
        userTitleDetail.computeIfAbsent("godKillTimes", k -> "0次");
        userTitleDetail.computeIfAbsent("godEncircleTimes", k -> "0次");
        if (!userTitleDetail.get("godKillTimes").toString().contains("次")) {
            userTitleDetail.put("godKillTimes", userTitleDetail.get("godKillTimes").toString() + "次");
        }
        if (!userTitleDetail.get("godEncircleTimes").toString().contains("次")) {
            userTitleDetail.put("godEncircleTimes", userTitleDetail.get("godEncircleTimes").toString() + "次");
        }
        resultMap.putAll(userTitleDetail);

        Map<String, List<AchievementVo>> userAchieveMap = userSocialRecordService.getUserAchievementVo(gameId, userId);
        List<AchievementVo> encircleAchievements = userAchieveMap.get("encircleAchievements");

        List<AchievementVo> killAchievements = userAchieveMap.get("killAchievementsNew");

        // 循环遍历点亮的灯
        List<Map<String, Object>> enAchieves = new ArrayList<>();
        List<Map<String, Object>> killAchieves = new ArrayList<>();
        for (AchievementVo achievementVo : encircleAchievements) {
            Map<String, Object> achieve = new HashMap<>();
            achieve.put("achieveName", achievementVo.getAchieveName());
            achieve.put("achieveDesc", achievementVo.getAchieveDesc());
            achieve.put("ifHighLight", achievementVo.getIfHighLight());
            achieve.put("socialType", "围号");
            enAchieves.add(achieve);

        }
        for (AchievementVo achievementVo : killAchievements) {
            Map<String, Object> achieve = new HashMap<>();
            achieve.put("achieveName", achievementVo.getAchieveName());
            achieve.put("achieveDesc", achievementVo.getAchieveDesc());
            achieve.put("ifHighLight", achievementVo.getIfHighLight());
            achieve.put("socialType", "杀号");
            killAchieves.add(achieve);
        }

        //获取用户等级
        Map<String, Object> userSocialLevel = new HashMap();
        userSocialLevel.put("userHeadImg", loginService.getUserLoginVo(userId).getHeadImgUrl());

        String key = RedisConstant.getUserIntegralKey(gameId, userId);
        UserSocialIntegralVo userSocialIntegralVo = redisService.kryoGet(key, UserSocialIntegralVo.class);
        Double percent = 0d;
        int diff = 0;
        String score = "";
        String levelName = "";
        int userLevel = 0;
        int nextLevel = 0;
        String levelColor = "#8AD765";
        String levelEdgeColor = "#83C963";
        if (userSocialIntegralVo != null) {
            score = userSocialIntegralVo.getIntegral();
            String nextScore = userSocialIntegralVo.getUpgradeIntegral();
            if (StringUtils.isNotBlank(nextScore) && !nextScore.equals("0")) {
                percent = Double.valueOf(score) / Double.valueOf(nextScore) * 100;
                diff = Integer.valueOf(nextScore) - Integer.valueOf(score);
            }
            userLevel = userSocialIntegralVo.getSocialLevel();
            levelName = userSocialIntegralVo.getLevelName();
            SocialLevelIntegralVo nextLevleVo = SocialLevelIntegralCache.getUserLevelVoByIntegralByScore(Long.valueOf
                    (nextScore), 0);
            nextLevel = userSocialIntegralVo.getSocialLevel();
            if (nextLevleVo != null) {
                nextLevel = nextLevleVo.getLevelId();
            }
            levelColor = SocialLevelIntegralCache.getColorByLevel(userLevel);
            levelEdgeColor = SocialLevelIntegralCache.getEdgeColorByLevel(userLevel);
        }
        userSocialLevel.put("levelColor", levelColor);
        userSocialLevel.put("levelEdgeColor", levelEdgeColor);
        userSocialLevel.put("userScore", score);
        userSocialLevel.put("percent", percent.intValue());
        userSocialLevel.put("userLevel", "LV." + userLevel);
        userSocialLevel.put("userLevelName", "LV." + userLevel + levelName);
        userSocialLevel.put("userNextLevel", "LV." + nextLevel);
        String desc = "累积获得" + score + "积分，距升级还差" + diff + "积分";
        userSocialLevel.put("desc", desc);

        resultMap.put("userSocialLevel", userSocialLevel);
        resultMap.put("enAchieves", enAchieves);
        resultMap.put("killAchieves", killAchieves);

        return buildSuccJson(resultMap);
    }

    @RequestMapping("/classicEncircleList")
    @ResponseBody
    public Object socialClassicEncircle(@RequestParam String gameEn, String lastPeriodId, @RequestAttribute String
            versionCode, @RequestAttribute(required = false) Integer clientType) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("参数异常，彩种不存在");
        }
        if (StringUtils.isBlank(lastPeriodId)) {
            GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
            lastPeriodId = gamePeriod.getPeriodId();
        }

        Map result = socialClassicEncircleCodeService.getClassicEncircleList(game.getGameId(), lastPeriodId,
                SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED);
        compatibleService.classicEncircleListCompateUserId(game.getGameId(), result, versionCode);
        return buildSuccJson(result);
    }

    @RequestMapping("/socialBigData")
    @ResponseBody
    public Object socialBigData(@RequestParam String token, @RequestParam String gameEn) {
        Long userId = exchangeUserId(token);
        if (userId == null) {
            return buildErrJson("请登录后查看");
        }
        Game game = GameCache.getGame(gameEn);
        //check vip
        boolean res = vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        //check if gold purchase
        Map<String, Object> itemInfo = payService.getAccessIdByType(CommonConstant.ACCESS_BIG_DATA, game.getGameId());
        Integer itemId = (Integer) itemInfo.get("itemId");
        GamePeriod gamePeriod = PeriodRedis.getAwardCurrentPeriod(game.getGameId());
        boolean isPayed = payService.checkUserAccess(userId, game.getGameId(), gamePeriod.getPeriodId()
                , itemId);
        if (!res && !isPayed) {
            return buildErrJson("成为vip后可以查看");
        }

        Map<String, Object> result = socialEncircleCodeService.getSocialBigData(game.getGameId());
        return buildSuccJson(result);
    }

    @RequestMapping("/socialLevelDetail")
    @ResponseBody
    public Object socialLevelDetil(String gameEn) {
        Map result = new HashMap();
        List<Map> integrals = socialLevelIntegralService.getSocialLevelIntegrals();
        result.put("adMsg", "获得积分：参与预测社区围号和杀号即可获得积分");
        result.put("integrals", integrals);
        return buildSuccJson(result);
    }

    @RequestMapping("/userIntegralList")
    @ResponseBody
    public Object userIntegralList(@RequestParam String gameEn, @RequestParam String token, String lastPeriodId) {
        Long userId = exchangeUserId(token);
        if (userId == null) {
            return buildErrJson("用户存在");
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("彩种不存在");
        }

        Map result = socialIntegralLogService.getUserIntegralLogInfo(game.getGameId(), lastPeriodId, userId);
        return buildSuccJson(result);
    }
}
