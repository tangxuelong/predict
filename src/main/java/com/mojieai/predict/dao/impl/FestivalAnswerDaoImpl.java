package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.FestivalAnswerDao;
import com.mojieai.predict.entity.po.FestivalAnswer;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FestivalAnswerDaoImpl extends BaseDao implements FestivalAnswerDao {

    @Override
    public List<FestivalAnswer> getAnswerByQuestionId(String questionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("questionId", questionId);
        return sqlSessionTemplate.selectList("FestivalAnswer.getAnswerByQuestionId", params);
    }

    @Override
    public FestivalAnswer getRightAnswer(String questionId) {
        Map<String, Object> params = new HashMap<>();
        params.put("questionLevel", questionId);
        return sqlSessionTemplate.selectOne("FestivalAnswer.getRightAnswer", params);
    }
}
