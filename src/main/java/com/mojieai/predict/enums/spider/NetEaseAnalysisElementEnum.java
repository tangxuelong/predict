package com.mojieai.predict.enums.spider;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.JsonUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.*;

public enum NetEaseAnalysisElementEnum implements AnalysisElement {
    SSQ_ANALYSIS_ELEMENT(GameConstant.SSQ) {

    }, DLT_ANALYSIS_ELEMENT(GameConstant.DLT) {

    }, FC3D_ANALYSIS_ELEMENT(GameConstant.FC3D) {
        @Override
        public String getRemarkInfo(Elements allElements, Document doc) {
            //1.获取试机号
            String tryNum = "";
            Elements tryNumElements = doc.getElementById("zj_area").getElementsByClass("tryNum");
            if (tryNumElements != null) {
                tryNum = tryNumElements.get(0).getElementsByTag("strong").get(0).text();
            }
            //2.返回remark
            String poolBonus = TrendUtil.processMoney(allElements.attr("pool"));
            String periodSale = TrendUtil.processMoney(allElements.attr("sale"));
            if (StringUtils.isBlank(tryNum)) {
                return "";
            }
            return JsonUtil.addJsonStr("", CommonConstant.GREP_REMARK_TRY_POOL, poolBonus, CommonConstant
                    .GREP_REMARK_TRY_SALE, periodSale, CommonConstant.GREP_REMARK_TRY_NUM, tryNum);
        }
    };

    private String gameEn;

    NetEaseAnalysisElementEnum(String gameEn) {
        this.gameEn = gameEn;
    }

    public static NetEaseAnalysisElementEnum getNetEaseAnalysisElementEnumByGameEn(String gameEn) {
        if (StringUtils.isBlank(gameEn)) {
            return null;
        }
        for (NetEaseAnalysisElementEnum netEase : NetEaseAnalysisElementEnum.values()) {
            if (netEase.gameEn.equals(gameEn)) {
                return netEase;
            }
        }
        return null;
    }

    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Override
    public Map<String, Object> analysisDocument(long gameId, String periodId, Document doc) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(periodId)) {
            return result;
        }
        //1.获取奖级数据
        Element element = doc.getElementsByClass(CommonConstant.AWARD_163_DOWNLOAD_ELEMENT_CLASS).get(1)
                .getElementsByTag("a").get(0);
        if (element == null || !Objects.equals(element.text(), periodId)) {
            log.info("parseElement2AwardInfo is ex.content is null.gameId = " + gameId);
            return result;
        }
        log.info("begin to parse awardinfo for " + CommonUtil.mergeUnionKey(gameId, periodId));
        //2.数据解析
        Elements allElements = element.getAllElements();
        String bonus = allElements.attr("bonus");//奖级信息
        String winingNumber = anlaysisWinNum(allElements.attr("matchBall"));//开奖号码

        String remark = getRemarkInfo(allElements, doc);
        List<AwardInfo> awardInfoList = getAwardInfoList(gameId, periodId, bonus);
        log.info("end to parse awardinfo for " + CommonUtil.mergeUnionKey(gameId, periodId));
        result.put("remark", remark);
        result.put("winingNumber", winingNumber);
        result.put("awardInfoList", awardInfoList);
        return result;
    }

    private String anlaysisWinNum(String matchBall) {
        if (StringUtils.isBlank(matchBall) || matchBall.equals("- - -")) {
            return "";
        }
        return matchBall;
    }

    /* 解析奖级信息*/
    public List<AwardInfo> getAwardInfoList(long gameId, String periodId, String awardInfoStr) {
        if (StringUtils.isBlank(awardInfoStr)) {
            return null;
        }
        List<AwardInfo> awardInfos = new ArrayList<>();
        //1.获取默认奖级模版
        List<AwardInfo> awardInfoList = GameFactory.getInstance().getGameBean(gameId).getDefaultAwardInfoList();
        if (awardInfoList == null || awardInfoList.isEmpty()) {
            return null;
        }

        String[] bonusArray = awardInfoStr.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                .COMMON_VERTICAL_STR);
        int count = 0;
        for (AwardInfo info : awardInfoList) {
            String[] split = bonusArray[count].split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .COMMA_SPLIT_STR);
            for (String str : split) {
                if (str.equals("--")) {
                    return null;
                }
            }
            awardInfos.add(new AwardInfo(gameId, periodId, info.getAwardLevel(), info.getLevelName(), new BigDecimal
                    (split[2]), Integer.parseInt(split[1])));
            count++;
        }
        return awardInfos;
    }

    public String getRemarkInfo(Elements allElements, Document doc) {
        String poolBonus = allElements.attr("pool");
        String periodSale = allElements.attr("sale");
        if (StringUtils.isBlank(poolBonus) || StringUtils.isBlank(periodSale)) {
            return "";
        }
        poolBonus = TrendUtil.processMoney(poolBonus);
        periodSale = TrendUtil.processMoney(periodSale);
        return JsonUtil.addJsonStr("", CommonConstant.GREP_REMARK_TRY_POOL, poolBonus, CommonConstant
                .GREP_REMARK_TRY_SALE, periodSale);
    }
}
