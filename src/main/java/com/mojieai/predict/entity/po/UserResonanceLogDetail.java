package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserResonanceLogDetail implements Serializable{
    private static final long serialVersionUID = -5181335592570900338L;

    private Long userId;
    private long gameId;
    private String resonanceLogId;
    private Integer periodId;
    private Integer isPay;
    private Timestamp createTime;
    private Timestamp updateTime;
}
