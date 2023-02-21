package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.VipProgram;
import com.mojieai.predict.entity.vo.ResultVo;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/7.
 */
@Service
public interface VipProgramService {
    ResultVo productVipProgram(Integer awardNum, Integer recommendNum, String price, String programInfo);

    Map<String, Object> getVipProgramList(Long userId);

    Map<String, Object> getRedVipProgram(Long lastIndex);

    void rebuildRedVipProgramRedis();

    Boolean saveRedVipProgram2Redis(VipProgram vipProgram);

    void vipProgramOpenPrizeTiming();

    Boolean updateVipProgramStatusAfterCalculate(String programId, String calculateProgramInfo, Integer matchCount);
}
