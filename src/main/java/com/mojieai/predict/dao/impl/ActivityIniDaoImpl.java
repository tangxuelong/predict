package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityIniDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.ActivityIni;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ActivityIniDaoImpl extends BaseDao implements ActivityIniDao {
    @Override
    public List<ActivityIni> getAllIni() {
        return sqlSessionTemplate.selectList("ActivityIni.getAllIni");
    }

    @Override
    public ActivityIni getIni(String iniName) {
        return sqlSessionTemplate.selectOne("ActivityIni.getIni", iniName);
    }

    @Override
    public void updateIni(ActivityIni activityIni) {
        sqlSessionTemplate.update("ActivityIni.update", activityIni);
    }

    @Override
    public void insert(ActivityIni activityIni) {
        sqlSessionTemplate.insert("ActivityIni.insert", activityIni);
    }

    @Override
    public List<Integer> monitorDB() {
        return sqlSessionTemplate.selectList("ActivityIni.monitorDB");
    }
}
