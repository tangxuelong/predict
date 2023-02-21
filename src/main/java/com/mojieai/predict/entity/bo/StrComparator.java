package com.mojieai.predict.entity.bo;

import com.mojieai.predict.constant.CommonConstant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StrComparator implements Comparable {

    private String strNum;

    public StrComparator(String strNum) {
        this.strNum = strNum;
    }

    @Override
    public int compareTo(Object o) {
        StrComparator strComparator = (StrComparator) o;
        String num1 = this.strNum.replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_STAR_STR,
                CommonConstant.SPACE_NULL_STR);
        String num2 = strComparator.getStrNum().replaceAll(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                .COMMON_STAR_STR, CommonConstant.SPACE_NULL_STR);
        if (Integer.valueOf(num1) > Integer.valueOf(num2)) {
            return 1;
        } else if (Integer.valueOf(num1) < Integer.valueOf(num2)) {
            return -1;
        }
        return 0;
    }
}
