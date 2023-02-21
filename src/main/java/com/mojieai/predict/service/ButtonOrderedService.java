package com.mojieai.predict.service;


import java.util.Map;

public interface ButtonOrderedService {

    Map<String, Object> getToolsIndexButtons(Long gameId, String versionCode, Integer clientType);
}
