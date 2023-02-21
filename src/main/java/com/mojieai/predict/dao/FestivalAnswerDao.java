package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.FestivalAnswer;

import java.util.List;

public interface FestivalAnswerDao {
    List<FestivalAnswer> getAnswerByQuestionId(String questionId);

    FestivalAnswer getRightAnswer(String questionId);
}
