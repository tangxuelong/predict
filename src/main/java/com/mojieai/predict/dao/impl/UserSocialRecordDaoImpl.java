package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserSocialRecordDao;
import com.mojieai.predict.entity.po.UserSocialRecord;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserSocialRecordDaoImpl extends BaseDao implements UserSocialRecordDao {

    @Override
    public UserSocialRecord getUserSocialRecordByUserIdAndPeriodIdAndType(long gameId, Long userId, String periodId,
                                                                          Integer recordType) {
        List<UserSocialRecord> result = null;
        result = getUserSocialRecordByCondition(gameId, periodId, userId, recordType);
        if (result == null || result.size() <= 0) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public UserSocialRecord getLatestUserSocialRecord(long gameId, Long userId, Integer recordType) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("recordType", recordType);
        return sqlSessionTemplate.selectOne("UserSocialRecord.getLatestUserSocialRecord", params);
    }

    @Override
    public List<UserSocialRecord> getAllUserRecordByPeriodId(long gameId, Long userId, String periodId, Integer
            socialType) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("periodId", periodId);
        params.put("socialType", socialType);
        return sqlSessionTemplate.selectList("UserSocialRecord.getAllUserRecordByPeriodId", params);
    }

    @Override
    public List<UserSocialRecord> getUserSocialRecordByCondition(long gameId, String periodId, Long userId, Integer
            recordType) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("periodId", periodId);
        params.put("userId", userId);
        params.put("recordType", recordType);
        return sqlSessionTemplate.selectList("UserSocialRecord.getUserSocialRecordByCondition", params);
    }

    @Override
    public String getLatestUserSocialRecordBySocialType(long gameId, Long userId, Integer socialType) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("socialType", socialType);
        return sqlSessionTemplate.selectOne("UserSocialRecord.getLatestUserSocialRecordBySocialType", params);
    }

    @Override
    public int insert(UserSocialRecord userSocialRecord) {
        return sqlSessionTemplate.insert("UserSocialRecord.insert", userSocialRecord);
    }


}
