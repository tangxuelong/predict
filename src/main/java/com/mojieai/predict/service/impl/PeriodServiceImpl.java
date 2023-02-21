package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.dao.PeriodScheduleDao;
import com.mojieai.predict.dao.PredictScheduleDao;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PeriodSchedule;
import com.mojieai.predict.entity.po.PredictSchedule;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.service.PeriodService;
import com.mojieai.predict.service.cron.BaseScheduler;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PeriodServiceImpl implements PeriodService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private GamePeriodDao gamePeriodDao;
    @Autowired
    private PeriodScheduleDao periodScheduleDao;
    @Autowired
    private PeriodRedisService periodRedisService;
    @Autowired
    protected BaseScheduler baseScheduler;
    @Autowired
    private PredictScheduleDao predictScheduleDao;

    @Override
    public List<GamePeriod> getPeriodsByGameIdAndPeriods(Long gameId, Set<String> periodIds) {
        return gamePeriodDao.getPeriodsByGameIdAndPeriods(gameId, periodIds);
    }

    public int createFuturePeriods(List<GamePeriod> periods, Long gameId) {
        int result = 0;
        if (periods != null) {
            for (GamePeriod gamePeriod : periods) {
                String periodId = gamePeriod.getPeriodId();
                GamePeriod currentPeriod = gamePeriodDao.getPeriodByGameIdAndPeriod(gameId, periodId);
                if (currentPeriod == null) {
                    gamePeriodDao.insert(gamePeriod);
                    result++;
                }
            }
        } else {
            if (!isSpringFestivalDays()) {
                log.warn("createFuturePeriods periodRedis is empty, please check");
            }
        }
        return result;
    }

    public void createPeriodSchedule(List<GamePeriod> periods, Long gameId) {
        if (periods != null) {
            for (GamePeriod gamePeriod : periods) {
                PeriodSchedule periodSchedule = periodScheduleDao.getPeriodSchedule(gameId, gamePeriod.getPeriodId());
                if (periodSchedule == null) {
                    String periodId = gamePeriod.getPeriodId();
                    periodScheduleDao.insert(gameId, periodId);
                }
            }
        } else {
            if (!isSpringFestivalDays()) {
                log.warn("createFuturePeriods periodRedis is empty, please check");
            }
        }
    }

    public void createPredcitSchedule(List<GamePeriod> periods, Long gameId) {
        if (periods != null) {
            for (GamePeriod gamePeriod : periods) {
                PredictSchedule predictSchedule = predictScheduleDao.getPredictSchedule(gameId, gamePeriod
                        .getPeriodId());
                if (predictSchedule == null) {
                    String periodId = gamePeriod.getPeriodId();
                    predictScheduleDao.insert(gameId, periodId);
                }
            }
        } else {
            if (!isSpringFestivalDays()) {
                log.warn("createFuturePeriods periodRedis is empty, please check");
            }
        }
    }

    @Override
    public void prepareInitPeriods() {
        List<Long> gameIds = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIds) {
            Game game = GameCache.getGame(gameId);
            GameFactory gameFactory = GameFactory.getInstance();
            AbstractGame gi = gameFactory.getGameBean(game.getGameEn());
            setInitPeriods(gameId, gi.getDailyPeriod(), gi.getPeriodInterval(), gi.getInitPeriodFormat());
        }
    }

    @Override
    public void predictAllGamePeriods() {
        List<Long> gameIds = new ArrayList<>(GameCache.getAllGameMap().keySet());
        gameIds.forEach(this::predictGamePeriods);
    }

    @Override
    public void predictGamePeriods(Long gameId) {
        GamePeriod period = PeriodRedis.getCurrentPeriod(gameId);
        Game game = GameCache.getGame(gameId);
        GameFactory gameFactory = GameFactory.getInstance();
        AbstractGame gi = gameFactory.getGameBean(game.getGameEn());
        List<GamePeriod> periods = gi.getFuturePeriods(period);
        int result = createFuturePeriods(periods, gameId);
        createPeriodSchedule(periods, gameId);
        createPredcitSchedule(periods, gameId);
        if (result > 0) {
            periodRedisService.refreshTimeline(gameId);
            log.info("refreshTimeline predict GamePeriods, gameId= " + gameId + ", newPeriods= " + result);
        }
    }

    @Override
    public void checkUnfinishedWorks() {
        log.info("定时任务检验未完成的schedule");
        Map<Long, Game> gameMap = GameCache.getAllGameMap();
        gameMap.values().stream().filter(game -> game.getTaskSwitch() == CommonStatusEnum.YES.getStatus()).forEach
                (game -> {
                    //期次处理任务
                    List<GamePeriod> periods = getUnfinishedWorks(game.getGameId());
                    for (GamePeriod period : periods) {
                        Task task = new Task(period.getGameId(), period.getPeriodId());

                        if (!baseScheduler.taskContain(CommonUtil.mergeUnionKey(task.getGameId(), task.getPeriodId()))) {
                            baseScheduler.offer(task);
                            baseScheduler.addInstance(CommonUtil.mergeUnionKey(task.getGameId(), task.getPeriodId()));
                        }
                    }
                });
    }

    private List<GamePeriod> getUnfinishedWorks(long gameId) {
        Map<String, GamePeriod> map = new HashMap<>();
        //init period schedule
        List<GamePeriod> periods = PeriodRedis.getCurrentPeriods(gameId);
        if (periods.size() > 1) {
            Collections.sort(periods, (p1, p2) -> p2.getStartTime().compareTo(p1.getStartTime()));
        }
        GamePeriod currentSalePeriod = periods.get(periods.size() - 1);
        List<PeriodSchedule> schedules = periodScheduleDao.getUnFinishedSchedules(gameId, currentSalePeriod
                .getPeriodId());
        List<PredictSchedule> predictSchedules = predictScheduleDao.getUnFinishedSchedules(gameId, currentSalePeriod.getPeriodId());
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
        for (PredictSchedule schedule : predictSchedules) {
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

    public void setInitPeriods(Long gameId, int totalPeriods, int interval, String dataFormat) {
        String lastEndTime = dataFormat.split("_")[0];
        String startTime = dataFormat.split("_")[1].split(",")[0];
        String endTime = dataFormat.split("_")[1].split(",")[1];
        String awardTime = dataFormat.split("_")[1].split(",")[2];
        String dateFormat = dataFormat.split("_")[2].split(",")[0];
        String idFormat = dataFormat.split("_")[2].split(",")[1];

        List<GamePeriod> periods = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String time = sdf.format(new Date());
        DecimalFormat df = new DecimalFormat(idFormat);
        String periodId = time + df.format(1);

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        Calendar awardCal = Calendar.getInstance();

        String[] startStr = startTime.split(":");
        String[] endStr = endTime.split(":");
        String[] awardStr = awardTime.split(":");

        startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startStr[0]));
        startCal.set(Calendar.MINUTE, Integer.parseInt(startStr[1]));
        startCal.set(Calendar.SECOND, Integer.parseInt(startStr[2]));

        endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endStr[0]));
        endCal.set(Calendar.MINUTE, Integer.parseInt(endStr[1]));
        endCal.set(Calendar.SECOND, Integer.parseInt(endStr[2]));

        awardCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(awardStr[0]));
        awardCal.set(Calendar.MINUTE, Integer.parseInt(awardStr[1]));
        awardCal.set(Calendar.SECOND, Integer.parseInt(awardStr[2]));

        Calendar firstStartCal = Calendar.getInstance();
        String[] lastEndDay = lastEndTime.split(",");
        String[] firstStartStr = lastEndDay[1].split(":");
        firstStartCal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(lastEndDay[0]));
        firstStartCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(firstStartStr[0]));
        firstStartCal.set(Calendar.MINUTE, Integer.parseInt(firstStartStr[1]));
        firstStartCal.set(Calendar.SECOND, Integer.parseInt(firstStartStr[2]));

        Calendar firstEndCal = Calendar.getInstance();
        String[] firstEndStr = lastEndDay[2].split(":");
        firstEndCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(firstEndStr[0]));
        firstEndCal.set(Calendar.MINUTE, Integer.parseInt(firstEndStr[1]));
        firstEndCal.set(Calendar.SECOND, Integer.parseInt(firstEndStr[2]));

        Calendar firstAwardCal = Calendar.getInstance();
        String[] firstAwardStr = lastEndDay[3].split(":");
        firstAwardCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(firstAwardStr[0]));
        firstAwardCal.set(Calendar.MINUTE, Integer.parseInt(firstAwardStr[1]));
        firstAwardCal.set(Calendar.SECOND, Integer.parseInt(firstAwardStr[2]));

        Timestamp start = new Timestamp(firstStartCal.getTimeInMillis());
        Timestamp end = new Timestamp(firstEndCal.getTimeInMillis());
        Timestamp award = new Timestamp(firstAwardCal.getTimeInMillis());

        GamePeriod period = new GamePeriod(gameId, periodId, start, end,
                award, new Timestamp(System.currentTimeMillis()));
        periods.add(period);
        for (int i = 2; i < totalPeriods + 1; i++) {
            periodId = time + df.format(i);
            start = new Timestamp(startCal.getTimeInMillis());
            end = new Timestamp(endCal.getTimeInMillis());
            award = new Timestamp(awardCal.getTimeInMillis());
            startCal.add(Calendar.MINUTE, interval);
            endCal.add(Calendar.MINUTE, interval);
            awardCal.add(Calendar.MINUTE, interval);
            period = new GamePeriod(gameId, periodId, start, end,
                    award, new Timestamp(System.currentTimeMillis()));
            periods.add(period);
        }
        createFuturePeriods(periods, gameId);
        createPeriodSchedule(periods, gameId);
        createPredcitSchedule(periods, gameId);
    }

    private Boolean isSpringFestivalDays() {
        String[] springFestivalDays = IniCache.getIniValue(IniConstant.SPRING_FESTIVAL_DAYS, "20170127,20170202")
                .split
                        (CommonConstant.COMMA_SPLIT_STR);
        String now = DateFormatUtils.format(DateUtil.getCurrentTimestamp().getTime(), DateUtil
                .DATE_FORMAT_YYYYMMDD);
        if (now.compareTo(springFestivalDays[0]) < 0 || now.compareTo(springFestivalDays[1]) > 0) {
            return false;
        }
        return true;
    }
}