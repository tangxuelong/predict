package com.mojieai.predict.entity.po;

import com.mojieai.predict.enums.CommonStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PredictSchedule {
    private Long gameId;
    private String periodId;
    private Integer ifAward;
    private Timestamp ifAwardTime;
    private Integer ifPredict;
    private Timestamp ifPredictTime;
    private Integer ifHistoryWinning;
    private Timestamp ifHistoryWinningTime;
    private Integer ifPredictRedBallTwenty;
    private Timestamp ifPredictRedBallTwentyTime;
    private Integer ifAwardInfo;
    private Timestamp ifAwardInfoTime;
    private Integer ifHistoryWinBonus;
    private Timestamp ifHistoryWinBonusTime;
    private Integer ifPredictBlueThree;
    private Timestamp ifPredictBlueThreeTime;
    private Integer ifPredictLastKillCode;
    private Timestamp ifPredictLastKillCodeTime;
    private Timestamp createTime;

    public PredictSchedule(long gameId, String periodId, Timestamp createTime){
        this.gameId = gameId;
        this.periodId = periodId;
        this.ifAward = CommonStatusEnum.NO.getStatus();
        this.ifPredict = CommonStatusEnum.NO.getStatus();
        this.ifHistoryWinning = CommonStatusEnum.NO.getStatus();
        this.ifPredictRedBallTwenty = CommonStatusEnum.NO.getStatus();
        this.ifAwardInfo = CommonStatusEnum.NO.getStatus();
        this.ifPredictBlueThree = CommonStatusEnum.NO.getStatus();
        this.ifHistoryWinBonus = CommonStatusEnum.NO.getStatus();
        this.ifPredictLastKillCode = CommonStatusEnum.NO.getStatus();
        this.createTime = createTime;
    }
}
