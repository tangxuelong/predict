package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserTitle implements Serializable {
    private static final long serialVersionUID = 1587681000715928499L;

    private Long gameId;
    private Long userId;
    private Integer titleId;
    private Timestamp endTime;
    private Integer counts;
    private Timestamp updateTime;

    public UserTitle(Long userId, Integer titleId) {
        this.userId = userId;
        this.titleId = titleId;
        this.endTime = null;
        this.counts = 0;
    }
}
