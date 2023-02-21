package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class RongShuAutLog implements Serializable {
    private static final long serialVersionUID = 413570892436070769L;

    private Long userId;
    private Integer autType;
    private Integer autStatus;
    private String autResult;
    private Timestamp createTime;

    public RongShuAutLog(Long userId, Integer autType, Integer autStatus, String autResult) {
        this.userId = userId;
        this.autType = autType;
        this.autStatus = autStatus;
        this.autResult = autResult;
    }
}
