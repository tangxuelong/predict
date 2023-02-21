package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.PeriodSchedule;

import java.util.List;

public interface AwardInfoService {
    List<AwardInfo> getAwardInfos(Long gameId, String periodId);

    Boolean downloadCommonGameAwardInfo(Long gameId, String periodId, PeriodSchedule dirtyPeriodSchedule);

    void downLoadAwardArea(long gameId, String periodId, PeriodSchedule dirtyPeriodSchedule);

    void downLoadTestNum();
}
