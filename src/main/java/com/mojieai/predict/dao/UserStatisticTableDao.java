package com.mojieai.predict.dao;

import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.UserStatisticTable;

import java.util.List;

public interface UserStatisticTableDao {
    PaginationList<UserStatisticTable> getUserStatisticTableByPage(Integer page);

    Integer countRecords();

    UserStatisticTable getUserStatisticTableByDateId(Integer dateId);

    void update(UserStatisticTable userStatisticTable);

    void insert(UserStatisticTable userStatisticTable);
}
