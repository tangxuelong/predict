package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.DltSocialResonanceData;

import java.util.List;

public interface DltSocialResonanceDataDao {
    DltSocialResonanceData getTypeResonanceCurrentPeriod(String periodId, Integer socialType, Integer resonanceType);

    List<DltSocialResonanceData> getAllTypeResonanceCurrentPeriod(String periodId, Integer socialType);

    void update(DltSocialResonanceData dltSocialResonanceData);

    void insert(DltSocialResonanceData dltSocialResonanceData);
}
