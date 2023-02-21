package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Data
@NoArgsConstructor
public class SocialEncircleAwardLevel {
    private Long gameId;
    private Integer levelId;
    private Integer encircleNums;
    private Integer rightNums;
    private Integer rankScore;
    private Integer ballType;
    private Timestamp createTime;
    private Timestamp updateTime;
}
