package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * Created by tangxuelong on 2017/10/16.
 */
@Data
@NoArgsConstructor
public class SocialKillAwardLevel {
    private Integer levelId;
    private Integer gameId;
    private Integer killNums;
    private Integer rank;
    private Integer rankScore;
    private Integer ballType;
    private Integer rightNums;
    private Timestamp createTime;
    private Timestamp updateTime;
}
