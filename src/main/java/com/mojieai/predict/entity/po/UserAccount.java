package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.DateUtil;
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
public class UserAccount {
    private Long userId;           //用户ID
    private Long accountBalance;   //账户余额
    private Integer accountType;   //账户类型
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserAccount(Long userId, Integer accountType) {
        this.userId = userId;
        this.accountBalance = 0L;
        this.accountType = accountType;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}