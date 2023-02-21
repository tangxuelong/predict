package com.mojieai.predict.entity.bo;

import lombok.Data;

/**
 * 用于比较int大小
 */
@Data
public class IntegerComparable implements Comparable{

    private int num;

    public IntegerComparable(int num){
        this.num = num;
    }

    @Override
    public int compareTo(Object o) {
        IntegerComparable intCmpr = (IntegerComparable) o;
        if(this.num > intCmpr.num){
            return 1;
        }else if(this.num<intCmpr.num){
            return -1;
        }else{
            return 0;
        }
    }
}
