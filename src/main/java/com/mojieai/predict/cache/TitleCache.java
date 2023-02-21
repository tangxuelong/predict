package com.mojieai.predict.cache;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.TitleDao;
import com.mojieai.predict.entity.po.Title;
import com.mojieai.predict.util.qiniu.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class TitleCache {
    private static final Logger log = LogConstant.commonLog;

    private static List<Title> allTitle = new ArrayList<>();

    @Autowired
    private TitleDao titleDao;

    private TitleCache() {
    }

    public void init() {
        refresh();
    }

    public void refresh() {
        allTitle = titleDao.getAllTitle();
    }

    public static Title getTitleById(Integer titleId) {
        if (titleId == null) {
            return null;
        }
        for (Title title : allTitle) {
            if (title.getTitleId().equals(titleId)) {
                return title;
            }
        }
        return null;
    }

    public static Title getTitleByEn(String titleEn) {
        if (StringUtils.isBlank(titleEn)) {
            return null;
        }
        for (Title title : allTitle) {
            if (title.getTitleEn().equals(titleEn)) {
                return title;
            }
        }
        return null;
    }


}
