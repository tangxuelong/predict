package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.MatchScheduleDao;
import com.mojieai.predict.entity.po.MatchSchedule;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class MatchScheduleDaoImpl extends BaseDao implements MatchScheduleDao {

    @Override
    public MatchSchedule getMatchScheduleByPk(Integer matchId, Integer lotteryCode) {
        Map param = new HashMap();
        param.put("matchId", matchId);
        param.put("lotteryCode", lotteryCode);
        return sqlSessionTemplate.selectOne("MatchSchedule.getMatchScheduleByPk", param);
    }

    @Override
    public List<MatchSchedule> getNeedDealBuyRecommendMatch() {
        return sqlSessionTemplate.selectList("MatchSchedule.getNeedDealBuyRecommendMatch");
    }

    @Override
    public List<MatchSchedule> getNeedDealWithdrawMatch() {
        return sqlSessionTemplate.selectList("MatchSchedule.getNeedDealWithdrawMatch");
    }

    @Override
    public List<MatchSchedule> getNeedDealRankMatch() {
        return sqlSessionTemplate.selectList("MatchSchedule.getNeedDealRankMatch");
    }

    @Override
    public List<MatchSchedule> getAllNeedDealCancelRankMatch() {
        return sqlSessionTemplate.selectList("MatchSchedule.getAllNeedDealCancelRankMatch");
    }

    @Override
    public List<MatchSchedule> getAllNotOpeningMatch() {
        return sqlSessionTemplate.selectList("MatchSchedule.getAllNotOpeningMatch");
    }

    @Override
    public List<MatchSchedule> getMatchByMatchStatus(Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("ifEnd", status);
        return sqlSessionTemplate.selectList("MatchSchedule.getMatchByMatchStatus", params);
    }

    @Override
    public MatchSchedule getNotBeginRecentMatchSchedule(String leagueMatchName) {
        Map<String, Object> param = new HashMap<>();
        param.put("leagueMatchName", leagueMatchName);
        return sqlSessionTemplate.selectOne("MatchSchedule.getNotBeginRecentMatchSchedule", param);
    }

    @Override
    public List<MatchSchedule> getLatestEndMatch(String leagueMatchName, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("count", count);
        params.put("leagueMatchName", leagueMatchName);
        return sqlSessionTemplate.selectList("MatchSchedule.getLatestEndMatch", params);
    }

    @Override
    public List<MatchSchedule> getAllWaitRankRedisMatch() {
        return sqlSessionTemplate.selectList("MatchSchedule.getAllWaitRankRedisMatch");
    }

    @Override
    public List<MatchSchedule> getVipProgramMatch() {
        return sqlSessionTemplate.selectList("MatchSchedule.getVipProgramMatch");
    }

    @Override
    public Integer updateMatchStatus(Integer matchId, Integer lotteryCode, String flagColumn, String timeColumn) {
        return updateMatchStatus(matchId, lotteryCode, 1, flagColumn, timeColumn);
    }

    @Override
    public Integer updateMatchStatus(Integer matchId, Integer lotteryCode, Integer status, String flagColumn, String timeColumn) {
        Map param = new HashMap();
        param.put("matchId", matchId);
        param.put("lotteryCode", lotteryCode);
        param.put("flagColumn", flagColumn);
        param.put("timeColumn", timeColumn);
        param.put("status", status);
        return sqlSessionTemplate.update("MatchSchedule.updateMatchStatus", param);
    }

    @Override
    public void updateMatchStatusBySetIds(Integer matchStatus, Integer lotteryCode, Set<Integer> matchEndIds) {
        Map params = new HashMap();
        params.put("matchStatus", matchStatus);
        params.put("lotteryCode", lotteryCode);
        params.put("matchEndIds", matchEndIds);
        sqlSessionTemplate.update("MatchSchedule.updateMatchStatusBySetIds", params);
    }

    @Override
    public Integer insert(MatchSchedule matchSchedule) {
        return sqlSessionTemplate.insert("MatchSchedule.insert", matchSchedule);
    }
}
