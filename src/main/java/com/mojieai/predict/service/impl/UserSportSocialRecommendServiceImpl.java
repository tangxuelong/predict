package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.IndexMatchRecommendDao;
import com.mojieai.predict.dao.SocialUserFollowDao;
import com.mojieai.predict.dao.UserSportSocialRecommendDao;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.IndexMatchRecommend;
import com.mojieai.predict.entity.po.SocialUserFollow;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import com.mojieai.predict.entity.vo.SportSocialRankVo;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserSportSocialRecommendServiceImpl implements UserSportSocialRecommendService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private IndexMatchRecommendDao indexMatchRecommendDao;
    @Autowired
    private SocialUserFollowDao socialUserFollowDao;
    @Autowired
    private VipMemberService vipMemberService;

    @Override
    public Map<String, Object> getUserSportSocialRecommends(Long userId, String lastIndex) {
        Map<String, Object> res = new HashMap<>();

        Integer page = 10;
        boolean hasNext = false;
        List<UserSportSocialRecommend> userSportRecommends = userSportSocialRecommendDao.getUserSportRecommendsBySize
                (userId, lastIndex, page + 1);
        if (userSportRecommends != null && userSportRecommends.size() > 0) {
            lastIndex = userSportRecommends.get(userSportRecommends.size() - 1).getRecommendId();
            if (userSportRecommends.size() > page) {
                hasNext = true;
                lastIndex = userSportRecommends.get(page).getRecommendId();
            }
        }

        res.put("hasNext", hasNext);
        res.put("lastIndex", lastIndex);
        res.put("recommend", userSportRecommends);
        return res;
    }

    @Override
    public List<Map<String, Object>> getSportSocialIndexHotRecommend() {
        String key = RedisConstant.getSportSocialIndexHotRecommend();
        List<Map<String, Object>> res = redisService.kryoGet(key, ArrayList.class);
        if (res == null || res.size() == 0) {
            res = rebuildSportSocialIndexHotRecommend(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT);
        }
        //如果重构还是没有，从免费中拿
        if (res == null || res.size() == 0) {
            res = rebuildSportSocialIndexHotRecommend(SportsProgramConstant.SPORT_RECOMMEND_LIST_FREE);
        }
        for (Map<String, Object> temp : res) {
            if (!temp.containsKey("data")) {
                continue;
            }
            List<Map<String, Object>> data = (List<Map<String, Object>>) temp.get("data");
            packageRecommendMap(data);
        }
        return res;
    }

    @Override
    public Map<String, Object> getUserRecommendListFromRedis(Long userId, Integer listType, Integer playType, String
            lastIndex) {
        Map<String, Object> res = new HashMap<>();
        boolean hasNext = false;
        Integer pageSize = 30;
        List<Map<String, Object>> datas = null;
        List<Map<String, Object>> manualRecommends = new ArrayList<>();
        boolean operateFlag = true;
        if (listType.equals(SportsProgramConstant.SPORT_RECOMMEND_LIST_FOLLOW)) {
            Long lastUserId = null;
            if (StringUtils.isNotBlank(lastIndex)) {
                lastUserId = Long.valueOf(lastIndex);
                operateFlag = false;
            }
            datas = getUserFollowPersonRecommend(userId, listType, playType, lastUserId, pageSize + 1);
            datas = datas.stream().sorted(new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Timestamp o1Time = (Timestamp) o1.get("createTimeOrigin");
                    Timestamp o2Time = (Timestamp) o2.get("createTimeOrigin");
                    return o2Time.compareTo(o1Time);
                }
            }).collect(Collectors.toList());

            if (datas != null && datas.size() > 0) {
                hasNext = true;
                lastIndex = datas.get(datas.size() - 1).get("userId").toString();
                if (datas.size() > pageSize) {
                    hasNext = true;
                    lastIndex = datas.get(pageSize - 1).get("userId").toString();
                }
            }
        } else {
            if (StringUtils.isNotBlank(lastIndex)) {
                operateFlag = false;
            }
            datas = getRecommendFromRedis(listType, playType, lastIndex, pageSize + 1);
            //设置最后
            if (datas != null && datas.size() > 0) {
                Timestamp createTime = (Timestamp) datas.get(datas.size() - 1).get("createTimeOrigin");
                Long price = (Long) datas.get(datas.size() - 1).get("price");
                String hitRate = datas.get(datas.size() - 1).get("hitRate").toString();
                String leagueMatch = "";
                if (datas.get(datas.size() - 1).containsKey("leagueMatch")) {
                    leagueMatch = datas.get(datas.size() - 1).get("leagueMatch").toString();
                }
                if (datas.size() > 30) {
                    hasNext = true;
                    createTime = (Timestamp) datas.get(pageSize - 1).get("createTimeOrigin");
                    price = (Long) datas.get(pageSize - 1).get("price");
                    hitRate = datas.get(pageSize - 1).get("hitRate").toString();
                    leagueMatch = "";
                    if (datas.get(pageSize - 1).containsKey("leagueMatch")) {
                        leagueMatch = datas.get(pageSize - 1).get("leagueMatch").toString();
                    }
                }
                long timeSore = createTime.getTime();
                if (listType.equals(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT)) {
                    timeSore = Long.valueOf(hitRate + "" + timeSore);
                }

                Integer tempPrice = SportsUtils.getTempScoreByPrice(price, leagueMatch);
                if (tempPrice != null) {
                    timeSore = Long.valueOf(tempPrice + "" + timeSore);
                }
                lastIndex = String.valueOf(timeSore);
            }
            if (operateFlag) {
                String manualRecommendKey = RedisConstant.getManualRecommendKey(listType, playType);
                manualRecommends = redisService.kryoZRevRangeByScoreGet(manualRecommendKey, Long.MIN_VALUE, Long
                        .MAX_VALUE, HashMap.class);
            }
        }
        packageRecommendMap(datas);

        List<Map<String, Object>> dataAll = new ArrayList<>();
        if (manualRecommends != null && manualRecommends.size() > 0) {
            packageRecommendMap(manualRecommends);
            dataAll = manualRecommends;
        }
        dataAll.addAll(datas);

        res.put("hasNext", hasNext);
        res.put("lastIndex", lastIndex);
        res.put("datas", dataAll);
        return res;
    }

    @Override
    public Map<String, Object> getMatchBasicData(String matchId) {
        Map<String, Object> result = new HashMap<>();

        result.putAll(thirdHttpService.getMatchFundamentals(matchId));
        return result;
    }

    @Override
    public Map<String, Object> getMatchOddsData(String matchId) {
        Map<String, Object> result = new HashMap<>();

        result.put("odds", thirdHttpService.getAllPartOdds(matchId));
        return result;
    }

    @Override
    public Map<String, Object> getMatchPredictData(String matchId, String lastIndex) {
        Map<String, Object> result = new HashMap<>();

        boolean hasNext = false;
        String key = RedisConstant.getSportSocialOneMatchRecommendListKey(matchId);
        Long maxScore = Long.MAX_VALUE;
        if (StringUtils.isNotBlank(lastIndex)) {
            maxScore = Long.valueOf(lastIndex);
        }
        int pageSize = 30;
        Long predictCount = redisService.kryoZCount(key, Long.MIN_VALUE, Long.MAX_VALUE);

        List<Map<String, Object>> predicts = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, maxScore, 0,
                pageSize + 1, HashMap.class);

        if (predicts == null || predicts.size() == 0) {
            rebuildSportOneMatchRecommend(matchId);
            predicts = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, maxScore, 0, pageSize + 1, HashMap
                    .class);
        }

        List<Map<String, Object>> subPredicts = null;
        if (predicts != null && predicts.size() > 0) {
            Timestamp createTime = (Timestamp) predicts.get(predicts.size() - 1).get("createTimeOrigin");
            String hitRate = predicts.get(predicts.size() - 1).get("hitRate").toString();
            Long price = (Long) predicts.get(predicts.size() - 1).get("price");
            subPredicts = predicts;
            String leagueMatch = "";
            if (predicts.get(predicts.size() - 1).containsKey("leagueMatch")) {
                leagueMatch = predicts.get(predicts.size() - 1).get("leagueMatch").toString();
            }
            if (predicts.size() > pageSize) {
                hasNext = true;
                createTime = (Timestamp) predicts.get(pageSize - 1).get("createTimeOrigin");
                hitRate = predicts.get(pageSize).get("hitRate").toString();
                subPredicts = predicts.subList(0, pageSize - 1);
                price = (Long) predicts.get(pageSize - 1).get("price");
                if (predicts.get(pageSize - 1).containsKey("leagueMatch")) {
                    leagueMatch = predicts.get(pageSize - 1).get("leagueMatch").toString();
                }
            }
            long timeSore = createTime.getTime();
            timeSore = Long.valueOf(hitRate + "" + timeSore);
            Integer tempPrice = SportsUtils.getTempScoreByPrice(price, leagueMatch);
            if (tempPrice != null) {
                timeSore = Long.valueOf(tempPrice + "" + timeSore);
            }
            lastIndex = String.valueOf(timeSore);
        }

        //获取赛事信息
        Map<String, DetailMatchInfo> matchInfoMap = thirdHttpService.getMatchMapByMatchIds(matchId);
        Map<String, Object> matchInfo = new HashMap<>();
        if (matchInfoMap != null) {
            matchInfo = packageFundamentalsMatch(matchInfoMap.get(matchId));
        }

