package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 单关方案
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class DanguanProgram {
    private String matchId;
    private String programInfo;
    private Long price;
    private Long vipPrice;
    private String rightItem;
    private Integer isAwarded;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public DanguanProgram(String matchId, String programInfo, Long price, Long vipPrice) {
        this.matchId = matchId;
        this.programInfo = programInfo;
        this.price = price;
        this.vipPrice = vipPrice;
        this.isAwarded = 0;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}