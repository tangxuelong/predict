package com.mojieai.predict.service;

import com.mojieai.predict.entity.po.UserDeviceInfo;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.entity.vo.UserLoginVo;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public interface LoginService {

    /* 验证码校验*/
    String verifyCodeValidate(String mobile, String verifyCode);

    /* 密码校验通过手机*/
    Boolean passwordValidate(String mobile, String password);

    /* 次数校验*/
    Boolean checkValidateTimes(String mobile, String type);

    /* 密码校验通过userId*/
    Boolean passwordValidateByUserId(Long userId, String password);

    /* 根据用户手机号获取用户*/
    UserLoginVo getUserLoginVo(String mobile, String oauthId, Integer oauthType);

    /* 根据用户token获取用户*/
    UserLoginVo getUserLoginVo(Long userId);

    /* 创建userId*/
    Long generateUserId();

    /* 创建token*/
    String generateUserToken(Long userId);

    /* checkToken*/
    UserToken checkToken(String token);

    /* checkToken*/
    Boolean checkUser(Long userId);

    /* 设置密码*/
    void setPassword(Long userId, String password);

    UserLoginVo modifyHeadImgOrNickName(String token, Long userId, String headImgUrl, String nickName);

    /* 登录*/
    UserLoginVo userLogin(String mobile, String password, String channelType, String oauthId, Integer oauthType,
                          String deviceId);

    Long getUserId(String mobile);

    /* 更新设备信息*/
    void updateDeviceInfo(UserDeviceInfo userDeviceInfo);

    /* 获取deviceId*/
    String generateDeviceId(String deviceId);

    LinkedBlockingQueue<UserDeviceInfo> UPDATE_DEVICE_QUEUE = new LinkedBlockingQueue<>();

    // 检查用户是否设置密码
    Boolean checkUserIsSetPassword(Long userId);

    //
    Map<String,Object> editUserFootballIntroduction(Long userId, String text);
}
