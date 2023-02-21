package com.mojieai.predict.dao;

import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserStatisticTable;
import com.mojieai.predict.entity.po.UserStatisticTableDay;

public interface UserStatisticTableDayDao {
    PaginationList<UserStatisticTableDay> getUserStatisticTableDayByPage(Integer page);

    UserStatisticTableDay getUserStatisticTableDayByDateId(Integer dateId);

    void update(UserStatisticTableDay userStatisticTableDay);

    void insert(UserStatisticTableDay userStatisticTableDay);
}
