package com.mojieai.predict.service;

import com.mojieai.predict.entity.vo.ResultVo;

import java.util.Map;

public interface UserInfoService {

    Map<String, Object> saveUserLotteryType(Long userId, Integer type);

    ResultVo saveUserWithdrawPwd(Long userId, String password);

    ResultVo updateUserWithdrawPwd(Long userId, String oldPassword, String newPassword);

    Boolean updateUserInfoRemark(String originRemark, String setRemark, Long userId);

    Boolean checkUserReceivePush(Long userId, Integer pushType);

    Integer getUserLotteryType(Long userId);

    Map<String, Object> getUserInfoByNickNameOrMobileFromOtter(String nickName, String mobile);

    Boolean safeCheck(String deviceId, String mobile);

    Map<String, Object> authenticateRealName(Long userId, String userName, String idCard);

    Boolean checkUserIfAuthenticate(Long userId);

    Map<String, Object> getPersonalData(Long userId);
}
