package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.dao.IndexMatchRecommendDao;
import com.mojieai.predict.dao.StarUserMatchDao;
import com.mojieai.predict.dao.UserSportSocialRecommendDao;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.IndexMatchRecommend;
import com.mojieai.predict.entity.po.StarUserMatch;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.StarUserMatchService;
import com.mojieai.predict.service.ThirdHttpService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jute.Index;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class StarUserMatchServiceImpl implements StarUserMatchService {
    private Logger log = LogConstant.commonLog;
    @Autowired
    private RedisService redisService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private StarUserMatchDao starUserMatchDao;
    @Autowired
    private IndexMatchRecommendDao indexMatchRecommendDao;
    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private ThirdHttpService thirdHttpService;

    @Override
    public Map<String, Object> getStarUserList() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> starUsers = null;

        String key = RedisConstant.getStarUserRankKey(201806005);
        starUsers = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, Long.MAX_VALUE, 0, 10, HashMap.class);
        packageStarUsers(starUsers);

        result.put("starUsers", starUsers);
        return result;
    }

    @Override
    public Map<String, Object> manualSetStarUserWeight(Long userId, Long weight, Integer operateType) {
        Map<String, Object> result = new HashMap<>();
        Integer code = ResultConstant.ERROR;
        String msg = "设置失败";
        Integer activityId = 201806005;
        String starUserIdsKey = RedisConstant.getStarUserIdsKey(activityId);

        Long res = 0L;
        if (operateType == 1) {
            res = redisService.kryoHset(starUserIdsKey, userId, weight);
        } else if (operateType == 2) {
            redisService.del(starUserIdsKey);
        } else {
            res = redisService.kryoHDel(starUserIdsKey, userId) ? 1L : 0L;
            String rankKey = RedisConstant.getStarUserRankKey(activityId);
            redisService.del(rankKey);
        }

        if (res != null && res > 0L) {
            code = ResultConstant.SUCCESS;
            msg = "设置成功";
        }
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }

    @Override
    public Map<String, Object> getTopStarUsers(String orderType) {
        Map<String, Object> result = new HashMap<>();
        Integer activityId = 201806005;
        String key = RedisConstant.getActivityStarUserInfoKey(activityId, orderType);
        List<Map<String, Object>> startUsers = redisService.kryoGet(key, ArrayList.class);
        if (startUsers == null || startUsers.size() == 0) {
            startUsers = buildTopStarUser(activityId, orderType);
        }
        String starUserIdsKey = RedisConstant.getStarUserIdsKey(activityId);
        for (Map<String, Object> temp : startUsers) {
            Long userId = Long.valueOf(temp.get("userId").toString());
            temp.put("isStar", redisService.kryoHExists(starUserIdsKey, userId) ? 1 : 0);
            temp.put("weight", redisService.kryoHget(starUserIdsKey, userId, Long.class));
        }
        result.put("startUsers", startUsers);
        return result;
    }

    private List<Map<String, Object>> buildTopStarUser(Integer activityId, String orderType) {
        List<Map<String, Object>> startUsers = new ArrayList<>();
        List<Map<String, Object>> userIds = starUserMatchDao.getNeedBuildListStarUserId(activityId, 9);

        for (Map<String, Object> userMap : userIds) {
            if (userMap.containsKey("USER_ID")) {
                Long userId = Long.valueOf(userMap.get("USER_ID").toString());
                Map<String, Object> tempAchieve = getStarUserAchieve(userId, activityId);
                if (tempAchieve != null) {
                    UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
                    tempAchieve.put("userName", userLoginVo.getNickName());
                    tempAchieve.put("userImg", userLoginVo.getHeadImgUrl());
                    startUsers.add(tempAchieve);
                }
            }
        }

        if (orderType.equals("hitRatio")) {
            startUsers.sort((p1, p2) -> Integer.valueOf(p2.get("hitRatio").toString()).compareTo(Integer
                    .valueOf(p1.get("hitRatio").toString())));
        } else {
            startUsers.sort((p1, p2) -> Double.valueOf(p2.get("profitRatio").toString()).compareTo(Double
                    .valueOf(p1.get("profitRatio").toString())));
        }

        if (startUsers.size() > 50) {
            startUsers = startUsers.subList(0, 50);
        }
        String key = RedisConstant.getActivityStarUserInfoKey(activityId, orderType);
        redisService.del(key);
        redisService.kryoSetEx(key, 600, startUsers);
        return startUsers;
    }

    @Override
    public void buildStarUserList() {
        Integer activityId = 201806005;
        String starUserIdsKey = RedisConstant.getStarUserIdsKey(activityId);
        Set<Long> userIds = redisService.kryoHKeys(starUserIdsKey, Long.class);

        if (userIds == null || userIds.size() <= 0) {
            return;
        }
        String rankKey = RedisConstant.getStarUserRankKey(activityId);
        redisService.del(rankKey);
        for (Long userId : userIds) {
            Map<String, Object> temp = getStarUserAchieve(userId, activityId);
            if (temp != null) {
                Long score = redisService.kryoHget(starUserIdsKey, userId, Long.class);
                if (score == null) {
                    score = 1L;
                }
                redisService.kryoZAddSet(rankKey, score, temp);
            }
        }
    }

    @Override
    public void reSaveIndexRecommend2StarUser() {
        Timestamp beginTime = DateUtil.formatString("2018-06-14 00:00:00", "yyyy-MM-dd HH:mm:ss");
        List<IndexMatchRecommend> indexMatchRecommends = indexMatchRecommendDao.slaveGetIndexMatchByTime(beginTime);
        for (IndexMatchRecommend index : indexMatchRecommends) {
            UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(index
                    .getUserId(), index.getRecommendId(), false);
            saveRecommend2StarUser(recommend);
        }
    }

    @Override
    public void saveRecommend2StarUser(UserSportSocialRecommend recommend) {
        if (recommend == null || recommend.getIsRight() == null || recommend.getIsRight().equals(SportsProgramConstant
                .RECOMMEND_STATUS_INIT)) {
            return;
        }
        Map<String, DetailMatchInfo> matchInfoMap = thirdHttpService.getMatchMapByMatchIds(recommend.getMatchId());
        DetailMatchInfo detailMatchInfo = matchInfoMap.get(recommend.getMatchId());
        if (StringUtils.isBlank(detailMatchInfo.getMatchName()) || !detailMatchInfo.getMatchName().equals("世界杯")) {
            return;
        }
        StarUserMatch starUserMatch = new StarUserMatch(201806005, recommend.getUserId(), Integer.valueOf(recommend
                .getMatchId()), recommend.getIsRight(), recommend.getAwardAmount(), DateUtil.formatString
                (detailMatchInfo.getMatchTime(), "yyyy-MM-dd HH:mm:ss"));
        try {
            starUserMatchDao.insert(starUserMatch);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> getStarUserAchieve(Long userId, Integer activityId) {
        Map<String, Object> result = new HashMap<>();

        String title = "";
        String hitRatio = "";
        String profitRatio = "";
        Integer hitCount = 0;
        Integer profitAmount = 0;
        List<Integer> worldCupRecord = new ArrayList<>();
        List<StarUserMatch> starUserMatches = starUserMatchDao.getNeedBuildListStarUserMatch(activityId, userId, 9);
        if (starUserMatches == null && starUserMatches.size() < 5) {
            return null;
        }

        for (StarUserMatch starUserMatch : starUserMatches) {
            Integer record = 0;
            if (starUserMatch.getIsRight().equals(SportsProgramConstant.RECOMMEND_STATUS_WINNING)) {
                hitCount++;
                record = 1;
            }
            profitAmount += starUserMatch.getAward();
            worldCupRecord.add(record);
        }
        hitRatio = CommonUtil.divide((hitCount * 100) + "", starUserMatches.size() + "", 0);
        profitRatio = CommonUtil.divide(profitAmount + "", (starUserMatches.size() * 100) + "", 1);

        title = SportsUtils.getWorldCupStarUserTitle(Integer.valueOf(hitRatio), Double.valueOf(profitRatio), worldCupRecord
                .size());

        result.put("userName", "");
        result.put("userImg", "");
        result.put("userId", userId);
        result.put("title", title);
        result.put("jumpUrl", "mjlottery://mjnative?page=userCenterFootballMain&userid=" + userId);
        result.put("profitRatio", profitRatio);
        result.put("hitRatio", hitRatio);
        result.put("worldCupRecord", worldCupRecord);
        return result;
    }

    private List<Map<String, Object>> packageStarUsers(List<Map<String, Object>> starUsers) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (starUsers == null || starUsers.size() == 0) {
            return result;
        }
        for (Map<String, Object> temp : starUsers) {

            Long userId = temp.containsKey("userId") ? Long.valueOf(temp.get("userId").toString()) : null;
            if (userId != null) {
                UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);
                temp.put("userName", userLoginVo.getNickName());
                temp.put("userImg", userLoginVo.getHeadImgUrl());
            }
            result.add(temp);
        }
        return result;
    }
}
