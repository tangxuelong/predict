package com.mojieai.predict.entity.vo;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.MatchInfo;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SportsUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TagMatchDetailVo implements Serializable, Comparable<TagMatchDetailVo> {
    private static final long serialVersionUID = -5354210124990150647L;

    private Integer matchId;
    private String hostName;
    private String awayName;
    private String hostImg;
    private String awayImg;
    private Integer matchStatus;
    private String matchStatusStr;
    private Timestamp matchTime;
    private String matchTimeStr;
    private Integer ifFirst;
    private Integer followStatus;
    private String halfScoreStr;
    private String score;
    private String matchDesc;
    private String predictDesc;
    private String hitImg;
    private String matchJumpUrl;
    private List<Map<String, String>> tags;

    public TagMatchDetailVo(String hostName, String awayName, String hostImg, String awayImg, Integer matchStatus,
                            Timestamp matchTime, String matchTimeStr, String halfScoreStr, String score, Integer
                                    followStatus, String matchDesc, String predictDesc, String jumpUrl,
                            List<Map<String, String>> tags) {
        this.hostName = hostName;
        this.awayName = awayName;
        this.hostImg = hostImg;
        this.awayImg = awayImg;
        this.matchStatus = matchStatus;
        this.matchStatusStr = SportsUtils.getMatchStatusCn(matchStatus);
        this.matchTime = matchTime;
        this.matchTimeStr = matchTimeStr;
        this.ifFirst = 0;
        this.halfScoreStr = halfScoreStr;
        this.score = score;
        this.followStatus = followStatus;
        this.tags = tags;
        this.matchDesc = matchDesc;
        this.predictDesc = predictDesc;
    }

    public TagMatchDetailVo(MatchInfo matchInfo, DetailMatchInfo detailMatchInfo, Integer followStatus, String
            predictDesc) {
        String score = "";
        if (detailMatchInfo.getHostScore() != null) {
            score = detailMatchInfo.getHostScore() + "-" + detailMatchInfo.getAwayScore();
        }

        String matchTimeStr = DateUtil.getTodayTomorrowAndAfterTomorrow(matchInfo.getMatchTime()) + DateUtil.formatTime
                (matchInfo.getMatchTime(), DateUtil.DATE_FORMAT_HHMM);
        String matchDesc = matchTimeStr + " " + detailMatchInfo.getMatchDate() + " " + detailMatchInfo.getMatchName();

        String matchStatusDesc = "";
        if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
            matchStatusDesc = detailMatchInfo.getLiveDesc().replace("'", "");
        } else {
            matchStatusDesc = SportsUtils.getMatchStatusCn(detailMatchInfo.getMatchStatus());
        }
        //比赛进行中和中场状态描述加上标红
        if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING) ||
                detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_MIDFIELD)) {
            matchStatusDesc = CommonUtil.packageColorHtmlTag2Str(matchStatusDesc, CommonConstant.COMMON_COLOR_RED);
            score = CommonUtil.packageColorHtmlTag2Str(score, CommonConstant.COMMON_COLOR_RED);
        }

        String hitImg = "";
        if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            hitImg = CommonUtil.getImgUrlWithDomain("cp_match_list_hit_award.png");
        }

        this.ifFirst = 0;
        this.score = score;
        this.hitImg = hitImg;
        this.matchDesc = matchDesc;
        this.followStatus = followStatus;
        this.predictDesc = predictDesc;
        this.matchStatusStr = matchStatusDesc;
        //direct get
        this.matchTime = matchInfo.getMatchTime();
        this.matchId = Integer.valueOf(detailMatchInfo.getMatchId());
        this.hostName = detailMatchInfo.getHostName();
        this.awayName = detailMatchInfo.getAwayName();
        this.hostImg = detailMatchInfo.getHostImg();
        this.awayImg = detailMatchInfo.getAwayImg();
        this.matchStatus = detailMatchInfo.getMatchStatus();
        this.halfScoreStr = detailMatchInfo.getHalfScore();
        this.matchTimeStr = detailMatchInfo.getMatchTime();
        this.tags = SportsUtils.getMatchTags(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF, detailMatchInfo.getTag());
        this.matchJumpUrl = SportsUtils.getMatchBottomPageJumpUrl(detailMatchInfo.getMatchId(), SportsProgramConstant
                .MATCH_BOTTOM_PAGE_PREDICT);
    }


    @Override
    public int compareTo(TagMatchDetailVo o) {
        if (o == null) {
            throw new IllegalArgumentException("标签赛事异常");
        }
        if (this.matchTime.compareTo(o.getMatchTime()) != 0) {
            return this.matchTime.compareTo(o.getMatchTime());
        }
        return this.matchId.compareTo(o.getMatchId());
    }
}
