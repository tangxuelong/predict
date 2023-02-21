package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.Program;

import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/7.
 */
public interface ProgramService {
    void productPrograms();

    void productPrograms(Game game);

    void produceSomePeriodProgram(GamePeriod gamePeriod);

    boolean calculateProgram(GamePeriod gamePeriod);

    List<Program> getProgramList(Long gameId, String periodId, Integer programType);

    Map<String, Object> getCurrentSalePrograms(Long gameId, Long userId, Integer programType);

    Map<String, Object> getHistoryAwardProgramList(Long gameId, String lastPeriodId, Long userId, Integer isAward);

    Map<String, Object> getPurchaseProgramInfo(Long userId, String programId, Integer clientId, Integer versionCode);

    Map<String, Object> checkProgram(Long userId, String programId);

    List<Program> rebuildSaleProgramList(Long gameId, String periodId, Integer programType);

    void productProgramByPeriod(GamePeriod period);
}
