package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.ButtonOrderedDao;
import com.mojieai.predict.entity.po.ButtonOrdered;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ButtonOrderedDaoImpl extends BaseDao implements ButtonOrderedDao {

    @Override
    public List<ButtonOrdered> getAllBtnOrdered() {
        return sqlSessionTemplate.selectList("ButtonOrdered.getAllBtnOrdered");
    }
}
