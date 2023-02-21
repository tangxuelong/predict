package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserSportSocialRecommend;

import java.util.Map;

public interface StarUserMatchService {

    Map<String, Object> getStarUserList();

    Map<String, Object> manualSetStarUserWeight(Long userId, Long weight, Integer operateType);

    Map<String, Object> getTopStarUsers(String orderType);

    void buildStarUserList();

    void reSaveIndexRecommend2StarUser();

    void saveRecommend2StarUser(UserSportSocialRecommend recommend);
}
