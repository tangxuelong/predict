package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tangxuelong on 2018/2/9.
 */
@Data
@NoArgsConstructor
public class FestivalAnswer {
    private String questionId;
    private String answerId;
    private String answerText;
    private Integer isRight;
}
