package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserTitleLog implements Serializable {
    private static final long serialVersionUID = 3680485618032899071L;

    private long gameId;
    private String titleLogId;
    private Long userId;
    private Integer titleId;
    private Integer dateNum;
    private String memo;
    private Timestamp createTime;
    private Integer isDistribute;
    private String dateStr;
}
