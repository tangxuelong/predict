package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserTitleVo implements Serializable {
    private static final long serialVersionUID = -1630885082530857741L;

    private Timestamp godKillEndTime;
    private Timestamp godEncircleEndTime;
    private Integer godKillTimes;
    private Integer godEncircleTimes;
}
