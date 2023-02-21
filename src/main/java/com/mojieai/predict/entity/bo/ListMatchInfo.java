package com.mojieai.predict.entity.bo;

import com.mojieai.predict.util.SportsUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class ListMatchInfo {
    private String matchId;
    private String matchName;
    private String matchTime;
    private String matchDate;
    private String hostName;
    private String awayName;
    private String hostImg;
    private String awayImg;
    private Integer matchStatus;
    private String btnName;
    private Integer isRecommend;
    private Timestamp matchTimeDetail;
    private String tag;//标签

    public ListMatchInfo(String matchId, String matchName, String matchTime, String matchDate, String hostName, String
            awayName, String hostImg, String awayImg, Integer matchStatus, String btnName, Integer isRecommend,
                         Timestamp matchTimeDetail, String tag) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.matchTime = matchTime;
        this.matchDate = matchDate;
        this.hostName = hostName;
        this.awayName = awayName;
        this.hostImg = SportsUtils.dealMatchImg(hostImg);
        this.awayImg = SportsUtils.dealMatchImg(awayImg);
        this.matchStatus = matchStatus;
        this.btnName = btnName;
        this.isRecommend = isRecommend;
        this.matchTimeDetail = matchTimeDetail;
        this.tag = tag;
    }
}
