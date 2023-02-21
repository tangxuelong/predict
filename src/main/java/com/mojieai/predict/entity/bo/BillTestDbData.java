package com.mojieai.predict.entity.bo;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class BillTestDbData {
    private Timestamp payDate;
    private Long money;

    public BillTestDbData(String payDateStr, Long money) {
        this.payDate = DateUtil.formatToTimestamp(payDateStr, "yyyy-MM-dd HH:mm:ss");
        this.money = money;
    }
}
