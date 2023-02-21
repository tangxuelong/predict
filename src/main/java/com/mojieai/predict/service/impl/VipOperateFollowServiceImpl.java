package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.VipMemberDao;
import com.mojieai.predict.dao.VipOperateFollowDao;
import com.mojieai.predict.dao.VipOperateIdSequenceDao;
import com.mojieai.predict.entity.po.VipMember;
import com.mojieai.predict.entity.po.VipOperateFollow;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SocialKillCodeService;
import com.mojieai.predict.service.VipOperateFollowService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class VipOperateFollowServiceImpl implements VipOperateFollowService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private VipOperateIdSequenceDao vipOperateIdSequenceDao;
    @Autowired
    private VipOperateFollowDao vipOperateFollowDao;
    @Autowired
    private VipMemberDao vipMemberDao;
    @Autowired
    private RedisService redisService;

    @Override
    public String generateVipOperateId(Long userId) {
        String userIdStr = userId + "";
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = vipOperateIdSequenceDao.getVipOperateIdSequence();
        String vipFollowId = Long.parseLong(timePrefix) + "VIPFOLLOW" + CommonUtil.formatSequence(seq) + userIdStr
                .substring(userIdStr.length() - 2);
        return vipFollowId;
    }
}
