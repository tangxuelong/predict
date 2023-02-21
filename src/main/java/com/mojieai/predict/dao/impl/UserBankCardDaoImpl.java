package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserBankCardDao;
import com.mojieai.predict.entity.po.UserBankCard;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserBankCardDaoImpl extends BaseDao implements UserBankCardDao {

    @Override
    public UserBankCard getUserBankCardById(Long userId, Integer bankId) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("bankId", bankId);
        return sqlSessionTemplate.selectOne("UserBankCard.getUserBankCardById", param);
    }

    @Override
    public UserBankCard getUserBankCardByBankNo(Long userId, String bankCard) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("bankCard", bankCard);
        return sqlSessionTemplate.selectOne("UserBankCard.getUserBankCardByBankNo", param);
    }

    @Override
    public Integer getUserBankCardCount(Long userId) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        return slaveSqlSessionTemplate.selectOne("UserBankCard.getUserBankCardCount", param);
    }

    @Override
    public List<UserBankCard> getUserAllBankCard(Long userId, Integer cardType) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("cardType", cardType);
        return sqlSessionTemplate.selectList("UserBankCard.getUserAllBankCard", param);
    }

    @Override
    public int updateBankCard(Long userId, Integer bankId, String realName, int status) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("bankId", bankId);
        param.put("status", status);
        param.put("accountName", realName);
        return sqlSessionTemplate.update("UserBankCard.updateBankCardStatus", param);
    }

    @Override
    public Integer updateBankCardStatus(Long userId, Integer bankId, int status) {
        return updateBankCard(userId, bankId, null, status);
    }

    @Override
    public Integer insert(UserBankCard userBankCard) {
        return sqlSessionTemplate.insert("UserBankCard.insert", userBankCard);
    }
}
