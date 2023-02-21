package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.SportSocialRankVo;
import com.mojieai.predict.enums.FootballCalculateResultEnum;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserFootballProgramServiceImpl implements UserFootballProgramService {

    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private SocialUserFollowInfoDao socialUserFollowInfoDao;
    @Autowired
    private SocialUserFansDao socialUserFansDao;
    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;
    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private InternetCelebrityRecommendDao internetCelebrityRecommendDao;

    @Override
    public Map<String, Object> getSportSocialPersonCenter(Long userId, Long visitorUserId, Integer lotteryCode,
                                                          String lastIndex) {
        Map<String, Object> res = new HashMap<>();
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        //1.粉丝数
        SocialUserFollowInfo socialUserFollowInfo = socialUserFollowInfoDao.getUserFollowInfo(userId, CommonConstant
                .SOCIAL_FOLLOW_FANS_TYPE_SPORT);
        Integer fansCount = 0;
        if (socialUserFollowInfo != null) {
            fansCount = socialUserFollowInfo.getFansCount();
        }

        //2.关注状态
        Integer followStatus = SocialEncircleKillConstant.SOCIAL_FOLLOW_STATUS_NO;
        SocialUserFans socialUserFans = socialUserFansDao.getUserFans(userId, visitorUserId, CommonConstant
                .SOCIAL_FOLLOW_FANS_TYPE_SPORT);
        if (socialUserFans != null && socialUserFans.getIsFans() == SocialEncircleKillConstant
                .SOCIAL_FOLLOW_STATUS_YES) {
            followStatus = SocialEncircleKillConstant.SOCIAL_FOLLOW_STATUS_YES;
        }

        //3.获取用户推荐信息
        boolean isMe = false;
        if (visitorUserId != null) {
            if (userId.equals(visitorUserId)) {
                isMe = true;
            }
        }
        //4.获取排行榜信息
        List<Map<String, Object>> rankInfo = getUserSportsRankInfo(userId);

        //5.获取用户推荐信息
        Map<String, Object> userRecommendsMap = userSportSocialRecommendService.getUserSportSocialRecommends(userId,
                lastIndex);
        List<Map<String, Object>> recommends = new ArrayList<>();
        if (userRecommendsMap != null && userRecommendsMap.containsKey("recommend")) {
            List<UserSportSocialRecommend> userSportRecommends = (List<UserSportSocialRecommend>) userRecommendsMap
                    .get("recommend");

            int count = 0;
            StringBuilder stringBuilder = new StringBuilder();
            for (UserSportSocialRecommend userSportSocialRecommend : userSportRecommends) {
                stringBuilder.append(userSportSocialRecommend.getMatchId());
                if (count < userSportRecommends.size() - 1) {
                    stringBuilder.append(CommonConstant.COMMA_SPLIT_STR);
                }
                count++;
            }
            //6.获取比赛信息
            Map<String, DetailMatchInfo> matchInfoMap = thirdHttpService.getMatchMapByMatchIds(stringBuilder.toString
                    ());

            for (UserSportSocialRecommend userSportSocialRecommend : userSportRecommends) {
                DetailMatchInfo matchInfo = matchInfoMap.get(userSportSocialRecommend.getMatchId());
                if (matchInfo == null) {
                    continue;
                }
                Map<String, Object> temp = packageUserRecommendWithoutOdds(userSportSocialRecommend, matchInfo, isMe,
                        visitorUserId);
                if (temp != null && !temp.isEmpty()) {
                    recommends.add(temp);
                }
            }
        }

        //推荐时跳转
        Map<String, Object> masterExplain = new HashMap<>();
        masterExplain.put("title", "<font color='#FF9317'>如何成为智慧预测师?</font>");
        masterExplain.put("jumpUrl", "https://m.caiqr.com/daily/zucaiyuceshi/index.htm");

        //
        String introduction = userInfo.getFootballIntroduce() == null ? "暂无个人简介" : userInfo.getFootballIntroduce();
        Integer isDefaultIntroduce = StringUtils.isBlank(userInfo.getFootballIntroduce()) ? 1 : 0;
        Integer editIntroducePermission = 0;
        if (isMe && userInfo.getIsReMaster() != null && userInfo.getIsReMaster().equals(1)) {
            editIntroducePermission = 1;
            introduction = StringUtils.isBlank(userInfo.getFootballIntroduce()) ? "您还没有编辑个人简介哦~（智慧预测师专享）" :
                    introduction;
            masterExplain.put("title", "<font color='#FF965B'>预测酬金将在比赛结束后到账</font>");
            masterExplain.put("jumpUrl", "");
        }

        res.put("isMe", isMe);
        res.put("isSportsVip", vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS));
        res.put("rankInfo", rankInfo);
        res.put("recommend", recommends);
        res.put("userName", userInfo.getNickName());
        res.put("userImg", userInfo.getHeadImgUrl());
        res.put("followStatus", followStatus);
        res.put("fans", "粉丝数" + fansCount);
        res.put("introduction", SportsUtils.introductionAdvertisementDeal(introduction, isMe));
        res.put("hasNext", userRecommendsMap.get("hasNext"));
        res.put("lastIndex", userRecommendsMap.get("lastIndex"));
        res.put("editIntroducePermission", editIntroducePermission);//0:不可编辑 1可编辑
        res.put("notMasterAd", "仅智慧师可以编辑简介");
        res.put("masterExplain", masterExplain);
        res.put("isDefaultIntroduce", isDefaultIntroduce);
        return res;
    }

    private List<Map<String, Object>> getUserSportsRankInfo(Long userId) {
        List<Map<String, Object>> res = new ArrayList<>();
        SportSocialRankVo rankVo = sportSocialService.getSportSocialRankVo(userId);
        Integer continueNum = 0;
        Integer hitNum = 0;
        Integer profitNum = 0;
        if (rankVo != null) {
            Map<Integer, Integer> continueRank = rankVo.getUserMaxNumsRank();
            continueNum = continueRank.get(SportsProgramConstant.SPORT_SOCIAL_RANK_PLAY_TYPE_MULTIPLE);
            if (continueNum == null) {
                continueNum = 0;
            }
            Map<Integer, Integer> hitRank = rankVo.getUserRightNumsRank();
            hitNum = hitRank.get(SportsProgramConstant.SPORT_SOCIAL_RANK_PLAY_TYPE_MULTIPLE);
            if (hitNum == null) {
                hitNum = 0;
            }

            Map<Integer, Integer> profitRank = rankVo.getUserAwardAmountRank();
            profitNum = profitRank.get(SportsProgramConstant.SPORT_SOCIAL_RANK_PLAY_TYPE_MULTIPLE);
            if (profitNum == null) {
                profitNum = 0;
            }
        }

        //1.连中
        Map<String, Object> continueRankInfo = new HashMap<>();
        continueRankInfo.put("rankAchieve", "<font color='#ff5050'>" + continueNum + "连中</font>");
        continueRankInfo.put("rankDes", "近期连中");
        continueRankInfo.put("unit", "");
        continueRankInfo.put("symbol", "");
        res.add(continueRankInfo);

        //2.命中
        Map<String, Object> hitRankInfo = new HashMap<>();
        hitRankInfo.put("rankAchieve", hitNum);
        hitRankInfo.put("rankDes", "近7天命中");
        hitRankInfo.put("symbol", "");
        hitRankInfo.put("unit", "<font color='#ff5050'>％</font>");
        res.add(hitRankInfo);

        //3.连中
        String symbol = CommonConstant.COMMON_ADD_STR;
        String color = "#ff5050";
        if (profitNum < 0) {
            symbol = CommonConstant.COMMON_DASH_STR;
            color = "#43BF44";
        }
        String profitRatio = Math.abs(profitNum) + "";
        Map<String, Object> profitRankInfo = new HashMap<>();
        profitRankInfo.put("rankAchieve", "<font color='" + color + "'>" + profitRatio + "</font>");
        profitRankInfo.put("rankDes", "近7天收益");
        profitRankInfo.put("symbol", "<font color='" + color + "'>" + symbol + "</font>");
        profitRankInfo.put("unit", "<font color='" + color + "'>％</font>");
        res.add(profitRankInfo);

        return res;
    }

    private Map<String, Object> packageUserRecommendWithoutOdds(UserSportSocialRecommend userSportSocialRecommend,
                                                                DetailMatchInfo detailMatchInfo, boolean isMe, Long
                                                                        visitorUserId) {
        Map<String, Object> res = new HashMap<>();

        String btnMsg = "免费";
        String recommendInfo = "免费"; //自己有recommendInfo 说明
        Integer recommendType = SportsProgramConstant.SPORT_RECOMMEND_TYPE_FREE;
        Long price = userSportSocialRecommend.getPrice();
        if (price != null && price > 0) {
            recommendType = SportsProgramConstant.SPORT_RECOMMEND_TYPE_PAY;
            String amountStr = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(price).toString());
            btnMsg = "<font color='#ff5050'>" + amountStr + "</font>" + CommonConstant.WISDOM_COIN_PAY_NAME;

            if (isMe) {
                if (userSportSocialRecommend.getPrice() > 0L) {
                    Integer saleCount = userSportSocialRecommend.getSaleCount() == null ? 0 : userSportSocialRecommend
                            .getSaleCount();
                    Integer couponSaleCount = userSportSocialRecommend.getCouponSaleCount() == null ? 0 :
                            userSportSocialRecommend.getCouponSaleCount();
                    String purchaseInfo = (saleCount - couponSaleCount) + "人购买";
                    recommendInfo = amountStr + CommonConstant.WISDOM_COIN_PAY_NAME + CommonConstant.COMMA_SPLIT_STR
                            + purchaseInfo;
                }
            }
        }
        //"赛尔塔 2:1 马拉加"
        String battle = "  VS  ";
        if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            battle = "  " + detailMatchInfo.getHostScore() + CommonConstant.COMMON_COLON_STR + detailMatchInfo
                    .getAwayScore() + "  ";
        }
        String teamInfo = detailMatchInfo.getHostName() + battle + detailMatchInfo.getAwayName();
        //西甲 周一001 01-13 12:00
        String matchDesc = detailMatchInfo.getMatchName() + " " + detailMatchInfo.getMatchDate() + " " + detailMatchInfo
                .getMatchTime();

        Integer programStatus = SportsProgramConstant.RECOMMEND_STATUS_INIT;
        if (userSportSocialRecommend.getIsRight() != null) {
            programStatus = userSportSocialRecommend.getIsRight();
        }
        Integer purchaseStatus = CommonConstant.PROGRAM_BUY_STATUS_NO_PURCHASE;
        if (userBuyRecommendService.checkUserPurchaseFootballProgramStatus(visitorUserId, userSportSocialRecommend
                .getRecommendId())) {
            purchaseStatus = CommonConstant.PROGRAM_BUY_STATUS_PAYED;
        }

        //依据比赛状态
        boolean teamInfoWeakColor = false;
        Integer matchStatus = detailMatchInfo.getMatchStatus();