//        List<IndexMatchRecommend> subIndex = null;
//        if (indexs != null && indexs.size() > 0) {
//            lastIndex = indexs.get(indexs.size() - 1).getRecommendId();
//            subIndex = indexs.subList(0, indexs.size() - 1);
//            if (indexs.size() == pageSize + 1) {
//                hasNext = true;
//                lastIndex = indexs.get(pageSize - 1).getRecommendId();
//                subIndex = indexs.subList(0, pageSize - 1);
//            }
//        }

//        List<Map<String, Object>> predicts = new ArrayList<>();
//        if (subIndex != null) {
//            for (IndexMatchRecommend index : subIndex) {
//                UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(index
//                        .getUserId(), index.getRecommendId(), false);
//                Map<String, Object> recommendMap = getUserRecommendMap(recommend);
//
//                Map<String, Object> userAchieve = getUserRecentAchieve(index.getUserId());
//                Integer userHitRatio = userAchieve.get("hitRate") == null ? 0 : Integer.valueOf(userAchieve.get
//                        ("hitRate").toString());
//                recommendMap.putAll(userAchieve);
//                recommendMap.put("hitRate", userHitRatio);
//                predicts.add(recommendMap);
//            }
//        }
//        Integer predictCount = indexMatchRecommendDao.getMatchPredictCount(matchId);
        predictCount = predictCount == null ? 0L : predictCount;

        packageRecommendMap(subPredicts);

        result.put("matchInfo", matchInfo);
        // 兼容
        if (subPredicts != null && subPredicts.size() > 0) {
            for (Map<String, Object> row : subPredicts) {
                try {
                    String[] rows = row.get("matchDesc").toString().split(CommonConstant.SPACE_SPLIT_STR);
                    Integer i = 0;
                    String matchS = "";
                    String matchN = "";
                    for (String s : rows) {
                        if (i.equals(0)) {
                            matchS = s;
                        } else {
                            matchN += s + " ";
                        }
                        i++;
                    }
                    row.put("matchS", matchS);
                    row.put("matchN", matchN);
                } catch (Exception e) {
                    continue;
                }
            }
        }
        result.put("predicts", subPredicts);
        result.put("predictCount", predictCount);
        result.put("hasNext", hasNext);
        result.put("lastIndex", lastIndex);
        return result;
    }

    @Override
    public void rebuildSportOneMatchRecommend(String matchId) {
        List<IndexMatchRecommend> indexs = indexMatchRecommendDao.getRecommendUserByMatchId(matchId);

        if (indexs != null && indexs.size() > 0) {
            for (IndexMatchRecommend index : indexs) {
                UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(index
                        .getUserId(), index.getRecommendId(), false);
                saveRecommendMap2MatchRedis(recommend);
            }
        }
    }

    @Override
    public void saveRecommendMap2MatchRedis(UserSportSocialRecommend recommend) {
        String key = RedisConstant.getSportSocialOneMatchRecommendListKey(recommend.getMatchId());
        Map<String, Object> recommendMap = getUserRecommendMap(recommend);

        Map<String, Object> userAchieve = getUserRecentAchieve(recommend.getUserId());
        Integer userHitRatio = userAchieve.get("hitRate") == null ? 0 : Integer.valueOf(userAchieve.get
                ("hitRate").toString());
        recommendMap.putAll(userAchieve);
        recommendMap.put("hitRate", userHitRatio);

        // 推荐标题 4.6.2
        userRecommendTitleLock(recommend);
        recommendMap.put("recommendTitle", recommend.getRecommendTitle());

        // 添加标签 4.6.4 单选 分析
        recommendMap.putAll(recommend.remark2marks());

        Timestamp createTime = (Timestamp) recommendMap.get("createTimeOrigin");
        String hitRate = recommendMap.get("hitRate").toString();
        long timeSore = createTime.getTime();
        timeSore = Long.valueOf(hitRate + "" + timeSore);

        Long price = (Long) recommendMap.get("price");
        String leagueMatch = "";
        if (recommendMap.get("leagueMatch") != null) {
            leagueMatch = recommendMap.get("leagueMatch").toString();
        }

        Integer tempPrice = SportsUtils.getTempScoreByPrice(price, leagueMatch);
        if (tempPrice != null) {
            timeSore = Long.valueOf(tempPrice + "" + timeSore);
        }

        try {
            if (redisService.kryoZScore(key, recommendMap) != null) {
                redisService.kryoZRem(key, recommendMap);
            }
            redisService.kryoZAddSet(key, timeSore, recommendMap);
            redisService.expire(key, 604800);
        } catch (Exception e) {
            log.error("保存推荐到赛事缓存列表", e);
        }
    }

    private Map<String, Object> packageFundamentalsMatch(DetailMatchInfo detailMatchInfo) {
        Map<String, Object> result = new HashMap<>();

        String score = "";
        if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            score = detailMatchInfo.getHostScore() + " - " + detailMatchInfo.getAwayScore();
        } else if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
            score = detailMatchInfo.getHostScore() + " : " + detailMatchInfo.getAwayScore();
        }

        String halfScore = "";
        if (StringUtils.isNotBlank(detailMatchInfo.getHalfScore())) {
            halfScore = "(" + detailMatchInfo.getHalfScore() + ")";
        }

        result.put("hostName", detailMatchInfo.getHostName());
        result.put("hostImg", detailMatchInfo.getHostImg());
        result.put("hostRank", "");
        result.put("awayName", detailMatchInfo.getAwayName());
        result.put("awayImg", detailMatchInfo.getAwayImg());
        result.put("awayRank", "");
        result.put("matchStatusDesc", SportsUtils.getMatchStatusCn(detailMatchInfo.getMatchStatus()));
        result.put("matchStatus", detailMatchInfo.getMatchStatus());
        result.put("matchName", detailMatchInfo.getMatchName());
        result.put("matchTime", detailMatchInfo.getMatchTime());
        result.put("score", score);
        result.put("halfScore", halfScore);
        result.put("liveDesc", detailMatchInfo.getLiveDesc());
        return result;
    }

    private void packageRecommendMap(List<Map<String, Object>> datas) {
        if (datas == null || datas.size() == 0) {
            return;
        }
        for (Map data : datas) {
            Long userId = Long.valueOf(data.get("userId").toString());
            UserLoginVo userLoginVo = loginService.getUserLoginVo(userId);

            Timestamp time = (Timestamp) data.get("createTimeOrigin");
            data.put("userName", userLoginVo.getNickName());
            data.put("userImg", userLoginVo.getHeadImgUrl());
            data.put("createTime", SportsUtils.getRecommendTimeShow(time));
            data.put("isSportsVip", vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                    .VIP_MEMBER_TYPE_SPORTS));
        }
    }

    private List<Map<String, Object>> getUserFollowPersonRecommend(Long userId, Integer listType, Integer playType, Long
            lastIndex, Integer pageSize) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (pageSize == 0) {
            return res;
        }
        //1.获取关注人的信息
        List<SocialUserFollow> follows = socialUserFollowDao.getFollowUserIdList(userId, CommonConstant
                .SOCIAL_FOLLOW_FANS_TYPE_SPORT, pageSize, lastIndex);
        if (follows == null || follows.size() == 0) {
            return res;
        }
        //2.依据关注的人查询条数
        for (SocialUserFollow socialUserFollow : follows) {
            List<UserSportSocialRecommend> sportSocialRecommends = userSportSocialRecommendDao
                    .getUserCanPurchaseRecommend(socialUserFollow.getFollowUserId(), playType);
            if (sportSocialRecommends == null || sportSocialRecommends.size() == 0) {
                lastIndex = socialUserFollow.getFollowUserId();
                continue;
            }
            for (UserSportSocialRecommend recommend : sportSocialRecommends) {
                //3.依据排行榜获取用户排行榜信息
                Map<String, Object> userAchieve = getUserRecentAchieve(recommend.getUserId());

                Integer userHitRatio = userAchieve.get("hitRate") == null ? 0 : Integer.valueOf(userAchieve.get
                        ("hitRate").toString());
                //2.从三方获取赛事信息
                Map<String, Object> userRecommendMap = getUserRecommendMap(recommend);

                userRecommendMap.putAll(userAchieve);
                userRecommendMap.put("hitRate", userHitRatio);
                res.add(userRecommendMap);
                pageSize--;
            }
            if (pageSize <= 0) {
                break;
            }
            lastIndex = socialUserFollow.getFollowUserId();
        }

        if (pageSize > 0) {
            List<Map<String, Object>> tempRes = getUserFollowPersonRecommend(userId, listType, playType, lastIndex,
                    pageSize);
            if (tempRes.size() != 0) {
                res.addAll(tempRes);
            }
        }
        return res;
    }

    private List<Map<String, Object>> getRecommendFromRedis(Integer listType, Integer playType, String lastIndex,
                                                            Integer pageSize) {
        String key = RedisConstant.getSportSocialRecommendListKey(listType, playType);
        Long maxScore = Long.MAX_VALUE;
        if (StringUtils.isNotBlank(lastIndex)) {
            maxScore = Long.valueOf(lastIndex);
        }

        List<Map<String, Object>> datas = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, maxScore, 0,
                pageSize, HashMap.class);
        if (datas == null) {
            rebuildSportRecommendListByMatchIndex(listType, playType);
            datas = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, maxScore, 0, pageSize, HashMap.class);
        }
        return datas;
    }

    @Override
    public void rebuildSportRecommendListByMatchIndex(Integer listType, Integer playType) {
        //1.先将redis推荐列表删除
        String key = RedisConstant.getSportSocialRecommendListKey(listType, playType);
        redisService.del(key);
        //2.获取所有的推荐索引
        List<IndexMatchRecommend> indexMatchRecommends = indexMatchRecommendDao.getAllWaitCalculateRecommend();
        for (IndexMatchRecommend indexRecommend : indexMatchRecommends) {
            UserSportSocialRecommend userSportSocialRecommend = userSportSocialRecommendDao
                    .getSportSocialRecommendById(indexRecommend.getUserId(), indexRecommend.getRecommendId(), false);

            if (checkRecommendIfRebuild(userSportSocialRecommend, playType, listType)) {
                buildRecommendList(userSportSocialRecommend, listType, playType);
            }
        }
    }

    @Override
    public void rebuildSportRecommendList() {
        rebuildSportRecommendListByMatchIndex(SportsProgramConstant.SPORT_RECOMMEND_LIST_FREE, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_SPF);
        rebuildSportRecommendListByMatchIndex(SportsProgramConstant.SPORT_RECOMMEND_LIST_FREE, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_RQSPF);
        rebuildSportRecommendListByMatchIndex(SportsProgramConstant.SPORT_RECOMMEND_LIST_FREE, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_ASIA);
        rebuildSportRecommendListByMatchIndex(SportsProgramConstant.SPORT_RECOMMEND_LIST_FREE, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_ALL);
        rebuildSportRecommendListByMatchIndex(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_SPF);
        rebuildSportRecommendListByMatchIndex(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_RQSPF);
        rebuildSportRecommendListByMatchIndex(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_ASIA);
        rebuildSportRecommendListByMatchIndex(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_ALL);

        //首页缓存刷新
        String key = RedisConstant.getSportSocialIndexHotRecommend();
        redisService.del(key);
    }

    @Override
    public Map<String, Object> getUserRecentRecommend(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<UserSportSocialRecommend> userRecommends = userSportSocialRecommendDao.getUserRecentRecommend(userId, 10);
        List<Map<String, Object>> recommends = new ArrayList<>();

        for (UserSportSocialRecommend recommend : userRecommends) {
            Map<String, Object> temp = new HashMap<>();
            Map<String, DetailMatchInfo> matchInfoMap = thirdHttpService.getMatchMapByMatchIds(recommend.getMatchId());
            DetailMatchInfo detailMatchInfo = matchInfoMap.get(recommend.getMatchId());
            if (!detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_INIT)) {
                continue;
            }

            if (DateUtil.compareDate(detailMatchInfo.getEndTime(), DateUtil.getCurrentTimestamp())) {
                continue;
            }

            temp.put("matchTime", detailMatchInfo.getMatchTime());
            temp.put("hostName", detailMatchInfo.getHostName());
            temp.put("awayName", detailMatchInfo.getAwayName());
            temp.put("recommendId", recommend.getRecommendId());

            // 添加推荐title 4.6.2
            userRecommendTitleLock(recommend);
            temp.put("recommendTitle", recommend.getRecommendTitle());

            // 添加标签 4.6.4 单选 分析
            temp.putAll(recommend.remark2marks());

            temp.put("recommendId", recommend.getRecommendId());
            temp.put("recommendInfo", getSampleRecommendInfo(recommend.getRecommendInfo(), recommend.getPlayType()));
            temp.put("createTime", DateUtil.formatTime(recommend.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            recommends.add(temp);
        }
        result.put("recommends", recommends);
        result.put("userId", userId);
        return result;
    }

    @Override
    public Boolean checkUserRecommend(Long userId, Integer taskTimes, Timestamp recommendDate) {

        Integer count = userSportSocialRecommendDao.getUserRecommendCount(userId, DateUtil.getBeginOfOneDay
                (recommendDate), DateUtil.getEndOfOneDay(recommendDate));
        if (count == null) {
            return false;
        }
        if (count >= taskTimes) {
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> operateHotRecommend(String recommendId, Long weight, Integer operateType) {
        Map<String, Object> result = new HashMap<>();
        Integer code = ResultConstant.ERROR;
        String msg = "保存失败";
        UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(CommonUtil
                .getUserIdSuffix(recommendId), recommendId, false);
        if (recommend == null) {
            result.put("msg", msg);
            result.put("code", code);
            return result;
        }

        String key = RedisConstant.getManualSportsHotRecommendIdsKey(recommend.getPlayType());
        if (operateType.equals(1)) {
            redisService.kryoHset(key, recommend.getRecommendId(), weight);
            redisService.expire(key, 604800);
            code = ResultConstant.SUCCESS;
            msg = "保存成功";
        } else {
            if (redisService.kryoHDel(key, recommend.getRecommendId())) {
                code = ResultConstant.SUCCESS;
                msg = "移除成功";
            }
        }
        rebuildManualRecommendList(recommend.getPlayType());
        result.put("msg", msg);
        result.put("code", code);
        return result;
    }

    @Override
    public Map<String, Object> getHotRecommendInfo(Integer playType) {
        Map<String, Object> result = new HashMap<>();
        String key = RedisConstant.getManualSportsHotRecommendIdsKey(playType);
        Map<String, Long> recommendWeightMap = redisService.kryoHgetAll(key, String.class, Long.class);
        List<Map<String, Object>> hotUserRecommend = new ArrayList<>();
        if (recommendWeightMap != null && recommendWeightMap.size() >= 0) {
            for (String recommendId : recommendWeightMap.keySet()) {
                UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(CommonUtil
                        .getUserIdSuffix(recommendId), recommendId, false);
                if (recommend == null) {
                    continue;
                }
                DetailMatchInfo matchInfo = thirdHttpService.getMatchListByMatchIds(recommend.getMatchId()).get(0);
                if (DateUtil.compareDate(matchInfo.getEndTime(), DateUtil.getCurrentTimestamp())) {
                    continue;
                }

                Map<String, Object> temp = new HashMap<>();
                UserLoginVo userLoginVo = loginService.getUserLoginVo(recommend.getUserId());
                temp.put("recommendId", recommend.getRecommendId());
                // 添加推荐 title 4.6.2
                userRecommendTitleLock(recommend);
                temp.put("recommendTitle", recommend.getRecommendTitle());

                // 添加标签 4.6.4 单选 分析
                temp.putAll(recommend.remark2marks());

                temp.put("userName", userLoginVo.getNickName());
                temp.put("hostName", matchInfo.getHostName());
                temp.put("awayName", matchInfo.getAwayName());
                temp.put("recommendInfo", getSampleRecommendInfo(recommend.getRecommendInfo(), playType));
                temp.put("matchTime", matchInfo.getMatchTime());
                hotUserRecommend.add(temp);
            }
        }
        result.put("hotUserRecommend", hotUserRecommend);
        return result;
    }

    @Override
    public Map<String, Object> clearManualHotRecommend(Integer playType) {
        Map<String, Object> result = new HashMap<>();
        String recommendDateKey = RedisConstant.getManualRecommendKey(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT,
                playType);
        redisService.del(recommendDateKey);
        String recommendIdsKey = RedisConstant.getManualSportsHotRecommendIdsKey(playType);
        redisService.del(recommendIdsKey);
        result.put("code", ResultConstant.SUCCESS);
        result.put("msg", "清除成功");
        return result;
    }

    private Boolean saveOperateHotRecommend2Redis(UserSportSocialRecommend recommend, Long weight) {
        Integer listType = SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT;
        if (recommend.getPrice() == null || recommend.getPrice() <= 0L) {
            return false;
        }
        String key = RedisConstant.getManualRecommendKey(listType, recommend.getPlayType());

        Map<String, Object> userRecommendMap = null;
        //2.从三方获取赛事信息
        try {
            userRecommendMap = getUserRecommendMap(recommend);

            Map<String, Object> userAchieve = getUserRecentAchieve(recommend.getUserId());
            Integer userHitRatio = userAchieve.get("hitRate") == null ? 0 : Integer.valueOf(userAchieve.get
                    ("hitRate").toString());
            userRecommendMap.putAll(userAchieve);
            userRecommendMap.put("hitRate", userHitRatio);
        } catch (Exception e) {
            log.error("三方获取比赛信息异常", e);
            return Boolean.FALSE;
        }

        //结束的比赛不在构建缓存中
        if (DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), recommend.getEndTime()) < 100) {
            return Boolean.FALSE;
        }

        redisService.kryoZAddSet(key, weight, userRecommendMap);
        return Boolean.TRUE;
    }

    @Override
    public void rebuildManualRecommendList() {
        rebuildManualRecommendList(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF);
        rebuildManualRecommendList(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF);
        rebuildManualRecommendList(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA);
    }

    public void rebuildManualRecommendList(Integer playType) {
        Map<String, Long> recommendWeightMap = redisService.kryoHgetAll(RedisConstant.getManualSportsHotRecommendIdsKey
                (playType), String.class, Long.class);
        Integer listType = SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT;
        String key = RedisConstant.getManualRecommendKey(listType, playType);
        redisService.del(key);
        if (recommendWeightMap == null || recommendWeightMap.isEmpty()) {
            return;
        }
        for (String recommendId : recommendWeightMap.keySet()) {
            UserSportSocialRecommend recommend = userSportSocialRecommendDao.getSportSocialRecommendById(CommonUtil
                    .getUserIdSuffix(recommendId), recommendId, false);
            if (recommend != null && playType.equals(recommend.getPlayType())) {
                saveOperateHotRecommend2Redis(recommend, recommendWeightMap.get(recommendId));
            }
        }
    }

    @Override
    public void buildRecommendListTiming() {
        String sportRecommendTempStorageListKey = RedisConstant.getSportSocialRecommendTempStorageListKey();
        if (redisService.llen(sportRecommendTempStorageListKey) <= 0) {
            return;
        }

        while (redisService.llen(sportRecommendTempStorageListKey) > 0) {
            UserSportSocialRecommend userRecommend = redisService.kryoLindex(sportRecommendTempStorageListKey, 0,
                    UserSportSocialRecommend.class);
            if (userRecommend != null) {
                Boolean removeFlag = buildRecommendList(userRecommend, null, null);

                if (removeFlag) {
                    redisService.kryoLrem(sportRecommendTempStorageListKey, 1, userRecommend);
                }
            }
        }
    }

    private Boolean buildRecommendList(UserSportSocialRecommend userRecommend, Integer listType, Integer playType) {
        Boolean res = Boolean.FALSE;
        Boolean dealFlag = Boolean.FALSE;
        //1.构建免费推荐列表
        if ((listType == null || listType.equals(SportsProgramConstant.SPORT_RECOMMEND_LIST_FREE)) && (userRecommend
                .getPrice() == null || userRecommend.getPrice() <= 0L)) {
            dealFlag = Boolean.TRUE;
            Boolean tempRes = saveUserSportRecommend2RecommendList(userRecommend, SportsProgramConstant
                    .SPORT_RECOMMEND_LIST_FREE);
            if (tempRes) {
                res = Boolean.TRUE;
            }
        }
        //2.构建热门推荐列表
        if ((listType == null || listType.equals(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT)) &&
                checkRecommendIfHotRecommend(userRecommend)) {
            dealFlag = Boolean.TRUE;
            Boolean tempRes = saveUserSportRecommend2RecommendList(userRecommend, SportsProgramConstant
                    .SPORT_RECOMMEND_LIST_HOT);
            if (tempRes) {
                res = Boolean.TRUE;
                //2.1首页热门推荐重构
                String key = RedisConstant.getSportSocialIndexHotRecommend();
                redisService.del(key);
                //2.2重构首页热门推荐
                rebuildSportSocialIndexHotRecommend(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT);
            }
        }
        if (!dealFlag) {
            res = Boolean.TRUE;
        }
        return res;
    }

    private boolean saveUserSportRecommend2RecommendList(UserSportSocialRecommend userRecommend, Integer listType) {
        String key = RedisConstant.getSportSocialRecommendListKey(listType, userRecommend.getPlayType());

        long score = userRecommend.getCreateTime().getTime();
        //1.依据排行榜获取用户排行榜信息
        Map<String, Object> userAchieve = getUserRecentAchieve(userRecommend.getUserId());

        Integer userHitRatio = userAchieve.get("hitRate") == null ? 0 : Integer.valueOf(userAchieve.get("hitRate")
                .toString());
        Map<String, Object> userRecommendMap = null;
        //2.从三方获取赛事信息
        try {
            userRecommendMap = getUserRecommendMap(userRecommend);
        } catch (Exception e) {
            log.error("三方获取比赛信息异常", e);
            return false;
        }

        //结束的比赛不在构建缓存中
        if (DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), userRecommend.getEndTime()) < 100) {
            return true;
        }

        userRecommendMap.putAll(userAchieve);
        userRecommendMap.put("hitRate", userHitRatio);

        userRecommendTitleLock(userRecommend);
        userRecommendMap.put("recommendTitle", userRecommend.getRecommendTitle());

        // 添加标签 4.6.4 单选 分析
        userRecommendMap.putAll(userRecommend.remark2marks());

        //3.计算用户排名
        if (listType.equals(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT)) {
            score = Long.valueOf(userHitRatio + "" + score);
        }
        Integer tempScore = SportsUtils.getTempScoreByPrice(userRecommend.getPrice(), userRecommendMap.get
                ("leagueMatch").toString());
        if (tempScore != null) {
            score = Long.valueOf(tempScore + "" + score);
        }

        if (redisService.kryoZScore(key, userRecommendMap) == null) {
            return redisService.kryoZAddSet(key, score, userRecommendMap);
        }
        return true;
    }

    private Map<String, Object> getUserRecentAchieve(Long userId) {
        Map<String, Object> res = new HashMap<>();
        SportSocialRankVo sportSocialRankVo = sportSocialService.getSportSocialRankVo(userId);
        Integer userHitRatio = 0;
        Integer continueNum = 0;
        if (sportSocialRankVo != null) {
            if (sportSocialRankVo.getUserRightNumsRank() != null) {
                Map<Integer, Integer> rightNumsRank = sportSocialRankVo.getUserRightNumsRank();
                userHitRatio = rightNumsRank.get(3);
            }
            if (sportSocialRankVo.getUserMaxNumsRank() != null) {
                Map<Integer, Integer> continueRank = sportSocialRankVo.getUserMaxNumsRank();
                continueNum = continueRank.get(3);
            }
        }
        // 成就
        List<Map> achieve = new ArrayList<>();
        Map tempMap = new HashMap();
        tempMap.put("name", "<font color='#FF762B'>" + continueNum + "连中</font>");
        tempMap.put("bgImg", "http://sportsimg.mojieai.com/sport_person_continue_bg.png");
        achieve.add(tempMap);

        res.put("achieve", achieve);
        res.put("hitRate", userHitRatio);
        res.put("htiRateUnit", "%");
        res.put("hitDesc", "七天命中");
        // 添加推荐标题

        return res;
    }

    @Override
    public void userRecommendTitleLock(UserSportSocialRecommend userSportSocialRecommend) {
        // 关闭用户TITLE，关闭所有用户TITILE
        if (null == userSportSocialRecommend.getRecommendTitle() || ActivityIniCache.getActivityIniIntValue
                (ActivityIniConstant.FOOTBALL_IS_SHOW_RECOMMEND_TITLE, 1).equals(0) || null != redisService.kryoZScore
                ("delUserRecommendTitle", userSportSocialRecommend.getUserId())) {
            userSportSocialRecommend.setRecommendTitle("");
        }
    }

    private Map<String, Object> getUserRecommendMap(UserSportSocialRecommend userRecommend) {
        Map<String, Object> res = new HashMap<>();

        UserLoginVo userLoginVo = loginService.getUserLoginVo(userRecommend.getUserId());

        //1.获取赛事信息
        Map<String, DetailMatchInfo> detailMatchInfoMap = thirdHttpService.getMatchMapByMatchIds(userRecommend
                .getMatchId());
        DetailMatchInfo detailMatchInfo = detailMatchInfoMap.get(userRecommend.getMatchId());
        String matchName = detailMatchInfo.getHostName() + "  VS  " + detailMatchInfo.getAwayName();
        if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            matchName = detailMatchInfo.getHostName() + "  " + detailMatchInfo.getHostScore() + ":" + detailMatchInfo
                    .getAwayScore() + "  " + detailMatchInfo.getAwayName();
        }

        //"胜平负 西甲 周一001 01-13 12:00"
        String matchDesc = SportsUtils.getPlayTypeCn(userRecommend.getLotteryCode(), userRecommend.getPlayType()) +
                " " + detailMatchInfo.getMatchName() + " " + detailMatchInfo.getMatchDate() + " " + detailMatchInfo
                .getMatchTime();
        String createTime = SportsUtils.getRecommendTimeShow(userRecommend.getCreateTime());

        String btnMsg = "免费";
        Long price = userRecommend.getPrice();
        if (price != null && price > 0l) {
            btnMsg = "<font color='#ff5050'>" + CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(price)
                    .toString()) + "</font>" + CommonConstant.WISDOM_COIN_PAY_NAME;
        }
        if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
            btnMsg = "<font color='#ff5050'>比赛中</font>";
        }
        Integer programStatus = 0;
        if (userRecommend.getIsRight() != null) {
            programStatus = userRecommend.getIsRight();
        }

        res.put("userName", userLoginVo.getNickName());
        res.put("userId", userLoginVo.getUserId());
        res.put("isSportsVip", vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                .VIP_MEMBER_TYPE_SPORTS));
        res.put("recommendId", userRecommend.getRecommendId());
        res.put("userImg", userLoginVo.getHeadImgUrl());
        res.put("matchName", matchName);
        res.put("matchDesc", matchDesc);
        res.put("tags", SportsUtils.getMatchTags(userRecommend.getPlayType(), detailMatchInfo.getTag()));
        res.put("btnMsg", btnMsg);
        res.put("price", userRecommend.getPrice());
        res.put("createTime", createTime);
        res.put("createTimeOrigin", userRecommend.getCreateTime());
        res.put("purchaseStatus", CommonConstant.FOOTBALL_PROGRAM_STATUS_NO_PAY);
        res.put("leagueMatch", detailMatchInfo.getMatchName());
        res.put("programStatus", programStatus);
        res.put("matchStatus", detailMatchInfo.getMatchStatus());
        return res;
    }

    private List<Map<String, Object>> rebuildSportSocialIndexHotRecommend(Integer listType) {
        List<Map<String, Object>> res = new ArrayList<>();

        Map<String, Object> spf = new HashMap<>();
        List<Map<String, Object>> spfRecommend = getRecommendFromRedis(listType, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_SPF, null, 5);
        spf.put("playType", SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF);
        spf.put("playName", SportsUtils.getPlayTypeCn(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF));
        spf.put("data", spfRecommend);
        res.add(spf);

        Map<String, Object> rqspf = new HashMap<>();
        List<Map<String, Object>> sqspfRecommend = getRecommendFromRedis(listType, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_RQSPF, null, 5);
        rqspf.put("playType", SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF);
        rqspf.put("playName", SportsUtils.getPlayTypeCn(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF));
        rqspf.put("data", sqspfRecommend);
        res.add(rqspf);

        Map<String, Object> asia = new HashMap<>();
        List<Map<String, Object>> asiaRecommend = getRecommendFromRedis(listType, SportsProgramConstant
                .FOOTBALL_PLAY_TYPE_ASIA, null, 5);
        asia.put("playType", SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA);
        asia.put("playName", SportsUtils.getPlayTypeCn(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA));
        asia.put("data", asiaRecommend);
        res.add(asia);

        boolean noData = false;
        if ((asiaRecommend == null || asiaRecommend.size() == 0) && (spfRecommend == null || spfRecommend.size() ==
                0) && (sqspfRecommend == null || sqspfRecommend.size() == 0)) {
            noData = true;
            res = null;
        }

        if (listType.equals(SportsProgramConstant.SPORT_RECOMMEND_LIST_HOT) && !noData) {
            String key = RedisConstant.getSportSocialIndexHotRecommend();
            redisService.kryoSetEx(key, 259200, res);
        }
        return res;
    }

    private boolean checkRecommendIfRebuild(UserSportSocialRecommend recommend, Integer playType, Integer listType) {
        if (recommend == null || recommend.getPlayType() == null) {
            return Boolean.FALSE;
        }
        if (!recommend.getPlayType().equals(playType)) {
            return Boolean.FALSE;
        }
        if (recommend.getPrice() != null && recommend.getPrice() > 0L) {
            if (listType.equals(SportsProgramConstant.SPORT_RECOMMEND_LIST_FREE)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    private boolean checkRecommendIfHotRecommend(UserSportSocialRecommend userRecommend) {
        if (userRecommend == null || userRecommend.getUserId() == null) {
            return false;
        }
        String redisKey = RedisConstant.getSportSocialRankKey(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM,
                3);
        String profitRedisKey = RedisConstant.getSportSocialRankKey(SportsProgramConstant
                .SPORT_SOCIAL_RANK_TYPE_PROFIT, 3);
        String continueRedisKey = RedisConstant.getSportSocialRankKey(SportsProgramConstant
                .SPORT_SOCIAL_RANK_TYPE_CONTINUE, 3);

        Long rank = redisService.kryoZRevRank(redisKey, userRecommend.getUserId());
        if (rank != null && rank < 500) {
            return true;
        }
        Long profitRank = redisService.kryoZRevRank(profitRedisKey, userRecommend.getUserId());
        if (profitRank != null && profitRank < 500) {
            return true;
        }
        Long continueRank = redisService.kryoZRevRank(continueRedisKey, userRecommend.getUserId());
        if (continueRank != null && continueRank < 500) {
            return true;
        }
        return false;
    }

    private String getSampleRecommendInfo(String recommendInfo, Integer playType) {
        StringBuffer result = new StringBuffer(SportsUtils.getPlayTypeCn(200, playType) + " ");
        Map<Integer, String> recommendOptionMap = SportsUtils.getUserRecommendOptionMap(recommendInfo);
        for (Integer key : recommendOptionMap.keySet()) {
            result.append(SportsUtils.getItemCn(200, playType, key + ""));
            result.append(":");
            result.append(recommendOptionMap.get(key));
            result.append(" ");
        }
        return result.toString();
    }
}
