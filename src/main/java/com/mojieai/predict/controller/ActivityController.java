package com.mojieai.predict.controller;

import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.MatchScheduleDao;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.MatchSchedule;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/22.
 */

@RequestMapping("/activity")
@Controller
public class ActivityController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserAuthController userAuthController;
    @Autowired
    private MatchInfoService matchInfoService;
    @Autowired
    private StarUserMatchService starUserMatchService;
    @Autowired
    private InternetCelebrityRecommendService internetCelebrityRecommendService;

    /* 抽奖*/
    @RequestMapping("/drawLottery")
    @ResponseBody
    public Object drawLottery(@RequestParam Integer activityId, @RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = userToken.getUserId();
        if (null == userId) {
            return buildErrJson("参数异常，用户不存在");
        }
        return buildSuccJson(activityService.drawLottery(activityId, userId));
    }

    /* 抽奖信息*/
    @RequestMapping("/drawLotteryInfo")
    @ResponseBody
    public Object drawLotteryInfo(@RequestParam Integer activityId, @RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = userToken.getUserId();
        if (null == userId) {
            return buildErrJson("参数异常，用户不存在");
        }
        return buildSuccJson(activityService.drawLotteryInfo(activityId, userId));
    }

    /* 分享出去*/
    @RequestMapping("/drawLotteryShareOut")
    @ResponseBody
    public Object drawLotteryShareOut(@RequestParam Integer activityId, @RequestParam(required = false) String token,
                                      @RequestParam(required = false) Long userId) {
        if (token != null) {
            UserToken userToken = loginService.checkToken(token);
            userId = userToken.getUserId();
            if (null == userId) {
                return buildErrJson("参数异常，用户不存在");
            }
        }
        activityService.drawLotteryShareOut(activityId, userId);
        return buildSuccJson();
    }

    /* 分享统计*/
    @RequestMapping("/drawNumberShare")
    @ResponseBody
    public Object drawNumberShare(@RequestParam Integer activityId, @RequestParam(required = false) String token,
                                  @RequestParam(required = false) Long userId, @RequestParam(required = false) String
                                          openId) {
        if (StringUtils.isNotBlank(token)) {
            UserToken userToken = loginService.checkToken(token);
            userId = userToken.getUserId();
        }
        if (null == userId) {
            return buildErrJson("参数异常，用户不存在");
        }
        activityService.drawNumberShare(activityId, userId, openId);
        return buildSuccJson();
    }

    // 领取号码接口
    @RequestMapping("/drawNumberAward")
    @ResponseBody
    public Object drawNumberAward(@RequestParam Integer activityId, @RequestParam String periodId, @RequestParam Integer
            levelId, @RequestParam Integer awardAmount) {
        return buildSuccJson(activityService.drawNumberAward(activityId, periodId, levelId, awardAmount));
    }

    // 分享接口 每日分享添加金币
    @RequestMapping("/newShare")
    @ResponseBody
    public Object newShare(@RequestParam Integer activityId, @RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("参数异常，用户不存在");
        }
        Map<String, Object> resultMap = activityService.newShare(activityId, userToken.getUserId());
        if (null == resultMap) {
            return buildErrJson("已经分享");
        }
        return resultMap;
    }

    // 注册接口
    @RequestMapping("/shareUserRegister")
    @ResponseBody
    public Object shareUserRegister(@RequestParam Integer activityId, @RequestParam Long fromUserId, @RequestParam
            String mobile, String verifyCode) {
        // 查询号码是否已经注册
        Long userId = loginService.getUserId(mobile);
        if (null != userId) {
            return buildErrJson("用户已注册");
        }
        Map<String, Object> resultData = (Map<String, Object>) userAuthController.login(mobile, verifyCode, null,
                "yaoqing", null, null);
        Map<String, Object> userData = (Map<String, Object>) resultData.get("data");
        UserLoginVo userLoginVo = (UserLoginVo) userData.get("userLogin");
        return buildSuccJson(activityService.shareUserRegister(activityId, fromUserId, userLoginVo.getUserId(),
                VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));
    }

    // 首页展示数据接口
    @RequestMapping("/shareUserIndex")
    @ResponseBody
    public Object shareUserIndex(@RequestParam Integer activityId, @RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (null != userToken) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(activityService.shareUserIndex(activityId, userId));
    }


    // 春节活动接口
    // 1. 首页接口
    @RequestMapping("/festivalIndexWithOutSign")
    @ResponseBody
    public Object festivalIndex(@RequestParam Integer activityId, @RequestParam(required = false) String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (null != userToken) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(activityService.festivalIndex(activityId, userId));
    }

    // 2. 答题接口
    @RequestMapping("/questionWithOutSign")
    @ResponseBody
    public Object questionWithOutSign(@RequestParam Integer activityId, @RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (null != userToken) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(activityService.questionAward(activityId, userId));
    }

    // 3. 答对接口
    @RequestMapping("/questionRightWithOutSign")
    @ResponseBody
    public Object questionRightWithOutSign(@RequestParam Integer activityId, @RequestParam String token, @RequestParam
            String questionId) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (null != userToken) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(activityService.questionRightWithOutSign(activityId, userId, questionId));
    }

    // 4. 答错接口
    @RequestMapping("/questionWrongWithOutSign")
    @ResponseBody
    public Object questionWrongWithOutSign(@RequestParam Integer activityId, @RequestParam String token, @RequestParam
            String questionId) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (null != userToken) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(activityService.questionWrongWithOutSign(activityId, userId, questionId));
    }

    //5. 榜单接口
    @RequestMapping("/rankWithOutSign")
    @ResponseBody
    public Object rankWithOutSign(@RequestParam Integer activityId, @RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (null != userToken) {
            userId = userToken.getUserId();
        }
        return buildSuccJson(activityService.rankWithOutSign(activityId, userId));
    }

    //6. 计算排行榜接口
    @RequestMapping("/rankDistributeWithOutSign")
    @ResponseBody
    public Object rankDistributeWithOutSign(@RequestParam Integer activityId) {
        return buildSuccJson(activityService.rankDistributeWithOutSign());
    }

    //7. 手机号清楚答题次数接口
    @RequestMapping("/clearTimesWithOutSign")
    @ResponseBody
    public Object clearTimesWithOutSign(@RequestParam Integer activityId, @RequestParam String mobile) {
        return buildSuccJson(activityService.clearTimesWithOutSign(activityId, mobile));
    }

    /*
     * 新手活动
     * */
    //
    // 检查用户是否已经领取过智慧币
    @RequestMapping("/checkIsGivenWisdomCoinWithOutSign")
    @ResponseBody
    public Object checkIsGivenWisdomCoinWithOutSign(@RequestParam Integer activityId, @RequestParam String token) {
        Map<String, Object> resultMap = new HashMap<>();
        UserToken userToken = loginService.checkToken(token);
        resultMap = activityService.checkIsGivenWisdomCoin(userToken.getUserId(), activityId);
        return buildSuccJson(resultMap);
    }

    // 已经登录用户直接领取智慧币
    @RequestMapping("/getWisdomCoinWithOutSign")
    @ResponseBody
    public Object getWisdomCoinWithOutSign(@RequestParam Integer activityId, @RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        activityService.registerGiveWisdomCoin(userToken.getUserId(), activityId);
        return buildSuccJson();
    }

    /*
     * 方案活动
     * */
    @RequestMapping("/getActivityInfo")
    @ResponseBody
    public Object getActivityProgram(@RequestParam Integer activityId, @RequestParam(required = false) String token) {
        Long userId = null;
        if (null != token) {
            UserToken userToken = loginService.checkToken(token);
            userId = userToken.getUserId();
        }
        return buildSuccJson(activityService.getActivityInfo(activityId, userId));
    }

    // 领取号码接口
    @RequestMapping("/drawNumber")
    @ResponseBody
    public Object drawNumber(@RequestParam Integer activityId, @RequestParam String token, @RequestParam Integer
            programId) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = userToken.getUserId();
        if (null == userId) {
            return buildErrJson("参数异常，用户不存在");
        }
        return buildSuccJson(activityService.drawNumber(activityId, userId, programId));
    }

    @RequestMapping("/get_coupon_by_activity")
    @ResponseBody
    public Object getCouponByActivity(@RequestParam String token, Integer activityId) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }

        return buildSuccJson(activityService.activityGiveCoupon2User(userToken.getUserId()));
    }

    @RequestMapping("/get_world_cup_match")
    @ResponseBody
    public Object getWorldCupMatch(Integer tagId) {
        if (tagId == null) {
            tagId = SportsProgramConstant.MATCH_TAG_FIFA_WORLD_CUP;
        }
        Map<String, Object> result = matchInfoService.getWorldCupMatchInfo(tagId);
        return buildSuccJson(result);
    }

    @RequestMapping("/get_star_users")
    @ResponseBody
    public Object getStarUsers() {
        return buildSuccJson(starUserMatchService.getStarUserList());
    }

    @RequestMapping("/get_internet_celebrity")
    @ResponseBody
    public Object getInternetCelebrity(@RequestParam Long celebrityUserId, String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }

        if (celebrityUserId == null) {
            return buildErrJson("用户不存在");
        }

        return buildSuccJson(internetCelebrityRecommendService.getInternetCelebrityInfo(celebrityUserId, userId));
    }

    /**
     * 获取当前可用的大咖推荐列表
     *
     * @param
     * @param token
     * @return
     */
    @RequestMapping("/internetCelebrities")
    @ResponseBody
    public Object getAllInternetCelebrities(@RequestParam(required = false) String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        Map<String, Object> result = internetCelebrityRecommendService.getAllUsableInternetCelebrities(userId);
        return buildSuccJson(result);
    }

    @RequestMapping("/internetCelebrities/unlock")
    @ResponseBody
    public Object unlock(@RequestParam String token, @RequestParam String recommendId) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("用户不存在");
        }
        String msg = internetCelebrityRecommendService.unlockCelebrityRecommendByCard(recommendId, userToken
                .getUserId());
        if (StringUtils.isNotBlank(msg)) {
            return buildErrJson(msg);
        }
        return buildSuccJson();
    }

    @RequestMapping("/add_celebrity_like_count")
    @ResponseBody
    public Object addLikeCount2Celebrity(@RequestParam String recommendId) {

        internetCelebrityRecommendService.addLikeCount(recommendId);
        return buildSuccJson("成功");
    }

    @RequestMapping("/get_internet_celebrity_info")
    @ResponseBody
    public Object getInternetCelebrityInfo() {
        return buildSuccJson(internetCelebrityRecommendService.getAddRecommendBaseInfo());
    }

    @RequestMapping("/get_internet_celebrities")
    @ResponseBody
    public Object getInternetCelebrities() {
        return buildSuccJson(internetCelebrityRecommendService.getAllInternetCelebrities());
    }

    @RequestMapping("/checkActivityFreeTicket")
    @ResponseBody
    public Object checkActivityFreeTicket(@RequestParam String token, @RequestParam Integer activityId) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isGetFreeTicket", activityService.checkUserTakepartActivity(userId, activityId, -1));
        return buildSuccJson(resultMap);
    }

    /**
     * 单关方案 活动 start
     **/
    @RequestMapping("/productDanguanProgram")
    @ResponseBody
    public Object productDanguanProgram(@RequestParam String matchId, @RequestParam String programInfo, @RequestParam
            Long price, @RequestParam Long vipPrice) {

        String msg = activityService.productDanguanProgram(matchId, programInfo, price, vipPrice);
        Integer code = 0;
        if (msg.indexOf("失败") > -1) {
            code = -1;
        }
        return buildJson(code, msg);
    }

    @RequestMapping("/danguanProgramInfo")
    @ResponseBody
    public Object danguanProgramInfo(@RequestParam(required = false) String token) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        Map<String, Object> resultMap = new HashMap<>();
        activityService.getRightAwardNums(resultMap);
        // 列表
        resultMap.put("matchList", activityService.danguanProgramList(userId));
        // 次数
        resultMap.put("LeftTimes", activityService.userDanguanTimes(userId));
        // 卡列表
        resultMap.put("cardList", activityService.danguanProgramCards());
        // 是否会员
        resultMap.put("isVip", activityService.checkVip(userId));
        return buildSuccJson(resultMap);
    }

    // 已经结束的比赛列表
    @RequestMapping("/danguanProgramHistory")
    @ResponseBody
    public Object danguanProgramHistory(@RequestParam(required = false) String token) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("matchList", activityService.danguanProgramHistory());
        return buildSuccJson(resultMap);
    }

    // 特权购买方案
    @RequestMapping("/privilegeBuyProgram")
    @ResponseBody
    public Object privilegeBuyProgram(@RequestParam String token, @RequestParam String matchId) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        Integer resultCode = activityService.privilegeBuyProgram(userId, matchId);
        String resultMsg = resultCode == 0 ? "购买成功" : "剩余特权次数不足";
        if (resultCode == -2) {
            resultMsg = "重复购买";
        }
        return buildJson(resultCode, resultMsg);
    }

    /*** 单关方案 活动 end**/

    /**
     * @return
     */
    @RequestMapping("/take_part_in_activity")
    @ResponseBody
    public Object takePartInActivity(@RequestParam String token, @RequestParam Integer activityId) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }

        return buildSuccJson(activityService.commonTakePartInActivity(activityId, userId));
    }

    @RequestMapping("/check_user_activity_status")
    @ResponseBody
    public Object checkUserActivityStatus(@RequestParam String token, @RequestParam Integer activityId) {
        UserToken userToken = loginService.checkToken(token);
        Long userId = null;
        if (userToken != null) {
            userId = userToken.getUserId();
        }
        boolean activityStatus = activityService.checkUserTakepartActivity(userId, activityId, -1);
        Map<String, Object> res = new HashMap<>();
        res.put("activityStatus", activityStatus ? 1 : 0);
        return buildSuccJson(res);
    }

}
