package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserNumberBook implements Serializable {
    private static final long serialVersionUID = -8725774887090515042L;

    private String numId;
    private Long userId;
    private Long gameId;
    private String periodId;
    private Integer numType;
    private String nums;
    private Integer numCount;
    private Integer ifAward;
    private String awardDesc;
    private Integer isEnable;
    private Timestamp createTime;
}
