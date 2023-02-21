package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserBuyRecommendDao;
import com.mojieai.predict.entity.po.UserBuyRecommend;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserBuyRecommendDaoImpl extends BaseDao implements UserBuyRecommendDao {

    @Override
    public UserBuyRecommend getUserBuyRecommendByPk(Long userId, String footballLogId, boolean isLock) {
        Map param = new HashMap();
        param.put("userId", userId);
        param.put("footballLogId", footballLogId);
        param.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("userBuyRecommend.getUserBuyRecommendByPk", param);
    }

    @Override
    public UserBuyRecommend getUserBuyRecommendByUniqueKey(Long userId, String programId, boolean isLock) {
        Map param = new HashMap();
        param.put("userId", userId);
        param.put("programId", programId);
        param.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("userBuyRecommend.getUserBuyRecommendByUniqueKey", param);
    }

    @Override
    public List<UserBuyRecommend> getUserPurchaseSportRecommend(Long userId, Integer lotteryCode, String
            lastIndex, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("lotteryCode", lotteryCode);
        params.put("lastIndex", lastIndex);
        params.put("count", count);
        return sqlSessionTemplate.selectList("userBuyRecommend.getUserPurchaseSportRecommend", params);
    }

    @Override
    public Integer getUserPurchaseRecommendByDate(Long userId, Integer lotteryCode, Timestamp beginTime, Timestamp
            endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("lotteryCode", lotteryCode);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        return sqlSessionTemplate.selectOne("userBuyRecommend.getUserPurchaseRecommendByDate", params);
    }

    @Override
    public Integer updatePayStatus(Long userId, String footballLogId, Integer setPayStatus, Integer oldPayStatus,
                                   Boolean couponFlag) {
        Map param = new HashMap<>();
        param.put("userId", userId);
        param.put("footballLogId", footballLogId);
        param.put("setPayStatus", setPayStatus);
        param.put("oldPayStatus", oldPayStatus);
        param.put("couponFlag", couponFlag);
        return sqlSessionTemplate.update("userBuyRecommend.updatePayStatus", param);
    }

    @Override
    public Integer updateWithdrawStatus(Long userId, String footballLogId, Integer setStatus, Integer oldStatus) {
        Map param = new HashMap<>();
        param.put("userId", userId);
        param.put("footballLogId", footballLogId);
        param.put("setStatus", setStatus);
        param.put("oldStatus", oldStatus);
        return sqlSessionTemplate.update("userBuyRecommend.updateWithdrawStatus", param);
    }

    @Override
    public Integer insert(UserBuyRecommend userBuyRecommend) {
        return sqlSessionTemplate.insert("userBuyRecommend.insert", userBuyRecommend);
    }

    @Override
    public Integer updateUserRecommendAwardStatus(Long userPrefix, String footballLogId, Integer awardStatus) {
        Map param = new HashMap<>();
        param.put("userId", userPrefix);
        param.put("footballLogId", footballLogId);
        param.put("awardStatus", awardStatus);
        return sqlSessionTemplate.update("userBuyRecommend.updateUserRecommendAwardStatus", param);
    }

    @Override
    public Map<String, Object> getCouponAmountAndCountFromOtter(Timestamp begin, Timestamp end) {
        Map<String, Object> param = new HashMap<>();
        param.put("begin", begin);
        param.put("end", end);
        return otterSqlSessionTemplate.selectOne("userBuyRecommend.getCouponAmountAndCountFromOtter", param);
    }

    @Override
    public List<Map<String,Object>> getNotCouponOrderFromOtter(Timestamp begin, Timestamp end) {
        Map<String, Object> param = new HashMap<>();
        param.put("begin", begin);
        param.put("end", end);
        return otterSqlSessionTemplate.selectList("userBuyRecommend.getNotCouponOrderFromOtter", param);
    }
}
