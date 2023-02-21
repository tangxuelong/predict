package com.mojieai.predict.entity.dto;

import lombok.Data;
/**
 * 基本走势的数据返回模版
 */
@Data
public class K3TrendChartRtnDto {
    private String period_num;
    private String period_name;
    private String bonus_code;
    private int sum_val;
    private int spans;
    private String [] omit_num;

    public K3TrendChartRtnDto(String period_num, String period_name, String bonus_code, int sum_val, int spans, String[] omit_num) {
        this.period_num = period_num;
        this.period_name = period_name;
        this.bonus_code = bonus_code;
        this.sum_val = sum_val;
        this.spans = spans;
        this.omit_num = omit_num;
    }

    public K3TrendChartRtnDto(String period_num, String period_name, String bonus_code, String[] omit_num) {
        this.period_num = period_num;
        this.period_name = period_name;
        this.bonus_code = bonus_code;
        this.omit_num = omit_num;
    }
}
