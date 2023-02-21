package com.mojieai.predict.entity.dto;

import lombok.Data;

@Data
public class HttpRespMsgDto<T> {
    private int code;  //http code
    private String msg;
    private T[] resp;
}