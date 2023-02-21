package com.mojieai.predict.entity.bo;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class DetailMatchInfo {
    private String matchId;
    private String matchName;
    private String matchTime;
    private Timestamp endTime;
    private String matchDate;
    private String hostName;
    private String awayName;
    private String hostImg;
    private String awayImg;
    private Integer matchStatus;
    private Integer hostScore;
    private Integer awayScore;
    private String tag;
    private String halfScore;
    private String liveDesc;
    private Map<String, Object> spf;
    private Map<String, Object> rqSpf;
    private Map<String, Object> asia;
    private Map<String, Object> score;
    private Map<String, Object> goal;
    private Map<String, Object> bqc;

    public DetailMatchInfo(String matchId, String matchName, String matchTime, String matchDate, String hostName,
                           String awayName, String hostImg, String awayImg, Map<String, Object> spf, Map<String,
            Object> rqSpf, Map<String, Object> asia, Map<String, Object> score, Map<String, Object> goal, Map<String, Object> bqc,
                           Integer matchStatus, Integer hostScore, Integer awayScore, Timestamp endTime, String tag,
                           String halfScore, String liveDesc) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.matchTime = matchTime;
        this.matchDate = matchDate;
        this.hostName = hostName;
        this.awayName = awayName;
        this.hostImg = hostImg;
        this.awayImg = awayImg;
        this.spf = spf;
        this.rqSpf = rqSpf;
        this.asia = asia;
        this.score = score;
        this.goal = goal;
        this.bqc = bqc;
        this.matchStatus = matchStatus;
        this.hostScore = hostScore;
        this.awayScore = awayScore;
        this.endTime = endTime;
        this.tag = tag;
        this.halfScore = halfScore;
        this.liveDesc = liveDesc;
    }

    public Map<String, Object> getOddsInfo(Integer playType) {
        if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
            return this.spf;
        } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
            return this.rqSpf;
        } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
            return this.asia;
        } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SCORE)) {
            return this.score;
        } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_GOAL)) {
            return this.goal;
        }else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_BQC)) {
            return this.bqc;
        }
        return null;
    }

    // 根据玩法类型获取盘口
    public String getHandicap(Integer playType) {
        Map<String, Object> odds = getOddsInfo(playType);
        if (odds == null || !odds.containsKey("handicap") || null == odds.get("handicap")) {
            return "0";
        }
        return odds.get("handicap").toString();
    }

    public String getItemOdd(Integer playType, String option) {
        String result = " ";
        Map<String, Object> odds = getOddsInfo(playType);

        if (odds == null || odds.isEmpty()) {
            return result;
        }

        List<Map<String, Object>> odd = (List<Map<String, Object>>) odds.get("odds");
        for (Map<String, Object> o : odd) {
            if (o.get("recommendInfo").toString().equals(option)) {
                result = o.get("odd").toString();
            }
        }

        return result;
    }

    public String getItemOddByName(Integer playType, String name) {
        String result = " ";
        Map<String, Object> odds = getOddsInfo(playType);

        if (odds == null || odds.isEmpty()) {
            return result;
        }

        List<Map<String, Object>> odd = (List<Map<String, Object>>) odds.get("odds");
        for (Map<String, Object> o : odd) {
            if (o.get("name").toString().equals(name)) {
                result = o.get("odd").toString();
            }
        }

        return result;
    }

    // 获取推荐和赔率
    public String getRecommendInfo(Integer playType, String recommendInfo) {
        Map<String, Object> odds = getOddsInfo(playType);
        StringBuffer stringBuffer = new StringBuffer();
        Integer index = 0;
        for (String recommend : recommendInfo.split(CommonConstant.COMMA_SPLIT_STR)) {

            List<Map<String, Object>> odd = (List<Map<String, Object>>) odds.get("odds");
            for (Map<String, Object> o : odd) {
                if (o.get("recommendInfo").toString().equals(recommend)) {
                    if (!index.equals(0)) {
                        stringBuffer.append(CommonConstant.COMMA_SPLIT_STR);
                    }
                    stringBuffer.append(recommend).append(CommonConstant.COMMON_COLON_STR).append(o.get
                            ("odd").toString());
                }
            }
            index++;
        }
        return stringBuffer.toString();
    }
}
