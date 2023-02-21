package com.mojieai.predict.dao;

import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserDeviceWeekReport;

import java.util.List;

public interface UserDeviceWeekReportDao {

    PaginationList<UserDeviceWeekReport> getUserStatisticTableWeekByPage(Integer page);

    UserDeviceWeekReport getUserDeviceWeekReportByDate(String dateId);

    Integer update(UserDeviceWeekReport userDeviceWeekReport);

    Integer insert(UserDeviceWeekReport userDeviceWeekReport);
}
