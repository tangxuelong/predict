package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserNumberBookDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserNumberBook;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserNumberBookDaoImpl extends BaseDao implements UserNumberBookDao {
    @Override
    public Integer getUserNumBookCount(long gameId, Long userId) {
        Map params = new HashMap();

        params.put("gameId", gameId);
        params.put("userId", userId);
        return sqlSessionTemplate.selectOne("UserNumberBook.getUserNumBookCount", params);
    }

    @Override
    public String getUserMostRomoteDateId(long gameId, Long userId) {
        Map params = new HashMap();

        params.put("gameId", gameId);
        params.put("userId", userId);
        return sqlSessionTemplate.selectOne("UserNumberBook.getUserMostRomoteDateId", params);
    }

    @Override
    public List<String> getCurrentPageLastPeriodId(long gameId, String lastNumId, Long userId, Integer periodSize) {
        Map params = new HashMap();

        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("lastNumId", lastNumId);
        params.put("periodSize", periodSize);
        return sqlSessionTemplate.selectList("UserNumberBook.getCurrentPageLastPeriodId", params);
    }

    @Override
    public List<UserNumberBook> getUserNumsByUserIdAndLastNumId(long gameId, Long userId, String lastNumId, String
            periodId) {
        Map params = new HashMap();

        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("lastNumId", lastNumId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("UserNumberBook.getUserNumsByUserIdAndLastNumId", params);
    }

    @Override
    public PaginationList<UserNumberBook> getUserNumsByUserId(long gameId, Long userId, Integer currentPage, Integer
            pageSize) {
        Map params = new HashMap<>();

        params.put("gameId", gameId);
        params.put("userId", userId);
        PaginationInfo paginationInfo = new PaginationInfo(currentPage, pageSize);
        return selectPaginationList("UserNumberBook.getUserNumsByUserId", params, paginationInfo);
    }

    @Override
    public Integer updateUserNumEnable(String numId, Long userId, Integer isEnable) {
        Map params = new HashMap();

        params.put("numId", numId);
        params.put("userId", userId);
        params.put("isEnable", isEnable);
        return sqlSessionTemplate.update("UserNumberBook.updateUserNumEnable", params);
    }

    @Override
    public Integer insert(UserNumberBook userNumberBook) {
        return sqlSessionTemplate.insert("UserNumberBook.insert", userNumberBook);
    }

    @Override
    public List<UserNumberBook> getOneTaleAllDataByPeriodId(Long userId, long gameId, String periodId) {
        Map params = new HashMap();

        params.put("gameId", gameId);
        params.put("userId", userId);
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("UserNumberBook.getOneTaleAllDataByPeriodId", params);
    }

    @Override
    public void updateUserNumBookNumsAndAwardDesc(String numId, Long userId, String nums, String awardDesc) {
        Map params = new HashMap();

        params.put("numId", numId);
        params.put("userId", userId);
        params.put("nums", nums);
        params.put("awardDesc", awardDesc);
        sqlSessionTemplate.update("UserNumberBook.updateUserNumBookNumsAndAwardDesc", params);
    }
}
