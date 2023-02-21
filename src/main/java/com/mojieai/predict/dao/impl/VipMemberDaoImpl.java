package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.VipMemberDao;
import com.mojieai.predict.entity.po.VipMember;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VipMemberDaoImpl extends BaseDao implements VipMemberDao {

    @Override
    public VipMember getVipMemberByUserId(Long userId, Integer vipType) {
        return getVipByUserIdForUpdate(userId, vipType,false);
    }

    @Override
    public VipMember getVipByUserIdForUpdate(Long userId, Integer vipType, Boolean isLock) {
        Map param = new HashMap();
        param.put("userId", userId);
        param.put("vipType", vipType);
        param.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("VipMember.getVipMemberByUserId", param);
    }

    @Override
    public Integer updateUserVipStatus(Long userId, Integer vipType, Integer status, Timestamp beginTime, Timestamp
            endTime) {
        Map param = new HashMap();
        param.put("userId", userId);
        param.put("vipType", vipType);
        param.put("status", status);
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        return sqlSessionTemplate.update("VipMember.updateUserVipStatus", param);
    }

    @Override
    public Integer insert(VipMember vipMember) {
        return sqlSessionTemplate.insert("VipMember.insert", vipMember);
    }

    @Override
    public List<VipMember> getVipMemberByExpireDate(Timestamp date) {
        Map param = new HashMap();
        param.put("date", date);
        return otterSqlSessionTemplate.selectList("VipMember.getVipMemberByExpireDate",param);
    }
}
