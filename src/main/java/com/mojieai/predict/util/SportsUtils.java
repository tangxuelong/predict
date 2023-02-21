package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.MatchInfo;
import com.mojieai.predict.entity.po.MatchTag;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import com.mojieai.predict.enums.FootballCalculateResultEnum;
import com.mojieai.predict.redis.base.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SportsUtils {
    public static Logger log = LogConstant.commonLog;

    public static Long getWithdrawAmountByDivided(Long payAmount) {
        if (payAmount == null) {
            return null;
        }
        Integer ratio = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.FOOTBALL_WITHDRAW_OCCUPY_RATIO,
                SportsProgramConstant.SPORT_WITHDRAW_DEFAULT_OCCUPY_RATIO);
        if (ratio >= SportsProgramConstant.SPORT_WITHDRAW_MAX_OCCUPY_RATIO) {
            log.error("activity ini 中配置方案分成用户占比 ＊＊＊" + ratio + "%,＊＊＊ 请及时确认");
        }
        return ProgramUtil.getVipPrice(payAmount, ratio).longValue();
    }

    public static String getUserBuyRecommendRemark(Long programAmount, Long payAmount, Long withdrawAmount) {
        Map<String, Object> res = new HashMap<>();
        if (programAmount != null) {
            res.put("programAmount", "方案原始价格:" + CommonUtil.convertFen2Yuan(programAmount) + "元");
        }
        if (payAmount != null) {
            res.put("payAmount", "用户支付金额：" + CommonUtil.convertFen2Yuan(payAmount) + "元");
        }
        if (withdrawAmount != null) {
            res.put("withdrawAmount", "推荐师可提现金额：" + CommonUtil.convertFen2Yuan(withdrawAmount) + "元");
            Integer ratio = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.FOOTBALL_WITHDRAW_OCCUPY_RATIO,
                    SportsProgramConstant.SPORT_WITHDRAW_DEFAULT_OCCUPY_RATIO);
            res.put("occupy", "平台给用户分成占比" + ratio + "%");
        }

        if (res.isEmpty()) {
            return null;
        }
        return JSONObject.toJSONString(res);
    }

    public static Map<String, Object> getSportProgramDetailMatchInfo(DetailMatchInfo match, Integer lotteryCode,
                                                                     Integer playType, UserSportSocialRecommend
                                                                             recommend, boolean permission, Integer
                                                                             versionCode) {
        Map<String, Object> res = new HashMap<>();
        String matchId = "";
        String hostName = "";
        String awayName = "";
        String matchDate = "";
        String matchTime = "";
        String handicap = "";
        Integer matchStatus = 0;
        String matchStatusDesc = "";
        String score = "";
        List<Map<String, Object>> odds = new ArrayList<>();
        String matchDesc = "";
        Boolean bug4ManualOperate = false;
        if (match != null) {
            //1.依据玩法获取赔率信息
            Map<String, Object> thirdOdds = match.getOddsInfo(playType);
            String handicapStr = "";
            if (StringUtils.isNotBlank(recommend.getHandicap())) {
                handicapStr = recommend.getHandicap();
            } else {
                handicapStr = thirdOdds.containsKey("handicap") ? thirdOdds.get("handicap").toString() : "";
            }
            handicap = SportsUtils.getHandicapCn(lotteryCode, playType, handicapStr);

            matchStatus = match.getMatchStatus();
//            if (recommend.getIsRight() != null && recommend.getIsRight().equals(SportsProgramConstant
//                    .RECOMMEND_STATUS_CANCEL)) {
//                matchStatus = SportsProgramConstant.SPORT_MATCH_STATUS_QUIT;
//            }
            String hostScore = match.getHostScore() == null ? "" : String.valueOf(match.getHostScore());
            String awardScore = match.getAwayScore() == null ? "" : String.valueOf(match.getAwayScore());
            //2.将赔率信息转成自己需要的展示方式
            if (thirdOdds.containsKey("odds") && thirdOdds.get("odds") != null) {
                List<Map<String, Object>> playTypeOdds = (List<Map<String, Object>>) thirdOdds.get("odds");
//                List<Integer> recommendInfos = SportsUtils.getUserRecommendOption(recommend.getRecommendInfo());
                Map<Integer, String> userRecommendMap = getUserRecommendOptionMap(recommend.getRecommendInfo());

                for (Map<String, Object> onePlayTypeOdds : playTypeOdds) {
                    Map<String, Object> tempOdds = new HashMap<>();

                    //赛事选项
                    Integer recommendInfo = Integer.valueOf(onePlayTypeOdds.get("recommendInfo").toString());
                    //赛果
                    Integer betResult = 0;
                    Integer betResultOption = null;
                    if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
                        betResultOption = getBetResult(hostScore, awardScore, handicapStr, lotteryCode, playType);
                        if (betResultOption.equals(recommendInfo)) {
                            betResult = 1;
                        }
                    }

                    //是否为推荐
                    if (!matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_INIT) && !matchStatus.equals
                            (SportsProgramConstant.SPORT_MATCH_STATUS_DELAY)) {
                        permission = true;
                    }

                    Integer ifRecommend = SportsProgramConstant.MATCH_COMPETITION_ITEM_RECOMMEND_NO;
                    if (permission && userRecommendMap.containsKey(recommendInfo)) {
                        ifRecommend = SportsProgramConstant.MATCH_COMPETITION_ITEM_RECOMMEND_YES;
                    }

                    String name = onePlayTypeOdds.get("name").toString();
                    String odd = onePlayTypeOdds.get("odd").toString();
                    if (StringUtils.isNotBlank(userRecommendMap.get(recommendInfo))) {
                        odd = userRecommendMap.get(recommendInfo);
                    }

                    if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA) && matchStatus.equals
                            (SportsProgramConstant.SPORT_MATCH_STATUS_END) && betResultOption.equals(CommonConstant
                            .FOOTBALL_RQ_ASIA_ITEM_F) && userRecommendMap.containsKey(betResultOption) &&
                            versionCode < CommonConstant.VERSION_CODE_4_1) {
                        matchStatus = SportsProgramConstant.SPORT_MATCH_STATUS_GOING;//todo
                        bug4ManualOperate = true;
                    }

                    tempOdds.put("name", name + " " + odd);
                    tempOdds.put("recommend", ifRecommend);
                    tempOdds.put("betResult", betResult);//0不是 1是)
                    tempOdds.put("optionId", recommendInfo);
                    odds.add(tempOdds);
                }
            }
            //赛事基础数据
            hostName = match.getHostName();
            awayName = match.getAwayName();
            matchId = match.getMatchId();
            matchDate = match.getMatchDate();
            matchTime = match.getMatchTime();

            matchDesc = match.getMatchName() + " " + matchDate;
            matchStatusDesc = DateUtil.formatTime(match.getEndTime(), "yyyy-MM-dd HH:mm");
            if (!matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_INIT)) {
                matchDesc = matchDesc + " " + matchTime;
                if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
                    matchStatusDesc = "已结束";
                } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
                    matchStatusDesc = "<font color='#ff5050'>比赛中</font>";
                } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_QUIT)) {
                    matchStatusDesc = "取消比赛不开奖";
                } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_DELAY)) {
                    matchStatusDesc = "比赛延期";
                }
                if (match.getHostScore() != null) {
                    score = hostScore + ":" + awardScore;
                }
            }
        }

        if (bug4ManualOperate) {
            matchStatusDesc = "已结束";
        }

        List<Map<String, String>> tags = SportsUtils.getMatchTags(playType, match.getTag());
        if (CommonUtil.getIosReview(versionCode) == 0) {
            tags = null;
            matchDesc = matchDesc.replaceAll("世界杯", "");
        }

        res.put("playName", getPlayTypeCn(lotteryCode, playType));
        res.put("playType", playType);
        res.put("matchDesc", matchDesc);
        res.put("score", score);
        res.put("hostName", hostName);
        res.put("awayName", awayName);
        res.put("matchStatus", matchStatus);// 0:未开赛 1:比赛中 2:已结束,3取消 4延期
        res.put("matchStatusDesc", matchStatusDesc);
        res.put("matchId", matchId);
        res.put("odds", odds);
        res.put("handicap", handicap);
        if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
            res.put("handicap", "");
        }
        res.put("asiaHandicap", handicap);
        res.put("hostImg", SportsUtils.dealMatchImg(match.getHostImg()));
        res.put("awardImg", SportsUtils.dealMatchImg(match.getAwayImg()));

        res.put("tags", tags);
        return res;
    }

    private static List<Integer> getUserRecommendOption(String recommendInfo) {
        List<Integer> recommendOption = new ArrayList<>();

        String[] recommendAndOdds = recommendInfo.split(CommonConstant.COMMA_SPLIT_STR);
        for (String recommendAndOdd : recommendAndOdds) {
            String[] options = recommendAndOdd.split(CommonConstant.COMMON_COLON_STR);

            if (StringUtils.isNotBlank(options[0])) {
                recommendOption.add(Integer.valueOf(options[0]));
            }
        }
        return recommendOption;
    }

    public static Map<Integer, String> getUserRecommendOptionMap(String recommendInfo) {
        if (StringUtils.isBlank(recommendInfo)) {
            return new HashMap<>();
        }
        Map<Integer, String> res = new HashMap<>();
        String[] recommendAndOdds = recommendInfo.split(CommonConstant.COMMA_SPLIT_STR);
        for (String recommendAndOdd : recommendAndOdds) {
            String[] options = recommendAndOdd.split(CommonConstant.COMMON_COLON_STR);
            if (StringUtils.isNotBlank(options[0])) {
                String odd = options.length > 1 ? options[1] : null;
                res.put(Integer.valueOf(options[0]), odd);
            }
        }
        return res;
    }

    public static String getHandicapCn(Integer lotteryCode, Integer playType, String handicap) {
        if (lotteryCode == 200) {
            if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
                String color = "#ff5050";
                Integer handicapInt = Integer.valueOf(handicap);
                if (handicapInt < 0) {
                    color = "#5DB729";
                } else {
                    handicap = "+" + handicap;
                }
                return "让球<font color='" + color + "'>" + handicap + "</font>";
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
                return handicap;
            }
        }
        return "";
    }

    public static List<String> getIntegerCelebritySportsRecommendsByRemark(String remark, DetailMatchInfo
            detailMatchInfo) {
        List<String> result = null;
        Map<String, Object> remarkMap = JSONObject.parseObject(remark, HashMap.class);

        if (remarkMap != null) {
            result = new ArrayList<>();
            String recommendMap = remarkMap.get("recommendMap").toString();
            Map<Integer, String> playTypeRecommend = JSONObject.parseObject(recommendMap, HashMap.class);
            for (Integer key : playTypeRecommend.keySet()) {
                String playTypeName = SportsUtils.getPlayTypeCn(200, key);
                String[] itemArr = playTypeRecommend.get(key).split(",");
                StringBuffer itemName = new StringBuffer();
                String hitOption = "";
                if (detailMatchInfo != null) {
                    if (detailMatchInfo.getMatchStatus().equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
                        hitOption = FootballCalculateResultEnum.getEnum(key).getHitOptionStr(detailMatchInfo
                                .getHostScore() + "", detailMatchInfo.getAwayScore() + "", detailMatchInfo.getHandicap(key));
                    }

                    // 结束的赛事只展示中奖选项  主胜(3.45) 平(1.23)  / <font color>主胜(3.45)</font>
                    for (String item : itemArr) {
                        String odd = detailMatchInfo.getItemOdd(key, item);
                        if (StringUtils.isNotBlank(odd)) {
                            odd = "(" + odd + ") ";
                        }
                        if (StringUtils.isNotBlank(hitOption)) {
                            if (hitOption.equals(item)) {
                                itemName.append("<font color = '#FF5050'>");
                                itemName.append(SportsUtils.getItemCn(200, key, item) + odd);
                                itemName.append("</font> ");
                            }
                        } else {
                            itemName.append(SportsUtils.getItemCn(200, key, item) + odd);
                        }
                    }
                    if (StringUtils.isNotBlank(itemName.toString().trim())) {
                        if (key.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
                            String handicap = detailMatchInfo.getHandicap(key);
                            String color = "#1FBF43";
                            if (Integer.valueOf(handicap) > 0) {
                                color = "#FF5050";
                                handicap = "+" + handicap;
                            }
                            playTypeName = playTypeName + " <font color='" + color + "'>" + handicap + "</font>";
                        }
                        result.add(playTypeName + ": " + itemName.toString().trim());
                    }
                }
            }
        }
        return result;
    }

    public static String getMatchPredictInfo(RedisService redisService, String matchId) {
        String result = "";
        String key = RedisConstant.getSportSocialOneMatchRecommendListKey(matchId);
        List<Map<String, Object>> predicts = redisService.kryoZRevRangeByScoreGet(key, Long.MIN_VALUE, Long
                .MAX_VALUE, 0, 1, HashMap.class);

        String hitRatio = "88";
        if (predicts == null || predicts.size() == 0) {
            hitRatio = "0";
        } else {
            hitRatio = predicts.get(0).get("hitRate").toString();
        }

        result = getMatchPredictCount(redisService, matchId) + "位大神推单，最高胜率" + hitRatio + "%";
        return result;
    }

    public static Boolean matchInfoContainsTagId(MatchInfo matchInfo, Integer tagId) {
        if (matchInfo == null || StringUtils.isBlank(matchInfo.getMatchTagId())) {
            return Boolean.FALSE;
        }

        String[] tagIdArr = matchInfo.getMatchTagId().split(CommonConstant.COMMA_SPLIT_STR);
        Boolean result = false;
        for (String tempTagId : tagIdArr) {
            if (Integer.valueOf(tempTagId).equals(tagId)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static Boolean checkMatchBelongsToTag(MatchTag matchTag, DetailMatchInfo detailMatchInfo) {
        if (matchTag == null || detailMatchInfo == null) {
            return Boolean.FALSE;
        }

        if (matchTag.getTagId().equals(SportsProgramConstant.MATCH_TAG_SPORTTERY) && StringUtils.isNotBlank
                (detailMatchInfo.getMatchDate())) {
            return Boolean.TRUE;
        }

        if (detailMatchInfo.getMatchName().equals(matchTag.getTagName())) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    public static Long getMatchPredictCount(RedisService redisService, String matchId) {
        String key = RedisConstant.getSportSocialOneMatchRecommendListKey(matchId);
        Long predictCount = redisService.kryoZCount(key, Long.MIN_VALUE, Long.MAX_VALUE);
        return (predictCount == null ? 0L : predictCount);
    }

    public static String dealMatchImg(String hostImg) {
        if (StringUtils.isBlank(hostImg) || hostImg.equals(SportsProgramConstant.DEFAULT_OTHER_PLATE_TEAM_IMG)) {
            return SportsProgramConstant.DEFAULT_TEAM_IMG;
        }
        return hostImg;
    }

    public static String getPlayTypeCn(Integer lotteryCode, Integer playType) {
        String res = "";
        if (lotteryCode.equals(SportsProgramConstant.LOTTERY_LOTTERY_CODE_FOOTBALL)) {
            if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
                res = "胜平负";
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
                res = "让球";
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
                res = "亚盘";
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SCORE)) {
                res = "比分";
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_GOAL)) {
                res = "总进球";
            }
        }
        return res;
    }

    public static String getItemCn(Integer lotteryCode, Integer playType, String item) {
        String res = "";
        if (lotteryCode.equals(SportsProgramConstant.LOTTERY_LOTTERY_CODE_FOOTBALL)) {
            if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
                Integer tempItem = Integer.valueOf(item);
                if (tempItem.equals(CommonConstant.FOOTBALL_SPF_ITEM_S)) {
                    res = "主胜";
                } else if (tempItem.equals(CommonConstant.FOOTBALL_SPF_ITEM_P)) {
                    res = "平";
                } else {
                    res = "主负";
                }
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
                Integer tempItem = Integer.valueOf(item);
                if (tempItem.equals(CommonConstant.FOOTBALL_SPF_ITEM_S)) {
                    res = "让胜";
                } else if (tempItem.equals(CommonConstant.FOOTBALL_SPF_ITEM_P)) {
                    res = "让平";
                } else {
                    res = "让负";
                }
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA) || playType.equals
                    (SportsProgramConstant.FOOTBALL_PLAY_TYPE_SCORE)) {
                res = item;
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_GOAL)) {
                res = item + "球";
            }
        }

        return res;
    }

    public static String getPlayTypeEn(Integer lotteryCode, Integer playType) {
        String res = "";
        if (lotteryCode.equals(SportsProgramConstant.LOTTERY_LOTTERY_CODE_FOOTBALL)) {
            if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
                res = "spf";
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
                res = "rqspf";
            } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
                res = "asia";
            }
        }
        return res;
    }

    public static String getSocialRankCn(Integer rankType) {
        if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_PROFIT)) {
            return "收益榜";
        } else if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM)) {
            return "命中榜";
        } else if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_CONTINUE)) {
            return "连中榜";
        }
        return "";
    }

    public static String getSocialRankBg(Integer rankType) {
        if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_PROFIT)) {
            return "http://sportsimg.mojieai.com/sport_index_profit_rank_title1.png";
        } else if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_RIGHT_NUM)) {
            return "http://sportsimg.mojieai.com/sport_index_hit_rank_title1.png";
        } else if (rankType.equals(SportsProgramConstant.SPORT_SOCIAL_RANK_TYPE_CONTINUE)) {
            return "http://sportsimg.mojieai.com/sport_index_continue_rank_title1.png";
        }
        return "";
    }

    public static String getRecommendTimeShow(Timestamp createTime) {
        if (createTime == null) {
            return "刚刚";
        }
        Long second = DateUtil.getDiffSeconds(createTime, DateUtil.getCurrentTimestamp());
        if (second > 86400) {
            return DateUtil.formatTime(createTime, "yyyy-MM-dd HH:mm:ss");
        } else if (second > 3600) {
            return CommonUtil.divide(second + "", "3600", 0) + "小时前";
        } else if (second > 300) {
            return CommonUtil.divide(second + "", "60", 0) + "分钟前";
        } else {
            return "刚刚";
        }
    }

    public static Integer getBetResult(String hostScore, String awardScore, String handicap, Integer lotteryCode,
                                       Integer playType) {
        if (StringUtils.isBlank(hostScore)) {
            return -1;
        }
        if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA) && StringUtils.isBlank(handicap)) {
            return -1;
        }
        return FootballCalculateResultEnum.getEnum(playType).getHitOption(hostScore, awardScore, handicap);
    }

    public static String getThirdPlatePlayType(Integer playType) {
//        003 大小球
//        004 半全场
//        005 比分
//        006 亚盘
//        007 双方都进球
//        014 总进球
//        016 让球胜平负
        if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
            return "001";
        } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF)) {
            return "016";
        } else if (playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA)) {
            return "006";
        }
        return null;
    }

    //收益率为 返奖的钱／投入的钱＊100%
    //简单计为 返奖的钱／赛事数
    public static Integer getProfitRatio(Integer returnAward, Integer matchCount) {
        if (matchCount.equals(0)) {
            return 0;
        }
        return Integer.valueOf(CommonUtil.divide(String.valueOf(returnAward), String.valueOf(matchCount), 0));
    }

    /**
     * 亚盘收入计算
     *
     * @Param retOdd : host_score - awary_score + handcap
     */
    public static Integer getAsiaIncome(Double retOdd, Double waterLevel, Integer hitOption) {
        Double result = 100d;
        if (hitOption.equals(CommonConstant.FOOTBALL_RQ_ASIA_ITEM_S)) {
            if (retOdd.equals(0.25)) {
                result = result + 50 * waterLevel;
            } else if (retOdd > 0.25) {
                result = result * (waterLevel + 1);
            } else if (retOdd.equals(-0.25)) {
                result = 50 * waterLevel;
            } else if (retOdd < -0.25) {
                result = 0d;
            }
        } else {
            if (retOdd.equals(-0.25)) {
                result = result + 50 * waterLevel;
            } else if (retOdd < -0.25) {
                result = result * (waterLevel + 1);
            } else if (retOdd.equals(0.25)) {
                result = 50 * waterLevel;
            } else if (retOdd > 0.25) {
                result = 0d;
            }
        }
        return result.intValue();
    }

    public static Integer getTempScoreByPrice(Long price, String leagueMatch) {
        Integer result = null;
        if (price != null && price > 0) {
            result = 190;
            if (StringUtils.isNotBlank(leagueMatch) && leagueMatch.equals("世界杯")) {
                result = 290;
            }
//            if (price.equals(4500L)) {
//                result = 120;
//            } else if (price.equals(1800L)) {
//                result = 190;
//            }
        }
        return result;
    }

    public static List<Map<String, String>> getMatchTags(Integer playType, String tag) {
        if (!playType.equals(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF)) {
            return null;
        }
        List<Map<String, String>> tags = new ArrayList<>();
        if (StringUtils.isNotBlank(tag) && tag.equals(SportsProgramConstant.SPORTTERY_SINGLE_CN)) {
            Map<String, String> single = new HashMap<>();
            single.put("img", "http://p5q287kfg.bkt.clouddn.com/sporttery_single_title.png");
//            single.put("img", "http://sportsimg.mojieai.com/sporttery_single_world_match.png");
            single.put("ratio", "46:13");
            tags.add(single);

//            Map<String, String> hot = new HashMap<>();
//            hot.put("img", "http://sportsimg.mojieai.com/match_hot_tag.png");
//            hot.put("ratio", "28:13");
//            tags.add(hot);
        }
        return tags;
    }

    public static String getMatchStatusCn(Integer matchStatus) {
        if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_INIT)) {
            return "未开赛";
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
            return "比赛中";
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
            return "已结束";
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_QUIT)) {
            return "比赛取消";
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_DELAY)) {
            return "比赛延期";
        } else if (matchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_MIDFIELD)) {
            return "中场";
        } else {
            return "";
        }
    }

    public static String getWorldCupStarUserTitle(Integer hitRatio, Double profitRatio, Integer matchCount) {
        String res = "";
        if (profitRatio > 2.5) {
            res = "http://sportsimg.mojieai.com/world_cup_star_user_boleng_1.png";
        }
        return res;
    }

    public static Boolean checkBackMatchStatus(Integer oldMatchStatus, Integer newMatchStatus) {
        if (newMatchStatus == null) {
            return Boolean.FALSE;
        }
        if (oldMatchStatus == null) {
            return Boolean.TRUE;
        }
        if (oldMatchStatus.equals(newMatchStatus)) {
            return Boolean.FALSE;
        }
        if (oldMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_INIT) && newMatchStatus > 0) {
            return Boolean.TRUE;
        }
        if (oldMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING) && newMatchStatus > 1) {
            return Boolean.TRUE;
        }
        if (oldMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_DELAY)) {
            if (newMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
                return Boolean.TRUE;
            } else if (newMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
                return Boolean.TRUE;
            } else if (newMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_QUIT)) {
                return Boolean.TRUE;
            } else if (newMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_MIDFIELD)) {
                return Boolean.TRUE;
            }
        }
        if (oldMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_MIDFIELD)) {
            if (newMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_GOING)) {
                return Boolean.TRUE;
            } else if (newMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_END)) {
                return Boolean.TRUE;
            } else if (newMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_QUIT)) {
                return Boolean.TRUE;
            } else if (newMatchStatus.equals(SportsProgramConstant.SPORT_MATCH_STATUS_DELAY)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    public static String introductionAdvertisementDeal(String introduction, Boolean isMe) {
        if (StringUtils.isBlank(introduction) || isMe) {
            return introduction;
        }

        List<Long> nums = CommonUtil.extractNumFromString(introduction);
        if (nums == null || nums.size() == 0) {
            return introduction;
        }
        for (Long temp : nums) {
            if (temp <= 99L) {
                continue;
            }
            introduction = introduction.replace(temp + "", "");
        }
        return introduction;
    }

    public static String getMatchBottomPageJumpUrl(String matchId, Integer index) {
        return "mjlottery://mjnative?page=footballMatchDetail&matchId=" + matchId + "&selectIndex=" + index;
    }

    public static Object getTagMatchListJumpUrl(Integer type, Integer tagId) {
        return "mjlottery://mjnative?page=homeFootball&selectIndex=" + type + "&tagId=" + tagId;
    }
}
