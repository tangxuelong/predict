package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.UserVipProgramDateTimeDao;
import com.mojieai.predict.entity.po.UserVipProgramDateTime;
import com.mojieai.predict.service.UserVipProgramDateService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserVipProgramDateServiceImpl implements UserVipProgramDateService {

    @Autowired
    private UserVipProgramDateTimeDao userVipProgramDateTimeDao;
    @Autowired
    private VipMemberService vipMemberService;

    @Override
    public Integer getUserVipProgramPrivilegeStatus(Long userId) {
        Integer status = CommonConstant.VIP_PROGRAM_PROVILEGE_DISABLE;
        if (!vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
            return status;
        }
        String dateId = DateUtil.getCurrentDay();
        UserVipProgramDateTime dateTime = userVipProgramDateTimeDao.getUserVipProgramTimes(userId, dateId, false);
        if (dateTime == null || dateTime.getUseTimes() == null || dateTime.getUseTimes() == 0) {
            return CommonConstant.VIP_PROGRAM_PROVILEGE_ENABLE;
        }
        if (dateTime.getTimes() != null && dateTime.getTimes() > dateTime.getUseTimes()) {
            return CommonConstant.VIP_PROGRAM_PROVILEGE_ENABLE;
        }
        if (dateTime.getTimes() != null && dateTime.getTimes() <= dateTime.getUseTimes()) {
            return CommonConstant.VIP_PROGRAM_PROVILEGE_USEED;
        }
        return status;
    }
}
