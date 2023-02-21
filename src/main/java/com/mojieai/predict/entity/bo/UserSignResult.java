package com.mojieai.predict.entity.bo;

import com.mojieai.predict.entity.po.UserSign;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserSignResult implements Serializable {
    private static final long serialVersionUID = 783493153110896239L;

    private Integer code;
    private String msg;
    private UserSign userSign;
}
