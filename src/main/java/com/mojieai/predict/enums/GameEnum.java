package com.mojieai.predict.enums;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.exception.BusinessException;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Singal
 */
public enum GameEnum {
    SHKUAI3(GameConstant.SHKUAI3), HBD11(GameConstant.HBD11), JXD11(GameConstant.JXD11), SDD11(GameConstant.SDD11),
    SSQ(GameConstant.SSQ) {
        @Override
        public Integer getGameRedNumberMiddleLength() {
            return 17;
        }

        @Override
        public Integer getGameBlueNumberMiddleLength() {
            return 9;
        }

        @Override
        public Integer getGameRedNumberDiv1Length() {
            return 12;
        }

        @Override
        public Integer getGameRedNumberDiv2Length() {
            return 23;
        }

        @Override
        public Integer getGameRedNumberBLueLength() {
            return 6;
        }

        @Override
        public Integer getGameColdNumberLength(Integer periodNum, String ballType) {
            if (ballType == "RED") {
                return getHeatColdNumLength(periodNum, 12, 6, 3);
            } else {
                return getHeatColdNumLength(periodNum, 4, 2, 1);
            }
        }

        @Override
        public Integer getGameWarmNumberLength(Integer periodNum, String ballType) {
            if (ballType == "RED") {
                return getHeatColdNumLength(periodNum, 21, 11, 7);
            } else {
                return getHeatColdNumLength(periodNum, 7, 4, 3);
            }
        }

        @Override
        public String[] getRedBalls() {
            String[] strings = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
                    "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
                    "29", "30", "31", "32", "33"};
            return strings;
        }

