package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.TouristUserDao;
import com.mojieai.predict.entity.po.TouristUser;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.TouristUserService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.util.CommonUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TouristUserServiceImpl implements TouristUserService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private TouristUserDao touristUserDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private VipMemberService vipMemberService;

    @Override
    public Map<String, Object> checkDeviceId(String deviceId, String channelType) {
        Map<String, Object> result = new HashMap<>();
        String token = "";
        boolean isTourist = false;
        boolean isVip = false;
        boolean isSportsVip = false;
        UserLoginVo userLoginVo = null;
        TouristUser user = touristUserDao.getUserIdByDeviceId(deviceId);
        //1.如果用户为null先创建用户
        if (user == null) {
            String mobile = CommonUtil.generateTouristMoile();
            userLoginVo = loginService.userLogin(mobile, CommonConstant.PASSWORD, channelType, null, null, deviceId);
            if (userLoginVo != null) {
                user = new TouristUser();
                user.setDeviceId(deviceId);
                user.setUserId(userLoginVo.getUserId());
                user.setUserToken(userLoginVo.getToken());
                Integer res = touristUserDao.insert(user);
                if (res > 0) {
                    isTourist = true;
                    isVip = vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                            .VIP_MEMBER_TYPE_DIGIT);
                    isSportsVip = vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                            .VIP_MEMBER_TYPE_SPORTS);
                }
            }
        }
        if (user != null) {
            isTourist = true;
            userLoginVo = loginService.getUserLoginVo(user.getUserId());
            isVip = vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
            isSportsVip = vipMemberService.checkUserIsVip(userLoginVo.getUserId(), VipMemberConstant
                    .VIP_MEMBER_TYPE_SPORTS);

        }
        result.put("userLogin", userLoginVo);
        result.put("isVip", isVip);
        result.put("isSportsVip", isSportsVip);
        result.put("isTourist", isTourist);
        return result;
    }

    @Override
    public boolean checkUserIdIsTourist(Long userId) {
        if (userId == null) {
            return false;
        }
        TouristUser user = touristUserDao.getUserByUserId(userId);
        if (user != null && user.getUserId() != null) {
            return true;
        }
        return false;
    }
}
