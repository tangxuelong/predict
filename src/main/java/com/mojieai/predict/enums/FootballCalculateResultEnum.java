package com.mojieai.predict.enums;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.SportsProgramConstant;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public enum FootballCalculateResultEnum {
    SPF(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF) {
        @Override
        public Integer getHitOption(String hostScore, String awardScore, String handicap) {
            if (hostScore.equals(awardScore)) {
                return CommonConstant.FOOTBALL_SPF_ITEM_P;
            } else if (Integer.valueOf(hostScore) > Integer.valueOf(awardScore)) {
                return CommonConstant.FOOTBALL_SPF_ITEM_S;
            }
            return CommonConstant.FOOTBALL_SPF_ITEM_F;
        }
    }, RQSPF(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF) {
        @Override
        public Integer getHitOption(String hostScore, String awardScore, String handicap) {
            Integer hostRealScore = Integer.valueOf(hostScore) + Integer.valueOf(handicap);
            Integer awardRealScore = Integer.valueOf(awardScore);
            if (hostRealScore > awardRealScore) {
                return CommonConstant.FOOTBALL_RQ_SPF_ITEM_S;
            } else if (hostRealScore.equals(awardRealScore)) {
                return CommonConstant.FOOTBALL_RQ_SPF_ITEM_P;
            }
            return CommonConstant.FOOTBALL_RQ_SPF_ITEM_F;
        }
    }, ASIA(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA) {
        @Override
        public Integer getHitOption(String hostScore, String awardScore, String handicap) {
            if (StringUtils.isBlank(handicap)) {
                return null;
            }

            BigDecimal hostScoreB = new BigDecimal(hostScore);
            BigDecimal awayScoreB = new BigDecimal(awardScore);
            if (hostScoreB.add(new BigDecimal(handicap)).compareTo(awayScoreB) == 1) {
                return CommonConstant.FOOTBALL_RQ_ASIA_ITEM_S;
            }
            if (hostScoreB.add(new BigDecimal(handicap)).compareTo(awayScoreB) == 0) {
                return CommonConstant.FOOTBALL_RQ_ASIA_ITEM_P;
            }
            return CommonConstant.FOOTBALL_RQ_ASIA_ITEM_F;
        }
    }, BI_FEN(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_SCORE) {
        @Override
        public Integer getHitOption(String hostScore, String awardScore, String handicap) {
            return null;
        }

        @Override
        public String getHitOptionStr(String hostScore, String awardScore, String handicap) {
            if (hostScore == null || awardScore == null) {
                return "";
            }
            return hostScore + ":" + awardScore;
        }

    }, SCORE(200, SportsProgramConstant.FOOTBALL_PLAY_TYPE_GOAL) {
        @Override
        public Integer getHitOption(String hostScore, String awardScore, String handicap) {
            return null;
        }

        @Override
        public String getHitOptionStr(String hostScore, String awardScore, String handicap) {
            if (hostScore == null || awardScore == null) {
                return "";
            }
            return Integer.valueOf(hostScore) + Integer.valueOf(awardScore) + "";
        }

    };

    private Integer lotteryCode;
    private Integer playType;

    FootballCalculateResultEnum(Integer lotteryCode, Integer playType) {
        this.lotteryCode = lotteryCode;
        this.playType = playType;
    }

    public Integer getLotteryCode() {
        return lotteryCode;
    }

    public Integer getPlayType() {
        return playType;
    }

    public static FootballCalculateResultEnum getEnum(Integer playType) {
        for (FootballCalculateResultEnum fcre : FootballCalculateResultEnum.values()) {
            if (fcre.getPlayType().equals(playType)) {
                return fcre;
            }
        }
        return null;
    }

    public abstract Integer getHitOption(String hostScore, String awardScore, String handicap);

    public String getHitOptionStr(String hostScore, String awardScore, String handicap) {
        Integer hit = getHitOption(hostScore, awardScore, handicap);
        if (hit == null) {
            return "";
        }
        return hit + "";
    }
}
