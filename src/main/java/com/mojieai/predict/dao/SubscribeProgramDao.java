package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SubscribeProgram;

import java.util.List;

public interface SubscribeProgramDao {

    SubscribeProgram getSubscribePredictByProgramId(Integer programId);

    SubscribeProgram getSubscribePredictByUnique(long gameId, Integer programType, Integer predictType);

    List<SubscribeProgram> getSubscribeProgramByProgramType(long gameId, Integer programType);

    Integer insert(SubscribeProgram subscribeProgram);
}
