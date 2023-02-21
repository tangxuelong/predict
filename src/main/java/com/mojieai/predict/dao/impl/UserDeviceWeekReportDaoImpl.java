package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.UserDeviceWeekReportDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserDeviceWeekReport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDeviceWeekReportDaoImpl extends BaseDao implements UserDeviceWeekReportDao {

    @Override
    public PaginationList<UserDeviceWeekReport> getUserStatisticTableWeekByPage(Integer page) {
        PaginationInfo paginationInfo = new PaginationInfo(page, PaginationInfo.defaultRecordPerPage);
        return selectPaginationList("UserDeviceWeekReport.getUserStatisticTableWeekByPage", paginationInfo);
    }

    @Override
    public UserDeviceWeekReport getUserDeviceWeekReportByDate(String dateId) {
        Map<String, Object> param = new HashMap<>();
        param.put("dateId", dateId);
        return slaveSqlSessionTemplate.selectOne("UserDeviceWeekReport.getUserDeviceWeekReportByDate", param);
    }

    @Override
    public Integer update(UserDeviceWeekReport userDeviceWeekReport) {
        return sqlSessionTemplate.update("UserDeviceWeekReport.update", userDeviceWeekReport);
    }

    @Override
    public Integer insert(UserDeviceWeekReport userDeviceWeekReport) {
        return sqlSessionTemplate.insert("UserDeviceWeekReport.insert", userDeviceWeekReport);
    }
}
