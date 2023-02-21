package com.mojieai.predict.enums;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.MatchInfo;
import com.mojieai.predict.entity.vo.TagMatchDetailVo;
import com.mojieai.predict.entity.vo.TagMatchVo;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.MatchInfoService;
import com.mojieai.predict.service.ThirdHttpService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum TagMatchEnum {
    WISDOM_RECOMMEND("智慧推荐", SportsProgramConstant.MATCH_TAG_WISDOM_RECOMMEND) {

    }, WORLD_CUP("世界杯", SportsProgramConstant.MATCH_TAG_FIFA_WORLD_CUP) {

    }, PREMIER_LEAGUE("英超", SportsProgramConstant.MATCH_TAG_PREMIER_LEAGUE) {

    }, SERIE_A("意甲", SportsProgramConstant.MATCH_TAG_SERIE_A) {

    }, BUNDESLIGA("德甲", SportsProgramConstant.MATCH_TAG_BUNDESLIGA) {

    }, LA_LIGA("西甲", SportsProgramConstant.MATCH_TAG_LA_LIGA) {

    }, LIGUE_1("法甲", SportsProgramConstant.MATCH_TAG_LIGUE_1) {

    }, EUROPEAN_NATIONS_CUP("欧洲杯", SportsProgramConstant.MATCH_TAG_EUROPEAN_NATIONS_CUP) {

    }, EUROPEAN_CHAMPION_CLUBS_CUP("欧冠", SportsProgramConstant.MATCH_TAG_EUROPEAN_CHAMPION_CLUBS_CUP) {

    }, ALL_MATCH("全部", SportsProgramConstant.MATCH_TAG_ALL_MATCH) {

    }, USER_FOLLOW("关注", SportsProgramConstant.MATCH_TAG_USER_FOLLOW) {

    }, UEFA("欧罗巴", SportsProgramConstant.MATCH_TAG_UEFA) {

    }, SPORTTERY("竞彩", SportsProgramConstant.MATCH_TAG_SPORTTERY);


    private String tagName;
    private Integer tagId;

    TagMatchEnum(String tagName, Integer tagId) {
        this.tagName = tagName;
        this.tagId = tagId;
    }

    public String getTagName() {
        return this.tagName;
    }

    public Integer getTagId() {
        return this.tagId;
    }

    public static TagMatchEnum getTagMatchEnum(Integer tagId) {
        if (tagId == null) {
            return null;
        }
        for (TagMatchEnum tme : TagMatchEnum.values()) {
            if (tme.getTagId().equals(tagId)) {
                return tme;
            }
        }
        return null;
    }

    public Map<String, Object> getTagMatchList(RedisService redisService, ThirdHttpService thirdHttpService,
                                               MatchInfoService matchInfoService, Long lastHistoryId, Long
                                                       lastFutureId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        Long currentTime = System.currentTimeMillis() * 100;
        Integer pageSize = 20;
        Integer historyPageSize = 10;
        Boolean historyHasNext = false;
        Boolean futureHasNext = false;
        Boolean firstPage = false;
        if (lastHistoryId == null && lastFutureId == null) {
            firstPage = true;
        }

        List<MatchInfo> matchInfoList = null;
        if (lastFutureId == null) {
            Long lastIndex = lastHistoryId;
            if (lastHistoryId == null) {
                lastIndex = currentTime;
            }
            matchInfoList = getHistoryMatchIncludeEndMatch(redisService, thirdHttpService, matchInfoService, lastIndex,
                    historyPageSize + 1, firstPage, userId);
        }

        if (matchInfoList == null) {
            matchInfoList = new ArrayList<>();
        }
        Collections.reverse(matchInfoList);
        if (matchInfoList.size() > historyPageSize) {
            historyHasNext = true;
            matchInfoList.remove(0);
        }

        Integer futureCount = pageSize - matchInfoList.size();
        if (futureCount > 0 && lastHistoryId == null) {
            Long lastIndex = lastFutureId;
            if (lastFutureId == null) {
                lastIndex = currentTime;
            }

            List<MatchInfo> futureMatch = getFutureMatch(redisService, matchInfoService, lastIndex, futureCount + 1,
                    firstPage, userId);
            if (futureMatch != null && futureMatch.size() > 0) {
                if (futureMatch.size() > futureCount) {
                    futureHasNext = true;
                    futureMatch.remove(futureCount);
                }
                matchInfoList.addAll(futureMatch);
            }
        } else {
            List<MatchInfo> futureMatch = getFutureMatch(redisService, matchInfoService, lastFutureId, 1, firstPage,
                    userId);
            if (futureMatch != null && futureMatch.size() > 0) {
                futureHasNext = true;
            }
        }

        TreeSet<TagMatchVo> matchVos = dealWithMatchInfo2Show(matchInfoList, thirdHttpService, redisService,
                firstPage, userId);
        if (matchVos != null && matchVos.size() > 0) {
            TreeSet<TagMatchDetailVo> tagMatchDetailVos = matchVos.first().getTagMatchDetailVo();
            if (tagMatchDetailVos != null && tagMatchDetailVos.size() > 0) {
                TagMatchDetailVo tagMatchDetailVo = tagMatchDetailVos.first();
                lastHistoryId = tagMatchDetailVo.getMatchTime().getTime() * 100 + Long.valueOf(tagMatchDetailVo
                        .getMatchId());
            }

            TreeSet<TagMatchDetailVo> lastTagMatchDetailVos = matchVos.last().getTagMatchDetailVo();
            if (lastTagMatchDetailVos != null && lastTagMatchDetailVos.size() > 0) {
                TagMatchDetailVo tagMatchDetailVo = lastTagMatchDetailVos.last();
                lastFutureId = tagMatchDetailVo.getMatchTime().getTime() * 100 + Long.valueOf(tagMatchDetailVo
                        .getMatchId());
            }
        }

        result.put("historyHasNext", historyHasNext);
        result.put("futureHasNext", futureHasNext);
        result.put("lastHistoryId", lastHistoryId);
        result.put("lastFutureId", lastFutureId);
        result.put("matches", matchVos);
        return result;
    }

    private TreeSet<TagMatchVo> dealWithMatchInfo2Show(List<MatchInfo> matches, ThirdHttpService
            thirdHttpService, RedisService redisService, Boolean firstPage, Long userId) {
        TreeSet<TagMatchVo> result = new TreeSet<>();
        if (matches == null || matches.size() == 0) {
            return result;
        }

        Map<String, TreeSet<TagMatchDetailVo>> matchMap = new HashMap<>();
        Integer first = firstPage ? 1 : 0;
        for (int i = matches.size() - 1; i >= 0; i--) {
            MatchInfo matchInfo = matches.get(i);
            if (matchInfo == null) {
                continue;
            }
            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(matchInfo.getMatchId());
            String date = DateUtil.formatTime(matchInfo.getMatchTime(), "yyyyMMdd");
            TreeSet<TagMatchDetailVo> dateMatches = null;
            if (matchMap.containsKey(date)) {
                dateMatches = matchMap.get(date);
            } else {
                dateMatches = new TreeSet<>();
            }

            TagMatchDetailVo matchVo = new TagMatchDetailVo(matchInfo, detailMatchInfo, getUserFollowMatchStatus
                    (redisService, userId, detailMatchInfo), getMatchPredictDesc(redisService, matchInfo,
                    detailMatchInfo));
            if (first == 1 && detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
                first = 0;
                matchVo.setIfFirst(1);
            }
            dateMatches.add(matchVo);

            matchMap.put(date, dateMatches);
        }

        for (String date : matchMap.keySet()) {
            result.add(new TagMatchVo(DateUtil.formatString(date, "yyyyMMdd"), matchMap.get(date)));
        }
        return result;
    }

    private String getMatchPredictDesc(RedisService redisService, MatchInfo matchInfo, DetailMatchInfo detailMatchInfo) {
        String color = "";
        String predictDesc = "";
        if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            String hitDesc = "0";
            color = CommonConstant.COMMON_COLOR_ORIGIN;
            if (StringUtils.isNotBlank(matchInfo.getRemark())) {
                Map<String, Object> remarkMap = JSONObject.parseObject(matchInfo.getRemark(), HashMap.class);
                if (remarkMap != null && remarkMap.containsKey("hitPersonCount")) {
                    hitDesc = remarkMap.get("hitPersonCount").toString();
                }
            }
            predictDesc = hitDesc + "位大神命中";
        } else {
            color = CommonConstant.COMMON_COLOR_BLUE_1;
            predictDesc = SportsUtils.getMatchPredictInfo(redisService, matchInfo.getMatchId() + "");
        }

        return CommonUtil.packageColorHtmlTag2Str(predictDesc, color);
    }

    private Integer getUserFollowMatchStatus(RedisService redisService, Long userId, DetailMatchInfo detailMatchInfo) {
        Set<Integer> matchStatusSet = new HashSet<>();
        matchStatusSet.add(SportsProgramConstant.SPORT_MATCH_STATUS_END);
        matchStatusSet.add(SportsProgramConstant.SPORT_MATCH_STATUS_GOING);
        matchStatusSet.add(SportsProgramConstant.SPORT_MATCH_STATUS_MIDFIELD);

        if (matchStatusSet.contains(detailMatchInfo.getMatchStatus())) {
            return SportsProgramConstant.SPROT_MATCH_USER_FOLLOW_STATUS_STOP;
        }
        if (userId == null) {
            return SportsProgramConstant.SPROT_MATCH_USER_FOLLOW_STATUS_NO;
        }
        String key = RedisConstant.getUserFollowMatchListKey(SportsProgramConstant.MATCH_TAG_USER_FOLLOW, userId);
        if (null == redisService.kryoZScore(key, Integer.valueOf(detailMatchInfo.getMatchId()))) {
            return SportsProgramConstant.SPROT_MATCH_USER_FOLLOW_STATUS_NO;
        }
        return SportsProgramConstant.SPROT_MATCH_USER_FOLLOW_STATUS_YES;
    }

    private List<MatchInfo> getHistoryMatchIncludeEndMatch(RedisService redisService, ThirdHttpService
            thirdHttpService, MatchInfoService matchInfoService, Long lastIndex, int count, Boolean firstPage, Long
                                                                   userId) {
        List<MatchInfo> result = getHistoryMatch(redisService, matchInfoService, lastIndex, count, firstPage, userId);
        if (result == null || result.size() == 0) {
            return null;
        }
        Boolean repeatRequestFlag = true;
        for (MatchInfo matchInfo : result) {
            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(matchInfo.getMatchId());
            if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
                repeatRequestFlag = false;
                break;
            }
            lastIndex = matchInfo.getMatchTime().getTime();
        }
        if (repeatRequestFlag) {
            if (result.size() > 0) {
                firstPage = false;
            }
            List<MatchInfo> tempMatchInfos = getHistoryMatchIncludeEndMatch(redisService, thirdHttpService,
                    matchInfoService, lastIndex, count, firstPage, userId);
            if (tempMatchInfos != null && tempMatchInfos.size() > 0) {
                result.addAll(tempMatchInfos);
            }
        }
        return result;
    }

    private List<MatchInfo> getHistoryMatch(RedisService redisService, MatchInfoService matchInfoService, Long
            lastIndex, Integer count, Boolean firstPage, Long userId) {
        if (lastIndex == null) {
            return null;
        }
        try {
            int x = firstPage ? 0 : 1;
            String matchListKey = RedisConstant.getTagMatchListKey(getTagId());
            if (getTagId().equals(SportsProgramConstant.MATCH_TAG_USER_FOLLOW) && userId != null) {
                matchListKey = RedisConstant.getUserFollowMatchListKey(getTagId(), userId);
            }
            List<Integer> matchIds = redisService.kryoZRevRangeByScoreGet(matchListKey, Long.MIN_VALUE, lastIndex, x,
                    count, Integer.class);
            return getMatchInfoByMatchId(matchIds, matchInfoService);
        } catch (Exception e) {

        }
        return null;
    }

    private List<MatchInfo> getMatchInfoByMatchId(List<Integer> matchIds, MatchInfoService matchInfoService) {
        if (matchIds == null || matchIds.size() == 0) {
            return null;
        }
        List<MatchInfo> result = new ArrayList<>();
        for (Integer matchId : matchIds) {
            MatchInfo matchInfo = matchInfoService.getMatchInfoFromRedis(matchId);
            if (matchInfo != null) {
                result.add(matchInfo);
            }
        }
        return result;
    }

    private List<MatchInfo> getFutureMatch(RedisService redisService, MatchInfoService matchInfoService, Long
            lastIndex, Integer count, Boolean firstPage, Long userId) {
        if (lastIndex == null) {
            return null;
        }
        try {
            int x = firstPage ? 0 : 1;
            String matchListKey = RedisConstant.getTagMatchListKey(getTagId());
            if (getTagId().equals(SportsProgramConstant.MATCH_TAG_USER_FOLLOW) && userId != null) {
                matchListKey = RedisConstant.getUserFollowMatchListKey(getTagId(), userId);
            }
            List<Integer> matchIds = redisService.kryoZRangeByScoreGet(matchListKey, lastIndex, Long.MAX_VALUE, x,
                    count, Integer.class);
            return getMatchInfoByMatchId(matchIds, matchInfoService);
        } catch (Exception e) {

        }
        return null;
    }
}