        @Override
        public String[] getBlueBalls() {
            String[] strings = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
                    "13", "14", "15", "16"};
            return strings;
        }

        @Override
        public Integer getSingleRedBallLength() {
            return 6;
        }

        @Override
        public String getRedBallName() {
            return "??????";
        }

        @Override
        public String getRedBallHeZhiMin() {
            return "21";
        }

        @Override
        public String getRedBallHeZhiMax() {
            return "183";
        }

        @Override
        public Integer AcMax() {
            return 10;
        }

        @Override
        public Integer AcMin() {
            return 0;
        }

        @Override
        public Integer SpanMin() {
            return 5;
        }

        @Override
        public Integer SpanMax() {
            return 32;
        }

        @Override
        public String getHEZHIIntroduction() {
            return "?????????????????????????????????????????? 04 08 15 16 27 32 + 06 ?????????????????????102???";
        }

        @Override
        public String getSpanIntroduction() {
            return "????????????????????????????????????????????????????????????????????????32??????????????????5???";
        }

        @Override
        public String getBigSmallIntroduction() {
            return "?????????17~33?????????????????????01~16??????????????????????????????????????????????????????????????????????????????";
        }

        @Override
        public String getThreeDivIntroduction() {
            return "???????????????????????????01-11???????????????12-22???????????????23-33??????????????????????????????????????????????????????????????????";
        }

        @Override
        public List<String[]> getIndexHTools() {
            List<String[]> list = new ArrayList<>();
            String tools = ActivityIniCache.getActivityIniValue(ActivityIniConstant.getIndexHToolKey(GameConstant.SSQ));
            for (String tool : tools.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR)) {
                list.add(tool.split(CommonConstant.COMMA_SPLIT_STR));
            }
            return list;
        }

        @Override
        public List<String[]> getIndexVTools() {
            List<String[]> list = new ArrayList<>();
            String tools = ActivityIniCache.getActivityIniValue(ActivityIniConstant.getIndexVToolKey(GameConstant.SSQ));
            for (String tool : tools.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR)) {
                list.add(tool.split(CommonConstant.COMMA_SPLIT_STR));
            }
            return list;
        }

        @Override
        public Integer oneNumRedCount() {
            return 6;
        }

        @Override
        public Integer oneNumBlueCount() {
            return 1;
        }
    }, DLT(GameConstant.DLT) {
        @Override
        public Integer getGameRedNumberMiddleLength() {
            return 18;
        }

        @Override
        public Integer getGameBlueNumberMiddleLength() {
            return 7;
        }

        @Override
        public Integer getGameRedNumberDiv1Length() {
            return 13;
        }

        @Override
        public Integer getGameRedNumberDiv2Length() {
            return 25;
        }

        @Override
        public Integer getGameRedNumberBLueLength() {
            return 5;
        }

        @Override
        public Integer getGameColdNumberLength(Integer periodNum, String ballType) {
            if (ballType == "RED") {
                return getHeatColdNumLength(periodNum, 12, 6, 3);
            } else {
                return getHeatColdNumLength(periodNum, 11, 5, 3);
            }
        }

        @Override
        public Integer getGameWarmNumberLength(Integer periodNum, String ballType) {
            if (ballType == "RED") {
                return getHeatColdNumLength(periodNum, 18, 9, 6);
            } else {
                return getHeatColdNumLength(periodNum, 20, 10, 7);
            }
        }

        @Override
        public String[] getRedBalls() {
            String[] strings = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
                    "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
                    "29", "30", "31", "32", "33", "34", "35"};
            return strings;
        }

        @Override
        public String[] getBlueBalls() {
            String[] strings = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
            return strings;
        }

        @Override
        public Integer getSingleRedBallLength() {
            return 5;
        }

        @Override
        public String getRedBallName() {
            return "??????";
        }

        @Override
        public String getRedBallHeZhiMin() {
            return "15";
        }

        @Override
        public String getRedBallHeZhiMax() {
            return "165";
        }

        @Override
        public Integer AcMax() {
            return 6;
        }

        @Override
        public Integer AcMin() {
            return 0;
        }

        @Override
        public Integer SpanMin() {
            return 4;
        }

        @Override
        public Integer SpanMax() {
            return 34;
        }

        @Override
        public String getHEZHIIntroduction() {
            return "??????????????????????????????????????????  08 15 16 27 32 + 06 08 ?????????????????????98???";
        }

        @Override
        public String getSpanIntroduction() {
            return "????????????????????????????????????????????????????????????????????????34??????????????????4???";
        }

        @Override
        public String getBigSmallIntroduction() {
            return "?????????18~35?????????????????????01~17??????????????????????????????????????????????????????????????????????????????";
        }

        @Override
        public String getThreeDivIntroduction() {
            return "???????????????????????????01-12???????????????13-24???????????????25-35??????????????????????????????????????????????????????????????????";
        }

        @Override
        public List<String[]> getIndexHTools() {
            List<String[]> list = new ArrayList<>();
            String tools = ActivityIniCache.getActivityIniValue(ActivityIniConstant.getIndexHToolKey(GameConstant.DLT));
            for (String tool : tools.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR)) {
                list.add(tool.split(CommonConstant.COMMA_SPLIT_STR));
            }
            return list;
        }

        @Override
        public List<String[]> getIndexVTools() {
            List<String[]> list = new ArrayList<>();
            String tools = ActivityIniCache.getActivityIniValue(ActivityIniConstant.getIndexVToolKey(GameConstant.DLT));
            for (String tool : tools.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR)) {
                list.add(tool.split(CommonConstant.COMMA_SPLIT_STR));
            }
            return list;
        }

        @Override
        public Integer oneNumRedCount() {
            return 5;
        }

        @Override
        public Integer oneNumBlueCount() {
            return 2;
        }
    }, FC3D(GameConstant.FC3D) {
        /* ??????????????????*/
        public Integer getGameRedNumberMiddleLength() {
            return 5;
        }

        /* ????????????*/
        public Integer getGameRed1AreaMaxNum() {
            return 2;
        }

        /* ????????????*/
        public Integer getGameRed2AreaMaxNum() {
            return 6;
        }

        @Override
        public Integer AcMax() {
            return 2;
        }

        @Override
        public Integer AcMin() {
            return -2;
        }

        @Override
        public Integer SpanMin() {
            return 0;
        }

        @Override
        public Integer SpanMax() {
            return 9;
        }

        @Override
        public Integer getGameRedNumberDiv1Length() {
            return 3;
        }

        @Override
        public Integer getGameRedNumberDiv2Length() {
            return 7;
        }

        @Override
        public Integer getGameRedNumberBLueLength() {
            return 0;
        }

        /* ??????3d???????????????????????????*/
        @Override
        public String[] getRedBalls() {
            String[] strings = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
            return strings;
        }

    }, GDD11(GameConstant.GDD11), XJD11(GameConstant.XJD11),
    HLJD11(GameConstant.HLJD11), SXD11(GameConstant.SXD11), JXKUAI3(GameConstant.JXKUAI3);

    public static Map<String, Logger> resendLoggerFactory = new HashMap<>();
    private String gameEn;
    private Map<String, BigDecimal> awardInfoMap;

    GameEnum(String gameEn) {
        this.gameEn = gameEn;
    }

    public Integer getGameRedNumberMiddleLength() {
        throw new BusinessException("getGameRedNumberLength error");
    }

    public Integer getGameBlueNumberMiddleLength() {
        throw new BusinessException("getGameBlueNumberLength error");
    }

    public Integer getGameRedNumberDiv1Length() {
        throw new BusinessException("getGameRedNumberDiv1Length error");
    }

    public Integer getGameRedNumberDiv2Length() {
        throw new BusinessException("getGameRedNumberDiv1Length error");
    }

    public Integer getGameRedNumberBLueLength() {
        throw new BusinessException("getGameRedNumberDiv1Length error");
    }

    public Integer getGameColdNumberLength(Integer periodNum, String ballType) {
        throw new BusinessException("getGameRedNumberDiv1Length error");
    }

    public Integer getGameWarmNumberLength(Integer periodNum, String ballType) {
        throw new BusinessException("getGameRedNumberDiv1Length error");
    }

    public static GameEnum getGameEnumByEn(String gameEn) {
        for (GameEnum gameEnum : values()) {
            if (gameEnum.getGameEn().equals(gameEn)) {
                return gameEnum;
            }
        }
        return null;
    }

    public static GameEnum getGameEnumById(Long gameId) {
        String gameEn = GameCache.getGame(gameId).getGameEn();
        return getGameEnumByEn(gameEn);
    }

    public String getGameEn() {
        return gameEn;
    }

    public Game getGame() {
        return GameCache.getGame(gameEn);
    }

    public Map<String, BigDecimal> getAwardInfoMap(List<AwardInfo> awardInfoList) {
        if (awardInfoList == null || awardInfoList.size() <= 0) {
            throw new BusinessException("????????????");
        }
        awardInfoMap = new HashMap<>();
        for (AwardInfo awardInfo : awardInfoList) {
            awardInfoMap.put(awardInfo.getAwardLevel(), awardInfo.getBonus());
        }
        return awardInfoMap;
    }

    public Logger getResendLogger() {
        String loggerType = gameEn + "resend";
        if (resendLoggerFactory.containsKey(loggerType)) {
            return resendLoggerFactory.get(loggerType);
        }
        synchronized (loggerType.concat("Resend").intern()) {
            String path = CommonConstant.SEPARATOR_FILE + "resend";
            LogConstant.dynamicLog4j2(resendLoggerFactory, loggerType, path);
            return resendLoggerFactory.get(loggerType);
        }
    }

    public Integer getHeatColdNumLength(Integer periodNum, Integer num100, Integer num50, Integer num30) {
        if (periodNum == 100) {
            return num100;
        } else if (periodNum == 50) {
            return num50;
        } else {
            return num30;
        }

    }

    public String[] getRedBalls() {
        throw new AbstractMethodError("get redBalls error");
    }

    public String[] getBlueBalls() {
        throw new AbstractMethodError("get blueBalls error");
    }

    public Integer getSingleRedBallLength() {
        throw new AbstractMethodError("get redBalls error");
    }

    public String getHEZHIIntroduction() {
        throw new AbstractMethodError("get getHEZHIIntroduction error");
    }

    public String getSpanIntroduction() {
        throw new AbstractMethodError("get getSpanIntroduction error");
    }

    public String getBigSmallIntroduction() {
        throw new AbstractMethodError("get getBigSmallIntroduction error");
    }

    public String getThreeDivIntroduction() {
        throw new AbstractMethodError("get getThreeDivIntroduction error");
    }

    public List<String[]> getIndexHTools() {
        throw new AbstractMethodError("get getThreeDivIntroduction error");
    }

    public List<String[]> getIndexVTools() {
        throw new AbstractMethodError("get getThreeDivIntroduction error");
    }

    public String getRedBallName() {
        throw new AbstractMethodError("get redBalls error");
    }

    public String getRedBallHeZhiMin() {
        throw new AbstractMethodError("getRedBallHeZhiMin error");
    }

    public String getRedBallHeZhiMax() {
        throw new AbstractMethodError("getRedBallHeZhiMax error");
    }

    public Integer AcMax() {
        throw new AbstractMethodError("AcMax error");
    }

    public Integer AcMin() {
        throw new AbstractMethodError("AcMax error");
    }

    public Integer SpanMin() {
        throw new AbstractMethodError("SpanMin error");
    }

    public Integer SpanMax() {
        throw new AbstractMethodError("SpanMin error");
    }

    public Integer oneNumRedCount() {
        throw new AbstractMethodError("no format count error");
    }

    public Integer oneNumBlueCount() {
        throw new AbstractMethodError("no format count error");
    }

    public Integer getGameRed1AreaMaxNum() {
        throw new AbstractMethodError("no format count error");
    }

    public Integer getGameRed2AreaMaxNum() {
        throw new AbstractMethodError("no format count error");
    }
}
