package com.mojieai.predict.service.cron;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ZookeeperConstant;
import com.mojieai.predict.dao.CronTabDao;
import com.mojieai.predict.dao.PeriodScheduleDao;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.CronTab;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.ExecuteModeEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.thread.ScheduleCron;
import com.mojieai.predict.thread.TaskCron;
import com.mojieai.predict.thread.TaskDispatcher;
import com.mojieai.predict.thread.TaskProducer;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.NetUtil;
import com.mojieai.predict.zk.annotation.ClusterSync;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CronScheduleJobDetail implements CronScheduleJob, BeanSelfAware {
    private final Logger log = LogConstant.commonLog;

    @Autowired
    private CronTabDao cronTabDao;
    @Autowired
    private PeriodScheduleDao periodScheduleDao;
    @Autowired
    protected BaseScheduler baseScheduler;

    private CronScheduleJob self;//自身，用于zookeeper访问控制
    private static ScheduledExecutorService scheduler;
    private static ScheduledExecutorService executor;
    private static boolean isDoInit = false;//是否调用过doInit()方法标记，包括调用过但被ZK阻止的情况
    private static List<CronTab> cronList = new ArrayList<>();

    public static boolean isInit = false;//是否初始化成功标记
    public static byte[] initLock = new byte[0];

    private volatile Set<String> cronWatcher = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void cronScheduleInit() {

        String deployHostIps = IniCache.getIniValue(IniConstant.CLUSTER_CRON_IP, "127.0.0.1");
        boolean ifValidCandidate = NetUtil.containsIps(deployHostIps, ",");
        log.info("[CronManager]Deploy Ip:" + deployHostIps + ", ifValid = " + ifValidCandidate);
        if (!ifValidCandidate) {
            return;
        }
        //开始初始化调度程序
        if (!isInit) {
            log.info("cronManager init try");
        }
        self.doInit();
        if (!isDoInit) {
            isDoInit = true;
        }
    }

    @Override
    @ClusterSync(path = ZookeeperConstant.cronScheduleRootDirectory)
    public void doInit() {
        log.info("cron schedule init start");
        synchronized (initLock) {
            if (isInit) {
                log.info("cron schedule init is done");
                return;
            } else {
                isInit = true;
                if (isDoInit) {
                    log.error("cron schedule zookeeper switch");//记录ZK切换操作日志，用于监控报警
                }
            }
        }

        try {
            initCron();
            loadCronConfigure();
            startCronTab();
        } catch (Exception e) {
            log.error("启动调度出现异常：cron initSchedule=" + e);
        }

        log.info("cron schedule init end");
    }

    private synchronized void initCron() {
        //执行走势图业务
        ExecutorService dispatchExec = Executors.newSingleThreadExecutor();//运行一个dispatcher
        ExecutorService produceExec = Executors.newSingleThreadExecutor();//运行一个producer
        produceExec.execute(new TaskProducer(baseScheduler));
        dispatchExec.execute(new TaskDispatcher(baseScheduler));
        //针对每个彩种需要执行的任务
        Map<Long, Game> gameMap = GameCache.getAllGameMap();
        int maxThreadNum = gameMap.size() * CronEnum.values().length + gameMap.size() / 2;
        executor = Executors.newScheduledThreadPool(gameMap.size() == 0 ? 5 : maxThreadNum);
        gameMap.values().stream().filter(game -> game.getTaskSwitch() == CommonStatusEnum.YES.getStatus()).forEach
                (game -> {
                    //期次处理任务
                    List<GamePeriod> periods = getUnfinishedWorks(game.getGameId());
                    for (GamePeriod period : periods) {
                        Task task = new Task(period.getGameId(), period.getPeriodId());
                        baseScheduler.offer(task);
                        baseScheduler.addInstance(CommonUtil.mergeUnionKey(task.getGameId(), task.getPeriodId()));
                    }
                    //彩种定时处理任务
                    for (CronEnum cronEnum : CronEnum.values()) {
                        String key = CommonUtil.mergeUnionKey(cronEnum.getCron(), game.getGameEn());
                        if (cronWatcher.contains(key)) {
                            return;
                        }
                        synchronized (cronWatcher) {
                            if (cronWatcher.contains(key)) {
                                return;
                            }
                            startLoop(game, cronEnum);
                            cronWatcher.add(key);
                        }
                    }
                });
    }

    public List<GamePeriod> getUnfinishedWorks(Long gameId) {
        Map<String, GamePeriod> map = new HashMap<>();
        //init period schedule
        List<GamePeriod> periods = PeriodRedis.getCurrentPeriods(gameId);
        if (periods.size() > 1) {
            Collections.sort(periods, (p1, p2) -> p2.getStartTime().compareTo(p1.getStartTime()));
        }
        GamePeriod currentSalePeriod = periods.get(periods.size() - 1);
        List<PeriodSchedule> schedules = periodScheduleDao.getUnFinishedSchedules(gameId, currentSalePeriod
                .getPeriodId());
        for (PeriodSchedule schedule : schedules) {
            GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, schedule.getPeriodId());
            if (period == null) {
                log.error("查找游戏:" + gameId + ",期次:" + schedule.getPeriodId() + "为空，请检查系统!!!");
                continue;
            }
            if (checkIfPutIntoList(period, map)) {
                map.put(CommonUtil.mergeUnionKey(period.getGameId(), period.getPeriodId()), period);
            }
        }
        List<GamePeriod> periodList = new ArrayList<>();
        periodList.addAll(map.values());
        Collections.sort(periodList, (p1, p2) -> p2.getStartTime().compareTo(p1.getStartTime()));
        int maxSize = IniCache.getIniIntValue(IniConstant.ONCE_SCHEDULER_PERIOD_SIZE, 20);
        return periodList.size() <= maxSize ? periodList : periodList.subList(0, maxSize);
    }

    public boolean checkIfPutIntoList(GamePeriod gamePeriod, Map<String, GamePeriod> map) {
        if (map.containsKey(CommonUtil.mergeUnionKey(gamePeriod.getGameId(), gamePeriod.getPeriodId()))) {
            return false;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        //默认放入now+一定时长的期次
        int secondInterval = GameCache.getGame(gamePeriod.getGameId()).getTaskTimeOffset();
        Timestamp allowedPeriodTime = DateUtil.getIntervalSeconds(now, secondInterval);
        if (gamePeriod.getAwardTime().before(allowedPeriodTime)) {
            return true;
        } else {
            return false;
        }
    }

    public void startLoop(Game game, CronEnum cronEnum) {
        try {
            Long delay = CommonUtil.getNextDelayTime(game, cronEnum);
            if (null == delay) {
                log.error("startLoop delay is null. pl check it!game = " + game);
                if (null == game) {
                    throw new BusinessException("startLoop gameId error. game = " + game);
                }
                delay = cronEnum.getDefaultDelay(game);
            }
            executor.schedule(new ScheduleCron(game, cronEnum, this), delay, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("startResendLoop error. game = " + game, e);
        }
    }

    private synchronized void loadCronConfigure() {
        List<CronTab> cronTabs = cronTabDao.getAllCronTab();
        cronList.clear();
        cronList.addAll(cronTabs);
        scheduler = Executors.newScheduledThreadPool(cronList.size() == 0 ? 5 : cronList.size() * 2);
        if (cronList.size() >= 100) {
            log.error("ScheduledThreadPool is large,pls check it!");
        }
    }

    public void startCronTab() {
        for (CronTab tab : cronList) {
            try {
                ExecuteModeEnum modeEnum = ExecuteModeEnum.getEnum(tab.getExecuteMode());
                switch (modeEnum) {
                    case CRON:
                        Long delay = CronTab.getNextDelayTime(tab.getCron());
                        scheduler.schedule(new TaskCron(tab, scheduler), delay, TimeUnit.MILLISECONDS);
                        break;
                    case INTERVAL:
                        scheduler.scheduleWithFixedDelay(new TaskCron(tab, scheduler), 1, Long.parseLong(tab.getCron
                                ()), TimeUnit.MILLISECONDS);
                        break;
                }
            } catch (Exception e) {
                log.error("startSchedule CronTab error." + tab.toString(), e);
            }
        }
    }

    @Override
    public void setSelf(Object proxyBean) {
        this.self = (CronScheduleJob) proxyBean;
    }
}
