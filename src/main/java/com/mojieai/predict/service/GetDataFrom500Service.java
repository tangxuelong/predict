package com.mojieai.predict.service;


import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.GamePeriod;

import java.util.List;

public interface GetDataFrom500Service {
    List<GamePeriod> getDataFrom500(long gameId, String gameEn, int[] peroidArr);

    List<AwardInfo>  getAwordInfoFrom500(long gameId, String gameEn, int[] peroidArr);
}
