package com.mojieai.predict.constant;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.entity.bo.FestivalTimeRange;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;

import static com.mojieai.predict.constant.IniConstant.SPRING_FESTIVAL_DAYS;


/**
 * 节日
 *
 * @author Liaoxu
 */
public class FestivalConstant {
    private static Logger log = LogConstant.commonLog;
    public static Timestamp START;// 节日(正在进行的节日或者下个节日)开始时间
    public static Timestamp END; // 节日(正在进行的节日或者下个节日)结束时间

    public void init() {
        //先赋一个默认值，以防下面计算错误START、END值为空
        START = DateUtil.formatToTimestamp("20160208", DateUtil.DATE_FORMAT_YYYYMMDD);
        END = DateUtil.formatToTimestamp("20160214", DateUtil.DATE_FORMAT_YYYYMMDD);

        //输入当年ini数据（格式：20140130,20140205）
        String festivalTimes = IniCache.getIniValue(SPRING_FESTIVAL_DAYS, "20170127,20170202");
        String[] festivalTime = null;
        if (festivalTimes == null) {
            log.error("ini配置的春节开始时间festivalTimes为空，请检查");
        } else {
            festivalTime = festivalTimes.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.SEMICOLON_SPLIT_STR);
        }
        if (festivalTime == null || festivalTime.length <= 0) {
            log.error("ini配置的春节开始时间festivalTimes有误，请检查");
        } else {
            Timestamp start = null;
            Timestamp end = null;
            Timestamp now = DateUtil.getCurrentTimestamp();
            for (int i = 0; i < festivalTime.length; i++) {
                String festival[] = festivalTime[i].split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                        .COMMA_SPLIT_STR);
                if (festival == null || festival.length != 2) {
                    log.error("ini配置的春节开始时间festivalTimes格式有误，请检查");
                    continue;
                }
                try {
                    Timestamp startTemp = DateUtil.formatString(festival[0], DateUtil.DATE_FORMAT_YYYYMMDD);
                    Timestamp endTemp = DateUtil.getEndOfOneDay(DateUtil.formatString(festival[1], DateUtil
                            .DATE_FORMAT_YYYYMMDD));
                    if (now.before(endTemp) && (end == null || end.after(endTemp))) {
                        start = startTemp;
                        end = endTemp;
                    }
                } catch (Exception e) {
                    log.error("春节停售配置错误，请验证！！！，" + festival[0] + "," + festival[1], e);
                }
            }
            if (start != null && end != null) {
                START = start;
                END = end;
            } else {
                log.error("ini配置的春节开始时间festivalTimes没有下一个节日时间，请配置");
            }
        }
    }


    /**
     * 是否在假日期间
     *
     * @param time
     * @return 不是返回0，是返回假日天数
     */
    public static FestivalTimeRange getFestivalDays(Timestamp time) {
        if (time == null) {
            return null;
        }
        if (time.compareTo(START) >= 0 && time.compareTo(END) <= 0) {
            FestivalTimeRange festivalTimeRange = new FestivalTimeRange(START, END);
            return festivalTimeRange;
        }
        return null;
    }
}
