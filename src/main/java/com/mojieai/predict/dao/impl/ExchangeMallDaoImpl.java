package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.ExchangeMallDao;
import com.mojieai.predict.entity.po.ExchangeMall;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ExchangeMallDaoImpl extends BaseDao implements ExchangeMallDao {
    @Override
    public List<ExchangeMall> getExchangeMallList(Integer itemType) {
        return getExchangeMallList(itemType, null, null);
    }

    @Override
    public List<ExchangeMall> getExchangeMallList(Integer itemType, Long gameId, Integer clientId) {
        Map<String, Object> params = new HashMap<>();
        params.put("itemType", itemType);
        params.put("gameId", gameId);
        params.put("clientId", clientId);
        return sqlSessionTemplate.selectList("ExchangeMall.getExchangeMallList", params);
    }

    @Override
    public ExchangeMall getExchangeMall(Integer itemId) {
        Map<String, Object> params = new HashMap<>();
        params.put("itemId", itemId);
        return sqlSessionTemplate.selectOne("ExchangeMall.getExchangeMall", params);

    }

    @Override
    public List<ExchangeMall> getExchangeMallsByAccountType(Integer accountType) {
        Map<String, Object> params = new HashMap<>();
        params.put("accountType", accountType);
        return sqlSessionTemplate.selectOne("ExchangeMall.getExchangeMallsByAccountType", params);
    }

    @Override
    public void insert(ExchangeMall exchangeMall) {
        sqlSessionTemplate.update("ExchangeMall.insert", exchangeMall);
    }

    @Override
    public void update(ExchangeMall exchangeMall) {
        sqlSessionTemplate.update("ExchangeMall.update", exchangeMall);
    }
}
