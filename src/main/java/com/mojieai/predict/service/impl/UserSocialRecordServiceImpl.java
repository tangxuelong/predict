package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.UserSocialRecordDao;
import com.mojieai.predict.entity.po.UserSocialRecord;
import com.mojieai.predict.entity.vo.AchievementVo;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SocialService;
import com.mojieai.predict.service.UserSocialRecordService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserSocialRecordServiceImpl implements UserSocialRecordService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UserSocialRecordDao userSocialRecordDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SocialService socialService;

    @Override
    public List<UserSocialRecord> getUserLastestSocialRecords(long gameId, Long userId, Integer socialType) {
        List<UserSocialRecord> result = new ArrayList<>();
        String latestPeriodId = userSocialRecordDao.getLatestUserSocialRecordBySocialType(gameId, userId, socialType);

        result = userSocialRecordDao.getAllUserRecordByPeriodId(gameId, userId, latestPeriodId, socialType);
        return result;
    }

    @Override
    public Map<String, List<AchievementVo>> getUserAchievementVo(long gameId, Long userId) {
        if (userId == null) {
            return null;
        }
        String key = RedisConstant.getPersionalAchieveKey(gameId, userId);
        Map<String, List<AchievementVo>> userAchieveMap = redisService.kryoGet(key, HashMap.class);
//        if (userAchieveMap == null || userAchieveMap.get("killRecentAchievesNew") == null) {
//            String achievePeriodKey = RedisConstant.getAchievementFlag(gameId);
//            String periodId = redisService.kryoGet(achievePeriodKey, String.class);
//            if (StringUtils.isBlank(periodId)) {
//                periodId = PeriodRedis.getLastOpenPeriodByGameId(gameId).getPeriodId();
//            }
//            userAchieveMap = socialService.rebuildUserSocial2Redis(gameId, userId, periodId);
//        }
        String achievePeriodKey = RedisConstant.getAchievementFlag(gameId);
        String periodId = redisService.kryoGet(achievePeriodKey, String.class);
        if (StringUtils.isBlank(periodId)) {
            periodId = PeriodRedis.getLastOpenPeriodByGameId(gameId).getPeriodId();
        }
        userAchieveMap = socialService.rebuildUserSocial2Redis(gameId, userId, periodId);

        if (userAchieveMap.get("encircleRecentAchieves") != null) {
            List<AchievementVo> temp = userAchieveMap.get("encircleRecentAchieves");
            if (temp.size() == 0) {
                userAchieveMap.put("encircleRecentAchieves", null);
            }
        }
        if (userAchieveMap.get("killRecentAchieves") != null) {
            List<AchievementVo> temp = userAchieveMap.get("killRecentAchieves");
            if (temp.size() == 0) {
                userAchieveMap.put("killRecentAchieves", null);
            }
        }
        return userAchieveMap;
    }
}
