package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserDeviceMonthReportDao;
import com.mojieai.predict.dao.UserDeviceWeekReportDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserDeviceMonthReport;
import com.mojieai.predict.entity.po.UserDeviceWeekReport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDeviceMonthReportDaoImpl extends BaseDao implements UserDeviceMonthReportDao {

    @Override
    public PaginationList<UserDeviceMonthReport> getUserStatisticTableMonthByPage(Integer page) {
        PaginationInfo paginationInfo = new PaginationInfo(page, PaginationInfo.defaultRecordPerPage);
        return selectPaginationList("UserDeviceMonthReport.getUserStatisticTableMonthByPage", paginationInfo);

    }

    @Override
    public UserDeviceMonthReport getUserDeviceMonthReportByDate(String dateId) {
        Map<String, Object> param = new HashMap<>();
        param.put("dateId", dateId);
        return slaveSqlSessionTemplate.selectOne("UserDeviceMonthReport.getUserDeviceMonthReportByDate", param);
    }

    @Override
    public Integer update(UserDeviceMonthReport userDeviceWeekReport) {
        return sqlSessionTemplate.update("UserDeviceMonthReport.update", userDeviceWeekReport);
    }

    @Override
    public Integer insert(UserDeviceMonthReport userDeviceMonthReport) {
        return sqlSessionTemplate.insert("UserDeviceMonthReport.insert", userDeviceMonthReport);
    }
}
