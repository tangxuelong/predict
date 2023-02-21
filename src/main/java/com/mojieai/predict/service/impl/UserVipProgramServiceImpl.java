package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.UserVipProgramDao;
import com.mojieai.predict.dao.UserVipProgramDateTimeDao;
import com.mojieai.predict.dao.UserVipProgramIdSequenceDao;
import com.mojieai.predict.dao.VipProgramDao;
import com.mojieai.predict.dao.impl.UserVipProgramDateTimeDaoImpl;
import com.mojieai.predict.entity.po.UserProgram;
import com.mojieai.predict.entity.po.UserVipProgram;
import com.mojieai.predict.entity.po.UserVipProgramDateTime;
import com.mojieai.predict.entity.po.VipProgram;
import com.mojieai.predict.entity.vo.ResultVo;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.service.UserVipProgramService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserVipProgramServiceImpl implements UserVipProgramService, BeanSelfAware {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private UserVipProgramDao userVipProgramDao;
    @Autowired
    private VipProgramDao vipProgramDao;
    @Autowired
    private UserVipProgramIdSequenceDao userVipProgramIdSeqDao;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserVipProgramDateTimeDao userVipProgramDateTimeDao;

    private UserVipProgramService self;

    @Override
    public UserVipProgram produceUserVipProgramLog(Long userId, String programId, Integer payType) {
        VipProgram vipProgram = vipProgramDao.getVipProgramByProgramId(programId, false);
        if (vipProgram == null || DateUtil.compareDate(vipProgram.getEndTime(), DateUtil.getCurrentTimestamp())) {
            log.error("会员专区方案已过期或不存在。programId:" + programId);
            return null;
        }
        UserVipProgram userVipProgram = userVipProgramDao.getUserVipProgramByUnkey(userId, programId);
        if (userVipProgram != null && userVipProgram.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return userVipProgram;
        }

        boolean updateFlag = false;
        if (userVipProgram == null) {
            userVipProgram = new UserVipProgram(userId, programId, CommonUtil.generateStrId(userId, "VIPPROGRAM",
                    userVipProgramIdSeqDao), CommonConstant.PROGRAM_IS_PAY_NO, payType);
        } else {
            updateFlag = true;
            userVipProgram.setPayType(payType);
        }

        if (payType.equals(CommonConstant.USER_VIP_PROGRAM_PAY_TYPE_VIP)) {
            String dateId = DateUtil.getCurrentDay();
            UserVipProgramDateTime times = userVipProgramDateTimeDao.getUserVipProgramTimes(userVipProgram.getUserId
                    (), dateId, false);
            if (times != null && times.getTimes() != null && times.getUseTimes() != null && times.getTimes() <= times
                    .getUseTimes()) {
                return null;
            }
        }

        try {
            if (updateFlag) {
                userVipProgramDao.update(userVipProgram);
            } else {
                userVipProgramDao.insert(userVipProgram);
            }
        } catch (DuplicateKeyException e) {
        }

        return userVipProgram;
    }

    @Override
    public Boolean checkUserPurchaseVipProgram(Long userId, String programId) {
        UserVipProgram userVipProgram = userVipProgramDao.getUserVipProgramByUnkey(userId, programId);
        if (userVipProgram != null && userVipProgram.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean callBackMakeUserVipProgramEffective(String vipProgramPrePayId, String flowId) {
        UserVipProgram userVipProgram = userVipProgramDao.getUserVipProgramByPk(vipProgramPrePayId);
        if (userVipProgram == null) {
            log.error("vipProgramPrePayId:" + vipProgramPrePayId + " 不存在，回调失败");
            return Boolean.FALSE;
        }
        if (userVipProgram.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            return Boolean.TRUE;
        }
        if (userVipProgram.getPayType().equals(CommonConstant.USER_VIP_PROGRAM_PAY_TYPE_VIP)) {
            return Boolean.FALSE;
        }

        return updateUserProgramLogAfterPayed(userVipProgram.getUserId(), userVipProgram.getPrePayId());
    }

    @Override
    public Boolean updateUserProgramLogAfterPayed(Long userId, String prePayId) {
        Integer updateRes = userVipProgramDao.updatePayedStatus(prePayId, CommonConstant.PROGRAM_IS_PAY_YES,
                CommonConstant.PROGRAM_IS_PAY_NO);
        if (updateRes > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public ResultVo vipExchangeVipProgram(Long userId, String programId) {
        ResultVo resultVo = new ResultVo(ResultConstant.ERROR, "会员专享");
        if (!vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
            return resultVo;
        }
        String dateId = DateUtil.getCurrentDay();
        UserVipProgramDateTime userVipProgramDateTime = userVipProgramDateTimeDao.getUserVipProgramTimes(userId,
                dateId, false);
        if (userVipProgramDateTime != null && userVipProgramDateTime.getTimes() != null && userVipProgramDateTime
                .getUseTimes() != null && userVipProgramDateTime.getTimes() <= userVipProgramDateTime.getUseTimes()) {
            resultVo.setMsg("今日特权已使用");
            return resultVo;
        }
        if (userVipProgramDateTime == null) {
            try {
                userVipProgramDateTime = new UserVipProgramDateTime(userId, dateId, 1, 0, DateUtil
                        .getCurrentTimestamp(), DateUtil.getCurrentTimestamp());
                userVipProgramDateTimeDao.insert(userVipProgramDateTime);
            } catch (DuplicateKeyException e) {
            }
        }
        UserVipProgram userVipProgram = produceUserVipProgramLog(userId, programId, CommonConstant
                .USER_VIP_PROGRAM_PAY_TYPE_VIP);
        try {
            resultVo = self.updateUserVipProgramPayStatusAndPrivilegeTimes(userId, userVipProgram.getPrePayId(),
                    dateId);
        } catch (BusinessException e) {
            resultVo.setMsg(e.getMessage());
        }

        return resultVo;
    }

    @Transactional
    @Override
    public ResultVo updateUserVipProgramPayStatusAndPrivilegeTimes(Long userId, String prePayId, String dateId) {
        UserVipProgramDateTime times = userVipProgramDateTimeDao.getUserVipProgramTimes(userId, dateId, Boolean.TRUE);
        if (times.getUseTimes() >= times.getTimes()) {
            return new ResultVo(ResultConstant.ERROR, "今日特权已使用");
        }
        Integer oldTimes = times.getUseTimes();
        Integer newTimes = oldTimes + 1;
        Integer timesUpdateRes = userVipProgramDateTimeDao.updateUserVipProgramDateTimeUseTimes(userId, dateId,
                newTimes, oldTimes);
        if (timesUpdateRes <= 0) {
            throw new BusinessException("今日特权使用失败");
        }
        Integer updateProgramRes = userVipProgramDao.updatePayedStatus(prePayId, CommonConstant.PROGRAM_IS_PAY_YES,
                CommonConstant.PROGRAM_IS_PAY_NO);
        if (updateProgramRes <= 0) {
            throw new BusinessException("兑换失败");
        }
        return new ResultVo(ResultConstant.SUCCESS, "兑换成功");
    }

    @Override
    public void setSelf(Object proxyBean) {
        this.self = (UserVipProgramService) proxyBean;
    }
}
