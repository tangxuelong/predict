package com.mojieai.predict.service.cron;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.dao.PeriodScheduleDao;
import com.mojieai.predict.dao.PredictScheduleDao;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.entity.po.PredictSchedule;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.historyaward.HistoryAwardFactory;
import com.mojieai.predict.service.predict.PredictFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BaseScheduler {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private PeriodScheduleDao periodScheduleDao;
    @Autowired
    private AwardInfoService awardInfoService;
    @Autowired
    private PredictRedBallService predictRedBallService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TrendService trendService;
    @Autowired
    private TrendChartService trendChartService;
    @Autowired
    private PredictNumService predictNumService;
    @Autowired
    private PredictScheduleDao predictScheduleDao;
    @Autowired
    private PeriodRedisService periodRedisService;

    private static final long DEFAULT_RETRY_DELAY = 5000L;//毫秒
    private Set<String> taskSet = new HashSet<>();
    private Map<String, Boolean> chartMap = new ConcurrentHashMap<>();//执行结果图

    private DelayQueue<Task> nodeQueue = new DelayQueue<>();
    private BlockingQueue<Task> executorQueue = new LinkedBlockingQueue<>();
    //下面这么多变量就为了解决一个问题：显示正在运行的任务
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Task> current = Collections.synchronizedList(new ArrayList<>());

    public void schedule() {
        Task node = null;
        boolean offer = false;
        try {
            node = nodeQueue.poll(1L, TimeUnit.SECONDS);
            if (node == null) {
                return;
            }
            if (!executorQueue.contains(node)) {
                offer = executorQueue.offer(node);
            } else {
                log.warn("执行器中已经存在该任务不放入执行器！" + node);
            }
        } catch (InterruptedException e) {
            log.error("调度任务队列发生中断异常,继续下次执行", e);
        } catch (Exception e) {
            log.error("流程调度器处理任务发生异常", e);
        }
        if (node != null && !offer) {
            log.error("调度任务队列增加任务失败" + node);
        }
    }

    public boolean offer(Task node) {
        boolean offer = false;
        if (!nodeQueue.contains(node)) {
            offer = nodeQueue.offer(node);
        } else {
            log.warn("等待队列中已经存在该任务不放入执行器！" + node);
        }
        if (node != null && !offer) {
            log.error("调度任务队列增加任务失败" + node);
        }
        return offer;
    }

    public boolean execute(Task task) {
        long retryDelay = DEFAULT_RETRY_DELAY;
        String msg = "工作流运行器执行任务发生异常！";
        try {
            push(task);
            Boolean ifAwardInfo = Boolean.FALSE;
            log.info("Trend build schedule is run" + task.getPeriodId());
            PeriodSchedule dirtyPeriodSchedule = periodScheduleDao.getPeriodSchedule(task.getGameId(), task.getPeriodId
                    ());
            PredictSchedule dirtyPredictSchedule = predictScheduleDao.getPredictSchedule(task.getGameId(), task
                    .getPeriodId());

            //抓取彩果
            boolean ifSpiderAward = trendService.spiderAward(task, dirtyPeriodSchedule);
            if (!ifSpiderAward) {
                retryTask(task, retryDelay);
//                return false;
            }
            log.info("[baseSchedule]spiderAward end " + task.getPeriodId());

            //大盘彩下载奖级信息里面包含下载开奖号码，放到最前面执行
            if (GameCache.getGame(task.getGameId()).getGameType() == Game.GAME_TYPE_COMMON) {
                retryDelay = IniCache.getIniLongValue(IniConstant.COMMON_GAME_AWARD_INFO_DELAY, 300000L);
                ifAwardInfo = awardInfoService.downloadCommonGameAwardInfo(task.getGameId(), task.getPeriodId(),
                        dirtyPeriodSchedule);

                if (ifAwardInfo) {
                    upatePredictScheduleAwardInfo(dirtyPredictSchedule, task);
                }
            }
            //如果奖级和开奖号码都没抓到返回false
            if (!ifSpiderAward && !ifAwardInfo) {
                return false;
            }
            //抓取开奖地区
            awardInfoService.downLoadAwardArea(task.getGameId(), task.getPeriodId(), dirtyPeriodSchedule);

            /* 蓝球杀三*/
            if (dirtyPredictSchedule != null) {
                PredictFactory.getInstance().getPredictInfo(GameCache.getGame(task.getGameId()).getGameEn())
                        .killBluePredict(task.getGameId(), task.getPeriodId(), dirtyPredictSchedule);
            }
            /* 绝杀码*/
            if (dirtyPredictSchedule != null) {
                PredictFactory.getInstance().getPredictInfo(GameCache.getGame(task.getGameId()).getGameEn())
                        .lastKillCodePredictCal(task.getGameId(), task.getPeriodId(), dirtyPredictSchedule);
            }
            //开始预测
            predictNumService.generatePredictNums(task, dirtyPredictSchedule);
            log.info("generatePredictNums  end" + task.getPeriodId());
            //更新历史预测战绩(奖级)
            predictNumService.updateHistoryPredict(task, dirtyPredictSchedule);
            //更新历史预测金额
            boolean historyBonusFlag = predictNumService.updateHistoryPredictBonus(task, dirtyPredictSchedule);
            log.info("updateHistoryPredict  end" + task.getPeriodId());
            //更新红20码
            predictRedBallService.generateRedTwentyNums(task, dirtyPredictSchedule);
            //保存trendDB
            trendService.saveTrend2Db(task, dirtyPeriodSchedule);
            log.info("saveTrend2Db  end" + task.getPeriodId());
            //组建缓存
            trendChartService.saveTrend2Redis(task, dirtyPeriodSchedule);
            log.info("saveTrend2Redis  end" + task.getPeriodId());
            /* 所有都有号码近期表现*/
            HistoryAwardFactory.getInstance().getHistoryAward(GameConstant.SSQ).buildLastNumberBehave(GameConstant.SSQ);

            log.info("[baseSchedule]history end" + task.getPeriodId());

            if (!historyBonusFlag || !ifAwardInfo) {
                retryTask(task, retryDelay);
                return false;
            }
            removeInstance(CommonUtil.mergeUnionKey(task.getGameId(), task.getPeriodId()));
            log.info("[baseSchedule]Trend build schedule is run end--" + task.getPeriodId());
            return Boolean.TRUE;
        } catch (Exception e) {
            boolean errorRetry = retry(task, retryDelay);
            msg = errorRetry ? msg + "重新放入执行队列等待下次执行！" : msg;
            log.error(msg + task, e);
            return false;
        } finally {
            pop(task);
        }
    }

    private void retryTask(Task task, long retryDelay) {
        String msg = "工作流运行器执行任务发生异常！";
        boolean errorRetry = retry(task, retryDelay);
        msg = errorRetry ? msg + "重新放入执行队列等待下次执行！" : msg;
        log.info(msg + task);
    }

    private void push(Task task) {
        lock.lock();
        try {
            if (!current.contains(task)) {
                current.add(task);
            }
        } finally {
            lock.unlock();
        }
    }

    private void pop(Task task) {
        lock.lock();
        try {
            current.remove(task);
        } finally {
            lock.unlock();
        }
    }

    public boolean monitor() {
        StringBuffer sb = null;
        long timeout = IniCache.getIniIntValue(IniConstant.EXECUTOR_MONITOR_TIME_OUT, 3) * 60;
        int total = 0;
        for (Task task : current) {
            if (task != null) {
                long expired = task.getDelay(TimeUnit.SECONDS);
                if (expired < 0 && ((-expired) > timeout)) {
                    total++;
                    if (sb == null) {
                        sb = new StringBuffer();
                    }
                    sb.append("实例[").append(CommonUtil.mergeUnionKey(task.getGameId(), task.getPeriodId())).append
                            ("]任务[").append(task).append("]预期开始时间[").append(DateUtil.formatDate(new Date(System
                            .currentTimeMillis() + expired * 1000), DateUtil.DATE_FORMAT_YYYYMMDD_HHMMSS)).append("]");
                }
            }
        }
        if (sb != null) {
            sb.insert(0, "工作流如下" + total + "项任务执行时间超过报警值[" + timeout / 60 + "分钟]");
            log.error(sb.toString());
        }

        if (total > IniCache.getIniIntValue(IniConstant.TASK_THREAD_POOL_SIZE, 5)) {
            log.error("Task调度线程池已经被占满，并且所有正在执行的线程执行时间已经超过阈值");
        }

        log.info("[NODEQUEUE] -> ", nodeQueue);

        return true;
    }

    private boolean retry(Task task, long retryDelay) {
        task.setDelay(retryDelay);
        //重新发配给调度器，根据重试间隔，重新来过
        log.info("Task执行失败！重新放入调度器" + task);
        boolean ifOffer = offer(task);
        if (!ifOffer) {
            log.error("Task任务重新放入调度队列失败！" + task);
        }
        return true;

    }

    public BlockingQueue<Task> getExecutorQueue() {
        return executorQueue;
    }

    public void addInstance(String instanceId) {
        taskSet.add(instanceId);
    }

    public Boolean taskContain(String instancId) {
        return taskSet.contains(instancId);
    }

    public Boolean removeInstance(String instancId) {
        return taskSet.remove(instancId);
    }

    private void upatePredictScheduleAwardInfo(PredictSchedule dirtyPredictSchedule, Task task) {
        if (dirtyPredictSchedule != null && dirtyPredictSchedule.getIfAwardInfo() == CommonStatusEnum.NO
                .getStatus()) {
            PredictSchedule predictSchedule = predictScheduleDao.getPredictSchedule(task.getGameId(),
                    task.getPeriodId());
            if (predictSchedule.getIfAwardInfo() == CommonStatusEnum.NO.getStatus()) {
                predictScheduleDao.updatePredictSchedule(task.getGameId(), task.getPeriodId(),
                        "IF_AWARD_INFO", "IF_AWARD_INFO_TIME");
            }
        }
    }
}
