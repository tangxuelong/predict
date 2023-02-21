package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.util.SportsUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class UserBuyRecommend implements Serializable {
    private static final long serialVersionUID = 6541601367462825637L;

    private String footballLogId;
    private String programId;
    private Long userId;
    private Long programAmount;
    private Long payAmount;
    private Long withdrawAmount;
    private Integer withdrawStatus;
    private Integer payStatus;
    private Integer lotteryCode;
    private String matchId;
    private Integer playType;
    private String remark;
    private Integer awardStatus;
    private Timestamp createTime;
    private Timestamp updateTime;


    public void initUserBuyRecommend(String footballLogId, String programId, Long userId, Long programAmount,
                                           Long payAmount, Long withdrawAmount, Integer lotteryCode, String matchId,
                                           Integer playType) {
        this.footballLogId = footballLogId;
        this.programId = programId;
        this.userId = userId;
        this.programAmount = programAmount;
        this.payAmount = payAmount;
        this.withdrawAmount = withdrawAmount;
        this.withdrawStatus = SportsProgramConstant.PROGRAM_LOG_WITHDRAW_STATUS_WAIT;
        this.payStatus = CommonConstant.PROGRAM_IS_PAY_NO;
        this.remark = SportsUtils.getUserBuyRecommendRemark(programAmount, payAmount, withdrawAmount);
        this.lotteryCode = lotteryCode;
        this.matchId = matchId;
        this.playType = playType;
    }
}
