package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.SocialLevelIntegralCache;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.UserSocialIntegralDao;
import com.mojieai.predict.entity.po.UserSocialIntegral;
import com.mojieai.predict.entity.vo.SocialLevelIntegralVo;
import com.mojieai.predict.entity.vo.UserSocialIntegralVo;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.UserSocialIntegralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSocialIntegralServiceImpl implements UserSocialIntegralService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private UserSocialIntegralDao userSocialIntegralDao;

    @Override
    public UserSocialIntegralVo refreshUserIntegralRedis(long gameId, Long userId) {
        UserSocialIntegral userSocialIntegral = userSocialIntegralDao.getUserSocialIntegralByUserId(gameId, userId,
                false);
        UserSocialIntegralVo userSocialIntegralVo = null;
        if (userSocialIntegral != null) {
            userSocialIntegralVo = getUserSocialIntegralVo(userId, userSocialIntegral.getUserScore());
        } else {
            //用户还没有派发过奖励
            userSocialIntegralVo = getUserSocialIntegralVo(userId, 0l);
        }

        String key = RedisConstant.getUserIntegralKey(gameId, userId);
        redisService.kryoSetEx(key, 446400, userSocialIntegralVo);
        return userSocialIntegralVo;
    }

    private UserSocialIntegralVo getUserSocialIntegralVo(Long userId, Long userScore) {
        SocialLevelIntegralVo currentLevel = SocialLevelIntegralCache.getUserLevelVoByIntegralByScore
                (userScore, 0);
        SocialLevelIntegralVo nextLevel = SocialLevelIntegralCache.getUserLevelVoByIntegralByScore
                (userScore, 1);

        UserSocialIntegralVo userSocialIntegralVo = new UserSocialIntegralVo();
        if (currentLevel != null) {
            userSocialIntegralVo.setLevelName(currentLevel.getTitleName());
            userSocialIntegralVo.setSocialLevel(currentLevel.getLevelId());
            userSocialIntegralVo.setTitleBigImg(currentLevel.getBigImg());
            userSocialIntegralVo.setTitleSmallImg(currentLevel.getSmallImg());
        }
        if (nextLevel != null) {
            userSocialIntegralVo.setUpgradeIntegral(String.valueOf(nextLevel.getMinIntegral()));
        }
        userSocialIntegralVo.setIntegral(String.valueOf(userScore));
        userSocialIntegralVo.setUserId(String.valueOf(userId));
        return userSocialIntegralVo;
    }
}
