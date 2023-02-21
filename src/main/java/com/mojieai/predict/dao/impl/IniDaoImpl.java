package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.IniDao;
import com.mojieai.predict.entity.po.Ini;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IniDaoImpl extends BaseDao implements IniDao {
    @Override
    public List<Ini> getAllIni() {
        return sqlSessionTemplate.selectList("Ini.getAllIni");
    }

    @Override
    public Ini getIni(String iniName) {
        return sqlSessionTemplate.selectOne("Ini.getIni", iniName);
    }

    @Override
    public void updateIni(Ini ini) {
        sqlSessionTemplate.update("Ini.update", ini);
    }

    @Override
    public void insert(Ini ini) {
        sqlSessionTemplate.insert("Ini.insert", ini);
    }

    @Override
    public List<Integer> monitorDB() {
        return sqlSessionTemplate.selectList("Ini.monitorDB");
    }
}
