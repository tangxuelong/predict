package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityAwardLevelDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserStatisticTableDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.UserStatisticTable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserStatisticTableDaoImpl extends BaseDao implements UserStatisticTableDao {
    @Override
    public PaginationList<UserStatisticTable> getUserStatisticTableByPage(Integer page) {
        PaginationInfo paginationInfo = new PaginationInfo(page, PaginationInfo.defaultRecordPerPage);
        return selectPaginationList("UserStatisticTable.getUserStatisticTableByPage", paginationInfo);
    }

    @Override
    public Integer countRecords() {
        return sqlSessionTemplate.selectOne("UserStatisticTable.countRecords");
    }

    @Override
    public UserStatisticTable getUserStatisticTableByDateId(Integer dateId) {
        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        return sqlSessionTemplate.selectOne("UserStatisticTable.getUserStatisticTableByDateId", params);

    }

    @Override
    public void update(UserStatisticTable userStatisticTable) {
        sqlSessionTemplate.update("UserStatisticTable.update", userStatisticTable);
    }

    @Override
    public void insert(UserStatisticTable userStatisticTable) {
        sqlSessionTemplate.insert("UserStatisticTable.insert", userStatisticTable);
    }
}
