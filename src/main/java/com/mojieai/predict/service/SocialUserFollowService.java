package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.SocialUserFollow;

import java.util.List;
import java.util.Map;

public interface SocialUserFollowService {

    Map<String, Object> getUserFollowByPage(long gameId, Long userId, Integer followType, Integer page);

    Map<String, Object> getUserFollowKillNumList(Long gameId, Long userId, Long lastUserId, Integer followType);
}
