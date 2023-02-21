package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.FestivalQuestion;

import java.util.List;

public interface FestivalQuestionDao {
    List<FestivalQuestion> getQuestionByLevel(Integer level);

    FestivalQuestion getQuestionById(String questionId);
}
