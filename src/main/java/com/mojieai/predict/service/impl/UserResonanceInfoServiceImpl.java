package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.dao.UserResonanceInfoDao;
import com.mojieai.predict.entity.po.UserResonanceInfo;
import com.mojieai.predict.service.UserResonanceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserResonanceInfoServiceImpl implements UserResonanceInfoService {

    @Autowired
    private UserResonanceInfoDao userResonanceInfoDao;

    @Override
    public Integer checkUserResonanceInfoPayStatus(long gameId, Long userId, Integer periodId) {
        Integer status = CommonConstant.PROGRAM_IS_PAY_NO;//todo 可以考虑缓存
        UserResonanceInfo userResonanceInfo = userResonanceInfoDao.getUserResonanceInfo(userId, gameId, false);
        if (userResonanceInfo == null) {
            return status;
        }
        if (userResonanceInfo.getLastPeriod() != null && userResonanceInfo.getLastPeriod() >= periodId) {
            status = CommonConstant.PROGRAM_IS_PAY_YES;
        }
        return status;
    }
}
