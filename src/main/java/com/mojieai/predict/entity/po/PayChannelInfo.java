package com.mojieai.predict.entity.po;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局配置表对应的ini
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class PayChannelInfo {
    private Integer channelId;
    private String channelName;
    private String channelIcon;
    private String showText;
    private Integer isDefault;
    private String payUrl;
    private String notifyUrl;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Integer weight;
    private String remark;

    public Integer getChannelStatus(Long price) {
        if (getChannelStatus() == 0) {
            return 0;
        }

        Integer status = 1;
        Map<String, Object> channel = JSONObject.parseObject(this.remark, HashMap.class);

        Long upperLimit = null;
        Long lowerLimit = null;
        String upperLimitStr = CommonUtil.getValueFromMap("upperLimit", channel);
        String lowerLimitStr = CommonUtil.getValueFromMap("lowerLimit", channel);
        if (StringUtils.isNotBlank(upperLimitStr)) {
            upperLimit = Long.valueOf(upperLimitStr);
        }
        if (upperLimit != null && price > upperLimit) {
            status = 0;
        }

        if (StringUtils.isNotBlank(lowerLimitStr)) {
            lowerLimit = Long.valueOf(lowerLimitStr);
        }
        if (lowerLimit != null && price < lowerLimit) {
            status = 0;
        }
        return status;
    }

    public Integer getChannelStatus() {
        Integer status = 1;
        if (StringUtils.isBlank(this.remark)) {
            return status;
        }
        Map<String, Object> channel = JSONObject.parseObject(this.remark, HashMap.class);
        String limitTime = CommonUtil.getValueFromMap("limitTime", channel) == null ? "" : CommonUtil.getValueFromMap
                ("limitTime", channel);

        if (StringUtils.isNotBlank(limitTime)) {
            String[] timeArr = limitTime.split("-");
            Timestamp currentTime = DateUtil.getCurrentTimestamp();
            Timestamp beginTime = CommonUtil.getSomeDateJoinTime(currentTime, timeArr[0], "yyyy-MM-dd HH:mm");
            Timestamp endTime = CommonUtil.getSomeDateJoinTime(currentTime, timeArr[1], "yyyy-MM-dd HH:mm");
            if (DateUtil.compareDate(currentTime, beginTime) || DateUtil.compareDate(endTime, currentTime)) {
                status = 0;
            }
        }
        return status;
    }
}