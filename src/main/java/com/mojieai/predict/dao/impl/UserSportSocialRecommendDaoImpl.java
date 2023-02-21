package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSportSocialRecommendDao;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserSportSocialRecommendDaoImpl extends BaseDao implements UserSportSocialRecommendDao {

    @Override
    public UserSportSocialRecommend getSportSocialRecommendById(Long userIdPrefix, String recommendId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userIdPrefix);
        params.put("recommendId", recommendId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserSportSocialRecommend.getSportSocialRecommendById", params);
    }

    @Override
    public List<UserSportSocialRecommend> getSportSocialRecommendByMatchIdAndPlayType(Long userIdPrefix, String
            matchId, Integer playType) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userIdPrefix);
        params.put("matchId", matchId);
        params.put("playType", playType);
        return sqlSessionTemplate.selectList("UserSportSocialRecommend.getSportSocialRecommendByMatchIdAndPlayType",
                params);
    }

    @Override
    public List<UserSportSocialRecommend> getSportSocialRecommendByMatchId(Long userIdPrefix, String matchId) {
        Map<String, Object> params = new HashMap<>();
        params.put("matchId", matchId);
        params.put("userId", userIdPrefix);
        return sqlSessionTemplate.selectList("UserSportSocialRecommend.getSportSocialRecommendByMatchId", params);
    }

    @Override
    public List<UserSportSocialRecommend> getUserSportSocialRecommendByDate(Long userId, Timestamp beginTime,
                                                                            Timestamp endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        return sqlSessionTemplate.selectList("UserSportSocialRecommend.getSportSocialRecommendByDate", params);
    }

    @Override
    public List<UserSportSocialRecommend> getUserSportSocialRecommends(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return sqlSessionTemplate.selectList("UserSportSocialRecommend.getUserSportSocialRecommends", params);
    }

    @Override
    public List<UserSportSocialRecommend> getUserRecentRecommend(Long userId, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("count", count);
        return sqlSessionTemplate.selectList("UserSportSocialRecommend.getUserRecentRecommend", params);
    }

    @Override
    public Integer getUserSportSocialRecommendsByTime(Long userId, Timestamp begin, Timestamp
            end) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("begin", begin);
        params.put("end", end);
        return sqlSessionTemplate.selectOne("UserSportSocialRecommend.getUserSportSocialRecommendsByTime", params);
    }

    @Override
    public List<UserSportSocialRecommend> getUserCanPurchaseRecommend(Long userId, Integer playType) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("playType", playType);
        return sqlSessionTemplate.selectList("UserSportSocialRecommend.getUserCanPurchaseRecommend", params);
    }

    @Override
    public List<UserSportSocialRecommend> getUserSportRecommendsBySize(Long userId, String lastIndex, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("lastIndex", lastIndex);
        params.put("count", count);
        return sqlSessionTemplate.selectList("UserSportSocialRecommend.getUserSportRecommendsBySize", params);
    }

    @Override
    public Integer getUserRecommendCount(Long userId, Timestamp beginOfOneDay, Timestamp endOfOneDay) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("beginTime", beginOfOneDay);
        params.put("endTime", endOfOneDay);
        return sqlSessionTemplate.selectOne("UserSportSocialRecommend.getUserRecommendCount", params);
    }

    @Override
    public Integer insert(UserSportSocialRecommend userSportSocialRecommend) {
        return sqlSessionTemplate.insert("UserSportSocialRecommend.insert", userSportSocialRecommend);
    }

    @Override
    public void update(UserSportSocialRecommend userSportSocialRecommend) {
        sqlSessionTemplate.update("UserSportSocialRecommend.update", userSportSocialRecommend);
    }

    @Override
    public Integer updateSaleCount(Long userIdPrefix, String recommendId, Integer saleCount, Integer couponSaleCount) {
        Map params = new HashMap();
        params.put("userId", userIdPrefix);
        params.put("saleCount", saleCount);
        params.put("recommendId", recommendId);
        params.put("couponSaleCount", couponSaleCount);
        return sqlSessionTemplate.update("UserSportSocialRecommend.updateSaleCount", params);
    }

    @Override
    public List<UserSportSocialRecommend> getSportSocialRecommendByUserIdMatchId(Long userId, String matchId) {
        Map params = new HashMap();
        params.put("userId", userId);
        params.put("matchId", matchId);
        return sqlSessionTemplate.selectList("UserSportSocialRecommend.getSportSocialRecommendByUserIdMatchId", params);
    }
}
