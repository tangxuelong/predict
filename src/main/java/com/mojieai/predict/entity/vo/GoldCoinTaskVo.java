package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class GoldCoinTaskVo implements Serializable {
    private static final long serialVersionUID = -369436322581680150L;

    private String taskType;
    private String taskAward;
    private String taskName;
    private String taskDate;
    private Integer taskStatus;
    private String taskStatusText;
}
