package com.mojieai.predict.service.filter;

import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by tangxuelong on 2017/8/16.
 */
public class FilterFactory {
    private static FilterFactory instance = new FilterFactory();

    private FilterFactory() {
    }

    public static FilterFactory getInstance() {
        return instance;
    }

    public Filter getFilter(String gameEn) {
        if (StringUtils.isBlank(gameEn)) {
            throw new IllegalArgumentException("blank filter!");
        }
        Filter filter = SpringContextHolder.getBean(gameEn + "Filter");
        if (filter == null) {
            throw new BusinessException("工厂中的对象不存在:" + gameEn);
        }
        return filter;
    }
}
