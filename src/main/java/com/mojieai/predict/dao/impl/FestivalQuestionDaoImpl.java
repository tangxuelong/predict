package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.FestivalQuestionDao;
import com.mojieai.predict.entity.po.FestivalQuestion;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FestivalQuestionDaoImpl extends BaseDao implements FestivalQuestionDao {

    @Override
    public List<FestivalQuestion> getQuestionByLevel(Integer level) {
        Map<String, Object> params = new HashMap<>();
        params.put("questionLevel", level);
        return sqlSessionTemplate.selectList("FestivalQuestion.getQuestionByLevel", params);
    }

    @Override
    public FestivalQuestion getQuestionById(String questionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("questionId", questionId);
        return sqlSessionTemplate.selectOne("FestivalQuestion.getQuestionById", params);
    }
}
