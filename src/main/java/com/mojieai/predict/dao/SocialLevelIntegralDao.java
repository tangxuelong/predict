package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialLevelIntegral;

import java.util.List;

public interface SocialLevelIntegralDao {

    List<SocialLevelIntegral> getAllSocialLevelIntegral();

    Integer updateSocialLevelIntegralEnable(Integer levelId, Integer enable);

    Integer insert(SocialLevelIntegral socialLevelIntegral);
}
