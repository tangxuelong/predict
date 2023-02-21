package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.ProgramUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 活动奖级配置表
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class UserProgram {
    private String userProgramId;
    private Long userId;
    private String programId;
    private Long gameId;
    private String periodId;
    private Integer isReturnCoin;
    private Integer isAward;
    private Integer isPay;
    private Long payPrice;
    private Long programPrice;
    private String remark;
    private Integer programType;
    private Timestamp createTime;
    private Timestamp updateTime;

    public void initUserProgram(String userProgramId, Long userId, Program program, Long price, Long programPrice) {
        Integer isReturn = CommonConstant.PROGRAM_IS_RETURN_COIN_NO;
        if (program.getBuyType().equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE)) {
            isReturn = CommonConstant.PROGRAM_IS_RETURN_COIN_INIT;
        }
        this.userProgramId = userProgramId;
        this.userId = userId;
        this.programId = program.getProgramId();
        this.gameId = program.getGameId();
        this.periodId = program.getPeriodId();
        this.isReturnCoin = isReturn;
        this.isAward = CommonConstant.PROGRAM_IS_AWARD_NO;
        this.isPay = 0;
        this.programType = program.getProgramType();
        this.payPrice = price;
        this.programPrice = programPrice;
        this.remark = ProgramUtil.getRemark(program.getPrice(), program.getVipDiscount(), price);
    }
}