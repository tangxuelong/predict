package com.mojieai.predict.entity.po;

import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * 单关方案 用户购买记录
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class DanguanProgramUser {
    private Long userId;
    private String matchId;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public DanguanProgramUser(Long userId,String matchId){
        this.userId = userId;
        this.matchId = matchId;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
    }
}