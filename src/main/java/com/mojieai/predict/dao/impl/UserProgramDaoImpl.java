package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserProgramDao;
import com.mojieai.predict.entity.po.UserProgram;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserProgramDaoImpl extends BaseDao implements UserProgramDao {
    @Override
    public UserProgram getUserProgramByUserProgramId(String userProgramId, boolean isLock) {
        Long userId = Long.valueOf(userProgramId.substring(userProgramId.length() - 2, userProgramId.length()));
        Map<String, Object> params = new HashMap<>();
        params.put("userProgramId", userProgramId);
        params.put("userId", userId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserProgram.getUserProgramByUserProgramId", params);
    }

    @Override
    public UserProgram getUserProgramByProgramId(Long userId, String programId, Long programPrice) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("userId", userId);
        params.put("programPrice", programPrice);
        return sqlSessionTemplate.selectOne("UserProgram.getUserProgramByProgramId", params);
    }

    @Override
    public List<UserProgram> getAllUserProgramByProgramId(String programId, Integer isPay, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("userId", userId);
        params.put("isPay", isPay);
        return sqlSessionTemplate.selectList("UserProgram.getAllUserProgramByProgramId", params);
    }

    @Override
    public List<UserProgram> getUserPrograms(Long gameId, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        return sqlSessionTemplate.selectList("UserProgram.getUserPrograms", params);
    }

    @Override
    public List<UserProgram> getUserAwardProgram(Long gameId, Long userId, Integer isAward) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("isAward", isAward);
        return sqlSessionTemplate.selectList("UserProgram.getUserAwardProgram", params);
    }

    @Override
    public List<UserProgram> getUserProgramsByLastPeriodId(long gameId, String maxPeriodId, String minPeriodId, Long
            userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("maxPeriodId", maxPeriodId);
        params.put("minPeriodId", minPeriodId);
        params.put("isPay", 1);
        return sqlSessionTemplate.selectList("UserProgram.getUserProgramsByLastPeriodId", params);
    }

    @Override
    public List<String> getUserProgramPagePeriodId(long gameId, String maxPeriodId, Long userId, int count) {
        Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("count", count);
        params.put("maxPeriodId", maxPeriodId);
        return sqlSessionTemplate.selectList("UserProgram.getUserProgramPagePeriodId", params);
    }

    @Override
    public void update(UserProgram userProgram) {
        sqlSessionTemplate.update("UserProgram.update", userProgram);
    }

    @Override
    public int insert(UserProgram userProgram) {
        return sqlSessionTemplate.insert("UserProgram.insert", userProgram);
    }

    @Override
    public int updateUserProgramPayStatus(String userProgramId, int isPayStatus) {
        Map param = new HashMap();
        Long userId = Long.valueOf(userProgramId.substring(userProgramId.length() - 2, userProgramId.length()));
        param.put("userProgramId", userProgramId);
        param.put("isPay", isPayStatus);
        param.put("userId", userId);
        return sqlSessionTemplate.update("UserProgram.updateUserProgramPayStatus", param);
    }

    @Override
    public int updateUserProgramRefundStatus(String userProgramId, Integer refundStatus) {
        Map param = new HashMap();
        Long userId = Long.valueOf(userProgramId.substring(userProgramId.length() - 2, userProgramId.length()));
        param.put("userProgramId", userProgramId);
        param.put("isReturnCoin", refundStatus);
        param.put("userId", userId);
        return sqlSessionTemplate.update("UserProgram.updateUserProgramRefundStatus", param);
    }
}
