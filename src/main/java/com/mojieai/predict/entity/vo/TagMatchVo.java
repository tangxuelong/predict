package com.mojieai.predict.entity.vo;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.TreeSet;

@Data
public class TagMatchVo implements Serializable, Comparable<TagMatchVo> {
    private static final long serialVersionUID = -5354210124990150647L;

    private Timestamp matchTime;
    private String matchTimeStr;
    private TreeSet<TagMatchDetailVo> tagMatchDetailVo;

    public TagMatchVo(Timestamp matchTime, TreeSet<TagMatchDetailVo> tagMatchDetailVo) {
        this.matchTime = matchTime;
        this.tagMatchDetailVo = tagMatchDetailVo;
        this.matchTimeStr = DateUtil.formatTime(matchTime, "yyyy-MM-dd");
    }

    @Override
    public int compareTo(TagMatchVo o) {
        return this.matchTime.compareTo(o.getMatchTime());
    }
}
