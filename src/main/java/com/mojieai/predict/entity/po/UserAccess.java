package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 账户余额
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class UserAccess {
    private Long userId;
    private Long gameId;
    private String periodId;
    private String accessList;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserAccess(Long userId, Long gameId, String period, String accessList) {
        this.userId = userId;
        this.gameId = gameId;
        this.periodId = period;
        this.accessList = accessList;
    }
}