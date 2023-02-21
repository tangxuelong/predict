package com.mojieai.predict.entity.dto;

public class Result {
    private String code;
    private String desc;

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Result() {
    }

    public Result(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Result [code=");
        builder.append(this.code);
        builder.append(", desc=");
        builder.append(this.desc);
        builder.append("]");
        return builder.toString();
    }
}
