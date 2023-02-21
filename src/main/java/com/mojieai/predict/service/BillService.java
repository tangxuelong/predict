package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.ThirdPartyBillInfo;
import com.mojieai.predict.entity.po.UserStatisticTable;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface BillService {

    Map<String, Object> getDailyBill(Timestamp beginTime, Timestamp endTime);

    String wxReconciliation(String billDate);

    // 日常方案售卖统计
    void programSaleDailyStats();

    List<Map<String, Object>> getProgramSaleDailyStats(Integer beginDate, Integer endDate, Integer isVip);

    // 每日注册用户统计
    void userStatisticTable();

    Map<String, Object> getUserStatisticTable(Integer page);

    void productLastMonthUserStatistic();

    // 用户日报统计
    void userStatisticTableDay();

    // 用户周报统计
    void userStatisticTableWeek();

    // 用户月报统计
    void userStatisticTableMonth();

    Map<String, Object> getUserStatisticTableByType(Integer page, Integer type);

    void downloadYop();

    void downloadJDBill();

    void downloadJDBillCompensate(String date);


    void absolutePathJDBillFileImport();

    List<ThirdPartyBillInfo> getYopHistoryAccount(Integer beginTime, Integer endTime, String mchId);

    List<ThirdPartyBillInfo> getJdHistoryAccount(Integer beginTime, Integer endTime, String mchId, String status,
                                                 String businessType);

    Map<String, Object> getWithdrawReportForm(Timestamp beginTimeT, Timestamp endTimeT);
}
