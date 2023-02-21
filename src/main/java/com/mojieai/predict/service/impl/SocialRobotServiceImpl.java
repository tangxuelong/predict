package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.SocialRobotCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.RobotEncircleDao;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.EncircleVo;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SocialEncircleCodeService;
import com.mojieai.predict.service.SocialKillCodeService;
import com.mojieai.predict.service.SocialRobotService;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import com.mojieai.predict.util.TrendUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SocialRobotServiceImpl implements SocialRobotService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private RobotEncircleDao robotEncircleDao;
    @Autowired
    private SocialKillCodeService socialKillCodeService;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;

    @Override
    public void killUserEncircleCodeByRobot() {

        for (Game game : GameCache.getAllGameMap().values()) {
            if (game.getGameType() == Game.GAME_TYPE_COMMON) {
                if (game.getGameEn() != GameConstant.FC3D) {
                    robotKillNum(game);
                }
            }
        }
    }

    private void robotKillNum(Game game) {
        String robotKillEncircleKey = RedisConstant.getRobotKillEncircleKey(game.getGameId(),
                SocialEncircleKillConstant.ENCIRCLE_CODE_TYPE_RED);
        Integer delaySecond = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant
                .ROBOT_DELAY_KILL_NUM_MAX_SECOND, ActivityIniConstant.ROBOT_DELAY_KILL_NUM_DEFAULT_SECOND);
        Integer robotEncirclePercent = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant
                .ROBOT_ENCIRCLE_PERCENT_AFTER_KILL_NUM, ActivityIniConstant
                .ROBOT_ENCIRCLE_PERCENT_AFTER_KILL_NUM_DEFAULT);
        while (redisService.llen(robotKillEncircleKey) > 0) {
            try {
                //1.拿到数据先休息一会在做事
                long second = (new Random().nextInt(delaySecond) + 1) * 1000;
                Thread.sleep(second);
                //1.从redis队列头部中拿出圈号
                SocialEncircle socialEncircle = redisService.kryoLPop(robotKillEncircleKey, SocialEncircle.class);
                if (checkPeriodIsEnd(socialEncircle.getGameId(), socialEncircle.getPeriodId())) {
                    continue;
                }
                //2.在机器人中随机找出几个进行杀号
                Integer maxRandomCount = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant
                        .KILL_NUM_ROBOT_RANDOM_MAX_COUNT, ActivityIniConstant.KILL_NUM_ROBOT_MAX_COUNT);
                Integer robotCount = new Random().nextInt(maxRandomCount) + 1;
                List<SocialRobot> robotList = SocialRobotCache.getSomeRobotByRandom(robotCount);
                if (robotList == null || robotList.size() <= 0) {
                    continue;
                }
                for (SocialRobot robot : robotList) {
                    long robotDiffSecond = (new Random().nextInt(delaySecond) + 1) * 1000;
                    Thread.sleep(robotDiffSecond);
                    if (checkPeriodIsEnd(socialEncircle.getGameId(), socialEncircle.getPeriodId())) {
                        continue;
                    }
                    RobotEncircle robotEncircle = robotEncircleDao.getRobotEncircleById(socialEncircle.getGameId(),
                            robot.getRobotId(), socialEncircle.getPeriodId());
                    if (robotEncircle == null) {
                        robotEncircle = initRobotEncircleInfo(socialEncircle.getGameId(), socialEncircle.getPeriodId
                                (), robot.getRobotId());
                    }
                    //3.机器人开始杀号
                    if (robotEncircle.getKillNumTimes() >= SocialEncircleKillConstant.SOCIAL_KILL_NUM_MAX_COUNT) {
                        continue;
                    }
                    String robotKillNumModel = robotEncircle.getKillCode();
                    String[] userEncircleNums = socialEncircle.getUserEncircleCode().split(CommonConstant
                            .COMMA_SPLIT_STR);
                    List<String> robotKilledNum = new ArrayList<>();
                    for (String encircleCode : userEncircleNums) {
                        if (robotKillNumModel.contains(encircleCode)) {
                            robotKilledNum.add(encircleCode);
                        }
                    }
                    Integer requireCount = SocialEncircleKillCodeUtil.getEncircleMinKillCount(socialEncircle
                            .getKillNums());
                    if (robotKilledNum.size() <= 0) {
                        continue;
                    }
                    robotKillNum(robot.getRobotId(), robot.getRobotUserId(), socialEncircle, requireCount,
                            robotKilledNum);
                    //4.随机决定是否围号
                    Integer randomEncircle = new Random().nextInt(100) + 1;
                    if (robotEncircle.getKillNumTimes() >= SocialEncircleKillConstant.SOCIAL_KILL_NUM_MAX_COUNT ||
                            robotEncirclePercent >= randomEncircle) {
                        robotEncircleNum(socialEncircle.getGameId(), socialEncircle.getPeriodId(), robot
                                .getRobotUserId(), robotEncircle);
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    private boolean checkPeriodIsEnd(long gameId, String periodId) {
        boolean res = false;
        GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(gameId);
        GamePeriod socialPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        if (DateUtil.compareDate(socialPeriod.getEndTime(), gamePeriod.getEndTime())) {
            return true;
        }
        return res;
    }

    public void robotEncircleNum(long gameId, String periodId, Long robotUserId, RobotEncircle robotEncircle) {
        if (robotEncircle.getEncircleTimes() >= SocialEncircleKillConstant.SOCIAL_ENCIRCLE_MAX_COUNT) {
            return;
        }
        String[] robotEncircleCode = robotEncircle.getEncircleCode().split(CommonConstant.COMMA_SPLIT_STR);
        robotEncircleCode = SocialEncircleKillCodeUtil.removeStrOfNumArr(robotEncircleCode);
        List<String> robotList = Arrays.asList(robotEncircleCode).stream().sorted().collect(Collectors.toList());
        Collections.shuffle(robotList, new Random(robotEncircle.getKillNumTimes()));
        List<String> robot20 = robotList.subList(0, 20);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < robot20.size(); i++) {
            sb.append(robot20.get(i));
            if (i < robot20.size() - 1) {
                sb.append(CommonConstant.COMMA_SPLIT_STR);
            }
        }
        String encircleCodes = TrendUtil.orderNum(sb.toString().replace(CommonConstant.COMMA_SPLIT_STR, CommonConstant
                .SPACE_SPLIT_STR));
        encircleCodes = encircleCodes.replaceAll(CommonConstant.SPACE_SPLIT_STR, CommonConstant.COMMA_SPLIT_STR);

        //1.围号
        String killCount;
        Integer[] killNumCounts = null;
        List<EncircleVo> encircleNums = SocialEncircleKillCodeUtil.getEncircleRule(gameId, SocialEncircleKillConstant
                .ENCIRCLE_CODE_TYPE_RED);
        for (int i = 0; i < encircleNums.size(); i++) {
            EncircleVo encircleVo = encircleNums.get(i);
            if (encircleVo.getEncircleCount() == 20) {
                killNumCounts = encircleVo.getKillNumCounts();
                break;
            }
        }
        if (killNumCounts != null && killNumCounts.length > 0) {
            int killCountIndex = new Random(robotEncircle.getKillNumTimes()).nextInt(killNumCounts.length);
            killCount = killNumCounts[killCountIndex] + "";
        } else {
            killCount = "10";
        }
        Map<String, Object> addEncircleRes = socialEncircleCodeService.addEncircleCode(gameId, periodId, robotUserId,
                encircleCodes, 20, killCount, "1", "222.129.17.194", 1001);
        //2.更新围号
        if (Integer.valueOf(addEncircleRes.get("successFlag").toString()).equals(SocialEncircleKillConstant
                .SOCIAL_ADD_ENCIRCLE_SUCC_FLAG)) {
            robotEncircleDao.robotEncircleNumSuccessUpdateInfo(gameId, periodId, robotEncircle.getRobotId());
        }
    }

    @Override
    public void robotKillNum(Integer robotId, Long robotUserId, SocialEncircle socialEncircle, Integer requireCount,
                             List<String> robotCanKillCodes) {
        Integer killCount = 0;
        StringBuilder killNums = new StringBuilder();
        if (robotCanKillCodes.size() >= 5 && requireCount == 5) {
            killCount = 5;
        } else if (robotCanKillCodes.size() >= 3 && requireCount == 3) {
            killCount = 3;
        } else if (robotCanKillCodes.size() == 1 && requireCount == 1) {
            killCount = 1;
        }
        if (killCount == 0) {
            return;
        }
        for (int i = 0; i < killCount; i++) {
            killNums.append(robotCanKillCodes.get(i));
            if (i < killCount - 1) {
                killNums.append(CommonConstant.COMMA_SPLIT_STR);
            }
        }
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(socialEncircle.getGameId(), socialEncircle
                .getPeriodId());
        //1.保存杀号
        Map<String, Object> robotKillRes = socialKillCodeService.addKillCode(socialEncircle.getGameId(), gamePeriod,
                robotUserId, socialEncircle.getEncircleCodeId(), killNums.toString(), "222.129.17.194", 1001);
        //2.更新机器人杀号信息
        if (robotKillRes.get("successFlag").toString().equals(IniConstant.COMPATIBLE_SIGN_YES)) {
            robotEncircleDao.robotKillNumSuccessUpdateInfo(socialEncircle.getGameId(), socialEncircle.getPeriodId(),
                    robotId);
        }
    }

    @Override
    public RobotEncircle initRobotEncircleInfo(Long gameId, String periodId, Integer robotId) {
        RobotEncircle robotEncircle = new RobotEncircle();
        Map<String, String> balls = generateRobotEncircleAndKill(robotId, gameId, periodId);
        robotEncircle.setEncircleCode(balls.get("encircles"));
        robotEncircle.setKillCode(balls.get("killNums"));
        robotEncircle.setGameId(gameId);
        robotEncircle.setEncircleTimes(0);
        robotEncircle.setPeriodId(periodId);
        robotEncircle.setKillNumTimes(0);
        robotEncircle.setRobotId(robotId);
        robotEncircleDao.insert(robotEncircle);
        return robotEncircle;
    }

    private Map<String, String> generateRobotEncircleAndKill(Integer robotId, long gameId, String periodId) {
        Map<String, String> result = new HashMap<>();
        List<String> redBalls = Arrays.asList(GameEnum.getGameEnumById(gameId).getRedBalls());
        StringBuilder encircles = new StringBuilder();
        StringBuilder killNums = new StringBuilder();

        long seed = robotId * gameId * Integer.valueOf(periodId);
        Random random = new Random(seed);
        Collections.shuffle(redBalls, random);
        for (int i = 0; i < redBalls.size(); i++) {
            if (i < 25) {
                encircles.append(redBalls.get(i)).append(CommonConstant.COMMA_SPLIT_STR);
            } else {
                killNums.append(redBalls.get(i)).append(CommonConstant.COMMA_SPLIT_STR);
            }
        }
        result.put("encircles", encircles.toString());
        result.put("killNums", killNums.toString());
        return result;
    }
}
