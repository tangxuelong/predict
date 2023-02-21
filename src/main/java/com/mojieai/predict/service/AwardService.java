package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.vo.ButtonOrderNewVo;

import java.util.List;
import java.util.Map;

public interface AwardService {
    List<AwardDetail> calcAwardDetail(Long gameId, int periodNum, List<String> numberList);

    // 工具排序
    List<ButtonOrderNewVo> getSortedTools(Game game, String versionCode, Integer clientType);

    Map<String, Object> getAwardTable(Game game, String numberCount);
}