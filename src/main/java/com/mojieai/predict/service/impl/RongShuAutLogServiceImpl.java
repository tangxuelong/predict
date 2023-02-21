package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.RongShuAutLogDao;
import com.mojieai.predict.entity.po.RongShuAutLog;
import com.mojieai.predict.service.RongShuAutLogService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RongShuAutLogServiceImpl implements RongShuAutLogService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private RongShuAutLogDao rongShuAutLogDao;

    @Override
    public Boolean addRongShuAutLog(Long userId, Integer autType, Integer autStatus, String rongShuResult) {
        Boolean result = Boolean.FALSE;
        if (rongShuResult == null || rongShuResult.isEmpty() || userId == null) {
            return result;
        }
        try {
            RongShuAutLog rongShuAutLog = new RongShuAutLog(userId, autType, autStatus, rongShuResult);
            if (rongShuAutLogDao.insert(rongShuAutLog) > 0) {
                result = Boolean.TRUE;
            }
        } catch (Exception e) {
            log.error("添加日志异常", e);
        }
        return result;
    }
}
