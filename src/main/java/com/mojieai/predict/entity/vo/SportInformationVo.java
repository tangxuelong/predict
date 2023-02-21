package com.mojieai.predict.entity.vo;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

@Data
public class SportInformationVo implements Serializable {
    private static final long serialVersionUID = 7369605141545394217L;

    private String title;
    private String descShort;
    private String publishTime;
    private String imgUrl;
    private ItemDetail itemDetail;

    @Data
    private class ItemDetail {
        private Integer newsId;
        private String title;
        private String newsFrom;
        private String descShort;
        private String infoDetail;
        private List<String> images;
    }

    public SportInformationVo(Integer newsId, String title, String descShort, String imgUrl, String infoDetail,
                              List<String> images) {
        Timestamp currentTime = DateUtil.getCurrentTimestamp();
        currentTime = DateUtil.getIntervalMinutes(currentTime, new Random().nextInt(20));

        this.title = title;
        this.descShort = descShort;
        this.publishTime = DateUtil.formatTime(currentTime, "yyyy-MM-dd HH:mm:ss");
        this.imgUrl = imgUrl;

        ItemDetail detail = new ItemDetail();
        detail.setNewsId(newsId);
        detail.setTitle(title);
        detail.setNewsFrom("来源：智慧新闻 " + this.publishTime);
        detail.setDescShort(descShort);
        detail.setInfoDetail(infoDetail);
        detail.setImages(images);
        this.itemDetail = detail;

    }
}
