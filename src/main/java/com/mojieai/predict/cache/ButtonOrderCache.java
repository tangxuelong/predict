package com.mojieai.predict.cache;

import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.ButtonOrderedDao;
import com.mojieai.predict.entity.po.ButtonOrdered;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ButtonOrderCache {
    private static Map<String, List<ButtonOrdered>> BUTTON_ORDERED_ALL = new HashMap<>();

    protected static Logger log = LogConstant.commonLog;

    @Autowired
    private ButtonOrderedDao buttonOrderedDao;

    private ButtonOrderCache() {
    }

    public void init() {
        refresh();
    }

    public void refresh() {
        List<ButtonOrdered> buttonOrdereds = buttonOrderedDao.getAllBtnOrdered();

        for (ButtonOrdered temp : buttonOrdereds) {
            List<ButtonOrdered> tempList = null;
            String key = temp.getGameId() + CommonConstant.COMMON_SPLIT_STR + temp.getType();
            if (BUTTON_ORDERED_ALL.containsKey(key)) {
                tempList = BUTTON_ORDERED_ALL.get(key);
            } else {
                tempList = new ArrayList<>();
            }
            tempList.add(temp);
            BUTTON_ORDERED_ALL.put(key, tempList);
        }
        //排序
        for (String key : BUTTON_ORDERED_ALL.keySet()) {
            List<ButtonOrdered> tempList = BUTTON_ORDERED_ALL.get(key);
            tempList = tempList.stream().sorted(Comparator.comparing(ButtonOrdered::getWeight)).collect(Collectors
                    .toList());
            BUTTON_ORDERED_ALL.put(key, tempList);
        }
    }

    public static List<ButtonOrdered> getButtonOrdered(long gameId, Integer type) {
        String key = gameId + CommonConstant.COMMON_SPLIT_STR + type;
        return BUTTON_ORDERED_ALL.get(key);
    }

    public static List<ButtonOrdered> getButtonOrdered(long gameId, Integer type, String customNav) {
        String key = gameId + CommonConstant.COMMON_SPLIT_STR + type;
        String tempCustomNav = "";
        if (StringUtils.isNotBlank(customNav)) {
            tempCustomNav = customNav.replaceAll(CommonConstant.COMMA_SPLIT_STR, CommonConstant.SPACE_NULL_STR);
        }
        if (StringUtils.isBlank(tempCustomNav)) {
            customNav = ActivityIniCache.getActivityIniValue(ActivityIniConstant.getDigitIndexDefaultNav(gameId));
            tempCustomNav = customNav.replaceAll(CommonConstant.COMMA_SPLIT_STR, CommonConstant.SPACE_NULL_STR);
        }
        if (BUTTON_ORDERED_ALL.get(key) == null) {
            return null;
        }
        if (StringUtils.isBlank(tempCustomNav)) {
            throw new IllegalArgumentException("非法的customNav pls check" + customNav);
        }
        List<ButtonOrdered> result = new ArrayList<>();
        for (ButtonOrdered button : BUTTON_ORDERED_ALL.get(key)) {
            if (customNav.contains(String.valueOf(button.getBtnId()))) {
                result.add(button);
            }
        }
        return result;
    }


}
