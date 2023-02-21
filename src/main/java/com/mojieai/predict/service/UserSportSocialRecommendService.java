package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserSportSocialRecommend;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface UserSportSocialRecommendService {

    Map<String, Object> getUserSportSocialRecommends(Long userId, String lastIndex);

    List<Map<String, Object>> getSportSocialIndexHotRecommend();

    Map<String, Object> getUserRecommendListFromRedis(Long userId, Integer listType, Integer playType, String
            lastIndex);

    Map<String, Object> getMatchBasicData(String matchId);

    Map<String, Object> getMatchOddsData(String matchId);

    Map<String, Object> getMatchPredictData(String matchId, String lastIndex);

    void buildRecommendListTiming();

    void rebuildSportRecommendListByMatchIndex(Integer listType, Integer playType);

    void rebuildSportRecommendList();

    Boolean checkUserRecommend(Long userId, Integer taskTimes, Timestamp recommendDate);

    Map<String, Object> operateHotRecommend(String recommendId, Long weight, Integer operateType);

    Map<String, Object> getHotRecommendInfo(Integer playType);

    Map<String, Object> clearManualHotRecommend(Integer playType);

    void rebuildManualRecommendList();

    Map<String, Object> getUserRecentRecommend(Long userId);

    void saveRecommendMap2MatchRedis(UserSportSocialRecommend recommend);

    void rebuildSportOneMatchRecommend(String matchId);

    void userRecommendTitleLock(UserSportSocialRecommend userSportSocialRecommend);
}
