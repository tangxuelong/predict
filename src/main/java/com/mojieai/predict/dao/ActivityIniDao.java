package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityIni;

import java.util.List;

public interface ActivityIniDao {
    List<ActivityIni> getAllIni();

    ActivityIni getIni(String iniName);

    void updateIni(ActivityIni activityIni);

    void insert(ActivityIni activityIni);

    List<Integer> monitorDB();
}
