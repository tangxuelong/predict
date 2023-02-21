package com.mojieai.predict.entity.dto;

import java.util.Arrays;

/**
 * 数据统计的dto
 */
public class TrendStatisticDto {
    private String show_name;
    private int [] static_data;

    public TrendStatisticDto(){}

    public TrendStatisticDto(String show_name, int[] static_data) {
        this.show_name = show_name;
        this.static_data = static_data;
    }

    public String getShow_name() {
        return show_name;
    }

    public void setShow_name(String show_name) {
        this.show_name = show_name;
    }

    public int[] getStatic_data() {
        return static_data;
    }

    public void setStatic_data(int[] static_data) {
        this.static_data = static_data;
    }

    @Override
    public String toString() {
        return "TrendStatisticDto{" +
                "show_name='" + show_name + '\'' +
                ", static_data=" + Arrays.toString(static_data) +
                '}';
    }
}
