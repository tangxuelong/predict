package com.mojieai.predict.controller;

import com.mojieai.predict.dao.MatchScheduleDao;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.MatchSchedule;
import com.mojieai.predict.service.ThirdHttpService;
import com.mojieai.predict.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/activity")
public class MarketController extends BaseController {

    @Autowired
    private MatchScheduleDao matchScheduleDao;
    @Autowired
    private ThirdHttpService thirdHttpService;

    @RequestMapping("/get_sports_wisdomer_spread")
    @ResponseBody
    public Object getSportsWisdomerSpread() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> recentMatch = new HashMap<>();
        MatchSchedule matchSchedule = matchScheduleDao.getNotBeginRecentMatchSchedule(null);
        if (matchSchedule == null) {
            matchSchedule = matchScheduleDao.getNotBeginRecentMatchSchedule(null);
        }

        Map<String, DetailMatchInfo> matchInfoMap = thirdHttpService.getMatchMapByMatchIds(matchSchedule.getMatchId() +
                "");
        DetailMatchInfo detailMatchInfo = matchInfoMap.get(matchSchedule.getMatchId() + "");
        recentMatch.put("hostName", detailMatchInfo.getHostName());
        recentMatch.put("awayName", detailMatchInfo.getAwayName());
        recentMatch.put("hostImg", detailMatchInfo.getHostImg());
        recentMatch.put("awayImg", detailMatchInfo.getAwayImg());
        recentMatch.put("matchDesc", detailMatchInfo.getMatchDate() + " " + DateUtil.formatTime(detailMatchInfo
                .getEndTime(), DateUtil.DATE_FORMAT_HHMM));

        List<Map<String, Object>> historyMatch = new ArrayList<>();
        List<MatchSchedule> futureMatchSchedule = matchScheduleDao.getLatestEndMatch(null, 3);
        if (futureMatchSchedule == null || futureMatchSchedule.size() < 3) {
            futureMatchSchedule = matchScheduleDao.getLatestEndMatch(null, 3);
        }
        for (MatchSchedule futureMatch : futureMatchSchedule) {
            Map<String, DetailMatchInfo> tempMatchMap = thirdHttpService.getMatchMapByMatchIds(futureMatch.getMatchId
                    () + "");
            DetailMatchInfo matchInfo = tempMatchMap.get(futureMatch.getMatchId() + "");
            Map<String, Object> temp = new HashMap<>();
            temp.put("createTime", DateUtil.formatTime(DateUtil.getCurrentTimestamp()));
            temp.put("playType", "胜平负");
            temp.put("statusImg", "http://sportsimg.mojieai.com/cp_market_import_recommend_hit.png");
            temp.put("battleTeam", matchInfo.getHostName() + " VS " + matchInfo.getAwayName());
            temp.put("matchDesc", matchInfo.getMatchDate() + " " + DateUtil.formatTime(matchInfo.getEndTime(),
                    DateUtil.DATE_FORMAT_HHMM));
            historyMatch.add(temp);
        }

        result.put("recentMatch", recentMatch);
        result.put("historyMatch", historyMatch);
        return buildSuccJson(result);
    }
}
