package com.mojieai.predict.dao;

import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserDeviceMonthReport;
import com.mojieai.predict.entity.po.UserDeviceWeekReport;

import java.util.List;

public interface UserDeviceMonthReportDao {

    PaginationList<UserDeviceMonthReport> getUserStatisticTableMonthByPage(Integer page);

    UserDeviceMonthReport getUserDeviceMonthReportByDate(String dateId);

    Integer update(UserDeviceMonthReport userDeviceMonthReport);

    Integer insert(UserDeviceMonthReport userDeviceMonthReport);
}
