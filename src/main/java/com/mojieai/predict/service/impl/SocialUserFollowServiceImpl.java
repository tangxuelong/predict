package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.dao.SocialUserFollowDao;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.bo.UserEncircleInfo;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.SocialUserFollow;
import com.mojieai.predict.entity.vo.FollowInfoVo;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SocialEncircleCodeService;
import com.mojieai.predict.service.SocialUserFollowService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SocialUserFollowServiceImpl implements SocialUserFollowService {
    private static final Logger log = LogConstant.commonLog;
    @Autowired
    private SocialUserFollowDao socialUserFollowDao;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private RedisService redisService;

    @Override
    public Map<String, Object> getUserFollowByPage(long gameId, Long userId, Integer followType, Integer page) {
        Map<String, Object> result = new HashMap<>();
        boolean hasNext = false;
        String noFollowMsg = "";
        String followAdMsg = "";
        Integer pageSize = SocialEncircleKillConstant.SOCIAL_FOLLOW_PAGE_SIZE;
        //1.查询用户关注人的列表
        PaginationList<SocialUserFollow> socialUserFollows = socialUserFollowDao.getFollowUserListByPage(userId,
                followType, page, pageSize);
        List<SocialUserFollow> follows = socialUserFollows.getList();
        int total = socialUserFollows.getPaginationInfo().getTotalPage();
        int currentPage = socialUserFollows.getPaginationInfo().getCurrentPage();
        if (total > currentPage) {
            hasNext = true;
        }

        List<UserEncircleInfo> encircles = null;
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(gameId);
        if (follows == null || follows.size() <= 0) {
            noFollowMsg = "还没有关注任何人";
            followAdMsg = "点大神头像，关注即可";
        } else {
            //2.1.查询关注用户的当前期圈号
            encircles = socialEncircleCodeService.getFollowsCurrentEncircleList(gamePeriod, follows, userId);
            followAdMsg = "大神们还在看号，可以关注更多哦~";
        }

        result.put("periodId", gamePeriod.getPeriodId());
        result.put("encircles", encircles);
        result.put("noFollowMsg", noFollowMsg);
        result.put("followAdMsg", followAdMsg);
        result.put("page", currentPage);
        result.put("hasNext", hasNext);
        return result;
    }

    @Override
    public Map<String, Object> getUserFollowKillNumList(Long gameId, Long userId, Long lastUserId, Integer followType) {
        Map<String, Object> result = new HashMap<>();
        boolean hasNext = false;
        String noFollowMsg = "";
        String followAdMsg = "";

        Integer followUserCount = socialUserFollowDao.getUserFollowCount(userId, followType);
        List<UserEncircleInfo> encircles = null;
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(gameId);
        if (followUserCount == null || followUserCount <= 0) {
            noFollowMsg = "还没有关注任何人";
            followAdMsg = "点大神头像，关注即可";
        } else {
            Map res = getUserFollowKillNums(gameId, userId, lastUserId, gamePeriod, 11, followType);
            hasNext = (boolean) res.get("hasNext");
            lastUserId = (Long) res.get("lastUserId");
            encircles = (List<UserEncircleInfo>) res.get("encircleInfos");
            followAdMsg = "大神们还在看号，可以关注更多哦~";
        }

        result.put("periodId", gamePeriod.getPeriodId());
        result.put("encircles", encircles);
        result.put("noFollowMsg", noFollowMsg);
        result.put("followAdMsg", followAdMsg);
        result.put("lastUserId", lastUserId);
        result.put("lastUserIdStr", lastUserId + "");
        result.put("hasNext", hasNext);
        return result;
    }

    private Map<String, Object> getUserFollowKillNums(Long gameId, Long userId, Long lastUserId, GamePeriod
            gamePeriod, Integer pageSize, Integer followType) {
        boolean hasNext = false;
        Map<String, Object> result = new HashMap<>();
        List<UserEncircleInfo> encircleInfos = new ArrayList<>();
        //1.获取粉丝
        List<SocialUserFollow> follows = socialUserFollowDao.getFollowUserIdList(userId, followType, pageSize,
                lastUserId);
        for (SocialUserFollow socialUserFollow : follows) {
            String key = RedisConstant.getUserCurrentEncircleVo(gamePeriod.getGameId(), gamePeriod.getPeriodId(),
                    SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED, socialUserFollow.getFollowUserId());
            List<UserEncircleInfo> temp = redisService.kryoGet(key, ArrayList.class);
            if (temp != null && temp.size() > 0) {
                if (pageSize <= 0) {
                    break;
                }
                //2.打包圈号信息
                socialEncircleCodeService.packageEncircleInfo(gamePeriod.getGameId(), temp, userId);
                encircleInfos.addAll(temp);
                pageSize--;
            }
            lastUserId = socialUserFollow.getFollowUserId();
        }
        if (pageSize > 0 && follows.size() > 0) {
            Map tempRes = getUserFollowKillNums(gameId, userId, lastUserId, gamePeriod, pageSize, followType);
            if (tempRes != null && tempRes.get("encircleInfos") != null) {
                hasNext = (boolean) tempRes.get("hasNext");
                lastUserId = (Long) tempRes.get("lastUserId");
                encircleInfos.addAll((Collection<? extends UserEncircleInfo>) tempRes.get("encircleInfos"));
            }
        }
        if (pageSize == 0 && encircleInfos.size() > 0) {
            hasNext = true;
            if (encircleInfos.size() >= 2) {
                lastUserId = encircleInfos.get(encircleInfos.size() - 2).getEncircleUserId();
            }
        }
        result.put("lastUserId", lastUserId);
        result.put("hasNext", hasNext);
        result.put("encircleInfos", encircleInfos);
        return result;

    }
}
