package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserVipProgram;
import com.mojieai.predict.entity.vo.ResultVo;

public interface UserVipProgramService {

    UserVipProgram produceUserVipProgramLog(Long userId, String goodsId, Integer payType);

    Boolean checkUserPurchaseVipProgram(Long userId, String programId);

    ResultVo vipExchangeVipProgram(Long userId, String programId);

    Boolean callBackMakeUserVipProgramEffective(String vipProgramPrePayId, String flowId);

    Boolean updateUserProgramLogAfterPayed(Long userId, String prePayId);

    ResultVo updateUserVipProgramPayStatusAndPrivilegeTimes(Long userId, String prePayId, String dateId);
}
