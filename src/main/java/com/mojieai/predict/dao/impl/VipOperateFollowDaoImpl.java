package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.VipOperateFollowDao;
import com.mojieai.predict.entity.po.VipOperateFollow;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VipOperateFollowDaoImpl extends BaseDao implements VipOperateFollowDao {

    @Override
    public VipOperateFollow getVipFollowByVipOperateCode(String vipOperateCode, Long userId) {
        Map param = new HashMap<>();
        param.put("vipOperateCode", vipOperateCode);
        param.put("userId", userId);
        return sqlSessionTemplate.selectOne("VipOperateFollow.getVipFollowByVipOperateCode", param);
    }

    @Override
    public VipOperateFollow getVipFollowByFollowIdForUpdate(String vipOperateCode, boolean lock) {
        Map params = new HashMap();
        params.put("vipOperateCode", vipOperateCode);
        params.put("lock", lock);
        return sqlSessionTemplate.selectOne("VipOperateFollow.getVipFollowByFollowIdForUpdate", params);
    }

    @Override
    public Integer insert(VipOperateFollow vipOperateFollow) {
        return sqlSessionTemplate.insert("VipOperateFollow.insert", vipOperateFollow);
    }

    @Override
    public Integer updateVipOpreateFollowIsPay(String vipOperateCode, Integer isPay, String exchangeFlowId) {
        Map params = new HashMap();
        params.put("isPay", isPay);
        params.put("vipOperateCode", vipOperateCode);
        params.put("exchangeFlowId", exchangeFlowId);
        return sqlSessionTemplate.update("VipOperateFollow.updateVipOpreateFlowIsPay", params);
    }

    /**
     * 之后都是统计
     *
     * @param userId
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public List<Map> getAllOrderCountAndTotalAmount(String userId, Timestamp beginTime, Timestamp endTime) {
        Map params = new HashMap();

        params.put("vipOperateCode", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("isPay", null);
        return sqlSessionTemplate.selectList("VipOperateFollow.getOrderCountAndAmount", params);
    }

    @Override
    public List<Map> getOrderPersonCount(String userId, Timestamp beginTime, Timestamp endTime) {
        Map params = new HashMap();

        params.put("vipOperateCode", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("isPay", null);
        return sqlSessionTemplate.selectList("VipOperateFollow.getOrderPersonCount", params);
    }

    @Override
    public List<Map> getPaySuccessPersonCount(String userId, Timestamp beginTime, Timestamp endTime) {
        Map params = new HashMap();

        params.put("vipOperateCode", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("isPay", 1);
        return sqlSessionTemplate.selectList("VipOperateFollow.getOrderPersonCount", params);
    }

    @Override
    public List<Map> getPaySuccessOrderCount(String userId, Timestamp beginTime, Timestamp endTime) {
        Map params = new HashMap();

        params.put("vipOperateCode", userId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        params.put("isPay", 1);
        return sqlSessionTemplate.selectList("VipOperateFollow.getOrderCountAndAmount", params);
    }

    @Override
    public List<Map> getFisrtOrderPersons(String userId) {
        Map params = new HashMap();

        params.put("vipOperateCode", userId);
        params.put("isPay", null);
        return sqlSessionTemplate.selectList("VipOperateFollow.getFisrtOrderPersons", params);
    }

    @Override
    public List<Map> getFisrtPayPersons(String userId) {
        Map params = new HashMap();

        params.put("vipOperateCode", userId);
        params.put("isPay", 1);
        return sqlSessionTemplate.selectList("VipOperateFollow.getFisrtOrderPersons", params);
    }
}
