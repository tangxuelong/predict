package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.Ini;

import java.util.List;

public interface IniDao {
    List<Ini> getAllIni();

    Ini getIni(String iniName);

    void updateIni(Ini ini);

    void insert(Ini ini);

    List<Integer> monitorDB();
}
