package com.mojieai.predict.service.timedtask;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.dao.SportsRobotRecommendDao;
import com.mojieai.predict.enums.SportsRobotEnum;
import com.mojieai.predict.service.SocialService;
import com.mojieai.predict.service.SportSocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SocialRobotTimer {

    @Autowired
    private SportsRobotRecommendDao sportsRobotRecommendDao;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private SocialService socialService;

    public void updateSportsRobotRecommendTimes() {
        sportsRobotRecommendDao.batchUpdateSportRobotRecommendTimes(0);
    }

    /* 机器人关注榜单前几名*/
    public void robotFollowRankTopPerson() {
        Set<Long> hasFollowUserId = new HashSet<>();
        //1.获取排行榜前几名人
        Set<Map<Long, Integer>> userIds = getRankUserId();
        //2.获取
        Map<Integer, Integer> followCountMap = getFollowCountMap();

        SportsRobotEnum sre = SportsRobotEnum.getCurrentTimeRobot();
        if (sre != null) {
            List<Long> robotUserId = sportsRobotRecommendDao.getRobotUserIdByDate(sre.getBatchNum());
            //3.机器人关注用户
            for (Map<Long, Integer> userIdMap : userIds) {
                for (Map.Entry<Long, Integer> userIdEntry : userIdMap.entrySet()) {
                    if (hasFollowUserId.contains(userIdEntry.getKey())) {
                        continue;
                    }
                    robotFollowUserId(userIdEntry.getKey(), followCountMap.get(userIdEntry.getValue()), robotUserId);
                    hasFollowUserId.add(userIdEntry.getKey());
                }
            }
        }

    }

    private void robotFollowUserId(Long userId, Integer fanCount, List<Long> robotUserId) {
        if (userId == null || fanCount == null || fanCount == 0 || robotUserId.size() == 0) {
            return;
        }
        Collections.shuffle(robotUserId);
        for (int i = 0; i < fanCount; i++) {
            if (i >= robotUserId.size()) {
                break;
            }
            socialService.follow(robotUserId.get(i), userId, CommonConstant.SOCIAL_FOLLOW_FANS_TYPE_SPORT);
        }

    }

    private Map<Integer, Integer> getFollowCountMap() {
        Map<Integer, Integer> res = new HashMap<>();

        for (int i = 1; i <= 10; i++) {
            res.put(i, getFansCount(i));
        }
        return res;
    }

    private Integer getFansCount(int rank) {
        Integer res = new Random().nextInt(3);
        if (rank <= 3) {
            res += 29;
        } else if (rank <= 7) {
            res += 14;
        } else if (rank <= 10) {
            res += 4;
        }
        return res;
    }

    private Set<Map<Long, Integer>> getRankUserId() {
        Set<Map<Long, Integer>> userIds = new HashSet<>();
        //1.从配置中获取关注人数
        Integer count = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.ROBOT_FOLLOW_RANK_TOP_NUM, 10);
        userIds.addAll(getRankUserIdByRankType(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_PROFIT, count));
        userIds.addAll(getRankUserIdByRankType(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM, count));
        userIds.addAll(getRankUserIdByRankType(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_CONTINUE, count));
        return userIds;
    }

    private List<Map<Long, Integer>> getRankUserIdByRankType(Integer rankType, Integer count) {
        List<Map<Long, Integer>> userIds = new ArrayList<>();

        List<Object> ranks = sportSocialService.getSportSocialRankList(rankType, SportsProgramConstant
                .SPORT_SOCIAL_RANK_PLAY_TYPE_MULTIPLE, count);

        for (Object rank : ranks) {
            Map<String, Object> rankMap = (Map<String, Object>) rank;
            Long userId = (Long) rankMap.get("userId");
            Integer userRank = (Integer) rankMap.get("userRank");
            Map<Long, Integer> userMap = new HashMap<>();
            userMap.put(userId, userRank);
            userIds.add(userMap);
        }
        return userIds;
    }

}
