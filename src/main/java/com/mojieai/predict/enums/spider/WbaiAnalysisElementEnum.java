package com.mojieai.predict.enums.spider;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum WbaiAnalysisElementEnum implements AnalysisElement {
    SSQ_ANALYSIS_ELEMENT(GameConstant.SSQ) {

    }, DLT_ANALYSIS_ELEMENT(GameConstant.DLT) {

    }, FC3D_ANALYSIS_ELEMENT(GameConstant.FC3D) {

    };

    private String gameEn;

    WbaiAnalysisElementEnum(String gameEn) {
        this.gameEn = gameEn;
    }

    public static WbaiAnalysisElementEnum getWbaiAnalysisElementEnumByGameEn(String gameEn) {
        if (StringUtils.isBlank(gameEn)) {
            return null;
        }
        for (WbaiAnalysisElementEnum wbaiAnalyEnum : WbaiAnalysisElementEnum.values()) {
            if (wbaiAnalyEnum.gameEn.equals(gameEn)) {
                return wbaiAnalyEnum;
            }
        }
        return null;
    }

    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Override
    public Map<String, Object> analysisDocument(long gameId, String periodId, Document doc) {
        Element element = doc.getElementsByClass("kj_tablelist02").first();
        Map<String, Object> result = new HashMap<>();
        if (element == null) {
            return result;
        }
        try {
            String nowPeriodId = doc.select(".td_title01 .span_left .cfont2 strong").first().text();
            if (doc == null || !periodId.contains(nowPeriodId)) {
                return result;
            }
            String remark = getRemarkInfo(doc);
            List<AwardInfo> awardInfoList = getAwardInfoList(gameId, periodId, doc);
            String winingNumber = anlaysisWinNum(doc);
            result.put("remark", remark);
            result.put("winingNumber", winingNumber);
            result.put("awardInfoList", awardInfoList);
        } catch (Exception e) {
            log.error("analysisDocument error " + e);
        }
        return result;
    }

    private String anlaysisWinNum(Document doc) {
        if (doc == null) {
            return "";
        }
        String winNum = doc.select(".ball_box01 ul").get(0).text();
        return winNum;
    }

    private String handleAmount(String amount) {
        if (amount.indexOf("元") != -1) {
            amount = amount.replaceAll("元|,", "");
        }
        if (amount.indexOf("亿") != -1) {
            amount = amount.replaceAll("亿", "");
            double tempDouble = Double.parseDouble(amount) * 100000000;
            amount = String.valueOf((long)tempDouble);
        }
        return amount;
    }

    public String getRemarkInfo(Document doc) {
        if (doc == null) {
            return "";
        }
        String periodSale = doc.select(".kj_tablelist02:nth-child(1) > tbody > tr:nth-child(3) span").get(0).text();
        String poolBonus = doc.select(".kj_tablelist02:nth-child(1) > tbody > tr:nth-child(3) span").get(1).text();

        periodSale = handleAmount(periodSale);
        poolBonus = handleAmount(poolBonus);
        return JsonUtil.addJsonStr("", CommonConstant.GREP_REMARK_TRY_POOL, poolBonus, CommonConstant
                .GREP_REMARK_TRY_SALE, periodSale);
    }

    public List<AwardInfo> getAwardInfoList(long gameId, String periodId, Document doc) {
        if (doc == null) {
            return null;
        }

        List<AwardInfo> awardInfos = new ArrayList<>();
        System.out.println(doc.select(".kj_tablelist02").get(1).text());
        Elements awardList = doc.select(".kj_tablelist02").get(1).select("tr[align]");
        try {
            for (Integer idx = 1; idx < awardList.size(); idx ++) {
                awardInfos.add(new AwardInfo(
                    gameId,
                    periodId,
                    idx.toString(),
                    awardList.get(idx).select("td").get(0).text(),
                    new BigDecimal(awardList.get(idx).select("td").get(2).text().replaceAll(",", "")),
                    Integer.parseInt(awardList.get(idx).select("td").get(1).text())
                ));
            }
        } catch (Exception e) {
            log.error("解析奖级发生异常", e);
            return null;
        }
        return awardInfos;
    }
}
