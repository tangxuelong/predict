package com.mojieai.predict.entity.vo;

import com.mojieai.predict.exception.BusinessException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
public class EncirclePeriodsVo implements Comparable {
    private String periodId;
    private long gameId;
    private String periodName;
    private String adMsg;
    private String leadEncircleAdMsg;
    private String leadEncircleBackAdMsg;
    private String filterEncircleBtnAdMsg;
    private Integer filterEncircleBtnStatus;
    private Set<MyEncircleVo> encircles = new TreeSet<>();

    public boolean addMyEncircleVo(MyEncircleVo myEncircleVo) {
        return this.encircles.add(myEncircleVo);
    }

    public void addAllMyEncircleVo(List<MyEncircleVo> myEncircleVos) {
        if (myEncircleVos != null && myEncircleVos.size() > 0) {
            encircles.addAll(myEncircleVos);
        }
    }

    public void addArrayListMyEncircleVo(List<MyEncircleVo> myEncircleVos) {
        if (myEncircleVos != null && myEncircleVos.size() > 0) {
            for (MyEncircleVo temp : myEncircleVos) {
                encircles.add(temp);
            }
        }
    }

    public void resetEncircles(Set<MyEncircleVo> encircles) {
        this.encircles = encircles;
    }

    @Override
    public int compareTo(Object o) {
        try {
            EncirclePeriodsVo comparPeriodVo = (EncirclePeriodsVo) o;
            Integer periodId = Integer.valueOf(this.periodId);
            Integer comparPeriodId = Integer.valueOf(comparPeriodVo.getPeriodId());
            if (periodId > comparPeriodId) {
                return -1;
            }
            if (periodId < comparPeriodId) {
                return 1;
            }
        } catch (Exception e) {
            throw new BusinessException("强转EncirclePeriodsVo异常", e);
        }

        return 0;
    }
}
