package com.mojieai.predict.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.BannerCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.dao.UserSportSocialRecommendDao;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.bo.RecommendCheckBo;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.entity.vo.BannerVo;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import com.mojieai.predict.util.qiniu.StringUtils;
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

@RequestMapping("/sportSocial")
@Controller
public class SportSocialController extends BaseController {

    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private CompatibleService compatibleService;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private UserFootballProgramService userFootballProgramService;
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;
    @Autowired
    private MarqueeService marqueeService;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private MatchInfoService matchInfoService;


    // 比赛列表接口
    @RequestMapping("/getMatchList")
    @ResponseBody
    public Object getMatchList(String token, @RequestParam(required = false) String lastDate, @RequestParam(required
            = false) String lastMatchId, @RequestAttribute Integer versionCode, @RequestAttribute Integer clientType) {
        Long userId = null;
        UserToken userToken = loginService.checkToken(token);
        if (null != userToken) {
            userId = userToken.getUserId();
        }

        Map<String, Object> result = thirdHttpService.getNotStartMatchResult(lastDate, lastMatchId, userId);
        result = compatibleService.iosReviewNotShowWorldCup(result, versionCode, clientType);
        return buildSuccJson(result);
    }

    // 发布推荐接口
    @RequestMapping("/getRecommendInfo")
    @ResponseBody
    public Object getRecommendInfo(@RequestParam String token, @RequestParam String matchId, @RequestParam Integer
            playType, @RequestAttribute Integer versionCode) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("用户信息校验失败");
        }

        Map<String, Object> result = sportSocialService.getRecommendInfo(matchId, userToken.getUserId(), playType);
        compatibleService.sportsRecommendRemunerationControl(result, versionCode);
        return buildSuccJson(result);
    }

    // 确认发布推荐接口
    @RequestMapping("/recommendMatch")
    @ResponseBody
    public Object recommendMatch(@RequestParam String token, @RequestParam String matchId, @RequestParam Integer
            playType, @RequestParam String recommendInfo, @RequestParam Integer itemId, @RequestParam(required =
            false) String baseOn, @RequestParam(required = false) String reason, @RequestAttribute(required = false)
                                         String visitorIp, @RequestAttribute(required = false) Integer clientType,
                                 @RequestParam(required = false) String recommendTitle, @RequestParam(required =
            false) String mainRecommend) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("用户信息校验失败");
        }
        // TODO 发布校验
        if (sportSocialService.getUserRecommend(userToken.getUserId(), DateUtil.getBeginOfToday(), DateUtil
                .getEndOfToday()) >= 5) {
            return buildErrJson("今天已发满5单");
        }
        DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchListByMatchIds(matchId).get(0);
        RecommendCheckBo recommendCheckBo = checkUserRecommendInfo(recommendInfo, playType, detailMatchInfo);
        if (recommendCheckBo.getCode().equals(ResultConstant.ERROR)) {
            return buildErrJson(recommendCheckBo.getMsg());
        }
        // 生成一个recommendId
        String recommendId = sportSocialService.generateRecommendId(userToken.getUserId());
        Long price = sportSocialService.getRecommendPriceById(itemId);
        UserSportSocialRecommend userSportSocialRecommend = new UserSportSocialRecommend(recommendId, userToken
                .getUserId(), matchId, 200, playType, detailMatchInfo.getRecommendInfo(playType, recommendInfo), itemId,
                price, baseOn, reason, null, 0, detailMatchInfo.getHandicap(playType), detailMatchInfo.getEndTime(), "");
        // 4.6.2版本新增title
        if (!StringUtils.isBlank(recommendTitle)) {
            userSportSocialRecommend.setRecommendTitle(CommonUtil.filterEmoji(recommendTitle));
        } else {
            userSportSocialRecommend.setRecommendTitle("");
        }
        Map<String, Object> remark = new HashMap<>();
        // 4.6.4版本新增主推 单选标签 分析标签
        if (!StringUtils.isBlank(mainRecommend)) {
            remark.put("mainRecommend", mainRecommend);
        }
        if (!playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA) && recommendInfo.split(CommonConstant
                .COMMA_SPLIT_STR).length == 1) {
            remark.put("singleRecommend", 1);
        }
        if (!StringUtils.isBlank(reason) && reason.length() > 20) {
            remark.put("analysis", 1);
        }
        userSportSocialRecommend.setRemark(JSONUtils.toJSONString(remark));

        sportSocialService.saveUserRecommend(userSportSocialRecommend, visitorIp, clientType);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("showText", "推荐发布成功！");
        return buildSuccJson(resultMap);
    }

    private RecommendCheckBo checkUserRecommendInfo(String recommendInfo, Integer playType, DetailMatchInfo
            detailMatchInfo) {
        RecommendCheckBo result = new RecommendCheckBo(ResultConstant.SUCCESS, "");
        if (StringUtils.isBlank(recommendInfo)) {
            return result;
        }
        String[] recommendArr = recommendInfo.split(",");
        if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF) || playType.equals(SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_RQSPF)) {
            if (recommendArr.length == 3) {
                result.setCode(ResultConstant.ERROR);
                result.setMsg("推荐选项不符合格式");
                return result;
            }
            if (recommendArr.length < 2) {
                return result;
            }
            String msg = "双推不能含1.6以下赔率";
            for (String option : recommendArr) {
                String odd = detailMatchInfo.getItemOdd(playType, option);
                if (Double.valueOf(odd) <= 1.6) {
                    result.setCode(ResultConstant.ERROR);
                    result.setMsg(msg);
                    break;
                }
            }
        } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
            if (recommendArr.length == 2) {
                result.setCode(ResultConstant.ERROR);
                result.setMsg("推荐选项不符合格式");
                return result;
            }
        }
        if (!detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_INIT)) {
            result.setCode(ResultConstant.ERROR);
            result.setMsg("赛事已过期");
            return result;
        }
        return result;
    }

    // 排行榜接口
    @RequestMapping("/sportSocialRank")
    @ResponseBody
    public Object sportSocialRank(@RequestParam(required = false) String token, @RequestParam Integer
            rankType, @RequestParam Integer playType, @RequestParam(required = false) Integer nextPage) {
        // 拼接排行榜数据
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (null != userToken) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(sportSocialService.getSportSocialRankList(userId, rankType, playType, nextPage));
    }

    @RequestMapping("/sportSocialIndex")
    @ResponseBody
    public Object footballIndex(String token, @RequestAttribute(required = false) Integer clientType,
                                @RequestAttribute(required = false) Integer versionCode) {
        Map<String, Object> res = new HashMap<>();
        //1.banner
        List<BannerVo> banners = BannerCache.getBannerVosV2(BannerCache.POSITION_TYPE_FOOTBALL_INDEX, versionCode,
                clientType);

        //2.rank
        List<Map<String, Object>> rank = getSportSocialIndexRank();

        //3.marquee
        List<Map<String, Object>> marquee = marqueeService.getRecentMarqueeInfo();

        //4.热门推荐
        List<Map<String, Object>> hotRecommend = userSportSocialRecommendService.getSportSocialIndexHotRecommend();

        //5.用户推荐信息
        Long userId = null;
        UserToken userToken = loginService.checkToken(token);
        if (userToken != null && userToken.getUserId() != null) {
            userId = userToken.getUserId();
        }
        Map<String, Object> userRecommend = getIndexUserRecommend(userId);

        //
        Map<String, Object> couponActivity = userCouponService.getCouponActivityIndexInfo(userId);

        res.put("banner", banners);
        res.put("rank", rank);
        res.put("marquee", marquee);
        res.put("hotRecommend", hotRecommend);
        res.put("userRecommend", userRecommend);
        res.put("couponActivity", couponActivity);
        return buildSuccJson(res);
    }

    @RequestMapping("/get_sports_index")
    @ResponseBody
    public Object sportsIndex(String token, @RequestAttribute Integer clientType, @RequestAttribute Integer
            versionCode) {
        Map<String, Object> res = new HashMap<>();
        Long userId = null;
        UserToken userToken = loginService.checkToken(token);
        if (userToken != null && userToken.getUserId() != null) {
            userId = userToken.getUserId();
        }

        List<Map<String, Object>> toolNavigation = ActivityIniCache.getActivityIniListValueByWeight(ActivityIniConstant
                .SPORTS_INDEX_TOOL_NAVIGATION_CONF + clientType);
        if (toolNavigation != null) {
            for (int i = 0; i < toolNavigation.size(); i++) {
                Map<String, Object> nav = toolNavigation.get(i);
                if (versionCode < CommonConstant.VERSION_CODE_4_6_4 && nav.get("umStatistics").toString().equals
                        ("click_vipZone_button")) {
                    toolNavigation.remove(nav);
                }
            }
        }

        res.put("banner", BannerCache.getBannerVosV2(BannerCache.POSITION_TYPE_FOOTBALL_INDEX, versionCode,
                clientType));
        res.put("marquee", marqueeService.getRecentMarqueeInfo());
        res.put("userRecommend", getIndexUserRecommend(userId));
        res.put("couponActivity", userCouponService.getCouponActivityIndexInfo(userId));
        res.put("toolNavigation", toolNavigation);
        res.put("activityNavigation", ActivityIniCache.getActivityIniListValueByWeight(ActivityIniConstant
                .SPORTS_INDEX_ACTIVITY_NAVIGATION_CONF + clientType));
        res.put("focusMatches", matchInfoService.getFocusMatches());
        res.put("focusMatchesJumpUrl", SportsUtils.getTagMatchListJumpUrl(SportsProgramConstant
                .MATCH_JUMP_TYPE_MATCH, SportsProgramConstant.MATCH_TAG_WISDOM_RECOMMEND));
        return buildSuccJson(res);
    }

    @RequestMapping("/sportSocialPersonCenter")
    @ResponseBody
    public Object sportSocialPersonCenter(@RequestParam String userId, Integer lotteryCode, String token, String
            lastIndex, @RequestAttribute Integer versionCode, @RequestAttribute Integer clientType) {
        Long visitUserId = null;
        UserToken userToken = loginService.checkToken(token);
        if (userToken != null && userToken.getUserId() != null) {
            visitUserId = userToken.getUserId();
        }
        Map<String, Object> res = userFootballProgramService.getSportSocialPersonCenter(Long.valueOf(userId),
                visitUserId, lotteryCode, lastIndex);
        compatibleService.sportsSocialPersonCenterIosReview(res, versionCode, clientType);
        return buildSuccJson(res);
    }

    @RequestMapping("/recommendList")
    @ResponseBody
    public Object recommendList(@RequestParam Integer listType, Integer playType, String token, String lastIndex,
                                @RequestAttribute Integer versionCode, @RequestAttribute Integer clientType) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        if (playType == null) {
            playType = SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF;
        }
        Map<String, Object> res = userSportSocialRecommendService.getUserRecommendListFromRedis(userId, listType,
                playType, lastIndex);
        compatibleService.recommendListWorldCup(res, versionCode, clientType);
        return buildSuccJson(res);
    }

    @RequestMapping("/sportSocialRecommendDetail")
    @ResponseBody
    public Object sportSocialRecommendDetail(@RequestParam String recommendId, String token, @RequestAttribute String
            versionCode) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        Map res = userFootballProgramService.getSportSocialRecommendDetail(recommendId, userId, versionCode);
        return buildSuccJson(res);
    }

    @RequestMapping("/userPurchaseSportRecommend")
    @ResponseBody
    public Object getUserPurchaseSportRecommend(@RequestParam Integer lotteryCode, @RequestParam String token, String
            lastIndex, @RequestAttribute Integer versionCode, @RequestAttribute Integer clientType) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        Map<String, Object> res = userBuyRecommendService.getUserPurchaseSportRecommend(userToken.getUserId(),
                lotteryCode, lastIndex);

        compatibleService.userPurchaseSportsIosReview(res, versionCode, clientType);
        return buildSuccJson(res);
    }

    @RequestMapping("/editUserIntroduction")
    @ResponseBody
    public Object editUserIntroduction(@RequestParam String token, @RequestParam String text) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        Map<String, Object> res = loginService.editUserFootballIntroduction(userToken.getUserId(), text);
        if (Integer.valueOf(res.get("code").toString()).equals(-1)) {
            return buildErrJson(res.get("msg").toString());
        }
        return buildSuccJson(res);
    }

    @RequestMapping("/getRecommendMatchPlayTypes")
    @ResponseBody
    public Object getRecommendMatchPlayTypes(@RequestParam String matchId, Integer lotteryCode) {

        Map<String, Object> res = thirdHttpService.getRecommendMatchPlayTypes(lotteryCode, matchId);
        return buildSuccJson(res);
    }

    @RequestMapping("/get_match_basic_data")
    @ResponseBody
    public Object getMatchBasicData(@RequestParam String matchId) {
        return buildSuccJson(userSportSocialRecommendService.getMatchBasicData(matchId));
    }

    @RequestMapping("/get_match_odds_data")
    @ResponseBody
    public Object getMatchOddsData(@RequestParam String matchId) {
        return buildSuccJson(userSportSocialRecommendService.getMatchOddsData(matchId));
    }

    @RequestMapping("/get_match_predict")
    @ResponseBody
    public Object getMatchPredict(@RequestParam String matchId, String lastIndex, @RequestAttribute Integer
            versionCode, @RequestAttribute Integer clientType) {
        Map<String, Object> res = userSportSocialRecommendService.getMatchPredictData(matchId, lastIndex);
        compatibleService.iosReviewMatchPredict(res, versionCode, clientType);
        return buildSuccJson(res);
    }

    @RequestMapping("/get_tag_list")
    @ResponseBody
    public Object getTagList() {
        return buildSuccJson(matchInfoService.getTagList());
    }

    @RequestMapping("/get_tag_match_list")
    @ResponseBody
    public Object getTagMatchList(@RequestParam Integer tagId, String token, Long lastHistoryId, Long lastFutureId) {

        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(matchInfoService.getTimeLineTagMatches(tagId, userId, lastHistoryId, lastFutureId));
    }

    /* 关注比赛接口*/
    @RequestMapping("/followMatch")
    @ResponseBody
    public Object followMatch(@RequestParam String token, @RequestParam String matchId) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (null == userToken) {
            return buildErrJson("请登陆后关注赛事");
        }
        userId = userToken.getUserId();
        Map<String, Object> resultMap = new HashMap();
        resultMap.put("followStatus", sportSocialService.followMatch(userId, matchId));
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/get_mj_match_tag")
    @ResponseBody
    public Object getMJMatchTag() {
        return buildSuccJson(sportSocialService.getMJMatchTag());
    }

    @RequestMapping("/get_mj_league_match_list")
    @ResponseBody
    public Object getMJLeagueMatchList(@RequestParam String leagueId) {
        return buildSuccJson(sportSocialService.getMJLeagueMatchList(leagueId));
    }

    @RequestMapping("/get_mj_league_group_match")
    @ResponseBody
    public Object getMJLeagueGroupMatch(String groupId) {
        return buildSuccJson(sportSocialService.getMJLeagueGroupMatch(groupId));
    }

    private Map<String, Object> getIndexUserRecommend(Long userId) {
        Map<String, Object> res = new HashMap<>();
        Integer leftTimes = 5;
        if (userId != null) {
            Integer userTimes = userSportSocialRecommendDao.getUserSportSocialRecommendsByTime(userId, DateUtil
                    .getBeginOfToday(), DateUtil.getEndOfToday());
            leftTimes = 5 - (userTimes == null ? 0 : userTimes);
        }

        res.put("leftTimes", leftTimes);
        res.put("noTimesAd", "今天已发满5单");
        return res;
    }

    private List<Map<String, Object>> getSportSocialIndexRank() {
        List<Map<String, Object>> res = new ArrayList<>();
        //

        Map<String, Object> indexProfitRank = getSportSocialIndexRank(SportsProgramConstant
                .SPORT_SOCIAL_RANK_TYPE_PROFIT);
        Map<String, Object> indexRightNumRank = getSportSocialIndexRank(SportsProgramConstant
                .SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM);
        Map<String, Object> indexContinueRank = getSportSocialIndexRank(SportsProgramConstant
                .SPORT_SOCIAL_RANK_TYPE_CONTINUE);

        res.add(indexProfitRank);
        res.add(indexRightNumRank);
        res.add(indexContinueRank);
        return res;
    }

    private Map<String, Object> getSportSocialIndexRank(Integer rankType) {
        Map<String, Object> res = new HashMap<>();
        List<Object> rank = sportSocialService.getSportSocialRankList(rankType, SportsProgramConstant
                .SPORT_SOCIAL_RANK_PLAY_TYPE_MULTIPLE, 2);

        res.put("rankName", SportsUtils.getSocialRankCn(rankType));
        res.put("rankType", rankType);
        res.put("rankNameBg", SportsUtils.getSocialRankBg(rankType));
        res.put("ranks", rank);
        return res;
    }

}
