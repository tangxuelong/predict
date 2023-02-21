package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class TouristUser implements Serializable {
    private static final long serialVersionUID = -8748539035160590433L;

    private String deviceId;
    private String userToken;
    private Long userId;
    private Timestamp createTime;
}
