package com.mojieai.predict.enums;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public enum DltDataGrep500Enum {
    WINNUMGREP {
        @Override
        public Map<String, Object> analyDataGrep500Doc(Document doc, String periodId) {
            Map<String, Object> rtn = new HashMap<>();
            try {
                if (doc != null) {
                    Element body = doc.body();
                    Elements ball_box01 = body.getElementsByClass("ball_box01");

                    Element ball = ball_box01.get(0);
                    Elements redBalls = ball.getElementsByClass("ball_red");
                    Elements blueBalls = ball.getElementsByClass("ball_blue");
                    StringBuffer redBall = new StringBuffer();
                    for (Element red : redBalls) {
                        redBall.append(red.text() + " ");
                    }
                    String redWinBall = redBall.toString().trim();
                    StringBuffer blueBall = new StringBuffer();
                    for (Element blue : blueBalls) {
                        blueBall.append(blue.text() + " ");
                    }
                    String buleWinBall = blueBall.toString().trim();

                    //获取开奖日期
                    String kjDate = "";
                    Elements kjxqBoxs = body.getElementsByClass("kjxq_box02");
                    if (!kjxqBoxs.isEmpty() && kjxqBoxs.size() > 0) {
                        Element kjxqBox = kjxqBoxs.first();
                        Element kjTable = kjxqBox.child(1).child(0);
                        Elements kjTd = kjTable.getElementsByClass("td_title01");
                        Element span_right = kjTd.get(0);
                        String endTimeStr = span_right.text();
                        //超级大乐透 第 17001期 开奖日期：2017年1月2日 兑奖截止日期：2017年3月2日
                        if (StringUtils.isNotEmpty(endTimeStr)) {
                            String[] timeArr = endTimeStr.split("：");
                            if (timeArr.length >= 3) {
                                String[] timeArr1 = timeArr[1].split(" ");
                                kjDate = timeArr1[0];
                            }
                        }
                    }

                    rtn.put("winNums", redWinBall + ":" + buleWinBall);
                    rtn.put("periodId", periodId);
                    rtn.put("openTime", kjDate);
                }
            } catch (Exception e) {
                return null;
            }
            return formateWinNums(rtn);
        }

        private Map<String, Object> formateWinNums(Map<String, Object> map) {
            Map<String, Object> rtnMap = new HashMap<>();
            try {
                String openTimes = map.get("openTime").toString().replace("年", CommonConstant.COMMON_DASH_STR)
                        .replace("月", CommonConstant.COMMON_DASH_STR).replace("日", CommonConstant.SPACE_SPLIT_STR);
                String awardTime = openTimes + "21:15:00";
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS);
                SimpleDateFormat sdfNoHours = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT);

                String endTime = openTimes + "20:00:00";
                Date endDate = sdf.parse(endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(sdfNoHours.parse(openTimes.trim()));
                int week = calendar.get(Calendar.DAY_OF_WEEK);
                Date beginTime = new Date();
                if (week == 7) {
                    beginTime = DateUtil.getIntervalDate(endDate, -3);
                } else if (week == 2 || week == 4) {
                    beginTime = DateUtil.getIntervalDate(endDate, -2);
                }
                rtnMap.put("periodId", map.get("periodId"));
                rtnMap.put("createTime", sdf.format(new Date()));
                rtnMap.put("awardTime", awardTime);
                rtnMap.put("endTime", endTime);
                rtnMap.put("startTime", sdf.format(beginTime.getTime()));
                rtnMap.put("winningNumbers", map.get("winNums"));

            } catch (ParseException e) {
                return null;
            }
            return rtnMap;
        }
    }, AWARDINFOGREP {//奖级信息

        @Override
        public Map<String, Object> analyDataGrep500Doc(Document doc, String periodId) {
            Map<String, Object> rtn = new HashMap<>();
            List<String> awardInfoList = new ArrayList<>();
            try {
                if (doc != null) {
                    Element body = doc.body();
                    Elements kjxqBoxs = body.getElementsByClass("kjxq_box02");
                    if (kjxqBoxs.get(0) != null) {
                        //奖级
                        awardInfoList = getElement(kjxqBoxs, periodId);
                        //奖池和销量
                        Element poolAndSaleTable = kjxqBoxs.get(0).child(1).child(0);
                        Elements poolAndSaleTbody = poolAndSaleTable.getElementsByTag("tbody");
                        Elements poolAndSaleSpans = poolAndSaleTbody.get(0).child(2).getElementsByTag("span");
                        int size = poolAndSaleSpans.size();
                        String periodSale = poolAndSaleSpans.get(0).text();
                        String poolBonus = poolAndSaleSpans.get(1).text();
                        if(size >2){
                            poolBonus = poolAndSaleSpans.get(2).text();
                        }
                        rtn.put("periodSale", periodSale);
                        rtn.put("poolBonus", poolBonus);
                        rtn.put("periodId", periodId);
                    }
                    rtn.put("awardInfoList", awardInfoList);
                }
            } catch (Exception e) {
                return null;
            }
            return formateAwardNums(rtn);
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
            rtnMap.put("poolBonus", map.get("poolBonus").toString().replace(",", ""));
            rtnMap.put("awardInfoList", awardInfos);
            return rtnMap;
        }

        private List<String> getElement(Elements kjxqBoxs, String periodId) {
            String usePeriodId = periodId.substring(2);

            List<String> awardInfoList = new ArrayList<>();
            Element kjxqTable = kjxqBoxs.get(0).child(1).child(2);

            boolean flag = false;
            int size = kjxqBoxs.get(0).child(1).children().size();
            if( size >4 && kjxqBoxs.get(0).child(1).child(4)!=null){
                Elements tbody = kjxqBoxs.get(0).child(1).child(4).getElementsByTag("tbody");
                if(tbody.size()>0 && tbody.get(0).child(3).text().contains("派奖")){
                    flag = true;
                    kjxqTable = kjxqBoxs.get(0).child(1).child(4);
                }
            }
            if(size>4 && kjxqBoxs.get(0).child(1).child(3)!=null){
                Elements tbody = kjxqBoxs.get(0).child(1).child(3).getElementsByTag("tbody");
                if(tbody.size()>0 && tbody.get(0).child(3).text().contains("派奖")){
                    flag = true;
                    kjxqTable = kjxqBoxs.get(0).child(1).child(3);
                }
            }

            if(flag){

                Elements tbody = kjxqTable.getElementsByTag("tbody");
                Elements kjTr = tbody.get(0).getElementsByTag("tr");
                String awardNameTd = "";
                for (int i = 2; i < kjTr.size() - 2; i++) {
                    Element tempElemet = kjTr.get(i);

                    Elements tdAwardInfos = tempElemet.getElementsByTag("td");
                    StringBuffer stringBuffer = new StringBuffer();
                    if(tdAwardInfos.get(0).text().contains("tdAwardInfos")){
                        continue;
                    }
                    if(tdAwardInfos.size() <=4){
                        stringBuffer.append(awardNameTd
                                + "[" + tdAwardInfos.get(0).text() + "]" + ":"
                                + tdAwardInfos.get(1).text() + ":"
                                + tdAwardInfos.get(2).text() + ":"
                                + getAwardLevelByName(awardNameTd) + "_z");
                    }else{
                        awardNameTd = tdAwardInfos.get(0).text();
                        stringBuffer.append(awardNameTd+ "["
                                + tdAwardInfos.get(1).text() + "]" + ":"
                                + tdAwardInfos.get(2).text() + ":"
                                + tdAwardInfos.get(3).text() + ":"
                                + getAwardLevelByName(awardNameTd));
                    }
                    awardInfoList.add(stringBuffer.toString());
                }
            }else{
                //09120之前只开到三等奖
                if (Integer.valueOf(usePeriodId) <= 9120) {
                    Elements tbody = kjxqTable.getElementsByTag("tbody");
                    Elements kjTr = tbody.get(0).getElementsByTag("tr");
                    for (int i = 2; i < kjTr.size() - 1; i++) {
                        Element tempElemet = kjTr.get(i);
                        int countTd = tempElemet.childNodeSize();
                        Elements tdAwardInfos = tempElemet.getElementsByTag("td");
                        StringBuffer stringBuffer = new StringBuffer();
                        if (i <= 7) {
                            if (tdAwardInfos.size() == 3) {
                                Element frontEle = kjTr.get(i - 1);
                                stringBuffer.append(frontEle.child(0).text()
                                        + "[" + tdAwardInfos.get(0).text() + "]" + ":"
                                        + tdAwardInfos.get(1).text() + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + getAwardLevelByName(frontEle.child(0).text()) + "_z");

                            } else {
                                stringBuffer.append(tdAwardInfos.get(0).text()
                                        + "[" + tdAwardInfos.get(1).text() + "]" + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + tdAwardInfos.get(3).text() + ":"
                                        + getAwardLevelByName(tdAwardInfos.get(0).text()));
                            }
                            awardInfoList.add(stringBuffer.toString());
                        } else {
                            for (Element element : tdAwardInfos) {
                                stringBuffer.append(element.text() + ":");
                            }
                            stringBuffer.append(getAwardLevelByName(tdAwardInfos.get(0).text()));
                            awardInfoList.add(stringBuffer.toString());
                        }
                    }
                } else if (Integer.valueOf(usePeriodId) <= 12057 || (Integer.valueOf(usePeriodId) >= 12072 && Integer.valueOf(usePeriodId) <= 14051)) {
                    Elements tbody = kjxqTable.getElementsByTag("tbody");
                    Elements kjTr = tbody.get(0).getElementsByTag("tr");
                    for (int i = 2; i < kjTr.size() - 1; i++) {
                        Element tempElemet = kjTr.get(i);
                        int countTd = tempElemet.childNodeSize();
                        Elements tdAwardInfos = tempElemet.getElementsByTag("td");
                        StringBuffer stringBuffer = new StringBuffer();
                        if (i <= 15) {
                            if (tdAwardInfos.size() == 3) {
                                Element frontEle = kjTr.get(i - 1);
                                stringBuffer.append(frontEle.child(0).text()
                                        + "[" + tdAwardInfos.get(0).text() + "]" + ":"
                                        + tdAwardInfos.get(1).text() + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + getAwardLevelByName(frontEle.child(0).text()) + "_z");

                            } else {
                                stringBuffer.append(tdAwardInfos.get(0).text()
                                        + "[" + tdAwardInfos.get(1).text() + "]" + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + tdAwardInfos.get(3).text() + ":"
                                        + getAwardLevelByName(tdAwardInfos.get(0).text()));
                            }
                            awardInfoList.add(stringBuffer.toString());
                        } else {
                            for (Element element : tdAwardInfos) {
                                stringBuffer.append(element.text() + ":");
                            }
                            stringBuffer.append(getAwardLevelByName(tdAwardInfos.get(0).text()));
                            awardInfoList.add(stringBuffer.toString());
                        }
                    }
                } else if (Integer.valueOf(usePeriodId) <= 12071 && Integer.valueOf(usePeriodId) >= 12058) {//加上了宝石砖石奖
                    Elements tbody = kjxqTable.getElementsByTag("tbody");
                    Elements kjTr = tbody.get(0).getElementsByTag("tr");
                    for (int i = 2; i < kjTr.size() - 1; i++) {
                        Element tempElemet = kjTr.get(i);
                        int countTd = tempElemet.childNodeSize();
                        Elements tdAwardInfos = tempElemet.getElementsByTag("td");
                        StringBuffer stringBuffer = new StringBuffer();
                        if (i <= 15) {
                            if (tdAwardInfos.size() == 3) {
                                Element frontEle = kjTr.get(i - 1);
                                stringBuffer.append(frontEle.child(0).text()
                                        + "[" + tdAwardInfos.get(0).text() + "]" + ":"
                                        + tdAwardInfos.get(1).text() + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + getAwardLevelByName(frontEle.child(0).text()) + "_z");

                            } else {
                                stringBuffer.append(tdAwardInfos.get(0).text()
                                        + "[" + tdAwardInfos.get(1).text() + "]" + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + tdAwardInfos.get(3).text() + ":"
                                        + getAwardLevelByName(tdAwardInfos.get(0).text()));
                            }
                            awardInfoList.add(stringBuffer.toString());
                        } else if (i == 16) {
                            for (Element element : tdAwardInfos) {
                                stringBuffer.append(element.text() + ":");
                            }
                            stringBuffer.append(getAwardLevelByName(tdAwardInfos.get(0).text()));
                            awardInfoList.add(stringBuffer.toString());
                        } else {
                            String awardName = "";
                            if (tdAwardInfos.size() == 3) {
                                Element frontEle = kjTr.get(i - 1);
                                stringBuffer.append(tdAwardInfos.get(0).text()
                                        + "[" + awardName + "]" + ":"
                                        + tdAwardInfos.get(1).text() + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + getAwardLevelByName(frontEle.child(0).text()));

                            } else {
                                awardName = tdAwardInfos.get(1).text();
                                stringBuffer.append(tdAwardInfos.get(1).text()
                                        + "[" + tdAwardInfos.get(0).text() + "]" + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + tdAwardInfos.get(3).text() + ":"
                                        + getAwardLevelByName(tdAwardInfos.get(1).text()));
                            }
                            awardInfoList.add(stringBuffer.toString());
                        }
                    }
                } else if (Integer.valueOf(usePeriodId) >= 14052 && Integer.valueOf(usePeriodId) <= 16041) {
                    Elements tbody = kjxqTable.getElementsByTag("tbody");
                    Elements kjTr = tbody.get(0).getElementsByTag("tr");
                    for (int i = 2; i < kjTr.size() - 1; i++) {
                        Element tempElemet = kjTr.get(i);
                        int countTd = tempElemet.childNodeSize();
                        Elements tdAwardInfos = tempElemet.getElementsByTag("td");
                        StringBuffer stringBuffer = new StringBuffer();
                        if (i <= 11) {
                            if (tdAwardInfos.size() == 3) {
                                Element frontEle = kjTr.get(i - 1);
                                stringBuffer.append(frontEle.child(0).text()
                                        + "[" + tdAwardInfos.get(0).text() + "]" + ":"
                                        + tdAwardInfos.get(1).text() + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + getAwardLevelByName(frontEle.child(0).text()) + "_z");

                            } else {
                                stringBuffer.append(tdAwardInfos.get(0).text()
                                        + "[" + tdAwardInfos.get(1).text() + "]" + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + tdAwardInfos.get(3).text() + ":"
                                        + getAwardLevelByName(tdAwardInfos.get(0).text()));
                            }
                            awardInfoList.add(stringBuffer.toString());
                        } else {
                            for (Element element : tdAwardInfos) {
                                stringBuffer.append(element.text() + ":");
                            }
                            stringBuffer.append(getAwardLevelByName(tdAwardInfos.get(0).text()));
                            awardInfoList.add(stringBuffer.toString());
                        }
                    }
                } else if (Integer.valueOf(usePeriodId) > 16042) {//结构发生了变化
                    kjxqTable = kjxqBoxs.get(0).child(1).child(4);
                    Elements tbody = kjxqTable.getElementsByTag("tbody");
                    if(tbody.size() == 0){
                        tbody = kjxqBoxs.get(0).child(1).child(3).getElementsByTag("tbody");
                    }

                    Elements kjTr = tbody.get(0).getElementsByTag("tr");
                    for (int i = 2; i < kjTr.size() - 2; i++) {
                        Element tempElemet = kjTr.get(i);
                        int countTd = tempElemet.childNodeSize();
                        Elements tdAwardInfos = tempElemet.getElementsByTag("td");
                        StringBuffer stringBuffer = new StringBuffer();
                        if (i <= 11) {
                            if (tdAwardInfos.size() == 4) {
                                Element frontEle = kjTr.get(i - 1);
                                stringBuffer.append(frontEle.child(0).text()
                                        + "[" + tdAwardInfos.get(0).text() + "]" + ":"
                                        + tdAwardInfos.get(1).text() + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + getAwardLevelByName(frontEle.child(0).text()) + "_z");

                            } else {
                                stringBuffer.append(tdAwardInfos.get(0).text()
                                        + "[" + tdAwardInfos.get(1).text() + "]" + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + tdAwardInfos.get(3).text() + ":"
                                        + getAwardLevelByName(tdAwardInfos.get(0).text()));
                            }
                            awardInfoList.add(stringBuffer.toString());
                        } else {
//                        for (Element element : tdAwardInfos) {
                            for (int j = 0; j < tdAwardInfos.size() - 1; j++) {
                                if (!tdAwardInfos.get(j).text().equals("基本")) {
                                    stringBuffer.append(tdAwardInfos.get(j).text() + ":");
                                }
                            }
                            stringBuffer.append(getAwardLevelByName(tdAwardInfos.get(0).text()));
                            awardInfoList.add(stringBuffer.toString());
                        }
                    }
                } else if (Integer.valueOf(usePeriodId) > 16042) {//结构发生了变化
                    kjxqTable = kjxqBoxs.get(0).child(1).child(4);
                    Elements tbody = kjxqTable.getElementsByTag("tbody");
                    Elements kjTr = tbody.get(0).getElementsByTag("tr");
                    for (int i = 2; i < kjTr.size() - 2; i++) {
                        Element tempElemet = kjTr.get(i);
                        int countTd = tempElemet.childNodeSize();
                        Elements tdAwardInfos = tempElemet.getElementsByTag("td");
                        StringBuffer stringBuffer = new StringBuffer();
                        if (i <= 11) {
                            if (tdAwardInfos.size() == 4) {
                                Element frontEle = kjTr.get(i - 1);
                                stringBuffer.append(frontEle.child(0).text()
                                        + "[" + tdAwardInfos.get(0).text() + "]" + ":"
                                        + tdAwardInfos.get(1).text() + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + getAwardLevelByName(frontEle.child(0).text()) + "_z");

                            } else {
                                stringBuffer.append(tdAwardInfos.get(0).text()
                                        + "[" + tdAwardInfos.get(1).text() + "]" + ":"
                                        + tdAwardInfos.get(2).text() + ":"
                                        + tdAwardInfos.get(3).text() + ":"
                                        + getAwardLevelByName(tdAwardInfos.get(0).text()));
                            }
                            awardInfoList.add(stringBuffer.toString());
                        } else {
//                        for (Element element : tdAwardInfos) {
                            for (int j = 0; j < tdAwardInfos.size() - 1; j++) {
                                if (!tdAwardInfos.get(j).text().equals("基本")) {
                                    stringBuffer.append(tdAwardInfos.get(j).text() + ":");
                                }
                            }
                            stringBuffer.append(getAwardLevelByName(tdAwardInfos.get(0).text()));
                            awardInfoList.add(stringBuffer.toString());
                        }
                    }
                }
            }
            return awardInfoList;
        }
    };

    public String getFirstPeriod() {
        return "2007001";
    }

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
        if (levelName.equals("一等奖")) {
            return "1";
        } else if (levelName.equals("二等奖")) {
            return "2";
        } else if (levelName.equals("三等奖")) {
            return "3";
        } else if (levelName.equals("四等奖")) {
            return "4";
        } else if (levelName.equals("五等奖")) {
            return "5";
        } else if (levelName.equals("六等奖")) {
            return "6";
        } else if (levelName.equals("七等奖")) {
            return "7";
        } else if (levelName.equals("八等奖")) {
            return "8";
        } else {
            return "12选2";
        }

    }
}
