package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.SocialRank;
import com.mojieai.predict.entity.po.SocialUserFans;
import com.mojieai.predict.entity.po.SocialUserFollow;
import com.mojieai.predict.entity.vo.AchievementVo;
import com.mojieai.predict.entity.vo.FollowInfoVo;

import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/10/11.
 */
public interface SocialService {
    void doDistribute();

    Map<String, Object> getSocialRankList(Long gameId, Long userId, String socialType, String rankType, Integer
            pageIndex);

    void updateSocialKillDao(Long userId, Long gameId, String periodId, SocialRank enPeriodRank,
                             Integer periodScore, Integer userWeekScore, Integer userMonthScore);

    void updateSocialEncircleRankDao(Long userId, Long gameId, String periodId, SocialRank enPeriodRank,
                                     Integer periodScore, Integer userWeekScore, Integer userMonthScore);

    Map<String, Integer> getAwardLevelMap(Long gameId, Integer ballType, String socialType);

    void setFivePredictNums(Long gameId, String periodId);

    void distributeKillPredictNums(Long gameId, String periodId);

    void distributeEnCirclePredictNums(Long gameId, String periodId);

    void updateUserAchievement(long gameId);

    void updateUserAchievement(long gameId, String periodId);

    Map<String, List<AchievementVo>> rebuildUserSocial2Redis(long gameId, Long userId, String periodId);

    List<Map<String, String>> awardPopup(Game game, Long userId);

    Map<String, Object> getSocialPersonTitle(Long userId, Long lookUpUserId);

    /* 关注和取消关注*/
    Integer follow(Long userId, Long followUserId, Integer followType);

    /* 关注列表和粉丝列表*/
    Map<String, Object> getFollowList(Long userId, Long followListUserId, String followType, Integer pageIndex);

    /* 个人关注详情*/
    FollowInfoVo getFollowInfo(Long userId);

    void updateSocialFollow(SocialUserFollow socialUserFollow, SocialUserFans socialUserFans, Integer type, Integer
            followType);
}
