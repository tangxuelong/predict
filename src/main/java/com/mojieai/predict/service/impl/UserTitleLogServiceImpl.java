package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.TitleCache;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.UserTitleLogDao;
import com.mojieai.predict.dao.UserTitleLogIdSequenceDao;
import com.mojieai.predict.entity.po.Title;
import com.mojieai.predict.entity.po.UserTitleLog;
import com.mojieai.predict.entity.po.UserTitleLogIdSequence;
import com.mojieai.predict.service.UserTitleLogService;
import com.mojieai.predict.service.UserTitleService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserTitleLogServiceImpl implements UserTitleLogService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UserTitleLogIdSequenceDao userTitleLogIdSequenceDao;
    @Autowired
    private UserTitleLogDao userTitleLogDao;
    @Autowired
    private UserTitleService userTitleService;

    @Override
    public String generateTitleLogId(Long userId) {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        String titleLogIdStr = userId + "";
        long seq = userTitleLogIdSequenceDao.getUserTitleLogIdSequence();
        String titleLogId = Long.parseLong(timePrefix) + "TITLELOG" + CommonUtil.formatSequence(seq) + titleLogIdStr
                .substring(titleLogIdStr.length() - 2);
        return titleLogId;
    }

    @Override
    public void distributeTitleCompensateTiming() {
        //1.获取所有等待派发的数据
        List<UserTitleLog> userTitleLogs = userTitleLogDao.getAllNeedDistributeTitle(100);
        if (userTitleLogs == null || userTitleLogs.size() <= 0) {
            return;
        }
        for (UserTitleLog userTitleLog : userTitleLogs) {
            try {
                Title title = TitleCache.getTitleById(userTitleLog.getTitleId());
                userTitleService.distributeTitle2User(userTitleLog.getGameId(), userTitleLog.getUserId(), title
                        .getTitleEn(), userTitleLog.getDateStr(), userTitleLog.getDateNum());
            } catch (Exception e) {
                log.error("distributeTitleCompensate error", e);
                continue;
            }
        }
    }
}
