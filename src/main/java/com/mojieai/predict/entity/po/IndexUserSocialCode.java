package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class IndexUserSocialCode {
    private Integer indexId;
    private Long userId;
    private Long gameId;
    private String periodId;
    private Integer socialCodeType;
    private Long socialCodeId;
    private String remark;
    private Integer socialCount;
    private Integer socialRightCount;
    private Timestamp createTime;
    private Timestamp updateTime;
}
