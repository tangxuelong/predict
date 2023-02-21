package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialEncircleAwardLevel;

import java.util.List;

/**
 * Created by tangxuelong on 2017/10/16.
 */
public interface SocialEncircleAwardLevelDao {
    void insert(SocialEncircleAwardLevel socialEncircleAwardLevel);

    List<SocialEncircleAwardLevel> getSocialEncircleAwardLevel(Long gameId, Integer ballType);

    int updateSocialEncircleAwardLevel(Integer levelId, Integer rankScore);
}
