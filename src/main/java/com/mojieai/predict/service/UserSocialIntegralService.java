package com.mojieai.predict.service;

import com.mojieai.predict.entity.vo.UserSocialIntegralVo;

public interface UserSocialIntegralService {

    UserSocialIntegralVo refreshUserIntegralRedis(long gameId, Long userId);
}
