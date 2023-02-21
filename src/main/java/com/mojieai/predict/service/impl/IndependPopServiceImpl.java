package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.dao.IndexUserSocialCodeDao;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.IndexUserSocialCode;
import com.mojieai.predict.entity.vo.SportSocialRankVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.socialPop.SocialPopEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.IndependPopService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.SportSocialService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndependPopServiceImpl implements IndependPopService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private IndexUserSocialCodeDao indexUserSocialCodeDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private SportSocialService sportSocialService;

    @Override
    public List<Map<String, Object>> getSocialPopup(Long userId) {
        List<Map<String, Object>> res = new ArrayList<>();
        for (Game game : GameCache.getAllGameMap().values()) {
            if (game.getGameType().equals(Game.GAME_TYPE_COMMON) && !game.getGameEn().equals(GameConstant.FC3D)) {
                for (SocialPopEnum spe : SocialPopEnum.values()) {
                    if (spe.getLotteryType().equals(CommonConstant.LOTTERY_TYPE_SPORTS)) {
                        continue;
                    }
                    List<Map<String, Object>> tempPop = spe.getPopContent(userId, game.getGameId(), redisService);
                    if (tempPop != null && tempPop.size() > 0) {
                        packagePopInfo(tempPop, userId, game.getGameId());
                        res.addAll(tempPop);
                    }
                }
            }
        }

        if (res.size() > 0) {
            res.sort(Comparator.comparing(p -> ((Integer) p.get("popupType"))));
        }

        return res;
    }

    @Override
    public List<Map<String, Object>> getSportsSocialPopup(Long userId) {
        List<Map<String, Object>> res = SocialPopEnum.FOOTBALL_MASTER_POP.getPopContent(userId, 0, redisService);
        if (res == null) {
            return res;
        }
        Map<String, Object> popupContent = (Map<String, Object>) res.get(0).get("popupContent");

        //获取用户🏳️头像
        UserLoginVo loginVo = loginService.getUserLoginVo(userId);
        //排行榜
        SportSocialRankVo rankVo = sportSocialService.getSportSocialRankVo(userId);
        Integer hitNum = 0;
        Integer profitNum = 0;
        if (rankVo != null) {
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

        String privilege = "1.发收费预测单，编写推荐理由<br>2.个性简介";
        popupContent.put("awardTitle", loginVo.getNickName());
        popupContent.put("userImg", loginVo.getHeadImgUrl());
        popupContent.put("awardDesc", "整体收益<font color='#FFD753'>+" + profitNum + "%</font>，命中率<font " +
                "color='#FFD753'>" + hitNum + "%</font>");
        popupContent.put("privilege", privilege);
        return res;
    }

    private void packagePopInfo(List<Map<String, Object>> tempPop, Long userId, long gameId) {
        for (Map<String, Object> temp : tempPop) {
            Integer popupType = Integer.valueOf(temp.get("popupType").toString());
            //1.包装中奖和奖金
            if (popupType.equals(CommonConstant.SOCIAL_POP_UP_TYPE_ACHIEVE) && temp.containsKey("popupContent")) {
                Map<String, Object> content = (Map<String, Object>) temp.get("popupContent");
                String socialType = content.get("socialType").toString();
                Integer awardType = Integer.valueOf(content.get("awardType").toString());
                String awardDesc = content.get("awardDesc").toString();
                if (StringUtils.isNotBlank(socialType) && socialType.equals(CommonConstant.SOCIAL_CODE_TYPE_ENCIRCLE)) {
                    GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
                    Date beginDate = null;
                    Date endDate = null;
                    if (awardType.equals(SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_MONTH_ENCIRCLE)) {
                        beginDate = DateUtil.getBeginDayOfMonth(lastOpenPeriod.getEndTime());
                        endDate = DateUtil.getEndDayOfMonth(lastOpenPeriod.getEndTime());
                    } else if (awardType.equals(SocialEncircleKillConstant.ACHIEVE_POP_AWARD_TYPE_WEEK_ENCIRCLE)) {
                        beginDate = DateUtil.getBeginDayOfWeek(lastOpenPeriod.getEndTime());
                        endDate = DateUtil.getEndDayOfWeek(lastOpenPeriod.getEndTime());
                    }

                    if (beginDate != null && endDate != null) {
                        Timestamp beginTime = DateUtil.formatString(DateUtil.formatDate(beginDate, "yyyy-MM-dd " +
                                "HH:mm:ss"), "yyyy-MM-dd HH:mm:ss");
                        Timestamp endTime = DateUtil.formatString(DateUtil.formatDate(endDate, "yyyy-MM-dd HH:mm:ss")
                                , "yyyy-MM-dd HH:mm:ss");

                        IndexUserSocialCode indexUserSocialCode = indexUserSocialCodeDao
                                .getUserMaxScoreEncircleByTime(gameId, userId, beginTime, endTime);
                        if (indexUserSocialCode != null) {
                            String achieveMes = "最佳围号为<font color='#FF5050'>" + indexUserSocialCode.getSocialCount()
                                    + "中" + indexUserSocialCode.getSocialRightCount() + "</font>，";
                            String rewardMsg = getRewardMoney(gameId, indexUserSocialCode.getSocialRightCount());
                            rewardMsg = "<br>投注最高可获得奖金<font>" + rewardMsg + "</font>元";
                            awardDesc = achieveMes + awardDesc + rewardMsg;
                            content.put("awardDesc", awardDesc);
                        }
                    }
                }
            }
            if (!popupType.equals(CommonConstant.SOCIAL_POP_UP_TYPE_REWARD) && temp.containsKey("popupContent")) {
                Map<String, Object> content = (Map<String, Object>) temp.get("popupContent");
                if (content != null && content.containsKey("awardTitle")) {
                    String awardTitle = content.get("awardTitle").toString();
                    UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
                    String nickName = getUserShowName(userLoginVo.getNickName());
                    awardTitle = nickName + awardTitle;
                    content.put("awardTitle", awardTitle);
                }
            }
        }
    }

    private String getUserShowName(String nickName) {
        if (nickName.length() > 6) {
            return nickName.substring(0, 6) + "... ";
        }
        return nickName + " ";
    }

    private String getRewardMoney(long gameId, Integer socialRightCount) {
        Integer blue = 1;
        Integer redTuo = 6;
        GamePeriod period = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        AbstractGame ag = GameFactory.getInstance().getGameBean(gameId);
        Game game = ag.getGame();
        if (game.getGameEn().equals(GameConstant.DLT)) {
            blue = 2;
            redTuo = 5;
        }

        List<AwardInfo> awardInfos = ag.getDefaultAwardInfoList();
        String bonus = null;

        int[] awardLevels = ag.analyseBidAwardLevels(0, redTuo, 0, blue, 0, socialRightCount,
                0, blue, period.getPeriodId());
        for (int i = 0; i < awardLevels.length; i++) {
            if (awardLevels[i] != 0) {
                AwardInfo awardInfo = awardInfos.get(awardLevels[i]);
                BigDecimal realBonus = awardInfo.getBonus();
                if (realBonus.intValue() == -1) {
                    bonus = "10万";
                    if (awardInfo.getAwardLevel().equals("2")) {
                        bonus = "100万";
                    } else if (awardInfo.getAwardLevel().equals("1")) {
                        bonus = "1000万";
                    }
                } else {
                    bonus = realBonus.toString();
                }
                break;
            }
        }
        return bonus;
    }
}
