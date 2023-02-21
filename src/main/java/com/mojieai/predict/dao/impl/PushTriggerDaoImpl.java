package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PushTriggerDao;
import com.mojieai.predict.entity.po.PushTrigger;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PushTriggerDaoImpl extends BaseDao implements PushTriggerDao {
    @Override
    public List<PushTrigger> getAllPushRecords() {
        Map<String, Object> params = new HashMap<>();
        return sqlSessionTemplate.selectList("PushTrigger.getAllPushRecords", params);
    }

    @Override
    public List<PushTrigger> getAllNeedPushRecords() {
        return sqlSessionTemplate.selectList("PushTrigger.getAllNeedPushRecords");
    }

    @Override
    public void update(PushTrigger pushTrigger) {
        sqlSessionTemplate.update("PushTrigger.update", pushTrigger);
    }

    @Override
    public void insert(PushTrigger pushTrigger) {
        sqlSessionTemplate.insert("PushTrigger.insert", pushTrigger);
    }
}
