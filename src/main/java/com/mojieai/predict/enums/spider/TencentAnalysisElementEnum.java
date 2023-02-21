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
import com.mojieai.predict.util.GameUtil;
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

public enum TencentAnalysisElementEnum implements AnalysisElement {
    SSQ_ANALYSIS_ELEMENT(GameConstant.SSQ) {

    }, DLT_ANALYSIS_ELEMENT(GameConstant.DLT) {

    }, FC3D_ANALYSIS_ELEMENT(GameConstant.FC3D) {
        @Override
        public String getRemarkInfo(Map lastInfo) {
            //QQ不支持试机号所有remark不返回值
            return "";
        }
    };

    private String gameEn;

    TencentAnalysisElementEnum(String gameEn) {
        this.gameEn = gameEn;
    }

    public static TencentAnalysisElementEnum getTencentAnalysisElementEnumByGameEn(String gameEn) {
        if (StringUtils.isBlank(gameEn)) {
            return null;
        }
        for (TencentAnalysisElementEnum tencentAnalyEnum : TencentAnalysisElementEnum.values()) {
            if (tencentAnalyEnum.gameEn.equals(gameEn)) {
                return tencentAnalyEnum;
            }
        }
        return null;
    }

    private static final Logger log = CronEnum.PERIOD.getLogger();

    @Override
    public Map<String, Object> analysisDocument(long gameId, String periodId, Document doc) {
        Element element = doc.getElementsByTag("body").first();
        Map<String, Object> result = new HashMap<>();
        if (element == null) {
            return result;
        }
        try {
            Map openAward = JSONObject.parseObject(element.text(), HashMap.class);
            if (openAward != null) {
                List kaijiangList = (List) openAward.get("kaijiang_list");
                if (kaijiangList != null && kaijiangList.size() > 0) {
                    List<AwardInfo> awardInfoList = new ArrayList<>();
                    Map<String, Object> lastInfo = (Map<String, Object>) kaijiangList.get(0);
                    if (lastInfo == null || !periodId.contains(lastInfo.get("qihao").toString())) {
                        return result;
                    }
                    //1.解析map中数据
                    String remark = getRemarkInfo(lastInfo);
                    awardInfoList = getAwardInfoList(gameId, periodId, lastInfo);
                    String winingNumber = anlaysisWinNum(lastInfo);

                    log.info("end to parse awardinfo for " + CommonUtil.mergeUnionKey(gameId, periodId));
                    //2.判断是否需要开启QQ抓取奖级
                    String spiberQQ = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                            .DOWNLOAD_AWARD_INFO_SWITCH_QQ, CommonConstant.SWITCH_OFF);
                    if (CommonConstant.SWITCH_OFF.equals(spiberQQ)) {
                        remark = null;
                        awardInfoList = null;
                    }
                    result.put("remark", remark);
                    result.put("winingNumber", winingNumber);
                    result.put("awardInfoList", awardInfoList);
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("analysisDocument error " + e);
        }
        return result;
    }

    private String anlaysisWinNum(Map<String, Object> lastInfo) {
        if (lastInfo == null || !lastInfo.containsKey("kjhm")) {
            return "";
        }
        return lastInfo.get("kjhm").toString().replace(CommonConstant.COMMA_SPLIT_STR, CommonConstant
                .SPACE_SPLIT_STR).replace(CommonConstant.COMMON_VERTICAL_STR, CommonConstant.COMMON_COLON_STR);
    }

    public String getRemarkInfo(Map lastInfo) {
        if (lastInfo == null || !lastInfo.containsKey("saleAmount")) {
            return "";
        }
        String poolBonus = lastInfo.get("poolInfo").toString();
        String periodSale = lastInfo.get("saleAmount").toString();
        return JsonUtil.addJsonStr("", CommonConstant.GREP_REMARK_TRY_POOL, poolBonus, CommonConstant
                .GREP_REMARK_TRY_SALE, periodSale);
    }

    public List<AwardInfo> getAwardInfoList(long gameId, String periodId, Map lastInfo) {
        if (lastInfo == null || !lastInfo.containsKey("bonusInfo")) {
            return null;
        }
        Map awardInfoMap = (Map) lastInfo.get("bonusInfo");
        String awardLevelStr = awardInfoMap.get("data").toString();

        //1.判断腾讯不抓奖级
        List<AwardInfo> awardInfos = new ArrayList<>();
        String spiberQQ = ActivityIniCache.getActivityIniValue(ActivityIniConstant.DOWNLOAD_AWARD_INFO_SWITCH_QQ,
                CommonConstant.SWITCH_OFF);
        if (CommonConstant.SWITCH_OFF.equals(spiberQQ)) {
            return awardInfos;
        }
        List<Map> bonusInfos = JSONObject.parseObject(awardLevelStr, ArrayList.class);

        //2.开始遍历奖级
        List<AwardInfo> awardInfoList = GameFactory.getInstance().getGameBean(gameId).getDefaultAwardInfoList();

        int count = 0;
        try {
            if (awardInfoList.size() != bonusInfos.size()) {
                return null;
            }
            int awardPeopleNum = 0;
            for (AwardInfo info : awardInfoList) {
                Map awardMap = bonusInfos.get(count);
                if (awardMap.containsKey("bonus_num")) {
                    awardInfos.add(new AwardInfo(gameId, periodId, info.getAwardLevel(), info.getLevelName(), new
                            BigDecimal(awardMap.get("bonus_money").toString()), Integer.parseInt(awardMap.get
                            ("bonus_num").toString())));
                    awardPeopleNum = awardPeopleNum + Integer.valueOf(awardMap.get("bonus_num").toString());
                }
                count++;
            }
            //没有中奖人数返回空
            if (awardPeopleNum <= 0) {
                return null;
            }
            return awardInfos;
        } catch (Exception e) {
            log.error("解析奖级发生异常", e);
            return null;
        }
    }
}
