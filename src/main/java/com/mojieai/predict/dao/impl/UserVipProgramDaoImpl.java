package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserVipProgramDao;
import com.mojieai.predict.dao.VipProgramDao;
import com.mojieai.predict.entity.po.UserVipProgram;
import com.mojieai.predict.entity.po.VipProgram;
import com.mojieai.predict.util.CommonUtil;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserVipProgramDaoImpl extends BaseDao implements UserVipProgramDao {

    @Override
    public UserVipProgram getUserVipProgramByPk(String prePayId) {
        Map<String, Object> param = new HashMap<>();
        param.put("prePayId", prePayId);
        param.put("userId", CommonUtil.getUserIdSuffix(prePayId));
        return sqlSessionTemplate.selectOne("UserVipProgram.getUserVipProgramByPk", param);
    }

    @Override
    public UserVipProgram getUserVipProgramByUnkey(Long userId, String programId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("programId", programId);
        return sqlSessionTemplate.selectOne("UserVipProgram.getUserVipProgramByUnkey", params);
    }

    @Override
    public List<UserVipProgram> getUserVipProgram(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return sqlSessionTemplate.selectList("UserVipProgram.getUserVipProgram", params);
    }

    @Override
    public Integer update(UserVipProgram userVipProgram) {
        return sqlSessionTemplate.update("UserVipProgram.update", userVipProgram);
    }

    @Override
    public Integer updatePayedStatus(String prePayId, Integer setPayStatus, Integer oldPayStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("prePayId", prePayId);
        params.put("userId", CommonUtil.getUserIdSuffix(prePayId));
        params.put("setPayStatus", setPayStatus);
        params.put("oldPayStatus", oldPayStatus);
        return sqlSessionTemplate.update("UserVipProgram.updatePayedStatus", params);
    }

    @Override
    public Integer insert(UserVipProgram userVipProgram) {
        return sqlSessionTemplate.insert("UserVipProgram.insert", userVipProgram);
    }
}
