package com.mojieai.predict.service;

import com.mojieai.predict.entity.vo.ResultVo;

public interface UserBankCardService {

    Boolean checkUserIfBankCard(Long userId);

    ResultVo addUserBankCardWithOutAuth(Long userId, String realName, String bankCardNo, String mobile);

    ResultVo unbindUserBankCard(Long userId, Integer bankId);
}
