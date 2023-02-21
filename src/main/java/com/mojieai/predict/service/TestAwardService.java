package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.po.GamePeriod;

import java.util.List;

public interface TestAwardService {
    List<AwardDetail> calcAwardDetail(Long gameId, int periodNum, int type);

    List<String> generateNumberList(int type, GamePeriod period);
}