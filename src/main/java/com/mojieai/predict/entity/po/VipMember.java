package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.VipMemberConstant;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class VipMember {
    private String vipId;
    private Integer vipLevel;
    private Integer vipType;
    private Long userId;
    private Integer status;
    private Timestamp beginTime;
    private Timestamp endTime;
    private Timestamp createTime;
    private Timestamp updateTime;

    public VipMember(Integer status, Long userId, String vipId, Integer vipType) {
        this.status = status;
        this.userId = userId;
        this.vipId = vipId;
        this.vipLevel = VipMemberConstant.VIP_LEVEL_ONE;
        this.vipType = vipType;
    }
}
