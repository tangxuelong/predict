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

public enum SsqDataGrep500Enum {
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
                        //双色球 第 03001 期 开奖日期：2003年2月23日 兑奖截止日期：2003年3月23日
                        if (StringUtils.isNotEmpty(endTimeStr)) {
                            String[] timeArr = endTimeStr.split("：");
                            if (timeArr.length >= 3) {
                                String[] timeArr1 = timeArr[1].split(" ");
                                kjDate = timeArr1[0];
                            }
                        }
                    }

                    rtn.put("winNums", redWinBall + ":" + buleWinBall);
                    rtn.put("periodId", "20" + periodId);
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
                if (week == 1) {
                    beginTime = DateUtil.getIntervalDate(endDate, -3);
                } else if (week == 3 || week == 5) {
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
                        Element kjxqTable = kjxqBoxs.get(0).child(1).child(2);
                        Elements tbody = kjxqTable.getElementsByTag("tbody");
                        Elements kjTr = tbody.get(0).getElementsByTag("tr");
                        for (int i = 2; i < kjTr.size() - 1; i++) {
                            Element tempElemet = kjTr.get(i);
                            Elements tdAwardInfos = tempElemet.getElementsByTag("td");
                            StringBuffer stringBuffer = new StringBuffer();
                            for (Element element : tdAwardInfos) {
                                stringBuffer.append(element.text() + ":");
                            }
                            stringBuffer.append(i - 1);
                            awardInfoList.add(stringBuffer.toString());
                        }
                        //奖池和销量
                        Element poolAndSaleTable = kjxqBoxs.get(0).child(1).child(0);
                        Elements poolAndSaleTbody = poolAndSaleTable.getElementsByTag("tbody");
                        Elements poolAndSaleSpans = poolAndSaleTbody.get(0).child(2).getElementsByTag("span");
                        String periodSale = poolAndSaleSpans.get(0).text();
                        String poolBonus = poolAndSaleSpans.get(1).text();
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
    };

    public String getFirstPeriod() {
        return "2003001";
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
}
