package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.TitleDao;
import com.mojieai.predict.entity.po.Title;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TitleDaoImpl extends BaseDao implements TitleDao {

    @Override
    public List<Title> getAllTitle() {
        return sqlSessionTemplate.selectList("Title.getAllTitle");
    }
}
