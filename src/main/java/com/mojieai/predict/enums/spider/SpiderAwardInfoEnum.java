package com.mojieai.predict.enums.spider;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.GameUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public enum SpiderAwardInfoEnum {
    NetEase("WANG_YI") {
        @Override
        public String webSitLotteryName(String gameEn) {
            if (gameEn.equals(GameConstant.FC3D)) {
                return CommonConstant.NET_EASE_FC3D_EN;
            }
            return gameEn;
        }

        @Override
        public String getAwardUrl(String webSitLotteryName) {
            return new StringBuffer().append(CommonConstant.AWARD_163_DOWNLOAD_URL_PREFIX).append(webSitLotteryName)
                    .append(CommonConstant.URL_SPLIT_STR).toString();
        }

        @Override
        public Map<String, Object> getAwardInfo(Game game, String periodId) {
            Map<String, Object> result = null;

            Document document = connectAwardResource(webSitLotteryName(game.getGameEn()));
            if (document == null) {
                log.info("when get awardInfo, Document is null error.gameEn = " + game.getGameEn());
                return result;
            }
            return NetEaseAnalysisElementEnum.getNetEaseAnalysisElementEnumByGameEn(game.getGameEn()).analysisDocument
                    (game.getGameId(), periodId, document);
        }
    }, Tencent("TEND_XUN") {
        @Override
        public String webSitLotteryName(String gameEn) {
            return gameEn;
        }

        @Override
        public String getAwardUrl(String webSitLotteryName) {
            return new StringBuffer().append(CommonConstant.AWARD_QQ_DOWNLOAD_URL_PREDIX).append(CommonConstant
                    .QQ_OPEN_AWARD_PAGE).append(webSitLotteryName).append(CommonConstant.SUFFIX_JS).toString();
        }

        @Override
        public Map<String, Object> getAwardInfo(Game game, String periodId) {
            Map<String, Object> result = null;

            Document doc = connectAwardResource(game.getGameEn());
            if (doc != null) {
                result = TencentAnalysisElementEnum.getTencentAnalysisElementEnumByGameEn(game.getGameEn())
                        .analysisDocument(game.getGameId(), periodId, doc);
            }
            return result;
        }
    }, Wbai("WBAI") {
        @Override
        public String webSitLotteryName(String gameEn) { return gameEn; }

        @Override
        public String getAwardUrl(String webSitLotteryName) {
            return new StringBuffer().append(CommonConstant.AWARD_WBAI_SSQ_DOWNLOAD_URL_PREDIX).append(webSitLotteryName)
                    .append(CommonConstant.GREP_500_URL_SUFFIX).toString();
        }

        @Override
        public Map<String, Object> getAwardInfo(Game game, String periodId) {
            Map<String, Object> result = null;

            Document doc = connectAwardResource(game.getGameEn());
            if (doc != null) {
                result = WbaiAnalysisElementEnum.getWbaiAnalysisElementEnumByGameEn(game.getGameEn())
                        .analysisDocument(game.getGameId(), periodId, doc);
            }
            return result;
        }
    };

    private String nameStr;

    SpiderAwardInfoEnum(String nameStr) {
        this.nameStr = nameStr;
    }

    public String getNameStr() {
        return nameStr;
    }

    private static final Logger log = CronEnum.PERIOD.getLogger();

    abstract public String webSitLotteryName(String gameEn);

    abstract public String getAwardUrl(String webSitLotteryName);

    abstract public Map<String, Object> getAwardInfo(Game game, String periodId);

    protected Document connectAwardResource(String gameEn) {
        try {
            String url = getAwardUrl(gameEn);
            Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(CommonConstant
                    .AWARD_163_DOWNLOAD_TIMEOUT_MSEC).get();
            return doc;
        } catch (Exception ex) {
            Timestamp current = DateUtil.getCurrentTimestamp();
            String targetStr = DateUtil.formatTime(current, "yyyy-MM-dd") + " 21:30:00";
            Timestamp targetTime = DateUtil.formatToTimestamp(targetStr, "yyyy-MM-dd HH:mm:ss");
            if (DateUtil.compareDate(targetTime, current)) {
                log.error("connection award is error.awardInfo download maybe is fail.gameEn = " + gameEn + " 抓取方式："
                        + getNameStr(), ex);
            }

            return null;
        }
    }
}
