package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.IndexMatchRecommendDao;
import com.mojieai.predict.dao.MatchInfoDao;
import com.mojieai.predict.dao.MatchTagDao;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.bo.ListMatchInfo;
import com.mojieai.predict.entity.bo.PersistTagMatchInfoModel;
import com.mojieai.predict.entity.po.MatchInfo;
import com.mojieai.predict.entity.po.MatchTag;
import com.mojieai.predict.enums.TagMatchEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.MatchInfoService;
import com.mojieai.predict.service.ThirdHttpService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class MatchInfoServiceImpl implements MatchInfoService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private IndexMatchRecommendDao indexMatchRecommendDao;
    @Autowired
    private MatchInfoDao matchInfoDao;
    @Autowired
    private MatchTagDao matchTagDao;
    @Autowired
    private MatchInfoService matchInfoService;

    @Override
    public TreeSet<Map<String, Object>> getAllMatchInfoFromRedis() {
        String key = RedisConstant.getAllMatchesKey(200);
        TreeSet<Map<String, Object>> result = redisService.kryoGet(key, TreeSet.class);
        if (result == null || result.isEmpty()) {
            result = rebuildMatchInfoRedis();
        }
        return result;
    }

    @Override
    public TreeSet<Map<String, Object>> rebuildMatchInfoRedis() {
        //1.获取所有的比赛
        TreeSet<Map<String, Object>> listMatchInfos = getAllMathInfo();
        //2.将比赛存至redis
        String key = RedisConstant.getAllMatchesKey(200);
        redisService.kryoSetEx(key, 172800, listMatchInfos);
        return listMatchInfos;
    }

    /********  db **********/
    @Override
    public Map<String, Object> getWorldCupMatchInfo(Integer tagId) {
        //        Map<String, List<Map<String, Object>>> matchDateInfo = getLatestMatchesByDate("", "");

        Map<String, Object> result = new HashMap<>();
        Map<String, List<Map<String, Object>>> matchDateInfo = getMatchDateInfo(tagId);

        List<Map<String, Object>> latestMatches = new ArrayList<>();
        Map<String, List<Map<String, Object>>> futureMatches = new HashMap<>();
        Integer minDateKey = null;
        if (matchDateInfo != null) {
            for (String key : matchDateInfo.keySet()) {
                if (minDateKey == null || minDateKey > Integer.valueOf(key)) {
                    minDateKey = Integer.valueOf(key);
                }
            }
            for (String key : matchDateInfo.keySet()) {
                if (latestMatches.size() == 0 && Integer.valueOf(key).equals(minDateKey)) {
                    latestMatches = matchDateInfo.get(key);
                } else {
                    futureMatches.put(key, matchDateInfo.get(key));
                }
            }
        }

        result.put("latestMatches", packageLatestMatches(minDateKey, latestMatches));
        result.put("futureMatches", packageFutureMatches(futureMatches));
        return result;
    }

    @Override
    public void buildNewMatchTagTimeLine(Integer tagId) {
        MatchTag matchTag = matchTagDao.getMatchTag(tagId);
        if (matchTag == null) {
            log.error("buildNewMatchTagTimeLine error tagId:" + tagId + " not exist");
            return;
        }
        //清除历史
        redisService.del(RedisConstant.getTagMatchListKey(tagId));
        //build
        List<Integer> matchInfoId = matchInfoDao.getAllTagMatchId();
        for (Integer matchId : matchInfoId) {
            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(matchId);
            if (detailMatchInfo == null) {
                continue;
            }

            if (SportsUtils.checkMatchBelongsToTag(matchTag, detailMatchInfo)) {
                PersistTagMatchInfoModel persistTagMatchInfoModel = new PersistTagMatchInfoModel(matchId, matchTag
                        .getTagName(), detailMatchInfo.getEndTime(), detailMatchInfo.getMatchDate());
                saveTagMatchInfo(persistTagMatchInfoModel);
            }
        }
    }

    @Override
    public Map<String, Object> getTagList() {
        String matchTagKey = RedisConstant.getMatchTagKey();
        Map<String, Object> result = redisService.kryoGet(matchTagKey, HashMap.class);
        if (result == null || result.isEmpty()) {
            result = rebuildMatchTagList();
        }
        return result;
    }

    private Map<String, Object> rebuildMatchTagList() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tagList = new ArrayList<>();
        Map<String, Object> followMatch = new HashMap<>();
        followMatch.put("tagId", SportsProgramConstant.MATCH_TAG_USER_FOLLOW);
        followMatch.put("tagName", "关注");
        tagList.add(followMatch);

        List<MatchTag> matchTags = matchTagDao.getAllMatchTag();
        if (matchTags != null && matchTags.size() > 0) {
            for (MatchTag tag : matchTags) {
                Map<String, Object> tempMap = new HashMap<>();
                tempMap.put("tagId", tag.getTagId());
                tempMap.put("tagName", tag.getTagName());
                tagList.add(tempMap);
            }
        }
        result.put("tagList", tagList);
        String matchTagKey = RedisConstant.getMatchTagKey();
        redisService.kryoSetEx(matchTagKey, 604800, result);
        return result;
    }

    @Override
    public Map<String, Object> getTagMatches(Integer tagId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> matches = new ArrayList<>();

        List<MatchInfo> matchInfos = matchInfoDao.getAllNoStartMatchInfo(tagId);
        if (matchInfos != null && matchInfos.size() > 0) {
            for (MatchInfo matchInfo : matchInfos) {
                if (!SportsUtils.matchInfoContainsTagId(matchInfo, tagId)) {
                    continue;
                }
                Map<String, Object> temp = packageManualSetTagMatchMap(matchInfo);
                if (temp != null) {
                    matches.add(temp);
                }
            }
        }

        result.put("matches", matches);
        return result;
    }

    @Override
    public Map<String, Object> getTimeLineTagMatches(Integer tagId, Long userId, Long lastHistoryId, Long
            lastFutureId) {
        TagMatchEnum tagMatchEnum = TagMatchEnum.getTagMatchEnum(tagId);
        if (tagMatchEnum != null) {
            return tagMatchEnum.getTagMatchList(redisService, thirdHttpService, matchInfoService, lastHistoryId,
                    lastFutureId, userId);
        }
        return null;
    }

    @Override
    public void rebuildTagMatchListTimeLine() {
        List<MatchInfo> matchInfoList = matchInfoDao.getAllTagMatchInfo();
        if (matchInfoList == null || matchInfoList.size() == 0) {
            return;
        }
        for (TagMatchEnum tme : TagMatchEnum.values()) {
            redisService.del(RedisConstant.getTagMatchListKey(tme.getTagId()));
        }
        for (MatchInfo matchInfo : matchInfoList) {
            if (StringUtils.isBlank(matchInfo.getMatchTagId())) {
                continue;
            }
            String[] tagIdArr = matchInfo.getMatchTagId().split(CommonConstant.COMMA_SPLIT_STR);
            for (String tagId : tagIdArr) {
                saveTagMatches2TimeLine(matchInfo, Integer.valueOf(tagId), null);
            }
        }
    }

    @Override
    public Boolean saveMatchInfo2Redis(MatchInfo matchInfo) {
        String matchInfoKey = RedisConstant.getMatchInfoKey(matchInfo.getMatchId());
        redisService.del(matchInfoKey);
        redisService.kryoSetEx(matchInfoKey, 604800, matchInfo);
        return Boolean.TRUE;
    }

    @Override
    public MatchInfo getMatchInfoFromRedis(Integer matchId) {
        MatchInfo matchInfo = redisService.kryoGet(matchId + "", MatchInfo.class);
        if (matchInfo == null) {
            matchInfo = matchInfoDao.getMatchInfoByMatchId(matchId, false);
            if (matchInfo != null) {
                saveMatchInfo2Redis(matchInfo);
            }
        }
        return matchInfo;
    }

    @Override
    public Boolean saveTagMatches2TimeLine(MatchInfo matchInfo, Integer tagId, Long userId) {
        if (matchInfo == null || tagId == null) {
            return Boolean.FALSE;
        }
        String key = RedisConstant.getTagMatchListKey(tagId);
        if (tagId.equals(SportsProgramConstant.MATCH_TAG_USER_FOLLOW)) {
            key = RedisConstant.getUserFollowMatchListKey(tagId, userId);
        }

        Long score = matchInfo.getMatchTime().getTime() * 100 + Long.valueOf(matchInfo.getMatchId());
        redisService.kryoZAddSet(key, score, matchInfo.getMatchId());
        return Boolean.TRUE;
    }

    @Override
    public List<Map<String, Object>> getFocusMatches() {
        String focusMatchKey = RedisConstant.getFocusMatchesKey();
        List<Map<String, Object>> matches = redisService.kryoGet(focusMatchKey, ArrayList.class);
        if (matches == null || matches.size() == 0) {
            matches = rebuildFocusMatches();
        }
        return matches;
    }

    @Override
    public List<Map<String, Object>> rebuildFocusMatches() {
        String focusMatchKey = RedisConstant.getFocusMatchesKey();
        redisService.del(focusMatchKey);
        List<MatchInfo> matchInfoList = matchInfoDao.getMatchInfoByTagId(SportsProgramConstant
                .MATCH_TAG_WISDOM_RECOMMEND);
        List<Map<String, Object>> focusMatches = new ArrayList<>();
        if (matchInfoList != null) {
            for (MatchInfo matchInfo : matchInfoList) {
                Map<String, Object> tempMap = packageFocusMatchInfo(matchInfo);
                if (tempMap != null) {
                    focusMatches.add(tempMap);
                }
            }
        }
        focusMatches = CommonUtil.CollectionsSortedByWeight(focusMatches, "weight");

        redisService.kryoSetEx(focusMatchKey, 604800, focusMatches);
        return focusMatches;
    }

    @Override
    public Map<String, Object> focusMatchHandler(Integer matchId, Integer weight, Integer ifFocus) {
        Map<String, Object> result = new HashMap<>();
        MatchInfo matchInfo = matchInfoDao.getMatchInfoByMatchId(matchId, false);
        if (matchInfo == null) {
            result.put("code", ResultConstant.ERROR);
            result.put("msg", "赛事不存在");
            return result;
        }
        if (!matchInfo.getMatchTagId().contains(SportsProgramConstant.MATCH_TAG_OPERATE_TYPE_DESC + "")) {
            result.put("code", ResultConstant.ERROR);
            result.put("msg", "焦点赛事应先是智慧推荐赛事");
            return result;
        }

        String oldRemark = matchInfo.getRemark();
        Map<String, Object> focusMap = new HashMap<>();
        focusMap.put("weight", weight);
        focusMap.put("ifFocus", ifFocus);

        if (matchInfoDao.saveMatchRemark(matchId, oldRemark, MatchInfo.remarkAddInfo(focusMap, oldRemark)) > 0) {
            result.put("code", ResultConstant.SUCCESS);
            result.put("msg", "设置成功");
            return result;
        }
        result.put("code", ResultConstant.ERROR);
        result.put("msg", "更新操作失败");
        return result;
    }

    private Map<String, Object> packageFocusMatchInfo(MatchInfo matchInfo) {
        if (matchInfo == null) {
            return null;
        }
        if (StringUtils.isBlank(matchInfo.getRemark())) {
            return null;
        }
        Map<String, Object> remarkMap = JSONObject.parseObject(matchInfo.getRemark(), HashMap.class);
        if (!remarkMap.containsKey("ifFocus") || !remarkMap.containsKey("weight") || !Integer.valueOf(remarkMap.get
                ("ifFocus").toString()).equals(1)) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(matchInfo.getMatchId());

        result.put("weight", remarkMap.get("weight"));
        result.put("matchDesc", detailMatchInfo.getMatchName());
        result.put("hostName", detailMatchInfo.getHostName());
        result.put("hostImg", detailMatchInfo.getHostImg());
        result.put("awayName", detailMatchInfo.getAwayName());
        result.put("awayImg", detailMatchInfo.getAwayImg());
        result.put("matchTime", detailMatchInfo.getMatchTime());
        result.put("tags", SportsUtils.getMatchTags(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF, detailMatchInfo
                .getTag()));
        result.put("recommendCount", SportsUtils.getMatchPredictCount(redisService, String.valueOf(matchInfo
                .getMatchId())) + "人推荐");
        result.put("jumpUrl", SportsUtils.getMatchBottomPageJumpUrl(detailMatchInfo.getMatchId(), SportsProgramConstant
                .MATCH_BOTTOM_PAGE_PREDICT));
        return result;
    }

    private Map<String, Object> packageManualSetTagMatchMap(MatchInfo matchInfo) {
        if (matchInfo == null || matchInfo.getMatchId() == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        String matchId = matchInfo.getMatchId() + "";
        DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchIds(matchId).get(matchId);
        if (detailMatchInfo == null) {
            return null;
        }
        Integer status = 0;
        String[] tagIds = matchInfo.getMatchTagId().split(",");
        List<Integer> matchTagIdList = new ArrayList<>();
        for (String tagId : tagIds) {
            matchTagIdList.add(Integer.valueOf(tagId));
        }
        if (matchInfo.getMatchTagId() != null && matchTagIdList.contains(SportsProgramConstant.MATCH_TAG_WISDOM_RECOMMEND)) {
            status = 1;
        }
        Integer focusWeight = 0;
        Integer ifFocus = 0;
        if (StringUtils.isNotBlank(matchInfo.getRemark())) {
            Map<String, Object> remarkMap = JSONObject.parseObject(matchInfo.getRemark());
            if (remarkMap.containsKey("ifFocus")) {
                ifFocus = Integer.valueOf(remarkMap.get("ifFocus").toString());
            }
            if (remarkMap.containsKey("weight")) {
                focusWeight = Integer.valueOf(remarkMap.get("weight").toString());
            }
        }

        result.put("matchId", detailMatchInfo.getMatchId());
        result.put("matchTime", DateUtil.formatTime(detailMatchInfo.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
        result.put("team", detailMatchInfo.getHostName() + " VS " + detailMatchInfo.getAwayName());
        result.put("matchName", detailMatchInfo.getMatchName());
        result.put("status", status);
        result.put("focusWeight", focusWeight);
        result.put("ifFocus", ifFocus);
        return result;
    }

    @Override
    public Boolean saveTagMatchInfo(PersistTagMatchInfoModel tagMatchInfoModel) {
        if (tagMatchInfoModel == null || tagMatchInfoModel.getMatchId() == null) {
            return Boolean.FALSE;
        }
        Boolean saveFlag = Boolean.FALSE;
        String newTagIds = extractTagIdFromPersistTagModel(tagMatchInfoModel);
        MatchInfo matchInfo = matchInfoDao.getMatchInfoByMatchId(tagMatchInfoModel.getMatchId(), false);

        if (matchInfo != null) {
            newTagIds = combineMatchTagIds(SportsProgramConstant.MATCH_TAG_OPERATE_TYPE_ADD, matchInfo.getMatchTagId
                    (), newTagIds);
            Integer updateRes = matchInfoDao.updateMatchTagId(tagMatchInfoModel.getMatchId(), matchInfo
                    .getMatchTagId(), newTagIds);
            if (updateRes > 0) {
                saveFlag = Boolean.TRUE;
                matchInfo.setMatchTagId(newTagIds);
            }
        } else {
            matchInfo = new MatchInfo(tagMatchInfoModel.getMatchId(), newTagIds, tagMatchInfoModel.getMatchTime());
            if (matchInfoDao.insert(matchInfo) > 0) {
                saveFlag = Boolean.TRUE;
            }
        }

        if (saveFlag) {
            if (StringUtils.isNotBlank(newTagIds)) {
                if (saveMatchInfo2Redis(matchInfo)) {
                    String[] tagIdArr = newTagIds.split(CommonConstant.COMMA_SPLIT_STR);
                    for (String tagId : tagIdArr) {
                        saveTagMatches2TimeLine(matchInfo, Integer.valueOf(tagId), null);
                    }
                }
            }
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public Boolean addTag2MatchInfo(Integer matchId, String tagIds, Integer operateType) {
        if (matchId == null || StringUtils.isBlank(tagIds)) {
            return Boolean.FALSE;
        }
        MatchInfo matchInfo = matchInfoDao.getMatchInfoByMatchId(matchId, false);
        String oldTags = matchInfo.getMatchTagId();

        if (matchInfoDao.updateMatchTagId(matchId, oldTags, combineMatchTagIds(operateType, oldTags, tagIds)) > 0) {
            saveTagMatches2TimeLine(matchInfo, SportsProgramConstant.MATCH_TAG_WISDOM_RECOMMEND, null);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Integer saveTagMatchHit2Remark(String matchId, Integer rightCount) {
        if (StringUtils.isBlank(matchId) || rightCount == null || rightCount == 0) {
            return 0;
        }
        Integer matchIdInt = Integer.valueOf(matchId);
        MatchInfo matchInfo = matchInfoDao.getMatchInfoByMatchId(matchIdInt, false);
        if (matchInfo == null) {
            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchId(matchIdInt);
            if (detailMatchInfo == null) {
                return 0;
            }
            matchInfo = new MatchInfo(matchIdInt, SportsProgramConstant.MATCH_TAG_ALL_MATCH + "", detailMatchInfo
                    .getEndTime());
            matchInfoDao.insert(matchInfo);
        }
        Map<String, Object> remarkMap = null;
        if (StringUtils.isNotBlank(matchInfo.getRemark())) {
            remarkMap = JSONObject.parseObject(matchInfo.getRemark(), HashMap.class);
        } else {
            remarkMap = new HashMap<>();
        }
        remarkMap.put("hitPersonCount", rightCount);
        return matchInfoDao.saveMatchRemark(matchIdInt, matchInfo.getRemark(), JSONObject.toJSONString(remarkMap));
    }

    private String combineMatchTagIds(Integer operateType, String oldTagIds, String newTagIds) {
        if (StringUtils.isBlank(newTagIds)) {
            return oldTagIds;
        }
        if (StringUtils.isBlank(oldTagIds)) {
            if (operateType.equals(SportsProgramConstant.MATCH_TAG_OPERATE_TYPE_ADD)) {
                return newTagIds;
            }
            return null;
        }
        String[] oldTagIdArr = oldTagIds.split(CommonConstant.COMMA_SPLIT_STR);
        String[] newTagIdArr = newTagIds.split(CommonConstant.COMMA_SPLIT_STR);
        if (oldTagIdArr.length == 0 || newTagIdArr.length == 0) {
            throw new IllegalArgumentException("参数未按要求赋值 old:" + oldTagIds + " new:" + newTagIds);
        }
        List<String> oldTagIdList = Arrays.asList(oldTagIdArr);
        Set<String> newTagIdSet = new HashSet<>(oldTagIdList);
        for (String tagId : newTagIdArr) {
            if (operateType.equals(SportsProgramConstant.MATCH_TAG_OPERATE_TYPE_DESC)) {
                newTagIdSet.remove(tagId);
                continue;
            }
            newTagIdSet.add(tagId);
        }
        if (newTagIdSet.size() == 0) {
            return null;
        }

        return String.join(CommonConstant.COMMA_SPLIT_STR, newTagIdSet);
    }

    private String extractTagIdFromPersistTagModel(PersistTagMatchInfoModel tagMatchInfoModel) {
        Set<String> matchTagIdSet = new HashSet<>();
        matchTagIdSet.add(SportsProgramConstant.MATCH_TAG_ALL_MATCH + "");

        MatchTag matchTag = matchTagDao.getMatchTagByTagName(tagMatchInfoModel.getMatchName());
        if (matchTag != null) {
            matchTagIdSet.add(matchTag.getTagId() + "");
        }
        if (StringUtils.isNotBlank(tagMatchInfoModel.getMatchDate()) && tagMatchInfoModel.getMatchDate
                ().contains("周")) {
            matchTagIdSet.add(SportsProgramConstant.MATCH_TAG_SPORTTERY + "");
        }

        return String.join(CommonConstant.COMMA_SPLIT_STR, matchTagIdSet);
    }

    private List<Map<String, Object>> packageFutureMatches(Map<String, List<Map<String, Object>>> futureMatches) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (String key : futureMatches.keySet()) {
            Map<String, Object> temp = packageLatestMatches(Integer.valueOf(key), futureMatches.get(key));
            if (temp != null) {
                result.add(temp);
            }
        }
        if (result.size() > 0) {
            Collections.sort(result, Comparator.comparing(p -> ((Integer) p.get("matchDate"))));
            Integer end = result.size() <= 1 ? result.size() : 2;
            result = result.subList(0, end);
        }
        return result;
    }

    private Map<String, Object> packageLatestMatches(Integer date, List<Map<String, Object>> dateMatchList) {
        Map<String, Object> result = new HashMap<>();

        if (date == null) {
            return result;
        }
        String title = DateUtil.formatTime(DateUtil.formatString(date + "", "yyyyMMdd"), DateUtil.DATE_FORMAT_M_D);
        if (date.equals(Integer.valueOf(DateUtil.getCurrentDay()))) {
            title = title + "(今日)";
        }

        result.put("title", title);
        result.put("matchDate", date);
        result.put("matchInfos", dateMatchList);
        return result;
    }

    private Map<String, Object> convert2MatchTagInfoMap(DetailMatchInfo detailMatchInfo) {
        Map<String, Object> result = new HashMap<>();

        String matchId = detailMatchInfo.getMatchId();

        Integer single = 0;
        if (StringUtils.isNotBlank(detailMatchInfo.getTag()) && detailMatchInfo.getTag().equals("竞彩单关")) {
            single = 1;
        }
        result.put("matchName", detailMatchInfo.getMatchName());
        result.put("single", single);
        result.put("hostName", detailMatchInfo.getHostName());
        result.put("awayName", detailMatchInfo.getAwayName());
        result.put("hostImg", SportsUtils.dealMatchImg(detailMatchInfo.getHostImg()));
        result.put("awayImg", SportsUtils.dealMatchImg(detailMatchInfo.getAwayImg()));
        result.put("matchId", matchId);
        result.put("matchDate", getMatchGroup(matchId) + " " + DateUtil.formatTime(detailMatchInfo.getEndTime(), DateUtil.DATE_FORMAT_HHMM));
        result.put("matchDesc", SportsUtils.getMatchPredictInfo(redisService, matchId));
        result.put("jumpUrl", SportsUtils.getMatchBottomPageJumpUrl(matchId, SportsProgramConstant
                .MATCH_BOTTOM_PAGE_PREDICT));
        return result;
    }

    public String getMatchGroup(String matchId) {
        String result = "";
        String groupStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant.WORLD_CUP_GROUP_NAME, "");
        if (StringUtils.isNotBlank(groupStr)) {
            Map<String, Object> matchNameMap = JSONObject.parseObject(groupStr, HashMap.class);
            result = matchNameMap.containsKey(matchId) ? matchNameMap.get(matchId).toString() : "";
        }
        return result;
    }

    private Map<String, List<Map<String, Object>>> getMatchDateInfo(Integer tagId) {
        List<MatchInfo> matchInfos = matchInfoDao.getMatchInfoByTagId(tagId);
        if (matchInfos == null || matchInfos.size() == 0) {
            return null;
        }
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        for (MatchInfo matchInfo : matchInfos) {
            if (!SportsUtils.matchInfoContainsTagId(matchInfo, tagId)) {
                continue;
            }
            String key = DateUtil.formatTime(matchInfo.getMatchTime(), "yyyyMMdd");
            List<Map<String, Object>> dateMatchList = null;
            if (result.containsKey(key)) {
                dateMatchList = result.get(key);
            } else {
                dateMatchList = new ArrayList<>();
            }
            DetailMatchInfo detailMatchInfo = thirdHttpService.getMatchMapByMatchIds(matchInfo.getMatchId() + "")
                    .get(matchInfo.getMatchId() + "");
            Map<String, Object> tempMatchMap = convert2MatchTagInfoMap(detailMatchInfo);
            if (tempMatchMap != null) {
                dateMatchList.add(tempMatchMap);
                result.put(key, dateMatchList);
            }
        }
        return result;
    }

    private TreeSet<Map<String, Object>> getAllMathInfo() {
        List<Map<String, Object>> matches = new ArrayList<>();
        Timestamp lastDate = DateUtil.getCurrentTimestamp();

        String date = DateUtil.formatTime(lastDate, "yyyyMMdd");
        String lastMatchId = null;
        String tempLastMatchId = null;
        String tempLastDate = "";

        Map<String, Object> temp = thirdHttpService.getNotStartMatchResult(date, lastMatchId, null);
        tempLastDate = new String(date);
        date = temp.get("lastDate").toString();
        tempLastMatchId = "";
        while (temp != null && !temp.isEmpty()) {
            if (!temp.containsKey("lastMatchId")) {
                return null;
            }
            try {
                lastMatchId = temp.get("lastMatchId").toString();
            }catch (Exception e){
                log.error("lastMatchId is null,temp. toString is ",temp.toString());
            }
            if (StringUtils.isNotEmpty(tempLastDate) && tempLastDate.equals(date) && lastMatchId.equals
                    (tempLastMatchId)) {
                break;
            }
            List<Map<String, Object>> matchInfos = (List<Map<String, Object>>) temp.get("dateMatchList");
            if (matchInfos.size() > 0) {
                matches.addAll(matchInfos);
            }
            temp = thirdHttpService.getNotStartMatchResult(date, lastMatchId, null);
            tempLastDate = new String(date);
            tempLastMatchId = StringUtils.isBlank(lastMatchId) ? "" : new String(lastMatchId);
            date = temp.get("lastDate").toString();
            lastMatchId = temp.get("lastMatchId").toString();
        }

        //2.包装所有比赛
        TreeSet<Map<String, Object>> allMatch = new TreeSet<>(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Timestamp o2Time = DateUtil.formatString(o2.get("matchTimeDetail").toString(), "yyyy-MM-dd HH:mm:ss");
                Timestamp o1Time = DateUtil.formatString(o1.get("matchTimeDetail").toString(), "yyyy-MM-dd HH:mm:ss");
                Long result = DateUtil.getDiffSeconds(o2Time, o1Time);
                return result.intValue();
            }
        });

        for (Map<String, Object> matchMap : matches) {
            List<Map<String, Object>> matchInfos = (List<Map<String, Object>>) matchMap.get("match");
            if (matchInfos != null && matchInfos.size() > 0) {
                allMatch.addAll(matchInfos);
            }
        }
        return allMatch;
    }

//    private Map<String, List<Map<String, Object>>> getLatestMatchesByDate(String lastDate, String lastMatchId) {
//        Map<String, List<Map<String, Object>>> result = new HashMap<>();
//        String matchInfoStr = thirdHttpService.getMatchListByDate(lastDate, lastMatchId);
//        if (StringUtils.isNotBlank(matchInfoStr)) {
//            Map<String, Object> match = JSONObject.parseObject(matchInfoStr, HashMap.class);
//            List<Map<String, Object>> resp = (List<Map<String, Object>>) match.get("resp");
//            if (resp == null || resp.size() == 0) {
//                return null;
//            }
//            Map<String, List<Map<String, Object>>> matchMap = new HashMap<>();
//            for (Map<String, Object> dateMatch : resp) {
//                String key = dateMatch.get("date").toString();
//                List<Map<String, Object>> temp = null;
//                if (result.containsKey(key)) {
//                    temp = result.get(key);
//                } else {
//                    temp = new ArrayList<>();
//                }
//                List<Map<String, Object>> data = (List<Map<String, Object>>) dateMatch.get("data");
//                for (Map<String, Object> matchInfo : data) {
//                    if (matchInfo.get("match_desc").toString().equals("世界杯")) {
//                        temp.add(matchInfo);
//                    }
//                }
//                if (temp != null && temp.size() > 0) {
//                    result.put(key, temp);
//                }
//                lastDate = key;
//                lastMatchId = data.get(data.size() - 1).get("match_id").toString();
//            }
//            if (result.size() < 3) {
//                Map<String, List<Map<String, Object>>> deepLatest = getLatestMatchesByDate(lastDate, lastMatchId);
//                if (deepLatest != null && deepLatest.size() > 0) {
//                    for (String key : deepLatest.keySet()) {
//                        List<Map<String, Object>> temp = null;
//                        if (result.containsKey(key)) {
//                            temp = result.get(key);
//                            temp.addAll(deepLatest.get(key));
//                        } else {
//                            temp = deepLatest.get(key);
//                        }
//                        result.put(key, temp);
//                    }
//                }
//            }
//        }
//        return result;
//    }

//    private List<Map<String, Object>> convertWorldCupMatchInfo(List<Map<String, Object>> dateMatchList) {
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        for (Map<String, Object> match : dateMatchList) {
//            Map<String, Object> temp = convertHttpData2Activity(match);
//            if (temp != null) {
//                result.add(temp);
//            }
//        }
//        return result;
//    }

//    private Map<String, Object> convertHttpData2Activity(Map<String, Object> match) {
//        Map<String, Object> result = new HashMap<>();
//
//        String matchId = match.get("match_id").toString();
//        String jumpUrl = "mjlottery://mjnative?page=footballMatchDetail&matchId=" + matchId + "&selectIndex=0";
//
//        Timestamp matchTime = DateUtil.formatString(match.get("match_time_detail").toString(), "yyyy-MM-dd HH:mm:ss");
//
//        result.put("hostName", match.get("host_name"));
//        result.put("awayName", match.get("away_name"));
//        result.put("hostImg", SportsUtils.dealMatchImg(match.get("host_team_image").toString()));
//        result.put("awayImg", SportsUtils.dealMatchImg(match.get("away_team_image").toString()));
//        result.put("matchId", matchId);
//        result.put("matchDate", getMatchGroup(matchId) + " " + DateUtil.formatTime(matchTime, DateUtil.DATE_FORMAT_HHMM));
//        result.put("matchDesc", getWorldCupMatchPredictInfo(matchId));
//        result.put("jumpUrl", jumpUrl);
//        return result;
//    }
}
