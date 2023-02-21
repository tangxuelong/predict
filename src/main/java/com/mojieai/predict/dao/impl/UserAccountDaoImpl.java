package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserAccountDao;
import com.mojieai.predict.entity.po.UserAccount;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserAccountDaoImpl extends BaseDao implements UserAccountDao {
    @Override
    public UserAccount getUserAccountBalance(Long userId, Integer accountType, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("accountType", accountType);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("UserAccount.getUserAccountBalance", params);
    }

    @Override
    public Integer update(UserAccount userAccount) {
        return sqlSessionTemplate.update("UserAccount.update", userAccount);
    }

    @Override
    public Integer updateUserBalance(Long userId, Integer accountType, Long setBalance, Long oldBalance) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("accountType", accountType);
        param.put("setBalance", setBalance);
        param.put("oldBalance", oldBalance);
        return sqlSessionTemplate.update("UserAccount.updateUserBalance", param);
    }

    @Override
    public void insert(UserAccount userAccount) {
        sqlSessionTemplate.insert("UserAccount.insert", userAccount);
    }
}
