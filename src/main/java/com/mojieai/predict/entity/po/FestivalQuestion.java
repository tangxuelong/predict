package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tangxuelong on 2018/2/9.
 */
@Data
@NoArgsConstructor
public class FestivalQuestion {
    private String questionId;
    private String questionText;
    private Integer questionLevel;
}
