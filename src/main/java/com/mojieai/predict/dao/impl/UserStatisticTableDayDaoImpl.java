package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserStatisticTableDao;
import com.mojieai.predict.dao.UserStatisticTableDayDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserStatisticTable;
import com.mojieai.predict.entity.po.UserStatisticTableDay;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserStatisticTableDayDaoImpl extends BaseDao implements UserStatisticTableDayDao {
    @Override
    public PaginationList<UserStatisticTableDay> getUserStatisticTableDayByPage(Integer page) {
        PaginationInfo paginationInfo = new PaginationInfo(page, PaginationInfo.defaultRecordPerPage);
        return selectPaginationList("UserStatisticTableDay.getUserStatisticTableDayByPage", paginationInfo);
    }

    @Override
    public UserStatisticTableDay getUserStatisticTableDayByDateId(Integer dateId) {
        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        return sqlSessionTemplate.selectOne("UserStatisticTableDay.getUserStatisticTableDayByDateId", params);
    }

    @Override
    public void update(UserStatisticTableDay userStatisticTableDay) {
        sqlSessionTemplate.update("UserStatisticTableDay.update", userStatisticTableDay);
    }

    @Override
    public void insert(UserStatisticTableDay userStatisticTableDay) {
        sqlSessionTemplate.insert("UserStatisticTableDay.insert", userStatisticTableDay);
    }
}
