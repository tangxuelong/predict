package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.MatchSchedule;

import java.util.List;
import java.util.Set;

public interface MatchScheduleDao {

    MatchSchedule getMatchScheduleByPk(Integer matchId, Integer lotteryCode);

    List<MatchSchedule> getNeedDealBuyRecommendMatch();

    List<MatchSchedule> getNeedDealWithdrawMatch();

    List<MatchSchedule> getNeedDealRankMatch();

    List<MatchSchedule> getAllNeedDealCancelRankMatch();

    List<MatchSchedule> getAllNotOpeningMatch();

    List<MatchSchedule> getMatchByMatchStatus(Integer status);

    MatchSchedule getNotBeginRecentMatchSchedule(String leagueMatchName);

    List<MatchSchedule> getLatestEndMatch(String leagueMatchName, Integer count);

    List<MatchSchedule> getAllWaitRankRedisMatch();

    List<MatchSchedule> getVipProgramMatch();

    Integer updateMatchStatus(Integer matchId, Integer lotteryCode, String flagColumn, String timeColumn);

    Integer updateMatchStatus(Integer matchId, Integer lotteryCode, Integer status, String flagColumn, String
            timeColumn);

    void updateMatchStatusBySetIds(Integer sportMatchStatusEnd, Integer lotteryCode, Set<Integer> matchEndIds);

    Integer insert(MatchSchedule matchSchedule);
}
