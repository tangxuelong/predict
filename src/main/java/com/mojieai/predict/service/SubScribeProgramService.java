package com.mojieai.predict.service;

import java.util.Map;

public interface SubScribeProgramService {

    Map<String, Object> getSubScribeProgram(Long userId, long gameId, Integer type);
}
