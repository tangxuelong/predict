package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.DetailMatchInfo;

import java.util.List;
import java.util.Map;

public interface ThirdHttpService {
    // 获取未开赛的比赛列表数据
    Map<String, Object> getNotStartMatchResult(String lastDate, String lastMatchId, Long userId);

    // 获取单独的比赛数据 其中多个matchId用逗号分隔
    List<DetailMatchInfo> getMatchListByMatchIds(String matchIds);

    Map<String, DetailMatchInfo> getMatchMapByMatchIds(String matchIds);

    DetailMatchInfo getMatchMapByMatchId(Integer matchId);

    String getMatchPredictOption(Integer matchId, Integer playType);

    Map<String, Object> getRecommendMatchPlayTypes(Integer lotteryCode, String matchId);

    List<Map<String, Object>> getNotStartMatchList(String lastDate, String lastMatchId, Long userId, Map<String,
            Object> resultMap);

    Map<String, Object> getMatchFundamentals(String matchId);

    List<Map<String, Object>> getAllPartOdds(String matchId);

    String getMatchListByDate(String lastDate, String lastMatchId);

    Map<String, Object> getLeagueList();

    Map<String, Object> getLeagueMatchList(String leagueId);

    Map<String, Object> getIntegralRank(String leagueId);

    Map<String, Object> getLeagueGroupMatch(String groupId);
}