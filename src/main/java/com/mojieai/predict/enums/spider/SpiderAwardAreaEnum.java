package com.mojieai.predict.enums.spider;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.util.HttpServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SpiderAwardAreaEnum {
    CWL("CWL") {
        @Override
        public Boolean ifSuppoertGameType(long gameId) {
            if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.SSQ)) {
                return true;
            }
            return false;
        }

        @Override
        public String getAwardAreaFromNet(long gameId, String periodId) {
            if (ifSuppoertGameType(gameId)) {
                Game game = GameCache.getGame(gameId);
                String url = getAwardAreaUrl(periodId);
                Map<String, String> header = new HashMap<>();
                header.put("Referer", "http://www.cwl.gov.cn/");
                String res = HttpServiceUtils.sendHttpPostRequestAndSetHeader(url, header, "", "UTF-8");

                return CWLAnalysisDoc(res, periodId);
            }
            return null;
        }

        private String CWLAnalysisDoc(String res, String periodId) {
            if (StringUtils.isBlank(res)) {
                return null;
            }
            String content = "";

            Map resMap = JSONObject.parseObject(res, HashMap.class);
            if (resMap == null || Integer.valueOf(resMap.get("state").toString()) != 0) {
                return null;
            }
            if (resMap.containsKey("result")) {
                List result = (List) resMap.get("result");
                Map periodInfo = (Map) result.get(0);
                if (periodInfo.get("code").equals(periodId)) {
                    content = periodInfo.get("content").toString().replaceAll(CommonConstant.COMMON_DOT_STR_CN,
                            CommonConstant.SPACE_NULL_STR);
                    content = content.substring(0, content.lastIndexOf(",共"));
                }
            }
            return content;
        }

        @Override
        public String getAwardAreaUrl(String periodId) {
            return CommonConstant.AWARD_AREA_CWL_DOWNLOAD_URL;
        }

        @Override
        public String analysisDoc(Document doc) {
            String result = "";
            Elements elements = doc.getElementsByClass("drawright");
            Element element = elements.get(0).getElementsByClass("mt10").get(0);
            if (element != null && StringUtils.isNotBlank(element.text())) {
                result = dealWithAreaStr(element.text().trim());
            }
            return result.trim();
        }

        @Override
        public Boolean verifyPeriodId(Document doc, String periodId) {
            Elements elements = doc.getElementsByClass("caizhong");
            Element element = elements.get(0).getElementsByTag("span").get(0);
            Pattern p = Pattern.compile("[^0-9]");
            Matcher m = p.matcher(element.text());
            String pagePeriodId = m.replaceAll(CommonConstant.SPACE_NULL_STR);
            if (pagePeriodId.equals(periodId)) {
                return true;
            }
            return false;
        }


    }, SPORT_LOTTERY("SPORT_LOTTERY") {
        @Override
        public Boolean ifSuppoertGameType(long gameId) {
            if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
                return true;
            }
            return false;
        }

        @Override
        public String getAwardAreaFromNet(long gameId, String periodId) {
            if (ifSuppoertGameType(gameId)) {
                Game game = GameCache.getGame(gameId);
                Document doc = connectAwardResource(game.getGameEn(), getAwardAreaUrl(periodId));
                if (doc != null) {
                    return analysisDoc(doc).trim();
                }
            }
            return null;
        }

        @Override
        public String getAwardAreaUrl(String periodId) {
            String drawNews = getSportLotteryDrawNews(periodId);
            if (StringUtils.isNotBlank(drawNews)) {
                return CommonConstant.AWARD_AREA_SPORT_LOTTERY_DOWNLOAD_URL + drawNews + CommonConstant.SUFFIX_HTML;
            }
            return null;
        }

        @Override
        public String analysisDoc(Document doc) {
            String result = "";
            Elements listEle = doc.getElementsByClass("k_list");
            Elements kjInfos = listEle.get(0).getElementsByClass("k_04");
            Elements awardAreEles = kjInfos.get(0).getElementsByTag("div");
            Element awardAreEle = null;
            if (awardAreEles.size() > 9) {
                awardAreEle = awardAreEles.get(9);
                if (!awardAreEle.text().contains("本期一等奖出自")) {
                    for (int i = 1; i < awardAreEles.size(); i++) {
                        if (awardAreEles.get(i).text().contains("本期一等奖出自")) {
                            awardAreEle = awardAreEles.get(i);
                            break;
                        }
                    }
                }
            }

            if (awardAreEle != null && awardAreEle.text().contains("本期一等奖出自")) {
                result = dealWithAreaStr(awardAreEle.text().trim());
            }

            return result;
        }

        @Override
        public Boolean verifyPeriodId(Document doc, String periodId) {
            return null;
        }
    };

    private String name;

    SpiderAwardAreaEnum(String name) {
        this.name = name();
    }

    public String getName() {
        return this.name;
    }

    private static final Logger log = CronEnum.PERIOD.getLogger();

    protected Document connectAwardResource(String gameEn, String url) {
        try {
            Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(CommonConstant
                    .AWARD_163_DOWNLOAD_TIMEOUT_MSEC).get();
            return doc;
        } catch (Exception ex) {
            log.error("connection award is error.awardAreaInfo download maybe is fail.gameEn = " + gameEn + " 抓取方式："
                    + getName(), ex);
            return null;
        }
    }

    public Boolean ifSuppoertGameType(long gameId) {
        return false;
    }

    public abstract String getAwardAreaFromNet(long gameId, String periodId);

    public abstract String getAwardAreaUrl(String periodId);

    public abstract String analysisDoc(Document doc);

    public abstract Boolean verifyPeriodId(Document doc, String periodId);

    /* 体彩大乐透获取DrawNews*/
    protected String getSportLotteryDrawNews(String periodId) {
        String drawNews = "";
        String url = CommonConstant.AWARD_AREA_SPORT_LOTTERY_DOWNLOAD_PAGE_URL + periodId;
        Document doc = connectAwardResource(GameConstant.DLT, url);
        Elements elements = doc.getElementsByTag("body");
        Element element = elements.get(0);
        Map<String, Object> info = new HashMap<>();
        if (StringUtils.isNotBlank(element.text())) {
            try {
                String jsonStr = element.text().substring(1, element.text().length() - 1);
                info = JSONObject.parseObject(jsonStr, HashMap.class);
                Map<String, Object> lotteryMap = (Map<String, Object>) info.get("lottery");
                drawNews = (String) lotteryMap.get("drawNews");
            } catch (Exception e) {
                log.error("dlt get drawNews exception", e);
            }
        }
        return drawNews;
    }

    protected String dealWithAreaStr(String innerText) {
        String result = "";
        String[] awardAreaArr = null;
        String areText = innerText.split(CommonConstant.COMMON_DOT_STR_CN)[0];
        if (areText.contains(CommonConstant.COMMON_COLON_STR)) {
            awardAreaArr = areText.split(CommonConstant.COMMON_COLON_STR);
        } else if (areText.contains(CommonConstant.COMMON_COLON_STR_CN)) {
            awardAreaArr = areText.split(CommonConstant.COMMON_COLON_STR_CN);
        }
        if (awardAreaArr.length > 0) {
            if (awardAreaArr[1].contains("共")) {
                result = awardAreaArr[1].substring(0, awardAreaArr[1].lastIndexOf(CommonConstant.COMMA_SPLIT_STR));
            } else {
                result = awardAreaArr[1];
            }
            if (result.contains(CommonConstant.COMMON_PAUSE_STR_CN)) {
                result = result.replaceAll(CommonConstant.COMMON_PAUSE_STR_CN, CommonConstant.COMMA_SPLIT_STR);
            }
        }
        return result;
    }
}
