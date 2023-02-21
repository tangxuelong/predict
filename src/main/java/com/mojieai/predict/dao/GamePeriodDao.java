package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.GamePeriod;

import java.util.List;
import java.util.Set;

@TableShard(tableName = ConfigConstant.GAME_PERIOD_TABLE_NAME, shardType = ConfigConstant.GAME_PERIOD_SHARD_TYPE,
        shardBy = ConfigConstant.GAME_PERIOD_SHARD_BY)
public interface GamePeriodDao {
    List<GamePeriod> getLoadedGamePeriod(Long gameId, Integer periodLoaded);

    GamePeriod getPeriodByGameIdAndPeriod(Long gameId, String periodId);

    GamePeriod getLastPeriodByGameIdAndPeriod(Long gameId, String periodId);

    List<GamePeriod> getPeriodsByGameIdAndPeriods(Long gameId, Set<String> periodIds);

    int updateGamePeriodWinningNumbers(Long gameId, String periodId, String WinningNumbers);

    void insert(GamePeriod gamePeriod);

    void updateRemark(Long gameId, String periodId, String oldRemark, String newRemark);

    void addGamePeriodBatch(List<GamePeriod> gamePeriods, long gameId);

    String getIntervalPeriod(Long gameId, String periodId, int periodNum);
}