package com.mojieai.predict.enums;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public enum FC3DDataGrep500Enum {
    WINNUMGREP {
        @Override
        public Map<String, Object> analyDataGrep500Doc(Document doc, String periodId) {
            Map<String, Object> rtn = new HashMap<>();
            Element kjTable = doc.getElementsByClass("kj_tablelist02").get(0);
            if (kjTable != null) {
                Elements elements = doc.getElementsByClass("kj_tablelist02").get(0).getElementsByTag("tr");
                Element periodInfo = elements.get(0);
                String getPeriodId = periodInfo.getElementsByTag("strong").get(0).text();
                Element openTimeStr = periodInfo.getElementsByClass("span_right").get(0);
                String[] openTimeArr = openTimeStr.text().split(CommonConstant.COMMON_COLON_STR_CN)[1].split(" ");
                String openTime = openTimeArr[0];
                Elements winBalls = elements.get(2).getElementsByTag("li");
                String winBall = winBalls.get(0).text() + " " + winBalls.get(1).text() + " " + winBalls.get(2).text();
                rtn.put("winNums", winBall);
                rtn.put("periodId", periodId);
                rtn.put("openTime", openTime);
                return formateWinNums(rtn);
            }
            return null;

        }

        private Map<String, Object> formateWinNums(Map<String, Object> map) {
            Map<String, Object> rtnMap = new HashMap<>();
            try {
                String openTimes = map.get("openTime").toString().replace("年", CommonConstant.COMMON_DASH_STR)
                        .replace("月", CommonConstant.COMMON_DASH_STR).replace("日", CommonConstant.SPACE_SPLIT_STR);
                String awardTime = openTimes + "21:15:00";
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS);

                String endTime = openTimes + "20:00:00";
                Date endDate = sdf.parse(endTime);
                Date beginTime = DateUtil.getIntervalDate(endDate, -1);
                rtnMap.put("periodId", map.get("periodId"));
                rtnMap.put("createTime", sdf.format(new Date()));
                rtnMap.put("awardTime", awardTime);
                rtnMap.put("endTime", endTime);
                rtnMap.put("startTime", sdf.format(beginTime.getTime()));
                rtnMap.put("winningNumbers", map.get("winNums"));

            } catch (Exception e) {
                return null;
            }
            return rtnMap;
        }
    }, AWARDINFOGREP {
        @Override
        public Map<String, Object> analyDataGrep500Doc(Document doc, String periodId) {
            Map<String, Object> res = new HashMap<>();

            Element kjTable = doc.getElementsByClass("kj_tablelist02").get(0);
            Element jJTable = doc.getElementsByClass("kj_tablelist02").get(1);
            if (kjTable != null && jJTable != null) {
                String tryNum = kjTable.getElementsByTag("table").get(1).getElementsByTag("td").get(2).text();
                tryNum = tryNum.split(CommonConstant.COMMON_COLON_STR_CN)[1];
                res.put("tryNum", tryNum);
                String periodSaleStr = kjTable.getElementsByTag("tr").get(3).text();
                if (periodSaleStr.contains(">")) {
                    String periodSale = periodSaleStr.split(">")[0].split("：")[1].replaceAll(",", "");
                    res.put("periodSale", periodSale);
                }
                //1.奖级
                Elements awardLevel = jJTable.getElementsByTag("tr");
                List<String> awardInfoList = new ArrayList<>();
                if (awardLevel.size() > 3) {
                    for (int i = 2; i < awardLevel.size() - 1; i++) {
                        Element awardLevelDetail = awardLevel.get(i);
                        Elements awardDetails = awardLevelDetail.getElementsByTag("td");
                        String awardLevelOrder = getAwardLevelByName(awardDetails.get(0).text());
                        if (StringUtils.isNotBlank(awardLevelOrder)) {
                            String awardInfo = awardDetails.get(0).text() + ":" + awardDetails.get(1).text() + ":" +
                                    awardDetails.get(2).text() + ":" + awardLevelOrder;
                            awardInfoList.add(awardInfo);
                        }
                    }
                    res.put("awardInfoList", awardInfoList);
                }
                res.put("periodId", periodId);
                return formateAwardNums(res);
            }
            return null;
        }

        private Map<String, Object> formateAwardNums(Map<String, Object> map) {
            Map<String, Object> rtnMap = new HashMap<>();
            List<Map> awardInfos = new ArrayList<>();
            List<String> awardInfoList = (List<String>) map.get("awardInfoList");
            for (String awardInfoStr : awardInfoList) {
                Map<String, String> awardInfo = new HashMap<>();
                String[] arr = awardInfoStr.split(":");
                awardInfo.put("levelName", arr[0]);
                if (StringUtils.isNumeric(arr[2].replace(",", ""))) {
                    awardInfo.put("bonus", arr[2].replace(",", ""));
                    awardInfo.put("awardCount", arr[1]);
                } else {
                    awardInfo.put("bonus", "0");
                    awardInfo.put("awardCount", "0");
                }
                awardInfo.put("awardLevel", arr[3]);
                awardInfos.add(awardInfo);
            }
            rtnMap.put("periodSale", map.get("periodSale").toString().replace(",", ""));
            rtnMap.put("periodId", map.get("periodId"));
            rtnMap.put("poolBonus", map.get("poolBonus"));
            rtnMap.put("awardInfoList", awardInfos);
            rtnMap.put("tryNum", map.get("tryNum"));
            return rtnMap;
        }
    };

    /*解析抓去的数据*/
    public abstract Map<String, Object> analyDataGrep500Doc(Document doc, String periodId);

    /*获取数据*/
    public Document getUrlData(String gameEn, String priodId) {
        StringBuffer url = new StringBuffer(CommonConstant.GREP_500_URL_PREFIX);
        Document doc = null;
        try {
            url.append(gameEn + CommonConstant.URL_SPLIT_STR);
            url.append(priodId).append(CommonConstant.GREP_500_URL_SUFFIX);
            doc = Jsoup.connect(url.toString()).timeout(3000).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    public String getAwardLevelByName(String levelName) {
        if (levelName.equals("单选")) {
            return "1";
        } else if (levelName.equals("组三")) {
            return "2";
        } else if (levelName.equals("组六")) {
            return "3";
        } else {
            return "";
        }
    }
}
