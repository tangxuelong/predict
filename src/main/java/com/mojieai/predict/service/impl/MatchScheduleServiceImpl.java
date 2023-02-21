package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.dao.MatchScheduleDao;
import com.mojieai.predict.dao.MissionDao;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.bo.ListMatchInfo;
import com.mojieai.predict.entity.bo.PersistTagMatchInfoModel;
import com.mojieai.predict.entity.po.MatchSchedule;
import com.mojieai.predict.entity.po.Mission;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class MatchScheduleServiceImpl implements MatchScheduleService {
    protected Logger log = LogConstant.commonLog;
    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MatchScheduleDao matchScheduleDao;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private MatchInfoService matchInfoService;
    @Autowired
    private MissionDao missionDao;
    @Autowired
    private UserBuyRecommendService userBuyRecommendService;

    @Override
    public void getMatchTiming() {
        Timestamp lastDate = DateUtil.getCurrentTimestamp();

        String date = DateUtil.formatTime(lastDate, "yyyyMMdd");
        String lastMatchId = null;
        String tempLastMatchId = null;
        String tempLastDate = "";
        Integer insertRes = 0;

        Map<String, Object> temp = thirdHttpService.getNotStartMatchResult(date, lastMatchId, null);
        tempLastDate = new String(date);
        date = temp.get("lastDate").toString();
        tempLastMatchId = "";
        if (!temp.containsKey("lastMatchId")) {
            return;
        }
        lastMatchId = temp.get("lastMatchId").toString();
        while (!temp.isEmpty()) {
            if (StringUtils.isNotEmpty(tempLastDate) && tempLastDate.equals(date) && lastMatchId.equals
                    (tempLastMatchId)) {
                break;
            }
            List<Map<String, Object>> matchInfos = (List<Map<String, Object>>) temp.get("dateMatchList");
            insertRes += saveMatchSchedule2db(matchInfos);
            temp = thirdHttpService.getNotStartMatchResult(date, lastMatchId, null);
            tempLastDate = new String(date);
            tempLastMatchId = StringUtils.isBlank(lastMatchId) ? "" : new String(lastMatchId);
            date = temp.get("lastDate").toString();
            lastMatchId = temp.get("lastMatchId").toString();
        }

        if (insertRes > 0) {
            //刷新机器人比赛
            matchInfoService.rebuildMatchInfoRedis();
        }

    }

    @Override
    public void expireMatchTiming() {
        //获取比赛中或者延期的比赛
        List<MatchSchedule> matches = matchScheduleDao.getAllNotOpeningMatch();
        if (matches == null || matches.size() == 0) {
            return;
        }
        Map<String, MatchSchedule> matchScheduleMap = new HashMap<>();

        StringBuilder matchIdsSb = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            MatchSchedule matchSchedule = matches.get(i);
            matchIdsSb.append(matchSchedule.getMatchId());
            if (i < matches.size() - 1) {
                matchIdsSb.append(CommonConstant.COMMA_SPLIT_STR);
            }
            matchScheduleMap.put(matchSchedule.getMatchId() + "", matchSchedule);
        }

        Map<Integer, Set<Integer>> matchIdStatusMap = new HashMap<>();
        Map<String, DetailMatchInfo> matchInfoMap = thirdHttpService.getMatchMapByMatchIds(matchIdsSb.toString());
        for (Map.Entry<String, DetailMatchInfo> entry : matchInfoMap.entrySet()) {
            if (!matchScheduleMap.containsKey(entry.getKey())) {
                continue;
            }
            Integer thirdMatchStatus = entry.getValue().getMatchStatus();
            Integer scheduleMatchStatus = matchScheduleMap.get(entry.getKey()).getIfEnd();
            if (!SportsUtils.checkBackMatchStatus(scheduleMatchStatus, thirdMatchStatus)) {
                continue;
            }
            Set<Integer> matchIds = null;
            if (matchIdStatusMap.containsKey(entry.getValue().getMatchStatus())) {
                matchIds = matchIdStatusMap.get(entry.getValue().getMatchStatus());
            } else {
                matchIds = new HashSet<>();
            }
            matchIds.add(Integer.valueOf(entry.getKey()));
            matchIdStatusMap.put(entry.getValue().getMatchStatus(), matchIds);
            //过期赛事预测
            String key = RedisConstant.getSportSocialOneMatchRecommendListKey(entry.getKey());
            redisService.del(key);
        }

        if (!matchIdStatusMap.isEmpty()) {
            for (Map.Entry<Integer, Set<Integer>> entry : matchIdStatusMap.entrySet()) {
                if (!entry.getKey().equals(SportsProgramConstant.SPORT_MATCH_STATUS_INIT)) {
                    try {
                        matchScheduleDao.updateMatchStatusBySetIds(entry.getKey(), 200, entry.getValue());
                    } catch (Exception e) {
                        log.error("expireMatchTiming 异常", e);
                        continue;
                    }
                }
            }

            //更新推荐列表
            userSportSocialRecommendService.rebuildSportRecommendList();

            //刷新机器人比赛
            matchInfoService.rebuildMatchInfoRedis();

            userSportSocialRecommendService.rebuildManualRecommendList();

            matchInfoService.rebuildFocusMatches();
        }

    }

    @Override
    public void cancelMatchTiming() {
        //1.获取所有赛事延期的比赛
        List<MatchSchedule> matchSchedules = getMatchByMatchStatus(SportsProgramConstant.SPORT_MATCH_STATUS_QUIT);
        if (matchSchedules == null || matchSchedules.size() == 0) {
            return;
        }

        for (MatchSchedule matchSchedule : matchSchedules) {
            String classId = matchSchedule.getLotteryCode() + ":" + matchSchedule.getMatchId();
            List<Mission> cancelMatchMission = missionDao.getSlaveMissionByClassId(classId, Mission
                    .MISSION_TYPE_FOOTBALL_WITHDRAW, Mission.MISSION_STATUS_INTI);

            for (Mission mission : cancelMatchMission) {
                userBuyRecommendService.footballMatchCancelUpdateWithdrawStatus(mission);
            }
            //2.check 所有该比赛下的方案是否都处理完
            Integer count = missionDao.getCountByClassIdAndStatus(classId, Mission.MISSION_TYPE_FOOTBALL_WITHDRAW,
                    Mission.MISSION_STATUS_INTI);
            if (count == 0) {
                matchScheduleDao.updateMatchStatus(matchSchedule.getMatchId(), matchSchedule.getLotteryCode(),
                        CommonConstant.IF_PURCHASE_LOG_REFUND, "IF_PURCHASE_LOG", "PURCHASE_LOG_TIME");
            }
        }
    }

    @Override
    public List<MatchSchedule> getMatchByMatchStatus(Integer status) {
        return matchScheduleDao.getMatchByMatchStatus(status);
    }

    private Integer saveMatchSchedule2db(List<Map<String, Object>> matchInfos) {
        Integer res = 0;
        for (Map<String, Object> temp : matchInfos) {
            List<Map<String, Object>> matchs = (List<Map<String, Object>>) temp.get("match");
            for (Map<String, Object> match : matchs) {
                Integer mathId = Integer.valueOf(match.get("matchId").toString());
                String leagueMatchName = match.get("matchName") == null ? "" : match.get("matchName").toString();
                Timestamp matchTimeDetail = (Timestamp) match.get("matchTimeDetail");
                MatchSchedule matchSchedule = new MatchSchedule(mathId, 200, leagueMatchName);
                PersistTagMatchInfoModel persistTagMatchInfoModel = new PersistTagMatchInfoModel(mathId,
                        leagueMatchName, matchTimeDetail, match.get("matchDate").toString());
                try {
                    matchInfoService.saveTagMatchInfo(persistTagMatchInfoModel);
                    res += matchScheduleDao.insert(matchSchedule);
                } catch (DuplicateKeyException e) {
                    continue;
                }

            }
        }
        return res;
    }

}
