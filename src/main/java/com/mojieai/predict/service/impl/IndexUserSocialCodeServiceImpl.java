package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.IndexUserSocialCodeDao;
import com.mojieai.predict.enums.achievement.DltAchievementEnum;
import com.mojieai.predict.enums.achievement.SsqAchievementEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.IndexUserSocialCodeService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndexUserSocialCodeServiceImpl implements IndexUserSocialCodeService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private IndexUserSocialCodeDao indexUserSocialCodeDao;
    @Autowired
    private RedisService redisService;

    @Override
    public List<Map> getRecentOpenedSocialIndex(Long gameId, Long userId, Integer recentPeriodCount, Integer
            socialType) {
        Set<String> periodIds = new HashSet<>();
        List<Map> result = new ArrayList<>();
        String periodId = redisService.kryoGet(RedisConstant.getDisTributeFlag(gameId), String.class);
        List<Map> userSocialIndex = indexUserSocialCodeDao.getUserAwardIndexSocials(gameId, userId, recentPeriodCount,
                socialType, periodId);
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.SSQ)) {
            SsqAchievementEnum ac = SsqAchievementEnum.getAchievementEnumBySocialType(socialType);
            for (Map temp : userSocialIndex) {
                if (periodIds.size() >= ac.getRecentAchieveCount()) {
                    break;
                }
                periodIds.add(temp.get("PERIOD_ID").toString());
                result.add(temp);
            }
        }

        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            DltAchievementEnum ac = DltAchievementEnum.getAchievementEnumBySocialType(socialType);
            for (Map temp : userSocialIndex) {
                if (periodIds.size() >= ac.getRecentAchieveCount()) {
                    break;
                }
                periodIds.add(temp.get("PERIOD_ID").toString());
                result.add(temp);
            }
        }

        return result;
    }
}
