package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SocialKillAwardLevel;

import java.util.List;

/**
 * Created by tangxuelong on 2017/10/16.
 */
public interface SocialKillAwardLevelDao {
    void insert(SocialKillAwardLevel socialKillAwardLevel);

    List<SocialKillAwardLevel> getSocialKillAwardLevel(Long gameId, Integer ballType);

    int updateSocialKillAwardLevel(Integer levelId, Integer rankScore);
}
