package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.exception.BusinessException;
import lombok.Data;
import org.apache.logging.log4j.Logger;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by qiwang
 */
@Data
public class CronTab {
    private String beanName;
    private String beanMethod;
    private Integer executeMode;//1.定时执行 2.顺序执行
    private String cron;
    private static Logger log = LogConstant.commonLog;

    public static Long getNextDelayTime(Object ob) {
        String cronStr;
        if (ob instanceof CronTab) {
            cronStr = ((CronTab) ob).getCron();
        } else {
            cronStr = (String) ob;
        }
        List<CronExpression> crons = new ArrayList<>();
        String[] cronArr = cronStr.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR);
        for (String cron : cronArr) {
            try {
                CronExpression ce = new CronExpression(cron);
                crons.add(ce);
            } catch (ParseException e) {
                log.warn("[getNextDelayTime] invalid cronExpression " + cron);
            }
        }
        if (crons.size() == 0) {
            throw new BusinessException("cron error." + ob.toString());
        }
        Date now = new Date();
        Date result = null;
        for (CronExpression cron : crons) {
            Date nextValidTime = cron.getNextValidTimeAfter(now);
            if (nextValidTime == null) {
                log.warn("[getNextDelayTime] getNextFire nextValidTime is null");
                continue;
            }
            if (result == null || nextValidTime.before(result)) {
                result = nextValidTime;
            }
        }
        long delay = result.getTime() - System.currentTimeMillis();
        return delay;
    }
}
