package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.AwardInfoDao;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.DltDataGrep500Enum;
import com.mojieai.predict.enums.FC3DDataGrep500Enum;
import com.mojieai.predict.enums.SsqDataGrep500Enum;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.service.GetDataFrom500Service;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class GetDataFrom500ServiceImpl implements GetDataFrom500Service {
    private final Logger log = LogConstant.commonLog;
    @Autowired
    private GamePeriodDao gamePeriodDao;
    @Autowired
    private AwardInfoDao awardInfoDao;
    @Autowired
    PeriodRedisService periodRedisService;

    @Override
    public List<GamePeriod> getDataFrom500(long gameId, String gameEn, int[] peroidArr) {
        List<GamePeriod> gamePeriods = new ArrayList<>();

        for (int periodId = peroidArr[0]; periodId < peroidArr[1]; periodId++) {

            try {
                Document doc = null;
                String periodIdStr = periodId + "";
                if (periodIdStr.length() <= 4) {
                    periodIdStr = "0" + periodIdStr;
                }
                if (periodIdStr.length() == 7) {
                    periodIdStr = periodIdStr.substring(2);
                }
                int num = Integer.valueOf(periodIdStr.substring(2));
//                if (num > 210) {
//                    continue;
//                }
                Map<String, Object> winNums = null;
                if (gameEn.equals(CommonConstant.GREP_500_URL_SSQ)) {
                    doc = SsqDataGrep500Enum.WINNUMGREP.getUrlData(CommonConstant.GREP_500_URL_SSQ, periodIdStr);
                    if (doc != null) {
                        winNums = SsqDataGrep500Enum.WINNUMGREP.analyDataGrep500Doc(doc, periodIdStr);
                    }
                } else if (gameEn.equals(CommonConstant.GREP_500_URL_DLT)) {
                    doc = DltDataGrep500Enum.WINNUMGREP.getUrlData(CommonConstant.GREP_500_URL_DLT, periodIdStr);
                    if (doc != null) {
                        winNums = DltDataGrep500Enum.WINNUMGREP.analyDataGrep500Doc(doc, periodIdStr);
                    }
                } else if (gameEn.equals(CommonConstant.GREP_500_URL_FC3D)) {
                    doc = FC3DDataGrep500Enum.WINNUMGREP.getUrlData(CommonConstant.GREP_500_URL_FC3D, periodIdStr);
                    if (doc != null) {
                        winNums = FC3DDataGrep500Enum.WINNUMGREP.analyDataGrep500Doc(doc, periodIdStr);
                    }
                }

                if (winNums != null) {
                    GamePeriod gamePeriod = new GamePeriod();
                    try {
                        gamePeriod.setGameId(gameId);
                        gamePeriod.setPeriodId(winNums.get("periodId").toString());
                        if (winNums.get("winningNumbers").toString().contains("Result")) {
                            gamePeriod.setWinningNumbers(" ");
                        } else {
                            gamePeriod.setWinningNumbers(winNums.get("winningNumbers").toString());
                        }
                        gamePeriod.setStartTime(DateUtil.parseUtilDate(winNums.get("startTime").toString(), 2));
                        gamePeriod.setAwardTime(DateUtil.parseUtilDate(winNums.get("awardTime").toString(), 2));
                        gamePeriod.setOpenTime(DateUtil.parseUtilDate(winNums.get("awardTime").toString(), 2));
                        gamePeriod.setEndTime(DateUtil.parseUtilDate(winNums.get("endTime").toString(), 2));
                        gamePeriod.setCreateTime(DateUtil.parseUtilDate(winNums.get("createTime").toString(), 2));

                        gamePeriods.add(gamePeriod);
                    } catch (Exception e) {
                        log.error("期次号" + periodId + "异常" + e.toString());
                        continue;
                    }
                }
//                if (gamePeriods.size() > 100) {
                System.out.println(gameId);
                save2DB(gamePeriods, gameId);
                gamePeriods.clear();
//                }
            } catch (Exception e) {
                log.error("期次号" + periodId + "异常" + e.toString());
                continue;
            }
        }
        if (gamePeriods.size() > 0) {
            System.out.println(gameId);
            save2DB(gamePeriods, gameId);
        }
        return gamePeriods;
    }

    @Override
    public List<AwardInfo> getAwordInfoFrom500(long gameId, String gameEn, int[] peroidArr) {
        List<AwardInfo> awardInfosRtn = new ArrayList<>();
//        String gameEn = GameCache.getGame(gameId).getGameEn();
        for (int periodId = peroidArr[0]; periodId < peroidArr[1]; periodId++) {
            try {
                Document doc = null;
                Map<String, Object> awardInfoMap = null;
                String periodIdStr = periodId + "";
                if (periodIdStr.length() <= 4) {
                    periodIdStr = "0" + periodIdStr;
                }
                int num = Integer.valueOf(periodIdStr.substring(2));
//                if (num > 210) {
//                    continue;
//                }
                if (gameEn.equals(CommonConstant.GREP_500_URL_SSQ)) {
                    doc = SsqDataGrep500Enum.AWARDINFOGREP.getUrlData(CommonConstant.GREP_500_URL_SSQ, periodIdStr);
                    if (doc != null) {
                        awardInfoMap = SsqDataGrep500Enum.AWARDINFOGREP.analyDataGrep500Doc(doc, "20" + periodIdStr);
                    }
                } else if (gameEn.equals(CommonConstant.GREP_500_URL_DLT)) {
                    doc = DltDataGrep500Enum.AWARDINFOGREP.getUrlData(CommonConstant.GREP_500_URL_DLT, periodIdStr);
                    if (doc != null) {
                        awardInfoMap = DltDataGrep500Enum.AWARDINFOGREP.analyDataGrep500Doc(doc, periodIdStr);
                    }
                } else if (gameEn.equals(CommonConstant.GREP_500_URL_FC3D)) {
                    doc = FC3DDataGrep500Enum.AWARDINFOGREP.getUrlData(CommonConstant.GREP_500_URL_FC3D, periodIdStr);
                    if (doc != null) {
                        awardInfoMap = FC3DDataGrep500Enum.AWARDINFOGREP.analyDataGrep500Doc(doc, periodIdStr);
                    }
                }

                if (awardInfoMap != null) {
                    List<Map> awardInfoList = (List<Map>) awardInfoMap.get("awardInfoList");
                    for (Map map : awardInfoList) {
                        AwardInfo awardInfo = new AwardInfo();
                        awardInfo.setGameId(gameId);
                        awardInfo.setPeriodId(awardInfoMap.get("periodId").toString());
                        awardInfo.setAwardCount(Integer.valueOf(map.get("awardCount").toString().replace("-", "0")));
                        awardInfo.setBonus(new BigDecimal(map.get("bonus").toString().replace("-", "0")));
                        awardInfo.setLevelName(map.get("levelName").toString());
                        awardInfo.setAwardLevel(map.get("awardLevel").toString());
                        awardInfosRtn.add(awardInfo);
                    }
                }
                if (StringUtils.isNotEmpty(awardInfoMap.get("periodSale").toString())
                        && StringUtils.isNotEmpty(awardInfoMap.get("tryNum").toString())) {
                    updateWinningNumber(gameId, awardInfoMap.get("periodId").toString(), "",
                            awardInfoMap.get("periodSale").toString(),
                            awardInfoMap.get("tryNum").toString());
                }
//                if (awardInfosRtn.size() > 100) {
                save2AwardDb(awardInfosRtn, gameId);
                awardInfosRtn.clear();
//                }
            } catch (Exception e) {
                continue;
            }
        }
        if (awardInfosRtn.size() > 0) {
            save2AwardDb(awardInfosRtn, gameId);
        }
        return awardInfosRtn;
    }

    private void save2DB(List<GamePeriod> gamePeriod, long gameId) {
        gamePeriodDao.addGamePeriodBatch(gamePeriod, gameId);
    }

    private void save2AwardDb(List<AwardInfo> awardInfos, long gameId) {
        awardInfoDao.addAwardInfoBatch(awardInfos, gameId);

    }

    private void updateWinningNumber(Long gameId, String periodId, String poolBonus, String
            periodSale, String testNum) {
        try {
            GamePeriod period = gamePeriodDao.getPeriodByGameIdAndPeriod(gameId, periodId);
            Boolean flag = Boolean.FALSE;
            if (period != null) {
                if (StringUtils.isNotBlank(testNum) && StringUtils.isNotBlank(periodSale)) {
//                    poolBonus = dealWithMoney(poolBonus);
                    periodSale = dealWithMoney(periodSale);
                    if (StringUtils.isNotBlank(testNum) || StringUtils.isNotBlank(periodSale)) {
                        String jsonStr = JsonUtil.addJsonStr(period.getRemark(), "pool", poolBonus, "sale",
                                periodSale, "testNum", testNum);
                        gamePeriodDao.updateRemark(gameId, periodId, period.getRemark(), jsonStr);
                        flag = Boolean.TRUE;
                    }
                }
                if (flag) {
                    Set<String> periods = new HashSet<>();
                    periods.add(periodId);
                    periodRedisService.consumePeriods(gameId, periods);
                }
            }
        } catch (Exception ex) {
            log.error("download from 163 and update winningNumber is error.gameId = " + gameId + ".periodId = " +
                    periodId, ex);
        }
    }

    private String dealWithMoney(String money) {
        return money;
    }

}
