package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.entity.po.GamePeriod;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class GamePeriodDaoImpl extends BaseDao implements GamePeriodDao {
    @Override
    public List<GamePeriod> getLoadedGamePeriod(Long gameId, Integer periodLoaded) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodLoaded", periodLoaded);
        return sqlSessionTemplate.selectList("GamePeriod.getLoadedGamePeriod", params);
    }

    @Override
    public GamePeriod getPeriodByGameIdAndPeriod(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("GamePeriod.getGamePeriod", params);
    }

    @Override
    public GamePeriod getLastPeriodByGameIdAndPeriod(Long gameId, String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectOne("GamePeriod.getLastPeriod", params);
    }

    @Override
    public List<GamePeriod> getPeriodsByGameIdAndPeriods(Long gameId, Set<String> periodIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodIds", periodIds);
        return sqlSessionTemplate.selectList("GamePeriod.getPeriodsByGameIdAndPeriods", params);
    }

    @Override
    public int updateGamePeriodWinningNumbers(Long gameId, String periodId, String winningNumbers) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("winningNumbers", winningNumbers);
        return sqlSessionTemplate.update("GamePeriod.updateGamePeriodWinningNumbers", params);
    }

    @Override
    public void insert(GamePeriod gamePeriod) {
        sqlSessionTemplate.insert("GamePeriod.insert", gamePeriod);
    }

    @Override
    public void updateRemark(Long gameId, String periodId, String oldRemark, String newRemark) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("oldRemark", oldRemark);
        params.put("newRemark", newRemark);
        sqlSessionTemplate.update("GamePeriod.updateRemark", params);
    }

    @Override
    public void addGamePeriodBatch(List<GamePeriod> gamePeriods, long gameId) {
        Map gamePeriodMap = new HashMap();
        gamePeriodMap.put("gameId", gameId);
        gamePeriodMap.put("gamePeriods", gamePeriods);
        sqlSessionTemplate.insert("GamePeriod.addGamePeriodBatch", gamePeriodMap);
    }

    @Override
    public String getIntervalPeriod(Long gameId, String periodId, int periodNum) {
        Map gamePeriodMap = new HashMap();

        gamePeriodMap.put("gameId", gameId);
        gamePeriodMap.put("periodId", periodId);
        gamePeriodMap.put("periodNum", periodNum);
        List<String> periodIds = sqlSessionTemplate.selectList("GamePeriod.getIntervalPeriod", gamePeriodMap);
        String periodIdRes = "";
        if (periodNum == periodIds.size()) {
            periodIdRes = periodIds.get(periodNum - 1);
        }
        return periodIdRes;
    }
}
