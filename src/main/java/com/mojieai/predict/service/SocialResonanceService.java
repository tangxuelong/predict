package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.SocialEncircle;
import com.mojieai.predict.entity.po.SocialKillCode;

import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/7.
 */
public interface SocialResonanceService {
    // 重构社区共振数据
    void rebuildSocialResonance();

    // 实时更新数据 围号
    void updateSocialResonance(SocialEncircle socialEncircle);

    // 实时更新数据 杀号
    void updateSocialResonance(SocialKillCode socialKillCode);

    // 定时任务撤销过期数据
    void expireSocialResonance();

    Map<String, Object> getResonanceData(Game game, Integer resonanceType, Long userId, Integer clientId);

    Boolean livingBuildEncircleResonance(SocialEncircle socialEncircle);

    Boolean livingBuildKillResonance(SocialKillCode socialKillCode);
}
