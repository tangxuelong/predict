package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SsqSocialResonanceData;

import java.util.List;

public interface SsqSocialResonanceDataDao {
    SsqSocialResonanceData getTypeResonanceCurrentPeriod(String periodId, Integer socialType, Integer resonanceType);

    List<SsqSocialResonanceData> getAllTypeResonanceCurrentPeriod(String periodId, Integer socialType);

    void update(SsqSocialResonanceData ssqSocialResonanceData);

    void insert(SsqSocialResonanceData ssqSocialResonanceData);
}
