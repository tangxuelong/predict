package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ExchangeMall;

import java.util.List;

public interface ExchangeMallDao {
    List<ExchangeMall> getExchangeMallList(Integer itemType);

    List<ExchangeMall> getExchangeMallList(Integer itemType, Long gameId, Integer clientId);

    ExchangeMall getExchangeMall(Integer itemId);

    List<ExchangeMall> getExchangeMallsByAccountType(Integer accountType);

    void insert(ExchangeMall exchangeMall);

    void update(ExchangeMall exchangeMall);
}
