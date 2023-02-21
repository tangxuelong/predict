package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.CronTabDao;
import com.mojieai.predict.entity.po.CronTab;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CronTabDaoImpl extends BaseDao implements CronTabDao {
    @Override
    public List<CronTab> getAllCronTab() {
        return sqlSessionTemplate.selectList("CronTab.getAllCronTab");
    }
}
