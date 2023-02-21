package com.mojieai.predict.service.goldcointask;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.SocialEncircleKillConstant;
import com.mojieai.predict.dao.UserSocialTaskAwardDao;
import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.po.UserSocialTaskAward;
import com.mojieai.predict.entity.vo.GoldCoinTaskVo;
import com.mojieai.predict.enums.GoldCoinTaskEnum;
import com.mojieai.predict.service.GoldTaskAwardService;
import com.mojieai.predict.service.UserSocialTaskAwardService;
import com.mojieai.predict.thread.SocialTaskAwardTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;

public abstract class AbstractTask {
    protected Logger log = LogConstant.commonLog;
    @Autowired
    protected UserSocialTaskAwardService userSocialTaskAwardService;
    @Autowired
    protected GoldTaskAwardService goldTaskAwardService;
    @Autowired
    private UserSocialTaskAwardDao userSocialTaskAwardDao;

    public abstract GoldCoinTaskVo getGoldCoinTaskVo(Long userId, GoldCoinTaskEnum goldCoinTaskEnum);

    protected GoldTask getTaskAwardByType(String taskType) {
        GoldCoinTaskEnum goldCoinTaskEnum = GoldCoinTaskEnum.getGoldCoinTaskEnumByType(taskType);
        if (goldCoinTaskEnum != null) {
            return goldCoinTaskEnum.getGoldTask();
        }
        return null;
    }

    public Boolean recordSocialTask(Long userId, GoldCoinTaskEnum goldCoinTaskEnum, String clientIp, Integer clientId) {
        return false;
    }

    public Boolean recordSportsSocialTask(Long userId, GoldCoinTaskEnum goldCoinTaskEnum, String clientIp, Integer
            clientId) {
        UserSocialTaskAward userSocialTaskAward = userSocialTaskAwardService.initUserSocialTask(null, DateUtil
                .getCurrentDay(), userId, Integer.valueOf(goldCoinTaskEnum.getTaskType()));
        if (userSocialTaskAward.getIsAward().equals(SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_YES)) {
            return true;
        }
        Integer oldTaskTimes = userSocialTaskAward.getTaskTimes();
        if (goldCoinTaskEnum.getGoldTask() == null) {
            log.error("记录任务失败,任务奖励不存在.type" + goldCoinTaskEnum.getTaskType() + " " + goldCoinTaskEnum.getTaskEn());
            return false;
        }
        Integer isAward = SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_INIT;
        Integer taskTimes = (oldTaskTimes == null ? 0 : oldTaskTimes) + 1;
        if (oldTaskTimes != null && goldCoinTaskEnum.getGoldTask().getTaskTimes().equals(taskTimes)) {
            isAward = SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_WAIT;
        }

        userSocialTaskAwardDao.updateTaskTimesById(userSocialTaskAward.getTaskId(), userId, taskTimes, oldTaskTimes,
                isAward);
        //派发奖励
        if (isAward.equals(SocialEncircleKillConstant.SOCIAL_TASK_IS_AWARD_WAIT)) {
            ExecutorService exec = ThreadPool.getInstance().getUserSocialTaskExec();
            SocialTaskAwardTask task = new SocialTaskAwardTask(null, DateUtil.getCurrentDay(), userId, Integer
                    .valueOf(goldCoinTaskEnum.getTaskType()), clientIp, clientId, userSocialTaskAwardService);
            exec.submit(task);
        }
        return false;
    }
}