//        if (programStatus.equals(SportsProgramConstant.RECOMMEND_STATUS_CANCEL)) {
//            matchStatus = SportsProgramConstant.SPORT_MATCH_STATUS_QUIT;
//        }
        if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            btnMsg = "";
//            recommendInfo = "";
            if (programStatus.equals(SportsProgramConstant.RECOMMEND_STATUS_INIT)) {
                btnMsg = "<font color='#ff5050'>等待开奖</font>";
            } else if (programStatus.equals(SportsProgramConstant.RECOMMEND_STATUS_CANCEL)) {
                btnMsg = "<font color='#999999'>比赛延期</font>";
                matchStatus = SportsProgramConstant.SPORT_MATCH_STATUS_QUIT;
            }
            teamInfoWeakColor = true;
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
            btnMsg = "<font color='#ff5050'>比赛中</font>";
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_QUIT)) {
            btnMsg = "<font color='#999999'>比赛取消</font>";
            teamInfoWeakColor = true;
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_INIT)) {
            if (isMe) {
                btnMsg = "未开赛";
            }
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_DELAY)) {
            btnMsg = "<font color='#999999'>比赛延期</font>";
        }

        if (!isMe) {
            recommendInfo = "";
        }

        if (teamInfoWeakColor) {
            teamInfo = "<font color='#999999'>" + teamInfo + "</font>";
        }

        res.put("playName", SportsUtils.getPlayTypeCn(userSportSocialRecommend.getLotteryCode(),
                userSportSocialRecommend.getPlayType()));
        res.put("matchDesc", matchDesc);
        res.put("tags", SportsUtils.getMatchTags(userSportSocialRecommend.getPlayType(), detailMatchInfo.getTag()));
        res.put("teamInfo", teamInfo);
        res.put("recommendId", userSportSocialRecommend.getRecommendId());
        // 个人推荐添加title 4.6.2
        userSportSocialRecommendService.userRecommendTitleLock(userSportSocialRecommend);
        res.put("recommendTitle", userSportSocialRecommend.getRecommendTitle());

        // 添加标签 4.6.4 单选 分析
        res.putAll(userSportSocialRecommend.remark2marks());

        res.put("recommendType", recommendType);
        res.put("programStatus", programStatus);
        res.put("btnMsg", btnMsg);
        res.put("recommendInfo", recommendInfo);
        res.put("matchStatus", matchStatus);
        res.put("purchaseStatus", purchaseStatus);
        res.put("createTime", SportsUtils.getRecommendTimeShow(userSportSocialRecommend.getCreateTime()));
        return res;
    }

    @Override
    public Map<String, Object> getSportSocialRecommendDetail(String recommendId, Long userId, String versionCode) {
        Map<String, Object> res = new HashMap<>();
        Boolean isMe = false;
        Boolean free = false;
        //1.获取方案信息
        Long userPrefix = CommonUtil.getUserIdSuffix(recommendId);
        UserSportSocialRecommend socialRecommend = userSportSocialRecommendDao.getSportSocialRecommendById
                (userPrefix, recommendId, false);
        if (userId != null && userId.equals(socialRecommend.getUserId())) {
            isMe = true;
        }
        long endSecond = DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), socialRecommend.getEndTime());
        if (endSecond < 0 || socialRecommend.getRecommendInfo() == null || socialRecommend.getPrice() == 0 || isMe) {
            endSecond = 0;
        }
        //2.获取推荐人信息 todo 这块可以考虑抽出去放倒 socialUserFollowService中
        UserInfo userInfo = userInfoDao.getUserInfo(socialRecommend.getUserId());

        SocialUserFollowInfo socialUserFollowInfo = socialUserFollowInfoDao.getUserFollowInfo(userInfo.getUserId(),
                CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_SPORT);

        Integer fanCount = socialUserFollowInfo == null ? 0 : socialUserFollowInfo.getFansCount();

        //2.1关注情况
        Integer followStatus = SocialEncircleKillConstant.SOCIAL_FOLLOW_STATUS_NO;
        if (!isMe) {
            SocialUserFans socialUserFans = socialUserFansDao.getUserFans(socialRecommend.getUserId(), userId,
                    CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_SPORT);
            if (socialUserFans != null && socialUserFans.getIsFans().equals(SocialEncircleKillConstant
                    .SOCIAL_FOLLOW_STATUS_YES)) {
                followStatus = SocialEncircleKillConstant.SOCIAL_FOLLOW_STATUS_YES;
            }
        }

        if (socialRecommend.getPrice() == null || socialRecommend.getPrice() == 0L) {
            free = true;
        }

        //3.check 用户购买状态
        Integer purchaseStatus = CommonConstant.FOOTBALL_PROGRAM_STATUS_NO_PAY;
        if (userBuyRecommendService.checkUserPurchaseFootballProgramStatus(userId, recommendId) || isMe) {
            purchaseStatus = CommonConstant.FOOTBALL_PROGRAM_STATUS_PAYED;
        }

        Integer programStatus = SportsProgramConstant.RECOMMEND_STATUS_INIT;
        if (socialRecommend.getIsRight() != null) {
            programStatus = socialRecommend.getIsRight();
        }

        //4.获取方案详情中的赛事信息
        Integer buyPermission = 1;
        boolean permission = false;
        if (isMe || purchaseStatus.equals(CommonConstant.FOOTBALL_PROGRAM_STATUS_PAYED) || free || !programStatus
                .equals(SportsProgramConstant.RECOMMEND_STATUS_INIT)) {
            permission = true;
            buyPermission = 0;
            endSecond = 0;
        }
        Map<String, Object> matchInfo = null;
        List<DetailMatchInfo> matches = thirdHttpService.getMatchListByMatchIds(socialRecommend.getMatchId());
        if (matches != null && matches.size() > 0) {
            matchInfo = SportsUtils.getSportProgramDetailMatchInfo(matches.get(0), socialRecommend.getLotteryCode(),
                    socialRecommend.getPlayType(), socialRecommend, permission, Integer.valueOf(versionCode));
            if (!Integer.valueOf(matchInfo.get("matchStatus").toString()).equals(SportsProgramConstant
                    .SPORT_MATCH_STATUS_INIT)) {
                buyPermission = 0;
            }
//            packageMatchOdds(matchInfo, permission, socialRecommend);
        }

        //5.获取用户排行榜信息
        List<Map<String, Object>> rankInfo = getUserSportsRankInfo(socialRecommend.getUserId());
        res.put("rank", rankInfo);

        //6.依据用户购买设置状态
        String reason = "无";
        String according = "无";
        String priceDesc = "需支付:" + CommonUtil.convertFen2Yuan(socialRecommend.getPrice()) + CommonConstant
                .WISDOM_COIN_PAY_NAME;
        if (permission) {
            priceDesc = "";
            reason = StringUtils.isEmpty(socialRecommend.getReason()) ? "无" : socialRecommend.getReason();
            according = StringUtils.isEmpty(socialRecommend.getBaseOn()) ? "无" : socialRecommend.getBaseOn();
        }

        Map<String, Object> couponInfo = getUserCouponInfo(userId, buyPermission, recommendId);

        String introduction = userInfo.getFootballIntroduce() == null ? "暂无个人简介" : userInfo.getFootballIntroduce();
        if (isMe && userInfo.getIsReMaster() != null && userInfo.getIsReMaster().equals(1)) {
            introduction = StringUtils.isBlank(introduction) ? "您还没有编辑个人简介哦~（智慧预测师专享）" : introduction;
        }

        String sportsVipDesc = "成为会员享受免单资格，查看详情>>";
        if (vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS) || buyPermission == 0) {
            sportsVipDesc = "";
        }

        res.put("userName", userInfo.getNickName());
        res.put("isSportsVip", vipMemberService.checkUserIsVip(socialRecommend.getUserId(), VipMemberConstant
                .VIP_MEMBER_TYPE_SPORTS));
        res.put("fans", "粉丝数" + fanCount);
        res.put("userImg", userInfo.getHeadImgUrl());
        res.put("userId", userInfo.getUserId() + "");
        res.put("introduction", SportsUtils.introductionAdvertisementDeal(introduction, isMe));
        res.put("purchaseStatus", purchaseStatus);
        res.put("programStatus", programStatus);
        res.put("match", matchInfo);
        res.put("according", according);
        res.put("reason", reason);

        // 推荐标题 4.6.2
        userSportSocialRecommendService.userRecommendTitleLock(socialRecommend);
        res.put("recommendTitle", socialRecommend.getRecommendTitle());

        // 添加标签 4.6.4 单选 分析
        if (permission) {
            res.putAll(socialRecommend.remark2marks());
        }

        res.put("endSecond", endSecond);
        res.put("goodsId", recommendId);
        res.put("priceDesc", priceDesc);
        res.put("createTime", SportsUtils.getRecommendTimeShow(socialRecommend.getCreateTime()));
        res.put("buyPermission", buyPermission);
        res.put("recommendAd", "预测结果及推荐理由仅代表作者观点，与智慧彩票预测无关");
        res.put("jumpFlag", 0);
        res.put("jumpUrl", "");
        res.put("isMe", isMe);
        res.put("followStatus", followStatus);
        res.put("sportsVipDesc", sportsVipDesc);
        res.put("couponInfo", couponInfo);
        return res;
    }

    private Map<String, Object> getUserCouponInfo(Long userId, Integer buyPermission, String recommendId) {
        Map<String, Object> result = new HashMap<>();
        Integer couponStatus = 0;
        Integer count = internetCelebrityRecommendDao.getCelebrityRecommendCount(recommendId);
        if (buyPermission == 0 || (count != null && count > 0)) {
            result.put("couponStatus", couponStatus);
            result.put("couponCount", 0);
            result.put("couponId", "");
            result.put("couponBtnMsg", "使用免单");
            return result;
        }
        List<UserCoupon> userCoupons = userCouponService.getUserCouponCount(userId, CouponConstant
                .COUPON_ACCESS_TYPE_SPORTS);
        String couponId = "";
        Integer couponCount = 0;
        if (userCoupons != null && userCoupons.size() > 0) {
            couponId = userCoupons.get(0).getCouponId();
            couponCount = userCoupons.size();
            couponStatus = 1;
        }

        result.put("couponStatus", couponStatus);
        result.put("couponCount", couponCount);
        result.put("couponId", couponId);
        result.put("couponBtnMsg", "使用免单(" + couponCount + ")");
        return result;
    }
}
