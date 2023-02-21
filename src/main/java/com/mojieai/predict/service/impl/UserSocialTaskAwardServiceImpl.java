package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.ExchangeMallDao;
import com.mojieai.predict.dao.UserAccountDao;
import com.mojieai.predict.dao.UserSocialTaskAwardDao;
import com.mojieai.predict.dao.UserSocialTaskIdSequenceDao;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.po.ExchangeMall;
import com.mojieai.predict.entity.po.SocialEncircle;
import com.mojieai.predict.entity.po.UserAccount;
import com.mojieai.predict.entity.po.UserSocialTaskAward;
import com.mojieai.predict.entity.vo.GoldCoinTaskVo;
import com.mojieai.predict.enums.GoldCoinTaskEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.UserSocialTaskAwardService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.service.goldcointask.AbstractTask;
import com.mojieai.predict.service.goldcointask.GoldCoinTaskFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserSocialTaskAwardServiceImpl implements UserSocialTaskAwardService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UserSocialTaskIdSequenceDao userSocialTaskIdSequenceDao;
    @Autowired
    private UserSocialTaskAwardDao userSocialTaskAwardDao;
    @Autowired
    private PayService payService;

    @Override
    public String generateTaskId(Long userId) {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        String taskIdStr = userId + "";
        long seq = userSocialTaskIdSequenceDao.insertTaskIdSeq();
        String taskId = Long.parseLong(timePrefix) + "SOCIALTASK" + CommonUtil.formatSequence(seq) + taskIdStr.substring
                (taskIdStr.length() - 2);
        return taskId;
    }

    @Override
    public void doUserSocialTask(Long gameId, String periodId, Long userId, Integer taskType, String clientIp, Integer
            clientId) {
        //1.查询用户是否满足派发奖励条件
        UserSocialTaskAward userTask = userSocialTaskAwardDao.getUserSocialTaskAwardByUnitKey(gameId, periodId, userId,
                taskType);
        if (userTask != null && userTask.getIsAward() != null && userTask.getIsAward().equals
                (SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_WAIT)) {
            //2.满足条件派发奖励
            //取任务金币
            GoldTask goldTask = payService.getTaskMap(taskType + "");
            long totalAmount = goldTask.getTaskAward() == null ? 0 : goldTask.getTaskAward();
            try {
                //2.1 通知派发奖励
                Map<String, Object> payRes = payService.fillAccount(userId, userTask.getTaskId(), totalAmount,
                        CommonConstant.PAY_TYPE_GOLD_COIN, null, totalAmount, getTaskTypeCn(userId, taskType),
                        clientIp, clientId);
                //2.2支付成功更新奖励状态
                if (payRes != null && (payRes.get("payStatus").equals(ResultConstant.PAY_SUCCESS_CODE) || payRes.get
                        ("payStatus").equals(ResultConstant.REPEAT_CODE))) {
                    userSocialTaskAwardDao.updateTaskIsAward(userTask.getTaskId(), userId, SocialEncircleKillConstant
                            .SOCIAL_TASK_IS_AWARD_YES, SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_WAIT);
                }
            } catch (Exception e) {
                log.error("通知派发奖励异常" + userId, e);
            }
        }
    }

    private String getTaskTypeCn(Long userId, Integer taskType) {
        GoldCoinTaskEnum goldCoinTaskEnum = GoldCoinTaskEnum.getGoldCoinTaskEnumByType(taskType + "");
        if (goldCoinTaskEnum != null) {
            AbstractTask task = GoldCoinTaskFactory.getInstance().getTaskBean(goldCoinTaskEnum.getTaskEn());
            GoldCoinTaskVo goldCoinTaskVo = task.getGoldCoinTaskVo(userId, goldCoinTaskEnum);
            return goldCoinTaskVo.getTaskName();
        }
        return "";
    }

    @Override
    public void taskAwardCompensateTimer() {
        List<UserSocialTaskAward> userSocialTaskAwards = new ArrayList<>();
        Integer limitNum = 10;
        for (int i = 0; i < 100; i++) {
            List<UserSocialTaskAward> tempTask = userSocialTaskAwardDao.getEarlistNumUserSocialTask(Long.valueOf(i),
                    SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_WAIT, limitNum);
            userSocialTaskAwards.addAll(tempTask);
        }
        for (UserSocialTaskAward temp : userSocialTaskAwards) {
            doUserSocialTask(temp.getGameId(), temp.getPeriodId(), temp.getUserId(), temp.getTaskType(), null, null);
        }

    }

    @Override
    public boolean checkUserFinishTask(long gameId, String periodId, Long userId, Integer taskType) {
        boolean res = false;
        UserSocialTaskAward userTask = userSocialTaskAwardDao.getUserSocialTaskAwardByUnitKey(gameId, periodId, userId,
                taskType);
        if (userTask != null && userTask.getIsAward().equals(SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_YES)) {
            res = true;
        }
        return res;
    }

    @Override
    public UserSocialTaskAward initUserSocialTask(Long gameId, String periodId, Long userId, Integer taskType) {
        //1.查询用户是否已经做过任务，没有做过现插入
        UserSocialTaskAward userTask = userSocialTaskAwardDao.getUserSocialTaskAwardByUnitKey(gameId, periodId, userId,
                taskType);
        if (userTask == null) {
            userTask = new UserSocialTaskAward();
            userTask.setTaskTimes(0);
            userTask.setGameId(gameId);
            userTask.setTaskType(taskType);
            userTask.setIsAward(SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_INIT);
            userTask.setPeriodId(periodId);
            userTask.setTaskId(generateTaskId(userId));
            userTask.setUserId(userId);
            try {
                userSocialTaskAwardDao.insert(userTask);
            } catch (Exception e) {
                if (e instanceof DuplicateKeyException) {
                    return userSocialTaskAwardDao.getUserSocialTaskAwardByUnitKey(gameId, periodId, userId, taskType);
                } else {
                    log.error("UserSocialTaskAward插入异常", e);
                    throw new BusinessException("UserSocialTaskAward插入异常", e);
                }
            }
        }
        return userTask;
    }

    @Override
    public Map<String, Object> getEarnGoldCoinTaskList(Long userId, Integer versionCode, Integer clientType) {
        Map<String, Object> result = new HashMap<>();

        // 用户余额
        UserAccount userAccount = payService.getUserAccount(userId, CommonConstant.ACCOUNT_TYPE_GOLD, Boolean
                .FALSE);
        result.put("userAccountBalance", userAccount.getAccountBalance());
        result.put("userAccountName", ActivityIniCache.getActivityIniValue(ActivityIniConstant.ACCOUNT_BALANCE_NAME,
                "我的金币"));

        Map<Integer, List<GoldCoinTaskVo>> taskMap = new HashMap<>();
        GoldCoinTaskFactory goldCoinTaskFactory = GoldCoinTaskFactory.getInstance();
        for (GoldCoinTaskEnum goldCoinTaskEnum : GoldCoinTaskEnum.values()) {
            List<GoldCoinTaskVo> tempTaskVos = null;
            if (taskMap.containsKey(goldCoinTaskEnum.getGroupType())) {
                tempTaskVos = taskMap.get(goldCoinTaskEnum.getGroupType());
            } else {
                tempTaskVos = new ArrayList<>();
            }
            GoldCoinTaskVo goldCoinTaskVo = goldCoinTaskFactory.getTaskBean(goldCoinTaskEnum.getTaskEn())
                    .getGoldCoinTaskVo(userId, goldCoinTaskEnum);
            if (goldCoinTaskVo != null) {
                tempTaskVos.add(goldCoinTaskVo);
            }
            taskMap.put(goldCoinTaskEnum.getGroupType(), tempTaskVos);
        }

        List<Map<String, Object>> totalTaskList = new ArrayList<>();
        for (Integer key : taskMap.keySet()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("title", GoldTask.getGroupCn(key));
            temp.put("task", taskMap.get(key));
            if (!temp.isEmpty()) {
                totalTaskList.add(temp);
            }
        }

        result.put("totalTaskList", totalTaskList);
        return result;
    }

    @Override
    public Boolean recordSocialTask(Long userId, String taskType, String clientIp, Integer clientId) {
        GoldCoinTaskEnum goldCoinTaskEnum = GoldCoinTaskEnum.getGoldCoinTaskEnumByType(taskType);
        if (goldCoinTaskEnum != null) {
            AbstractTask task = GoldCoinTaskFactory.getInstance().getTaskBean(goldCoinTaskEnum.getTaskEn());
            return task.recordSocialTask(userId, goldCoinTaskEnum, clientIp, clientId);
        }
        return false;
    }

}
