package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.CelebrityRecommendVo;
import com.mojieai.predict.entity.vo.PrivilegedCardVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import com.yeepay.shade.com.google.common.collect.Lists;
import com.yeepay.shade.com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class InternetCelebrityRecommendServiceImpl implements InternetCelebrityRecommendService, BeanSelfAware {
    protected Logger log = LogConstant.commonLog;
    private InternetCelebrityRecommendService self;

    @Autowired
    private InternetCelebrityRecommendDao internetCelebrityRecommendDao;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MatchScheduleDao matchScheduleDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private ActivityInfoDao activityInfoDao;
    @Autowired
    private CouponConfigDao couponConfigDao;
    @Autowired
    private UserCouponDao userCouponDao;
    @Autowired
    private UserBuyRecommendDao userBuyRecommendDao;
    @Autowired
    private UserBuyRecommendIdSequenceDao userBuyRecommendIdSequenceDao;
    @Autowired
    private PayService payService;
    @Autowired
    private UserCouponFlowIdSeqDao userCouponFlowIdSeqDao;
    @Autowired
    private UserCouponIdSequenceDao userCouponIdSequenceDao;
    @Autowired
    private UserCouponFlowDao userCouponFlowDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;

    @Override
    public Map<String, Object> celebrityAddRecommend(Long userId, Integer matchId, Integer goodsPriceId, String
            recommendInfo, String reason, String rewardDesc, String visitorIp, Integer clientType, Integer
                                                             programType, String originPrice, Integer index, String
                                                             tips) {
        Map<String, Object> result = new HashMap<>();
        String msg = "";
        Integer status = ResultConstant.ERROR;
        Map<Integer, String> recommendMap = analysisRecommend(recommendInfo);
        if (recommendMap == null) {
            result.put("status", status);
            result.put("msg", "推荐信息填写错误");
            return result;
        }
        DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchListByMatchIds(String.valueOf(matchId)).get(0);
        //1.发胜平负预测
        UserSportSocialRecommend recommend = internetCelebrityRecommendBasePredict(userId, goodsPriceId, matchId,
                recommendMap.get(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF), reason, visitorIp, clientType,
                detailMatchInfo);
        if (recommend == null) {
            recommend.setReason(reason);
            userSportSocialRecommendDao.update(recommend);
            result.put("status", status);
            result.put("msg", "该场比赛已经预测");
            return result;
        }
        //2.组装大咖内容
        String recommendAll = packageCelebrity(recommendMap, rewardDesc, detailMatchInfo, programType, originPrice,
                tips);
        InternetCelebrityRecommend celebrityRecommend = new InternetCelebrityRecommend();
        celebrityRecommend.setLikeCount(0L);
        celebrityRecommend.setMatchId(Integer.valueOf(detailMatchInfo.getMatchId()));
        celebrityRecommend.setMatchTime(detailMatchInfo.getEndTime());
        celebrityRecommend.setRecommendId(recommend.getRecommendId());
        celebrityRecommend.setUserId(userId);
        celebrityRecommend.setPopularIndex(index);
        celebrityRecommend.setRemark(recommendAll);
        celebrityRecommend.setPrice(recommend.getPrice());
        celebrityRecommend.setStatus(1);
        try {
            if (internetCelebrityRecommendDao.insert(celebrityRecommend) > 0) {
                status = ResultConstant.SUCCESS;
                msg = "推荐成功";
            }
        } catch (DuplicateKeyException e) {
            internetCelebrityRecommendDao.update(celebrityRecommend);
            status = ResultConstant.SUCCESS;
            msg = "推荐已成功添加";
        }

        result.put("status", status);
        result.put("msg", msg);
        return result;
    }

    @Override
    public Map<String, Object> getInternetCelebrityInfo(Long celebrityUserId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        UserInfo userInfo = userInfoDao.getUserInfo(celebrityUserId);
        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("userName", userInfo.getNickName());
        userInfoMap.put("userImg", userInfo.getHeadImgUrl());
        userInfoMap.put("userIntroduce", userInfo.getFootballIntroduce());
        userInfoMap.put("celebrityUserId", userInfo.getUserId());

        InternetCelebrityRecommend celebrityRecommend = internetCelebrityRecommendDao.getRecentEnableRecommend
                (celebrityUserId);
        Map<String, Object> matchInfo = new HashMap<>();
        Map<String, DetailMatchInfo> detailMatchInfoMap = thirdHttpService.getMatchMapByMatchIds(celebrityRecommend
                .getMatchId() + "");
        DetailMatchInfo detailMatchInfo = null;
        if (detailMatchInfoMap != null) {
            detailMatchInfo = detailMatchInfoMap.get(celebrityRecommend.getMatchId() + "");

            Integer single = 0;
            if (StringUtils.isNotBlank(detailMatchInfo.getTag()) && detailMatchInfo.getTag().equals("竞彩单关")) {
                single = 1;
            }

            matchInfo.put("matchName", detailMatchInfo.getMatchName());
            matchInfo.put("single", single);
            matchInfo.put("hostName", detailMatchInfo.getHostName());
            matchInfo.put("awayName", detailMatchInfo.getAwayName());
            matchInfo.put("hostImg", detailMatchInfo.getHostImg());
            matchInfo.put("awayImg", detailMatchInfo.getAwayImg());
            matchInfo.put("matchTime", detailMatchInfo.getMatchTime());
            matchInfo.put("groupDesc", "");
            matchInfo.put("countDownSecond", DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), detailMatchInfo
                    .getEndTime()));
        }

        List<Map<String, Object>> historyRecords = new ArrayList<>();
        List<InternetCelebrityRecommend> historyRecommends = internetCelebrityRecommendDao.getHistoryLatestRecommend
                (celebrityUserId, 100);
        if (historyRecommends != null && historyRecommends.size() > 0) {
            for (InternetCelebrityRecommend historyRecommend : historyRecommends) {
                Map<String, Object> temp = convertCelebrityRecommend2Map(historyRecommend);
                if (temp != null) {
                    historyRecords.add(temp);
                }
            }
        }
        String key = RedisConstant.getInternetCelebrityLikeCount(celebrityRecommend.getRecommendId());
        String countStr = redisService.get(key);
        Long likeCount = celebrityRecommend.getLikeCount();
        if (StringUtils.isNotBlank(countStr)) {
            likeCount = Long.valueOf(countStr);
        }

        Integer discount = CommonUtil.getValueFromJSONMap(celebrityRecommend.getRemark(), "discount") == null ? 0 :
                Integer.valueOf(CommonUtil.getValueFromJSONMap(celebrityRecommend.getRemark(), "discount").toString());
        Integer programType = CommonUtil.getValueFromJSONMap(celebrityRecommend.getRemark(), "programType") == null ?
                0 : Integer.valueOf(CommonUtil.getValueFromJSONMap(celebrityRecommend.getRemark(), "programType")
                .toString());

        int availableTimes = getCouponAvailableTimesByType(userId, UserCoupon
                .ACCESS_TYPE_CELEBRITY_RECOMMEND, UserCoupon.USE_STATUS_USABLE);

        Boolean purchaseStatus = Boolean.FALSE;
        if (null != userId) {
            purchaseStatus = userBuyRecommendService.checkUserPurchaseFootballProgramStatus(userId,
                    celebrityRecommend.getRecommendId());
            purchaseStatus = checkUserIsPlateUser(userId, purchaseStatus);
        }
        Map<String, Object> payMemo = new HashMap<>();
        payMemo.put("goodsType", "footballRecommend");
        payMemo.put("goodsId", celebrityRecommend.getRecommendId());
        payMemo.put("versionCode", CommonConstant.VERSION_CODE_4_3);
        int recommendPayType = null != userId && availableTimes > 0 ? 1 : 0;//智慧币种
        payMemo.put("recommendPayType", recommendPayType);

        result.put("userInfo", userInfoMap);
        result.put("matchInfo", matchInfo);
        result.put("recommendInfo", getCelebrityRecommendInfo(userId, celebrityRecommend, detailMatchInfo,
                purchaseStatus, availableTimes));
        result.put("likeCount", likeCount);
        result.put("historyRecords", historyRecords);
        result.put("payMemo", JSONObject.toJSONString(payMemo));
        result.put("discount", discount);
        result.put("programType", programType);
        result.put("availableTimes", availableTimes);
        return result;
    }

    @Override
    public void addLikeCount(String recommendId) {
        String key = RedisConstant.getInternetCelebrityLikeCount(recommendId);
        Long likeCount = redisService.incr(key);
        if (likeCount % 20 == 0) {//todo 做成异步取的，现在先简单的来
            internetCelebrityRecommendDao.updateRecommendLikeCount(recommendId, likeCount, null);
        }
        redisService.expire(key, 604800);
    }

    @Override
    public Map<String, Object> getAddRecommendBaseInfo() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> matches = new ArrayList<>();
        List<MatchSchedule> matchSchedules = matchScheduleDao.getAllNotOpeningMatch();
        if (matchSchedules != null && matchSchedules.size() > 0) {
            for (MatchSchedule matchSchedule : matchSchedules) {
                if (matchSchedule.getIfEnd() != null && matchSchedule.getIfEnd().equals(1)) {
                    continue;
                }
                Map<String, DetailMatchInfo> map = thirdHttpService.getMatchMapByMatchIds(matchSchedule.getMatchId() +
                        "");
                if (map == null || map.isEmpty()) {
                    continue;
                }
                DetailMatchInfo detailMatchInfo = map.get(matchSchedule.getMatchId() + "");
                String desc = detailMatchInfo.getMatchName() + " " + detailMatchInfo.getMatchTime() + " " +
                        detailMatchInfo.getHostName() + " VS " + detailMatchInfo.getAwayName();
                Map<String, Object> tempMap = new HashMap<>();
                tempMap.put("matchId", matchSchedule.getMatchId());
                tempMap.put("desc", desc);
                matches.add(tempMap);
            }
        }
        //大咖人
        List<Map<String, Object>> celebrity = new ArrayList<>();
        List<Long> userIds = internetCelebrityRecommendDao.getAllCelebrityUser();
        for (Long userId : userIds) {
            UserLoginVo loginVo = loginService.getUserLoginVo(userId);
            if (loginVo != null) {
                Map<String, Object> temp = new HashMap<>();
                temp.put("userId", userId);
                temp.put("userName", loginVo.getNickName());
                temp.put("mobile", loginVo.getMobile());
                celebrity.add(temp);
            }
        }

        result.put("celebrity", celebrity);
        result.put("matches", matches);
        return result;
    }

    @Override
    public Map<String, Object> getAllInternetCelebrities() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();

        List<InternetCelebrityRecommend> celebrities = internetCelebrityRecommendDao.getAllShowCelebrities();
        for (InternetCelebrityRecommend celebrity : celebrities) {
            Map<String, Object> temp = new HashMap<>();

            Map<String, Object> remark = JSONObject.parseObject(celebrity.getRemark(), HashMap.class);
            Map<Integer, String> recommendMap = JSONObject.parseObject(remark.get("recommendMap").toString(), HashMap
                    .class);
            String rewardDesc = remark.get("rewardDesc").toString();
            Integer programType;
            try {
                programType = Integer.valueOf(remark.get("programType").toString());
            } catch (Exception e) {
                programType = 0;
            }
            Integer originPrice;
            try {
                originPrice = Integer.valueOf(remark.get("originPrice").toString());
            } catch (Exception e) {
                originPrice = null;
            }


            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(celebrity.getMatchId());
            String matchInfo = detailMatchInfo.getMatchName() + " " + detailMatchInfo.getMatchDate() + " " +
                    detailMatchInfo.getHostName() + " VS " + detailMatchInfo.getAwayName() + " " + detailMatchInfo
                    .getMatchTime();
            temp.put("userName", loginService.getUserLoginVo(celebrity.getUserId()).getNickName());
            temp.put("price", CommonUtil.convertFen2Yuan(celebrity.getPrice()) + CommonConstant.WISDOM_COIN_PAY_NAME);
            temp.put("matchInfo", matchInfo);
            temp.put("rewardDesc", rewardDesc);
            temp.put("programType", programType);
            temp.put("originPrice", originPrice);
            UserSportSocialRecommend userSportSocialRecommend = userSportSocialRecommendDao
                    .getSportSocialRecommendById(celebrity.getUserId(), celebrity.getRecommendId(), Boolean.FALSE);
            temp.put("reason", userSportSocialRecommend.getReason());
            temp.put("recommendInfo", getInternetCelebrityRecommendInfo(recommendMap));
            temp.put("likeCount", celebrity.getLikeCount());
            users.add(temp);
        }
        result.put("users", users);
        return result;
    }

    private String getInternetCelebrityRecommendInfo(Map<Integer, String> recommendMap) {
        StringBuilder sb = new StringBuilder();

        for (Integer key : recommendMap.keySet()) {
            sb.append(SportsUtils.getPlayTypeCn(200, key)).append(":");
            String[] optiones = recommendMap.get(key).split(",");
            for (int i = 0; i < optiones.length; i++) {
                sb.append(SportsUtils.getItemCn(200, key, optiones[i]));
                if (i < optiones.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("  ");
        }
        return sb.toString().trim();
    }

    private Map<String, Object> convertCelebrityRecommend2Map(InternetCelebrityRecommend historyRecommend) {
        Map<String, Object> result = new HashMap<>();

        Map<String, DetailMatchInfo> detailMatchInfoMap = thirdHttpService.getMatchMapByMatchIds(historyRecommend
                .getMatchId() + "");
        DetailMatchInfo detailMatchInfo = detailMatchInfoMap.get(historyRecommend.getMatchId() + "");
        if (!detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            return null;
        }

        String matchTime = detailMatchInfo.getMatchTime();
        String matchDesc = matchTime + " " + detailMatchInfo.getHostName() + " " + detailMatchInfo.getHostScore() +
                ":" + detailMatchInfo.getAwayScore() + " " + detailMatchInfo.getAwayName();

        List<String> recommends = SportsUtils.getIntegerCelebritySportsRecommendsByRemark(historyRecommend.getRemark()
                , detailMatchInfo);
        if (recommends == null || recommends.size() == 0) {
            return null;
        }
        result.put("matchDesc", matchDesc);
        result.put("recommends", recommends);
        return result;
    }

    /*
    private Map<String, Object> getCelebrityRecommendInfo(Long userId, InternetCelebrityRecommend celebrityRecommend,
                                                          DetailMatchInfo detailMatchInfo) {
        Map<String, Object> result = new HashMap<>();

        Boolean purchaseStatus = userBuyRecommendService.checkUserPurchaseFootballProgramStatus(userId,
                celebrityRecommend.getRecommendId());
        purchaseStatus = checkUserIsPlateUser(userId, purchaseStatus);
        String price = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(celebrityRecommend.getPrice())
                .toString());
        String btnMsg = price + "智慧币 立即查看";
        String recommendPlayType = "";
        Map<String, Object> remark = JSONObject.parseObject(celebrityRecommend.getRemark());
        if (remark != null) {
            String rewardDesc = remark.get("rewardDesc").toString();
            String rewardMin = rewardDesc.split(":")[0];
            String rewardMax = rewardDesc.split(":")[1];

            recommendPlayType = getAllPlayType(remark.get("recommendMap").toString());

            Map<String, Object> celebrityRecommends = new HashMap<>();
            List<String> recommends = new ArrayList<>();
            if (purchaseStatus) {
                btnMsg = "";
                recommends = SportsUtils.getIntegerCelebritySportsRecommendsByRemark(celebrityRecommend.getRemark(),
                        detailMatchInfo);
            }
            celebrityRecommends.put("memo", "提示：预测可能会变化，请比赛前20分钟再查看");
            celebrityRecommends.put("recommends", recommends);

            UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById
                    (celebrityRecommend.getUserId(), celebrityRecommend.getRecommendId(), false);

            String originPrice = "";
            String currentPrice = "";
            if (remark.containsKey("originPrice") && remark.get("originPrice") != null) {
                originPrice = CommonConstant.COMMON_YUAN_STR + remark.get("originPrice") + CommonConstant
                        .WISDOM_COIN_PAY_NAME;
                currentPrice = CommonConstant.COMMON_YUAN_STR + price + CommonConstant.WISDOM_COIN_PAY_NAME;
                btnMsg = "立即查看";
            }

            result.put("buyStatus", purchaseStatus ? 1 : 0);
            result.put("btnMsg", btnMsg);
            result.put("originPrice", originPrice);
            result.put("currentPrice", currentPrice);
            result.put("rewardMin", rewardMin);
            result.put("rewardMax", rewardMax);
            result.put("recommendPlayType", recommendPlayType);
            result.put("recommend", celebrityRecommends);
            result.put("reason", recommend.getReason());
            result.put("recommendId", recommend.getRecommendId());
            // 添加推荐title 4.6.2
            userSportSocialRecommendService.userRecommendTitleLock(recommend);
            result.put("recommendTitle", recommend.getRecommendTitle());

            result.put("price", CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(celebrityRecommend
                    .getPrice()).toString()));
        }
        return result;
    }
    */

    private Boolean checkUserIsPlateUser(Long userId, Boolean purchaseStatus) {
        if (purchaseStatus) {
            return true;
        }
        String masterUserIds = ActivityIniCache.getActivityIniValue(ActivityIniConstant.INTERNET_CELEBRITY_MASTER, "");
        if (userId != null && masterUserIds.contains(userId + "")) {
            return true;
        }
        return purchaseStatus;
    }

    private String getAllPlayType(String recommendMap) {
        String result = "";
        Map<Integer, String> playTypeRecommends = JSONObject.parseObject(recommendMap, HashMap.class);
        StringBuffer sb = new StringBuffer();
        for (Integer key : playTypeRecommends.keySet()) {
            sb.append(SportsUtils.getPlayTypeCn(200, key)).append(" ");
        }
        result = sb.toString().trim();
        result = result.replaceAll(" ", "、");
        return result;
    }

    private String packageCelebrity(Map<Integer, String> recommendMap, String rewardDesc, DetailMatchInfo
            detailMatchInfo, Integer programType, String originPrice, String tips) {
        Map<String, Object> result = new HashMap<>();
        result.put("rewardDesc", rewardDesc);
        result.put("tips", tips);
        result.put("recommendMap", JSONObject.toJSONString(recommendMap));
        result.put("teamName", detailMatchInfo.getHostName() + " VS " + detailMatchInfo.getAwayName());
        if (null != programType) {
            result.put("programType", programType);
        }
        if (null != originPrice) {
            result.put("originPrice", originPrice);
        }
        return JSONObject.toJSONString(result);
    }

    //胜平负:主胜,主负;让球:让胜
    private Map<Integer, String> analysisRecommend(String recommendInfo) {
        Map<Integer, String> result = new HashMap<>();
        if (StringUtils.isBlank(recommendInfo)) {
            return null;
        }
        String[] recommendArr = recommendInfo.split(";");
        for (String recommend : recommendArr) {
            String[] playRecommend = recommend.split("#");
            result.put(Integer.valueOf(playRecommend[0]), playRecommend[1]);
        }
        return result;
    }

    private UserSportSocialRecommend internetCelebrityRecommendBasePredict(Long userId, Integer goodsPriceId, Integer
            matchId, String sprRecommend, String reason, String visitorIp, Integer clientType, DetailMatchInfo
                                                                                   detailMatchInfo) {
        Long price = sportSocialService.getRecommendPriceById(goodsPriceId);
        List<UserSportSocialRecommend> recommendList = userSportSocialRecommendDao
                .getSportSocialRecommendByUserIdMatchId(userId, matchId + "");
        if (recommendList != null && recommendList.size() > 0) {
            for (UserSportSocialRecommend temp : recommendList) {
                if (temp.getPlayType().equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
                    if (price.equals(temp.getPrice())) {
                        return temp;
                    }
                }
            }
        }
        String recommendId = sportSocialService.generateRecommendId(userId);

        //明星预测师都要推一个胜平负
        Integer playType = SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF;

        UserSportSocialRecommend userSportSocialRecommend = new UserSportSocialRecommend(recommendId, userId, String
                .valueOf(matchId), 200, playType, detailMatchInfo.getRecommendInfo(playType, sprRecommend),
                goodsPriceId, price, "", reason, null, 0, detailMatchInfo.getHandicap(playType), detailMatchInfo
                .getEndTime(),"");
        try {
            sportSocialService.saveUserRecommend(userSportSocialRecommend, visitorIp, clientType);
        } catch (Exception e) {
            return null;
        }

        return userSportSocialRecommend;
    }

    @Override
    public Map<String, Object> getAllUsableInternetCelebrities(Long userId) {
        Map<String, Object> result = Maps.newHashMap();
        //卡片集合
        Integer activityId = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant
                .INTERNET_CELEBRITIES_RECOMMEND_ACTIVITY_ID, 201809001);
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        if (null == activityInfo) {
            return result;
        }
        if (Objects.equals(CommonStatusEnum.NO.getStatus(), activityInfo.getIsEnable()) || DateUtil
                .getCurrentTimestamp().after(activityInfo.getEndTime()) || DateUtil.getCurrentTimestamp().before
                (activityInfo.getStartTime())) {
            return result;
        }
        //大咖推荐卡
        List<PrivilegedCardVo> privilegedCardVos = getCelebrityRecommendPrivilegedCardVos(activityInfo);
        //剩余次数
        int availableTimes = getCouponAvailableTimesByType(userId, UserCoupon
                .ACCESS_TYPE_CELEBRITY_RECOMMEND, UserCoupon.USE_STATUS_USABLE);

        List<InternetCelebrityRecommend> celebrities = internetCelebrityRecommendDao
                .getAllInternetCelebrityLastRecommend();
        if (celebrities.isEmpty()) {
            return result;
        }
        List<CelebrityRecommendVo> recommendVos = Lists.newArrayList();
        CelebrityRecommendVo recommendVo = null;
        for (InternetCelebrityRecommend icr : celebrities) {
            recommendVo = getSingleUserLastRecommendation(icr, userId, availableTimes);
            if (null == recommendVo) {
                continue;
            }
            recommendVos.add(recommendVo);
        }
        Collections.sort(recommendVos, (o1, o2) -> o2.getPopularIndex().compareTo(o1.getPopularIndex()));
        result.put("recommendations", recommendVos);
        result.put("privilegedCardVos", privilegedCardVos);
        result.put("availableTimes", availableTimes);
        result.put("activityImg", activityInfo.getImgUrl());
        return result;
    }

    @Override
    public String unlockCelebrityRecommendByCard(String recommendId, Long userId) {
        List<UserCoupon> userCoupons = userCouponDao.getCouponHasAvailableTimesByType(userId, UserCoupon
                .ACCESS_TYPE_CELEBRITY_RECOMMEND, UserCoupon.USE_STATUS_USABLE);
        if (userCoupons.isEmpty()) {
            return CommonConstant.SPACE_NULL_STR;
        }
        UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(CommonUtil
                .getUserIdSuffix(recommendId), recommendId, false);
        if (null == recommend) {
            return "推荐不存在";
        }
        if (recommend.getEndTime().before(DateUtil.getCurrentTimestamp())) {
            return "方案已过期";
        }
        Collections.sort(userCoupons, Comparator.comparing(UserCoupon::getAvailableTimes));
        UserCoupon coupon = userCoupons.get(0);
        Boolean flag = self.unlockRecommend(coupon, recommend);
        if (!flag) {
            return "特权解锁失败";
        }
        flag = userBuyRecommendService.updateUserBuyRecommendAfterPayed(userId, recommend.getRecommendId(), false,
                true);
        if (!flag) {
            return "特权解锁失败";
        }
        return CommonConstant.SPACE_NULL_STR;
    }

    @Transactional
    @Override
    public Boolean unlockRecommend(UserCoupon userCoupon, UserSportSocialRecommend recommend) {
        userCoupon = userCouponDao.getUserCouponByUserIdAndCouponId(userCoupon.getUserId(), userCoupon.getCouponId(),
                Boolean.TRUE);
        if (!Objects.equals(UserCoupon.USE_STATUS_USABLE, userCoupon.getUseStatus()) || userCoupon.getAvailableTimes()
                < 1) {
            return Boolean.FALSE;
        }
        UserBuyRecommend userBuyRecommend = userBuyRecommendDao.getUserBuyRecommendByUniqueKey
                (userCoupon.getUserId(), recommend.getRecommendId(), Boolean.TRUE);
        Boolean flag = Boolean.FALSE;
        if (userBuyRecommend != null) {
            if (!Objects.equals(CommonConstant.PROGRAM_IS_PAY_NO, userBuyRecommend.getPayStatus())) {
                return Boolean.TRUE;
            }
            int i = userBuyRecommendDao.updatePayStatus(userCoupon.getUserId(), userBuyRecommend.getFootballLogId(),
                    CommonConstant.PROGRAM_IS_PAY_YES, CommonConstant.PROGRAM_IS_PAY_NO, Boolean.FALSE);
            if (i < 1) {
                return Boolean.FALSE;
            }
        } else {
            userBuyRecommend = new UserBuyRecommend();
            String footballLogId = CommonUtil.generateStrId(userCoupon.getUserId(), "SPORTPROGRAM",
                    userBuyRecommendIdSequenceDao);
            Long withdrawAmount = SportsUtils.getWithdrawAmountByDivided(null);
            userBuyRecommend.initUserBuyRecommend(footballLogId, recommend.getRecommendId(), userCoupon.getUserId(),
                    recommend
                            .getPrice(), null, withdrawAmount, recommend.getLotteryCode(), recommend.getMatchId(),
                    recommend
                            .getPlayType());
            userBuyRecommend.setPayStatus(CommonConstant.PROGRAM_IS_PAY_YES);
            flag = Boolean.TRUE;
        }
        int row = 0;
        if (userCoupon.getAvailableTimes() == 1) {
            row = userCouponDao.updateCouponAvailableTimes(userCoupon.getUserId(), userCoupon.getCouponId(), 0,
                    userCoupon.getAvailableTimes(), userCoupon.getUseStatus(), UserCoupon.USE_STATUS_UNUSABLE);
        } else {
            row = userCouponDao.updateCouponAvailableTimes(userCoupon.getUserId(), userCoupon.getCouponId(),
                    userCoupon.getAvailableTimes() - 1, userCoupon.getAvailableTimes(), userCoupon.getUseStatus(),
                    null);
        }
        if (row < 1) {
            return Boolean.FALSE;
        }
        if (flag) {
            userBuyRecommendDao.insert(userBuyRecommend);
        }
        return Boolean.TRUE;
    }

    @Override
    public Map<String, Object> buyCelebrityRecommendCard(Long userId, Long couponConfigId, Integer activityId, Integer
            payChannelId, String clientIp, Integer clientId, Integer bankId) {
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        if (null == activityInfo) {
            return null;
        }
        if (Objects.equals(CommonStatusEnum.NO.getStatus(), activityInfo.getIsEnable()) || DateUtil
                .getCurrentTimestamp().after(activityInfo.getEndTime()) || DateUtil.getCurrentTimestamp().before
                (activityInfo.getStartTime())) {
            return null;
        }
        CouponConfig couponConfig = couponConfigDao.getCouponConfigById(Long.valueOf(couponConfigId));
        if (null == couponConfig || !Objects.equals(CouponConfig.COUPON_CONFIG_STATUS_USABLE, couponConfig.getStatus
                ())) {
            return null;
        }
        if (!Objects.equals(CouponConfig.COUPON_TYPE_CELEBRITY_RECOMMEND_CARD, couponConfig.getCouponType())) {
            return null;
        }
        JSONObject remark = JSONObject.parseObject(couponConfig.getRemark());
        String userCouponId = CommonUtil.generateStrId(userId, "COUPON", userCouponIdSequenceDao);
        UserCouponFlow userCouponFlow = new UserCouponFlow(CommonUtil.generateStrId(userId, "COUPONFLOW",
                userCouponFlowIdSeqDao), userId, CommonConstant.SPACE_NULL_STR, userCouponId);
        userCouponFlowDao.insert(userCouponFlow);
        UserCoupon userCoupon = new UserCoupon(userCouponId, userId, couponConfig.getCouponType(), couponConfig
                .getCouponId(), couponConfig.getCouponName(), couponConfig.getCouponDesc(), DateUtil
                .getCurrentTimestamp(),
                DateUtil.getIntervalDays(DateUtil.getCurrentTimestamp(), couponConfig.getValidDay()),
                couponConfig.getAccessType(), UserCoupon.USE_STATUS_INIT, DateUtil.getCurrentTimestamp(), 0);
        Map<String, Object> payInfo = null;
        long moneyFen = CommonUtil.convertYuan2Fen(remark.getString("currentPrice")).longValue();
        Integer payStatus = null;
        if (payChannelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            payInfo = payService.payCreateFlow(userId, null, moneyFen, CommonConstant.ACCOUNT_TYPE_WISDOM_COIN,
                    payChannelId, moneyFen, "智慧币购买" + couponConfig.getCouponName(),
                    null, null, null, CommonConstant.PAY_OPERATE_TYPE_DEC, null);
            payStatus = (Integer) payInfo.get("payStatus");
            if (Objects.equals(ResultConstant.PAY_SUCCESS_CODE, payStatus)) {
                userCoupon.setAvailableTimes(couponConfig.getDistributeCount());
                userCoupon.setUseStatus(UserCoupon.USE_STATUS_USABLE);
            }
        } else {
            if (moneyFen <= 0) {
                log.error("buyCelebrityRecommendCard is ex, CouponConfig price set is error,  configId : " +
                        couponConfig.getCouponId());
                return null;
            }
            String payDesc = "现金购买" + couponConfig.getCouponName();
            payInfo = payService.payCreateFlow(userId, userCouponFlow.getCouponFlowId(),
                    moneyFen, CommonConstant.ACCOUNT_TYPE_CASH, payChannelId, moneyFen, payDesc, clientIp, clientId,
                    CouponConstant.CELEBRITY_RECOMMEND_PAY_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_DEC,
                    bankId);
            payStatus = (Integer) payInfo.get("payStatus");
            payInfo = (Map<String, Object>) payInfo.get("payForToken");
        }
//        if (Objects.equals(ResultConstant.PAY_FAILED_CODE, payStatus)) {
//            payInfo.put("code", ResultConstant.PAY_FAILED_CODE);
//            payInfo.put("msg", "支付失败");
//        }
        if (Objects.equals(ResultConstant.REPEAT_CODE, payStatus)) {
            payInfo.put("code", ResultConstant.SUCCESS);
            payInfo.put("msg", "重复支付");
        } else if (Objects.equals(ResultConstant.SUCCESS, payStatus) || Objects.equals(ResultConstant
                .PAY_SUCCESS_CODE, payStatus)) {
            payInfo.put("code", ResultConstant.SUCCESS);
            payInfo.put("msg", "支付成功");
            userCouponDao.insert(userCoupon);
        } else {
            payInfo.put("code", ResultConstant.PAY_FAILED_CODE);
            payInfo.put("msg", "支付失败");
        }
        return payInfo;
    }

    private List<PrivilegedCardVo> getCelebrityRecommendPrivilegedCardVos(ActivityInfo activityInfo) {
        JSONObject json = JSONObject.parseObject(activityInfo.getRemark());
        String couponConfigId = json.getString("couponConfigId");
        String[] configs = couponConfigId.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMA_SPLIT_STR);

        List<PrivilegedCardVo> privilegedCardVos = Lists.newArrayList();
        PrivilegedCardVo privilegedCardVo = null;
        CouponConfig couponConfig = null;
        for (String id : configs) {
            couponConfig = couponConfigDao.getCouponConfigById(Long.parseLong(id));
            if (null == couponConfig) {
                continue;
            }
            Map<String, Object> payMemo = Maps.newHashMap();
            payMemo.put("goodsType", "celebrityRecommendCard");
            payMemo.put("goodsId", couponConfig.getCouponId());
            payMemo.put("activityId", activityInfo.getActivityId());
            payMemo.put("versionCode", CommonConstant.VERSION_CODE_4_3);
            JSONObject configJson = JSONObject.parseObject(couponConfig.getRemark());
            privilegedCardVo = new PrivilegedCardVo(couponConfig.getCouponId(), configJson.getString("cardName"),
                    CommonConstant
                            .COMMON_YUAN_STR + configJson.getString("originalPrice"), CommonConstant.COMMON_YUAN_STR +
                    configJson.getString
                            ("currentPrice"), configJson.getString("tips"), configJson.getString("button"), JSONObject
                    .toJSONString(payMemo), configJson.getLong("currentPrice"));
            if (StringUtils.isBlank(privilegedCardVo.getButtonMsg())) {
                privilegedCardVo.setButtonMsg("立即拥有");
            }
            privilegedCardVos.add(privilegedCardVo);
        }
        return privilegedCardVos;
    }

    private CelebrityRecommendVo getSingleUserLastRecommendation(InternetCelebrityRecommend icr, Long userId, int
            privilegedCardTimes) {
        CelebrityRecommendVo celebrityRecommendVo = null;
        Map<String, Object> matchInfo = Maps.newHashMap();
        Map<String, Object> userInfoMap = Maps.newHashMap();
        Map<String, Object> recommendInfo = Maps.newHashMap();
        UserInfo userInfo = userInfoDao.getUserInfo(icr.getUserId());
        try {
            userInfoMap.put("userName", userInfo.getNickName());
            userInfoMap.put("userImg", userInfo.getHeadImgUrl());
            userInfoMap.put("userIntroduce", userInfo.getFootballIntroduce());
            userInfoMap.put("backImg", ActivityIniCache.getActivityIniValue(ActivityIniConstant
                    .INTERNET_CELEBRITY_USER_BACK_IMG_PREFIX, "https://cdn.caiqr.com/") + userInfo.getUserId() + "" +
                    ".png");
            JSONObject icrRemark = JSONObject.parseObject(icr.getRemark());
            userInfoMap.put("tips", icrRemark.getString("tips"));

            Map<String, DetailMatchInfo> detailMatchInfoMap = thirdHttpService.getMatchMapByMatchIds(String.valueOf(icr
                    .getMatchId()));
            DetailMatchInfo detailMatchInfo = null;
            if (detailMatchInfoMap != null) {
                detailMatchInfo = detailMatchInfoMap.get(String.valueOf(icr.getMatchId()));
                matchInfo.put("matchName", detailMatchInfo.getMatchName());
                matchInfo.put("single", StringUtils.isNotBlank(detailMatchInfo.getTag()) && detailMatchInfo.getTag()
                        .equals("竞彩单关") ? 1 : 0);
                matchInfo.put("hostName", detailMatchInfo.getHostName());
                matchInfo.put("awayName", detailMatchInfo.getAwayName());
                matchInfo.put("hostImg", detailMatchInfo.getHostImg());
                matchInfo.put("awayImg", detailMatchInfo.getAwayImg());
                matchInfo.put("matchTime", detailMatchInfo.getMatchTime());
                matchInfo.put("groupDesc", "");
                matchInfo.put("countDownSecond", DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), detailMatchInfo
                        .getEndTime()));
            }
            Boolean purchaseStatus = Boolean.FALSE;
            if (null != userId) {
                purchaseStatus = userBuyRecommendService.checkUserPurchaseFootballProgramStatus(userId,
                        icr.getRecommendId());
                purchaseStatus = checkUserIsPlateUser(userId, purchaseStatus);
            }
            recommendInfo = getCelebrityRecommendInfo(userId, icr, detailMatchInfo, purchaseStatus,
                    privilegedCardTimes);
            Map<String, Object> payMemo = Maps.newHashMap();
            payMemo.put("goodsType", "footballRecommend");
            payMemo.put("goodsId", icr.getRecommendId());
            payMemo.put("versionCode", CommonConstant.VERSION_CODE_4_3);
            String btnMsg = getCelebrityRecommendBtnMsg(privilegedCardTimes, purchaseStatus, userId, icr.getPrice());
            int recommendPayType = (null != userId && privilegedCardTimes > 0) ? InternetCelebrityRecommend
                    .CELEBRITY_RECOMMEND_PAY_TYPE_PRIVILEGED_CARD : InternetCelebrityRecommend
                    .CELEBRITY_RECOMMEND_PAY_TYPE_COIN;//智慧币种
            payMemo.put("recommendPayType", recommendPayType);

            Integer discount = CommonUtil.getValueFromJSONMap(icr.getRemark(), "discount") == null ? 0 :
                    Integer.valueOf(CommonUtil.getValueFromJSONMap(icr.getRemark(), "discount").toString());
            Integer programType = CommonUtil.getValueFromJSONMap(icr.getRemark(), "programType") == null ?
                    0 : Integer.valueOf(CommonUtil.getValueFromJSONMap(icr.getRemark(), "programType").toString());

            celebrityRecommendVo = new CelebrityRecommendVo(icr.getRecommendId(), String.valueOf(icr.getUserId()) +
                    "S", JSONObject.toJSONString(payMemo), userInfoMap, recommendInfo, matchInfo, icr.getPopularIndex
                    (), btnMsg,
                    purchaseStatus ? 1 : 0, programType, discount);
        } catch (Exception ex) {
            log.error("getSingleUserLastRecommendation is error, please check it. InternetCelebrityRecommend : " +
                    icr + ", error:" + ex, ex);
            return null;
        }
        return celebrityRecommendVo;
    }

    private String getCelebrityRecommendBtnMsg(int privilegedCardTimes, Boolean purchaseStatus, Long userId, Long
            price) {
        String btnMsg = "立即查看";
        if (!purchaseStatus) {
            if (null != userId && privilegedCardTimes > 0) {
                btnMsg = "使用特权查看";
            } else {
                String priceStr = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(price).toString());
                btnMsg = priceStr + "智慧币 立即查看";
            }
        }
        return btnMsg;
    }

    private Map<String, Object> getCelebrityRecommendInfo(Long userId, InternetCelebrityRecommend celebrityRecommend,
                                                          DetailMatchInfo detailMatchInfo, Boolean purchaseStatus,
                                                          int privilegedCardTimes) {
        Map<String, Object> result = Maps.newHashMap();
        String price = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(celebrityRecommend.getPrice())
                .toString());
        String btnMsg = getCelebrityRecommendBtnMsg(privilegedCardTimes, purchaseStatus, userId, celebrityRecommend
                .getPrice());
        String recommendPlayType = CommonConstant.SPACE_NULL_STR;
        Map<String, Object> remark = JSONObject.parseObject(celebrityRecommend.getRemark());
        if (remark != null) {
            String rewardDesc = remark.get("rewardDesc").toString();
            String rewardMin = rewardDesc.split(":")[0];
            String rewardMax = rewardDesc.split(":")[1];

            recommendPlayType = getAllPlayType(remark.get("recommendMap").toString());

            Map<String, Object> celebrityRecommends = new HashMap<>();
            List<String> recommends = new ArrayList<>();
            if (purchaseStatus) {
                btnMsg = CommonConstant.SPACE_NULL_STR;
                recommends = SportsUtils.getIntegerCelebritySportsRecommendsByRemark(celebrityRecommend.getRemark(),
                        detailMatchInfo);
            }
            celebrityRecommends.put("memo", "提示：预测可能会变化，请比赛前20分钟再查看");
            celebrityRecommends.put("recommends", recommends);

            UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById
                    (celebrityRecommend.getUserId(), celebrityRecommend.getRecommendId(), false);

            String originPrice = CommonConstant.SPACE_NULL_STR;
            String currentPrice = CommonConstant.SPACE_NULL_STR;

            if (StringUtils.isNotBlank((String) remark.get("originPrice"))) {
                originPrice = CommonConstant.COMMON_YUAN_STR + remark.get("originPrice") + CommonConstant
                        .WISDOM_COIN_PAY_NAME;
                currentPrice = CommonConstant.COMMON_YUAN_STR + price + CommonConstant.WISDOM_COIN_PAY_NAME;
                btnMsg = "立即查看";
            }

            result.put("buyStatus", purchaseStatus ? 1 : 0);
            result.put("btnMsg", btnMsg);
            result.put("originPrice", originPrice);
            result.put("currentPrice", currentPrice);
            result.put("rewardMin", rewardMin);
            result.put("rewardMax", rewardMax);
            result.put("recommendPlayType", recommendPlayType);
            result.put("recommend", celebrityRecommends);
            result.put("reason", recommend.getReason());
            result.put("recommendId", recommend.getRecommendId());
            // 添加推荐title 4.6.2
            userSportSocialRecommendService.userRecommendTitleLock(recommend);
            result.put("recommendTitle", recommend.getRecommendTitle());

            // 添加标签 4.6.4 单选 分析
            result.putAll(recommend.remark2marks());
            result.put("price", price);
        }
        return result;
    }

    private int getCouponAvailableTimesByType(Long userId, Integer accessType, Integer useStatus) {
        int availableTimes = 0;
        if (null != userId) {
            List<UserCoupon> userCoupons = userCouponDao.getCouponHasAvailableTimesByType(userId, accessType,
                    useStatus);
            if (null != userCoupons && !userCoupons.isEmpty()) {
                for (UserCoupon uc : userCoupons) {
                    availableTimes = availableTimes + uc.getAvailableTimes();
                }
            }
        }
        return availableTimes;
    }

    @Transactional
    @Override
    public Boolean celebrityRecommendPayCallBack(String payId, String flowId) {
        if (StringUtils.isBlank(payId) || StringUtils.isBlank(flowId)) {
            log.error("celebrityRecommendPayCallBack payId-flowId:" + payId + "--" + flowId);
            return false;
        }
        Long preFix = Long.parseLong(flowId.substring(flowId.length() - 2));
        UserAccountFlow userAccountFlow = userAccountFlowDao.getUserFlowByShardType(flowId, preFix, Boolean
                .TRUE);
        //1.检验交易流水状态。是否已经支付或者未付款
        if (userAccountFlow == null || userAccountFlow.getStatus() != CommonConstant.PAY_STATUS_FINISH) {
            Integer status = userAccountFlow == null ? -1 : userAccountFlow.getStatus();
            log.error("流水id:" + flowId + " 异常." + "流水状态为:" + status);
            return false;
        }
        userAccountFlow.setStatus(CommonConstant.PAY_STATUS_HANDLED);
        userAccountFlowDao.update(userAccountFlow);

        UserCouponFlow userCouponFlow = userCouponFlowDao.getUserCouponFlowById(userAccountFlow.getUserId(),
                userAccountFlow
                        .getPayId());
        if (null == userCouponFlow) {
            log.error("celebrityRecommendPayCallBack is error, not found UserCoupon, flowId:" + flowId + ", payId:"
                    + payId);
            throw new BusinessException("大咖推荐特权卡支付回调失败,未发现推荐卡流水 " + CommonUtil.mergeUnionKey(userAccountFlow
                    .getUserId(), userAccountFlow
                    .getPayId()));
        }
        UserCoupon coupon = userCouponDao.getUserCouponByUserIdAndCouponId(userAccountFlow.getUserId(),
                userCouponFlow.getCouponId(), Boolean.TRUE);
        if (null == coupon) {
            log.error("celebrityRecommendPayCallBack is error, not found UserCoupon, CouponId : " + userCouponFlow
                    .getCouponId() + " ,userId:" + userAccountFlow.getUserId());
            throw new BusinessException("大咖推荐特权卡支付回调失败,未发现用户推荐卡 " + CommonUtil.mergeUnionKey(userAccountFlow
                    .getUserId(), userCouponFlow.getCouponId()));
        }
        if (!Objects.equals(UserCoupon.USE_STATUS_INIT, coupon.getUseStatus())) {
            log.error("celebrityRecommendPayCallBack is error,this coupon status is ex, coupon :" + coupon);
            throw new BusinessException("大咖推荐特权卡支付回调失败,用户推荐卡状态有误 " + CommonUtil.mergeUnionKey(userAccountFlow.getUserId
                    (), userCouponFlow.getCouponId()));
        }
        CouponConfig config = couponConfigDao.getCouponConfigById(coupon.getCouponConfigId());
        int row = userCouponDao.updateCouponAvailableTimes(coupon.getUserId(), coupon.getCouponId(), config
                .getDistributeCount(), coupon.getAvailableTimes(), UserCoupon.USE_STATUS_INIT, UserCoupon
                .USE_STATUS_USABLE);
        if (row < 1) {
            log.error("updateCouponAvailableTimes is error,this coupon status is ex, coupon :" + coupon);
            throw new BusinessException("大咖推荐特权卡支付回调失败,用户推荐卡更新失败 " + CommonUtil.mergeUnionKey(coupon.getUserId(),
                    coupon.getCouponId()));
        }
        return Boolean.TRUE;
    }

    @Override
    public void setSelf(Object proxyBean) {
        this.self = (InternetCelebrityRecommendService) proxyBean;
    }
}
