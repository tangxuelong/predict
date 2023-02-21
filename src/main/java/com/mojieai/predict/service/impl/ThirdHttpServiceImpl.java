package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.dao.UserSportSocialRecommendDao;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.bo.ListMatchInfo;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SportSocialService;
import com.mojieai.predict.service.ThirdHttpService;
import com.mojieai.predict.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by tangxuelong on 2017/11/7.
 */
@Service
public class ThirdHttpServiceImpl implements ThirdHttpService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UserSportSocialRecommendDao userSportSocialRecommendDao;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private RedisService redisService;


    @Override
    public List<Map<String, Object>> getNotStartMatchList(String lastDate, String lastMatchId, Long userId,
                                                          Map<String, Object> resultMap) {
        if (resultMap == null) {
            resultMap = new HashMap<>();
        }
        String result = getMatchListByDate(lastDate, lastMatchId);

        Map<String, Object> data = JSON.parseObject(result, HashMap.class);
        if (data.get("code") == null || Integer.valueOf(data.get("code").toString()) != 0) {
            log.error("ERROR: get data from mojie is null pls make sure");
            return null;
        }
        List<Map<String, Object>> dateList = (List<Map<String, Object>>) data.get("resp");
        List<Map<String, Object>> dateMatchList = new ArrayList<>();//需要构建的数据

        for (Map<String, Object> date : dateList) {
            Map<String, Object> dateMatch = new HashMap<>(); //需要构建的数据
            List<Map<String, Object>> matchInfoList = (List<Map<String, Object>>) date.get("data");
            // 每场比赛获取到需要的数据
            List<ListMatchInfo> listMatchInfos = new ArrayList<>();//需要构建的数据
            List<Map<String, Object>> listMatchInfoMaps = new ArrayList<>();//需要构建的数据
            for (Map<String, Object> matchInfo : matchInfoList) {
                // 过滤掉已经完场的比赛
                if (Integer.valueOf(matchInfo.get("match_status").toString()).equals(1)) {
                    continue;
                }
                // 用户已经推荐列表
                List<UserSportSocialRecommend> userSportSocialRecommends = null;
                if (userId != null) {
                    userSportSocialRecommends = userSportSocialRecommendDao.getSportSocialRecommendByUserIdMatchId
                            (userId, matchInfo.get("match_id").toString());
                }
                String btnName = "去推荐";
                Integer isRecommend = 0;
                if (null != userSportSocialRecommends && userSportSocialRecommends.size() > 0) {
                    btnName = "已推荐";
                    isRecommend = 1;
                }
                // 比赛状态待定或者取消处理
                Integer matchStatus = Integer.valueOf(matchInfo.get("match_status").toString());
                if (matchInfo.get("match_status_desc").toString().equals("待定") || matchInfo.get("match_status_desc")
                        .toString().equals("取消")) {
                    matchStatus = CommonConstant.FOOTBALL_MATCH_STATUS_CANCEL;
                }
                //单关标签
                String tag = "";
                if (matchInfo.containsKey("tag") && matchInfo.get("tag") != null && matchInfo.get("tag").toString()
                        .equals("竞彩单关")) {
                    tag = matchInfo.get("tag").toString();
                }

                if (matchInfo.get("match_desc") != null && matchInfo.get("match_desc") != null && matchInfo.get
                        ("match_desc").toString().equals("世界杯")) {
                    tag = "竞彩单关";
                }

                ListMatchInfo listMatchInfo = new ListMatchInfo(matchInfo.get("match_id").toString(), matchInfo.get
                        ("match_desc").toString(), matchInfo.get("match_time").toString(), matchInfo.get("match_week")
                        .toString() + matchInfo.get("match_sn")
                        .toString(), matchInfo.get("host_name").toString(), matchInfo.get("away_name").toString(),
                        matchInfo.get("host_team_image").toString(), matchInfo.get("away_team_image").toString(),
                        matchStatus, btnName, isRecommend, DateUtil.formatToTimestamp(matchInfo.get
                        ("match_time_detail").toString(), "yyyy-MM-dd HH:mm:ss"), tag);
                Map matchInfoMao = BeanMapUtil.beanToMap(listMatchInfo);

                matchInfoMao.put("tags", SportsUtils.getMatchTags(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF, tag));
                listMatchInfoMaps.add(matchInfoMao);
                lastDate = date.get("date").toString();
                lastMatchId = listMatchInfo.getMatchId();
            }
            dateMatch.put("title", date.get("date_desc"));
//            dateMatch.put("match", listMatchInfos);
            dateMatch.put("match", listMatchInfoMaps);
            if (listMatchInfoMaps.size() > 0) {
                dateMatchList.add(dateMatch);
            }
        }

        if (null != userId) {
            Integer isPermission = 1;
            String isPermissionText = "";
            if (sportSocialService.getUserRecommend(userId, DateUtil.getBeginOfToday(), DateUtil.getEndOfToday()) >=
                    5) {
                isPermission = 0;
                isPermissionText = "今天已发满5单";
            }
            resultMap.put("isPermission", isPermission);
            resultMap.put("isPermissionText", isPermissionText);
        }

        resultMap.put("lastDate", lastDate);
        resultMap.put("lastMatchId", lastMatchId);

        return dateMatchList;
    }

    @Override
    public String getMatchListByDate(String lastDate, String lastMatchId) {
        String key = RedisConstant.getMatchListByDateKey(lastDate, lastMatchId);
        String result = redisService.kryoGet(key, String.class);

        if (StringUtils.isNotBlank(result)) {
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "zhihui_list_live_matches");
        if (StringUtils.isNotBlank(lastDate)) {
            params.put("last_date", lastDate);
        }
        if (StringUtils.isNotBlank(lastMatchId)) {
            params.put("last_match_id", lastMatchId);
        }

        result = CommonUtil.getSignMoJieData(params);
        redisService.kryoSetEx(key, 600, result);
        return result;
    }

    @Override
    public Map<String, Object> getLeagueList() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "home_page_league_list");
        String httpRes = CommonUtil.getSignMoJieData(params);
        if (StringUtils.isNotBlank(httpRes)) {
            result = JSON.parseObject(httpRes, HashMap.class);
        }
        return result;
    }

    @Override
    public Map<String, Object> getLeagueMatchList(String leagueId) {
        Map<String, Object> params = new HashMap<>();
        params.put("league_id", leagueId);
        return getDataFromMJThirdHttp("get_league_page", params);
    }

    @Override
    public Map<String, Object> getIntegralRank(String leagueId) {
        Map<String, Object> params = new HashMap<>();
        params.put("league_id", leagueId);
        return getDataFromMJThirdHttp("league_page_scoreboard", params);
    }

    @Override
    public Map<String, Object> getLeagueGroupMatch(String groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put("group_id", groupId);
        return getDataFromMJThirdHttp("list_league_group_matches", params);
    }

    @Override
    public Map<String, Object> getMatchFundamentals(String matchId) {
        Map<String, Object> result = null;
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "get_match_statistics");
        if (StringUtils.isNotBlank(matchId)) {
            params.put("match_id", matchId);
        }

        String httpRes = CommonUtil.getSignMoJieData(params);
        Map<String, Object> data = JSON.parseObject(httpRes, HashMap.class);
        List<Map<String, Object>> moJieData = new ArrayList<>();
        if (!data.isEmpty() && data.containsKey("resp")) {
            moJieData = (List<Map<String, Object>>) data.get("resp");
            result = (Map<String, Object>) moJieData.get(0).get("match_statistics");
//            result.remove("match_scoreboard");
//            result.remove("match_scoreboard");
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllPartOdds(String matchId) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "get_match_odds");
        if (StringUtils.isNotBlank(matchId)) {
            params.put("match_id", matchId);
        }

        String httpRes = CommonUtil.getSignMoJieData(params);

        Map<String, Object> data = JSON.parseObject(httpRes, HashMap.class);
        if (!data.isEmpty() && data.containsKey("resp")) {
            result = (List<Map<String, Object>>) data.get("resp");
        }
        return result;
    }

    @Override
    public Map<String, Object> getNotStartMatchResult(String lastDate, String lastMatchId, Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> dateMatchList = getNotStartMatchList(lastDate, lastMatchId, userId, resultMap);
        resultMap.put("dateMatchList", dateMatchList);
        return resultMap;
    }

    @Override
    public List<DetailMatchInfo> getMatchListByMatchIds(String matchIds) {
        String result = httpGetMatchInfoByMatchIds(matchIds);

        Map<String, Object> data = JSON.parseObject(result, HashMap.class);
        List<Map<String, Object>> matchInfoList = (List<Map<String, Object>>) data.get("resp");
        List<DetailMatchInfo> resultMatchInfoList = new ArrayList<>();
        for (Map<String, Object> matchInfo : matchInfoList) {

            DetailMatchInfo detailMatchInfo = packageDetailMatchInfo(matchInfo);
            if (detailMatchInfo != null) {
                resultMatchInfoList.add(detailMatchInfo);
            }
        }
        return resultMatchInfoList;
    }

    @Override
    public Map<String, DetailMatchInfo> getMatchMapByMatchIds(String matchIds) {
        Map<String, DetailMatchInfo> res = new HashMap<>();

        String result = httpGetMatchInfoByMatchIds(matchIds);

        Map<String, Object> data = JSON.parseObject(result, HashMap.class);
        List<Map<String, Object>> matchInfoList = (List<Map<String, Object>>) data.get("resp");
        for (Map<String, Object> matchInfo : matchInfoList) {

            DetailMatchInfo detailMatchInfo = packageDetailMatchInfo(matchInfo);
            if (detailMatchInfo != null) {
                res.put(matchInfo.get("match_id").toString(), detailMatchInfo);
            }
        }
        return res;
    }

    @Override
    public DetailMatchInfo getMatchMapByMatchId(Integer matchId) {
        if (matchId == null) {
            return null;
        }
        Map<String, DetailMatchInfo> matchInfoMap = getMatchMapByMatchIds(String.valueOf(matchId));
        if (matchInfoMap == null) {
            return null;
        }
        return matchInfoMap.get(String.valueOf(matchId));
    }

    @Override
    public String getMatchPredictOption(Integer matchId, Integer playType) {
        String thirdPlayType = SportsUtils.getThirdPlatePlayType(playType);

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "get_sumbit_info");
        params.put("match_id", matchId);
        params.put("play_code", thirdPlayType);

        String result = CommonUtil.getSignMoJieData(params);

        Map<String, Object> data = JSON.parseObject(result, HashMap.class);
        if (data != null && data.containsKey("resp")) {
            List<Map<String, Object>> matchPredicts = (List<Map<String, Object>>) data.get("resp");
            Collections.shuffle(matchPredicts);
            return matchPredicts.get(0).get("prediction").toString();
        }
        return null;
    }

    @Override
    public Map<String, Object> getRecommendMatchPlayTypes(Integer lotteryCode, String matchId) {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> tabList = new ArrayList<>();
        Map<String, DetailMatchInfo> matchInfoMap = getMatchMapByMatchIds(matchId);
        if (matchInfoMap != null && !matchInfoMap.isEmpty()) {
            DetailMatchInfo detailMatchInfo = matchInfoMap.get(matchId);
            //胜平负
            if (detailMatchInfo.getSpf() != null && !detailMatchInfo.getSpf().isEmpty()) {
                Map<String, Object> spf = new HashMap<>();
                spf.put("playName", "胜平负");
                spf.put("playType", SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF);
                tabList.add(spf);
            }
            if (detailMatchInfo.getRqSpf() != null && !detailMatchInfo.getRqSpf().isEmpty()) {
                Map<String, Object> rqspf = new HashMap<>();
                rqspf.put("playName", "让球胜平负");
                rqspf.put("playType", SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF);
                tabList.add(rqspf);
            }
            if (detailMatchInfo.getAsia() != null && !detailMatchInfo.getAsia().isEmpty()) {
                Map<String, Object> asia = new HashMap<>();
                asia.put("playName", "亚盘");
                asia.put("playType", SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA);
                tabList.add(asia);
            }

        }

        res.put("tabList", tabList);
        return res;
    }

    private DetailMatchInfo packageDetailMatchInfo(Map<String, Object> matchInfo) {
        if (matchInfo == null) {
            return null;
        }
        // 每场比赛信息
        Map<String, Object> spf = new HashMap<>();
        // 胜平负赔率
        List<Map<String, Object>> spfOdds = new ArrayList<>();
        Map<String, Object> matchInfoSpfOdds = (Map<String, Object>) matchInfo.get("spf_odds");
        if (matchInfoSpfOdds == null || matchInfoSpfOdds.isEmpty()) {
            return null;
        }
        Map<String, Object> sOdds = new HashMap<>();
        sOdds.put("recommendInfo", 3);
        sOdds.put("name", "主胜");
        sOdds.put("odd", matchInfoSpfOdds.get("curr_win"));
        Map<String, Object> pOdds = new HashMap<>();
        pOdds.put("recommendInfo", 1);
        pOdds.put("name", "平");
        pOdds.put("odd", matchInfoSpfOdds.get("curr_draw"));
        Map<String, Object> fOdds = new HashMap<>();
        fOdds.put("recommendInfo", 0);
        fOdds.put("name", "主负");
        fOdds.put("odd", matchInfoSpfOdds.get("curr_loss"));
        spfOdds.add(sOdds);
        spfOdds.add(pOdds);
        spfOdds.add(fOdds);

        spf.put("odds", spfOdds);

        // 让球胜平负赔率
        Map<String, Object> rqSpf = new HashMap<>();
        Map<String, Object> matchInfoRqSpfOdds = (Map<String, Object>) matchInfo.get("rqspf_odds");
        if (matchInfoRqSpfOdds != null) {
            List<Map<String, Object>> rqSpfOdds = new ArrayList<>();
            Map<String, Object> rqSOdds = new HashMap<>();
            rqSOdds.put("recommendInfo", 3);
            rqSOdds.put("name", "主胜");
            rqSOdds.put("odd", matchInfoRqSpfOdds.get("curr_win"));
            Map<String, Object> rqPOdds = new HashMap<>();
            rqPOdds.put("recommendInfo", 1);
            rqPOdds.put("name", "平");
            rqPOdds.put("odd", matchInfoRqSpfOdds.get("curr_draw"));
            Map<String, Object> rqFOdds = new HashMap<>();
            rqFOdds.put("recommendInfo", 0);
            rqFOdds.put("name", "主负");
            rqFOdds.put("odd", matchInfoRqSpfOdds.get("curr_loss"));
            rqSpfOdds.add(rqSOdds);
            rqSpfOdds.add(rqPOdds);
            rqSpfOdds.add(rqFOdds);
            rqSpf.put("odds", rqSpfOdds);
            rqSpf.put("handicap", matchInfoRqSpfOdds.get("company_odds"));
            rqSpf.put("handicapCn", SportsUtils.getHandicapCn(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF,
                    matchInfoRqSpfOdds.get("company_odds").toString()));
        }

        // 亚盘赔率
        Map<String, Object> asia = new HashMap<>();
        Map<String, Object> matchInfoAsiaOdds = (Map<String, Object>) matchInfo.get("asia_odds");
        if (matchInfoAsiaOdds != null) {
            List<Map<String, Object>> asiaOdds = new ArrayList<>();
            Map<String, Object> asiaHOdds = new HashMap<>();
            asiaHOdds.put("recommendInfo", 3);
            asiaHOdds.put("name", "主队");
            asiaHOdds.put("odd", matchInfoAsiaOdds.get("curr_home"));

            Map<String, Object> asiaAOdds = new HashMap<>();
            asiaAOdds.put("recommendInfo", 0);
            asiaAOdds.put("name", "客队");
            asiaAOdds.put("odd", matchInfoAsiaOdds.get("curr_away"));
            asiaOdds.add(asiaHOdds);
            asiaOdds.add(asiaAOdds);
            asia.put("odds", asiaOdds);
            asia.put("handicap", matchInfoAsiaOdds.get("curr_odds"));
            asia.put("handicapCn", matchInfoAsiaOdds.get("curr_odds"));
        }

        // 比分赔率
        Map<String, Object> score = new HashMap<>();
        List<Map<String, Object>> scoreOdds = new ArrayList<>();
        Map<String, Object> matchInfoScoreOdds = (Map<String, Object>) matchInfo.get("score_odds");
        if (matchInfoScoreOdds != null) {
            for (String key : matchInfoScoreOdds.keySet()) {
                if (!key.contains("-")) {
                    continue;
                }
                Map<String, Object> tempOdds = new HashMap<>();
                String biFen = key.replace("-", ":");
                tempOdds.put("recommendInfo", biFen);
                tempOdds.put("name", biFen);
                tempOdds.put("odd", matchInfoScoreOdds.get(key));
                scoreOdds.add(tempOdds);
            }
        }
        score.put("odds", scoreOdds);

        // 总进球赔率
        Map<String, Object> goal = new HashMap<>();
        Map<String, Object> matchInfoGoldOdds = (Map<String, Object>) matchInfo.get("goal_odds");
        if (matchInfoGoldOdds != null) {
            List<Map<String, Object>> goalOdds = new ArrayList<>();
            for (String key : matchInfoGoldOdds.keySet()) {
                if (!CommonUtil.isNumeric(key)) {
                    continue;
                }
                Map<String, Object> tempOdds = new HashMap<>();
                tempOdds.put("recommendInfo", key);
                tempOdds.put("name", key);
                tempOdds.put("odd", matchInfoGoldOdds.get(key));
                goalOdds.add(tempOdds);
            }
            goal.put("odds", goalOdds);
        }

        // 半全场赔率
        Map<String, Object> bqc = new HashMap<>();
        Map<String, Object> matchInfoBqcOdds = (Map<String, Object>) matchInfo.get("bqc_odds");
        if (matchInfoGoldOdds != null) {
            List<Map<String, Object>> bqcOdds = new ArrayList<>();
            if (matchInfoBqcOdds != null) {
                for (String key : matchInfoBqcOdds.keySet()) {
                    if (!key.contains("-")) {
                        continue;
                    }
                    Map<String, Object> tempOdds = new HashMap<>();
                    tempOdds.put("recommendInfo", key);
                    String nameStr = "";
                    for (String keyItem : key.split("-")) {
                        if (keyItem.equals("3")) {
                            nameStr += "胜";
                        }
                        if (keyItem.equals("1")) {
                            nameStr += "平";
                        }
                        if (keyItem.equals("0")) {
                            nameStr += "负";
                        }
                    }
                    tempOdds.put("name", nameStr);
                    tempOdds.put("odd", matchInfoBqcOdds.get(key));
                    bqcOdds.add(tempOdds);
                }
                bqc.put("odds", bqcOdds);
            }
        }


        Integer hostScore = null;
        Integer awayScore = null;
        if (null != matchInfo.get("host_score")) {
            hostScore = Integer.valueOf(matchInfo.get("host_score").toString());
        }
        if (null != matchInfo.get("away_score")) {
            awayScore = Integer.valueOf(matchInfo.get("away_score").toString());
        }

        String tag = "";
        if (matchInfo.containsKey("tag") && matchInfo.get("tag") != null && matchInfo.get("tag").toString().equals
                ("竞彩单关")) {
            tag = matchInfo.get("tag").toString();
        }

        if (matchInfo.get("match_desc").toString().equals("世界杯")) {
            tag = "竞彩单关";
        }

        Timestamp endTime = DateUtil.formatString(matchInfo.get("match_time_detail").toString(), 2);

        String liveDesc = matchInfo.get("live_desc").toString();

        Integer matchStatus = Integer.valueOf(matchInfo.get("match_status").toString());
        String matchStatusDesc = matchInfo.get("match_status_desc").toString();
        if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
            if ((matchStatusDesc.equals("改期") || matchStatusDesc.equals("待定"))) {
                matchStatus = SportsProgramConstant.SPORT_MATCH_STATUS_DELAY;
            }
            if (liveDesc.equals("中场")) {
                matchStatus = SportsProgramConstant.SPORT_MATCH_STATUS_MIDFIELD;
            }
        }
        if (matchStatusDesc.equals("取消")) {
            matchStatus = SportsProgramConstant.SPORT_MATCH_STATUS_QUIT;
        }

        //默认图片处理
        String hostTeamImg = SportsUtils.dealMatchImg(matchInfo.get("host_team_image").toString());
        String awayTeamImg = SportsUtils.dealMatchImg(matchInfo.get("away_team_image").toString());

        DetailMatchInfo detailMatchInfo = new DetailMatchInfo(matchInfo.get("match_id").toString(), matchInfo.get
                ("match_desc").toString(), matchInfo.get("match_time").toString(), matchInfo.get("match_week")
                .toString() + matchInfo.get("match_sn").toString(), matchInfo.get("host_name").toString(), matchInfo
                .get("away_name").toString(), hostTeamImg, awayTeamImg, spf, rqSpf, asia, score, goal, bqc, matchStatus,
                hostScore, awayScore, endTime, tag, matchInfo.get("half_score").toString(), liveDesc);

        return detailMatchInfo;
    }

    private String httpGetMatchInfoByMatchIds(String matchIds) {

        String matchInfoKey = RedisConstant.getHttpMatchInfoKey(matchIds);
        String result = redisService.kryoGet(matchInfoKey, String.class);
        if (StringUtils.isNotBlank(result)) {
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", "zhihui_get_matches_by_match_ids");
        params.put("match_ids", matchIds);
        result = CommonUtil.getSignMoJieData(params);

        redisService.kryoSetEx(matchInfoKey, 600, result);
        return result;
    }

    private Map<String, Object> getDataFromMJThirdHttp(String methodName, Map<String, Object> params) {
        params.put("cmd", methodName);
        String httpRes = CommonUtil.getSignMoJieData(params);
        if (StringUtils.isNotBlank(httpRes)) {
            return JSONObject.parseObject(httpRes, HashMap.class);
        }
        return null;
    }
}
