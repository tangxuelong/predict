package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.TitleCache;
import com.mojieai.predict.constant.UserTitleConstant;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.Title;
import org.apache.commons.lang3.StringUtils;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserTitleUtil {

    public static String getUserTitleMemo(String titleEn, String periodId, Integer date, Timestamp lastEndTime) {
        String res = "";
        Map paramMap = new HashMap<>();
        Title title = TitleCache.getTitleByEn(titleEn);
        String titleName = "";
        if (title != null) {
            titleName = "派发" + title.getTitleName() + "头衔";
        }

        paramMap.put("titleName", titleName);
        paramMap.put("periodId", periodId);
        paramMap.put("date", date + "天");
        paramMap.put("lastEndTime", DateUtil.formatTime(lastEndTime));
        if (!paramMap.isEmpty()) {
            res = JSONObject.toJSONString(paramMap);
        }
        return res;
    }

    public static String getCurrentWeekId() {
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYearOfCurrentDay() - 1)
                .toString();
    }

    public static String getTargetWeekId(Timestamp endTime) {
        Date date = new Date(endTime.getTime());
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYear(date) - 1)
                .toString();
    }

    public static String getTargetMonthId(Timestamp endTime) {
        Date date = new Date(endTime.getTime());
        return DateUtil.formatDate(date, "yyyyMM");
    }

    public static void main(String[] args) {
        getTargetMonthId(DateUtil.getCurrentTimestamp());
    }
}
