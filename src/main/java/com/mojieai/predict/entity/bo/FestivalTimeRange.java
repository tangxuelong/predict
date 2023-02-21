package com.mojieai.predict.entity.bo;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class FestivalTimeRange {
    private Timestamp startTime;
    private Timestamp endTime;

    public FestivalTimeRange(Timestamp startTime, Timestamp endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int restDays() {
        return DateUtil.getDiffDays(startTime, endTime) + 1;
    }
}
