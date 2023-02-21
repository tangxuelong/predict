package com.mojieai.predict.entity.po;

import com.mojieai.predict.enums.CommonStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class MatchSchedule implements Serializable {
    private static final long serialVersionUID = 6489277833543220709L;

    private Integer matchId;
    private Integer lotteryCode;
    private Integer ifEnd;
    private Timestamp endTime;
    private Integer ifPurchaseLog;
    private Timestamp purchaseLogTime;
    private Integer ifWithdraw;
    private Timestamp withdrawTime;
    private Integer ifRank;
    private String leagueMatchName;
    private Timestamp rankTime;
    private Integer ifRankRedis;
    private Timestamp rankRedisTime;
    private Integer ifVipProgram;
    private Timestamp vipProgramTime;

    public MatchSchedule(Integer matchId, Integer lotteryCode, String leagueMatchName) {
        this.matchId = matchId;
        this.lotteryCode = lotteryCode;
        this.ifRank = CommonStatusEnum.NO.getStatus();
        this.leagueMatchName = leagueMatchName;
        this.ifRankRedis = 0;
        this.ifVipProgram = 1;
    }
}
