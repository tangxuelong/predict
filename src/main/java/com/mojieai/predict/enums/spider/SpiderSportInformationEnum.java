package com.mojieai.predict.enums.spider;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.entity.vo.SportInformationVo;
import com.mojieai.predict.enums.CronEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum SpiderSportInformationEnum {
    SINA("新浪") {
        @Override
        public Map<String, Object> getSportInformation() {
            long current = System.currentTimeMillis();
            String url = "http://cre.mix.sina.com" +
                    ".cn/get/cms/feed?callback=jQuery1113008885818120724132_" + current + "&pcProduct=30&ctime=&merge=3&mod" +
                    "=pcsptw&cre=tianyi&statics=1&length=12&ad=%7B%22rotate_count%22%3A100%2C%22platform%22%3A%22pc" +
                    "%22%2C%22channel%22%3A%22tianyi_pcspt%22%2C%22page_url%22%3A%22http%3A%2F%2Fsports.sina.com" +
                    ".cn%2F%22%2C%22timestamp%22%3A1533605549067+%7D&_=" + current;
            try {
                Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(CommonConstant
                        .AWARD_163_DOWNLOAD_TIMEOUT_MSEC).get();
                Map<String, Object> result = new HashMap<>();

                result.put("sportsInfo", analysisDocument(doc));
                return result;
            } catch (IOException e) {
                log.error("爬取新浪赛事数据异常", e);
            }
            return null;
        }
    }, NET_EASE("网易") {
        @Override
        public Map<String, Object> getSportInformation() {
            String url = "http://sports.163.com/special/000587PN/newsdata_world_index.js?callback=data_callback";

            try {
//                Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(CommonConstant
//                        .AWARD_163_DOWNLOAD_TIMEOUT_MSEC).get();
                Document doc = Jsoup.parse(new URL(url).openStream(), "GBK", url);
                Map<String, Object> result = new HashMap<>();

                result.put("sportsInfo", analysisNetEaseDocument(doc));
                return result;
            } catch (IOException e) {
                log.error("爬取网易赛事数据异常", e);
            }
            return null;
        }
    };

    private String name;

    private static final Logger log = CronEnum.PERIOD.getLogger();

    SpiderSportInformationEnum(String name) {
    }

    public String getName() {
        return name;
    }

    public abstract Map<String, Object> getSportInformation();

    private static List<SportInformationVo> analysisDocument(Document doc) {
        List<SportInformationVo> sportInfoVoList = new ArrayList<>();
        if (doc == null) {
            return sportInfoVoList;
        }
        Elements sportsInfoDivs = doc.getElementsByTag("body");
        String infoText = sportsInfoDivs.first().text();
        if (StringUtils.isNotBlank(infoText)) {
            infoText = infoText.substring(infoText.indexOf("{"), infoText.lastIndexOf("}") + 1);
            Map<String, Object> data = JSONObject.parseObject(infoText, HashMap.class);
            List<Map<String, Object>> sports = JSONObject.parseObject(data.get("data").toString(), ArrayList.class);
            Long i = System.currentTimeMillis();
            for (Map<String, Object> sport : sports) {
                String descShort = sport.get("short_intro").toString();
                String title = sport.get("title").toString();
                String imgUrl = sport.get("thumb").toString();
                if (title.contains("视频")) {
                    continue;
                }
                List<String> images = new ArrayList<>();
                String infoDetail = "";
                String url = sport.get("url_https").toString();
                SportInformationVo sportInfoVo = new SportInformationVo(i.intValue(), title, descShort, imgUrl,
                        infoDetail, images);
                sportInfoVoList.add(sportInfoVo);
                i++;
            }
        }

        return sportInfoVoList;
    }

    public static List<SportInformationVo> analysisNetEaseDocument(Document doc) {
        List<SportInformationVo> sportInfoVoList = new ArrayList<>();
        if (doc == null) {
            return sportInfoVoList;
        }
        Elements sportsInfoDivs = doc.getElementsByTag("body");

        sportsInfoDivs.first();
        String sportsInfoTxt = sportsInfoDivs.first().text();
        if (StringUtils.isNotBlank(sportsInfoTxt)) {
            Long i = System.currentTimeMillis();
            sportsInfoTxt = sportsInfoTxt.substring(sportsInfoTxt.indexOf("(") + 1, sportsInfoTxt.lastIndexOf(")"));
            List<Map<String, Object>> sportsList = JSONObject.parseObject(sportsInfoTxt, ArrayList.class);
            int j = 0;
            for (Map<String, Object> sport : sportsList) {
                String descShort = sport.get("title").toString();
                String title = descShort;
                if (descShort.indexOf("，") > 0) {
                    title = descShort.substring(0, descShort.indexOf("，"));
                }

                String imgUrl = sport.get("imgurl").toString();
                Map<String, Object> detailInfo = getDetailInfo(sport.get("docurl").toString());
                if (detailInfo == null) {
                    continue;
                }
                List<String> images = (List<String>) detailInfo.get("images");
                SportInformationVo sportInfoVo = new SportInformationVo(i.intValue() + j, title, descShort, imgUrl,
                        detailInfo.get("infoDetail").toString(), images);
                sportInfoVoList.add(sportInfoVo);
                j++;
            }
        }

        return sportInfoVoList;
    }

    private static Map<String, Object> getDetailInfo(String docUrl) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder infoDetail = new StringBuilder();
        List<String> images = new ArrayList<>();

        if (StringUtils.isBlank(docUrl)) {
            return null;
        }
        try {
            Document doc = Jsoup.parse(new URL(docUrl).openStream(), "GBK", docUrl);
            Elements text = doc.getElementsByClass("post_text");

            if (text.size() == 0) {
                return null;
            }
            Elements textInfo = text.first().getElementsByTag("p");
            int index = 0;
            for (Element element : textInfo) {
                if (index > 10) {
                    break;
                }
                Elements imgs = element.getElementsByTag("img");
                if (imgs != null && imgs.size() > 0) {
                    images.add(imgs.first().attr("src"));
                } else {
                    infoDetail.append(element.text().replaceAll("网易", "智慧"));
                }
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        result.put("infoDetail", infoDetail.toString());
        result.put("images", images);
        return result;
    }
}


