package com.mojieai.predict.entity.po;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class MatchInfo implements Serializable {
    private static final long serialVersionUID = 2399583990765121581L;
    private Integer matchId;
    private String matchTagId;
    private Timestamp matchTime;
    private String remark;

    public MatchInfo(Integer matchId, String matchTagId, Timestamp matchTime) {
        this.matchId = matchId;
        this.matchTagId = matchTagId;
        this.matchTime = matchTime;
    }

    public static String remarkAddInfo(Map<String, Object> remarkInfo, String oldRemark) {
        if (remarkInfo == null || remarkInfo.size() == 0) {
            return oldRemark;
        }
        Map<String, Object> remarkMap = null;
        if (StringUtils.isNotBlank(oldRemark)) {
            remarkMap = JSONObject.parseObject(oldRemark, HashMap.class);
            remarkMap.putAll(remarkInfo);
        } else {
            remarkMap = remarkInfo;
        }
        return JSONObject.toJSONString(remarkMap);
    }

    public static String remarkAddInfo(String key, String value, String oldRemark) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return "";
        }
        Map<String, Object> temp = new HashMap<>();
        temp.put(key, value);
        return remarkAddInfo(temp, oldRemark);
    }
}
