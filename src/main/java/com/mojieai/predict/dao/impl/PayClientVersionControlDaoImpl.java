package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.PayClientVersionControlDao;
import com.mojieai.predict.entity.po.PayClientVersionControl;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PayClientVersionControlDaoImpl extends BaseDao implements PayClientVersionControlDao {
    @Override
    public List<PayClientVersionControl> getAllPayClientVersionControl() {
        return sqlSessionTemplate.selectList("PayClientVersionControl.getAllPayClientVersionControl");
    }

    @Override
    public PayClientVersionControl getPayClientVersionControl(Integer clientId, Integer channelId, Integer
            versionCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);
        params.put("channelId", channelId);
        params.put("versionCode", versionCode);
        return sqlSessionTemplate.selectOne("PayClientVersionControl.getPayClientVersionControl", params);
    }

    @Override
    public void update(PayClientVersionControl payClientVersionControl) {
        sqlSessionTemplate.update("PayClientVersionControl.update", payClientVersionControl);
    }

    @Override
    public void insert(PayClientVersionControl payClientVersionControl) {
        sqlSessionTemplate.insert("PayClientVersionControl.insert", payClientVersionControl);
    }
}
