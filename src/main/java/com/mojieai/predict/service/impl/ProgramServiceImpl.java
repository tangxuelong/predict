package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.ProgramDao;
import com.mojieai.predict.dao.ProgramIdSequenceDao;
import com.mojieai.predict.dao.UserProgramDao;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.Program;
import com.mojieai.predict.entity.po.UserProgram;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.PayService;
import com.mojieai.predict.service.ProgramService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.predict.PredictFactory;
import com.mojieai.predict.service.predict.PredictInfo;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.ProgramUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by tangxuelong on 2017/11/7.
 */
@Service
public class ProgramServiceImpl implements ProgramService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ProgramDao programDao;
    @Autowired
    private UserProgramDao userProgramDao;
    @Autowired
    private ProgramIdSequenceDao programIdSequenceDao;
    @Autowired
    private PayService payService;
    @Autowired
    private VipMemberService vipMemberService;


    private static final Logger log = LogConstant.commonLog;


    // 定时生成每期的方案 计算上期方案中奖情况
    @Override
    public void productPrograms() {
        List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
        for (Long gameId : gameIdList) {
            try {
                Game game = GameCache.getGame(gameId);
                if (null == game) {
                    log.error("winningNumberPush get game is null");
                    throw new BusinessException("winningNumberPush getgame is null");
                }
                /* 大盘彩*/
                if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                    if (game.getGameEn().equals(GameConstant.FC3D)) {
                        continue;
                    }
                    productPrograms(game);
                }
            } catch (Exception e) {
                // // 报警 继续下一个彩种推送
                log.error("push winingNumber error gameId is " + gameId, e);
            }
        }
    }

    @Override
    public void productPrograms(Game game) {
        // 如果已经自动生成过了，就不在执行 每期自动执行一次
        Long gameId = game.getGameId();
        /* 获取(AwardTime)上一期的期次信息*/
        List<GamePeriod> openPeriodList = PeriodRedis.getLastAwardPeriodByGameId(gameId);
        GamePeriod openingPeriod = openPeriodList.get(0);
        if (null == openingPeriod) {
            log.error("get currentPeriod is null when push check gameId is" + gameId);
            throw new BusinessException("get currentPeriod is null when push check gameId:" + gameId);
        }
        /* 当前时间在awardTime范围内 */
        Timestamp start = DateUtil.getIntervalMinutes(openingPeriod.getAwardTime(), -180);
        Timestamp end = DateUtil.getIntervalMinutes(openingPeriod.getAwardTime(), 180);
        Timestamp now = DateUtil.getCurrentTimestamp();
        if (!DateUtil.isBetween(now, start, end)) {
            return;
        }
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, openingPeriod
                .getPeriodId());
        /* 检查redis标识位*/
        String gameProductProgramFlag = redisService.kryoGet(RedisConstant.getGameProductProgramFlag(nextPeriod
                .getGameId(), nextPeriod.getPeriodId()), String.class);
        if (StringUtils.isNotBlank(gameProductProgramFlag)) {
            return;
        }
        /* 检查period中是否有开奖号码*/
        if (StringUtils.isBlank(openingPeriod.getWinningNumbers())) {
            return;
        }
        // 生成新一期方案
        produceSomePeriodProgram(nextPeriod);

        // 上期方案算奖 更新 isAward字段 设置redis缓存
        calculateProgram(openingPeriod);
        // 设置本期redis标志位
        redisService.kryoSetEx(RedisConstant.getGameProductProgramFlag(nextPeriod.getGameId(), nextPeriod.getPeriodId
                ()), (int) DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(), nextPeriod.getAwardTime()), "1");
    }

    @Override
    public void productProgramByPeriod(GamePeriod period) {
        // 生成新一期方案
        produceSomePeriodProgram(period);

        GamePeriod lastOpenPeriod = PeriodRedis.getLastPeriodByGameIdAndPeriodId(period.getGameId(), period
                .getPeriodId());

        // 上期方案算奖 更新 isAward字段 设置redis缓存
        calculateProgram(lastOpenPeriod);
    }

    @Override
    public void produceSomePeriodProgram(GamePeriod gamePeriod) {
        Game game = GameCache.getGame(gamePeriod.getGameId());
        // 根据格式生成方案 15 5 12 3
        List<Map<String, Object>> programList = ProgramUtil.getProgramListFromActivityIni(gamePeriod.getGameId());

        for (Map<String, Object> programConfig : programList) {
            List<Program> programs = new ArrayList<>();
            Integer programType = Integer.valueOf(programConfig.get("programType").toString());

            // 每种方案类型
            // 每种购买类型 0 限购 1 不中包赔 2 普通
            for (int i = 0; i < 3; i++) {
                List<Program> programDbList = programDao.getProgramsByType(game.getGameId(), gamePeriod
                        .getPeriodId(), programType, i);
                Integer buyTypeLimitCount = ProgramUtil.getProgramBuyTypeNums(programConfig.get("programNumberType")
                        .toString() + CommonConstant.COMMON_COLON_STR + i);
                Integer limitBuyTypeCount = Integer.valueOf(programConfig.get("limitBuyTypeCount").toString());
                // 如果方案类型的购买类型数量已经达到 则不再插入
                if (programDbList.size() >= buyTypeLimitCount) {
                    continue;
                }
                for (int j = 0; j < buyTypeLimitCount; j++) {
                    Random random = new Random();
                    Integer wisdomScore = random.nextInt(Integer.valueOf(programConfig.get("wisdomScoreMax").toString
                            ()) - Integer.valueOf(programConfig.get("wisdomScoreMin").toString()) + 1) +
                            Integer.valueOf(programConfig.get("wisdomScoreMin").toString());
                    Program program = productProgramsByType(game.getGameEn(), programConfig.get("programNumberType")
                            .toString(), gamePeriod, wisdomScore, programType, i, limitBuyTypeCount);
                    if (program != null) {
                        programs.add(program);
                    }
                }
            }
            saveProgram2Redis(gamePeriod, programType, programs);
        }
    }

    private void saveProgram2Redis(GamePeriod gamePeriod, Integer programType, List<Program> programs) {
        String key = RedisConstant.getSaleProgramList(gamePeriod.getGameId(), gamePeriod.getPeriodId(), programType);
        int expireTime = TrendUtil.getExprieSecond(gamePeriod.getAwardTime(), 3600);
        redisService.kryoSetEx(key, expireTime, programs);
    }

    @Override
    public boolean calculateProgram(GamePeriod gamePeriod) {
        boolean res = false;
        if (StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
            return res;
        }
        // 获取上期方案
        List<Program> programs = getProgramListByPeriod(gamePeriod);
        if (null != programs && programs.size() > 0) {
            for (Program program : programs) {
                String redBalls = gamePeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[0];
                String blueBalls = gamePeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR)[1];
                int redRightCount = 0;
                int blueRightCount = 0;
                Integer isAward = CommonConstant.PROGRAM_IS_AWARD_NO;
                if (program.getRedNumber().contains(CommonConstant.COMMON_STAR_STR) || program.getRedNumber().contains
                        (CommonConstant.COMMON_STAR_STR)) {
                    continue;
                }
                for (String redBall : program.getRedNumber().split(CommonConstant.SPACE_SPLIT_STR)) {
                    if (redBalls.contains(redBall)) {
                        program.setRedNumber(program.getRedNumber().replaceAll(redBall, CommonConstant
                                .COMMON_STAR_STR + redBall));
                        redRightCount++;
                    }
                }
                for (String blueBall : program.getBlueNumber().split(CommonConstant.SPACE_SPLIT_STR)) {
                    if (blueBalls.contains(blueBall)) {
                        program.setBlueNumber(program.getBlueNumber().replaceAll(blueBall, CommonConstant
                                .COMMON_STAR_STR + blueBall));
                        blueRightCount++;
                    }
                }
                // 计算是否中奖
                if (calculateNumIsAward(gamePeriod.getGameId(), redRightCount, blueRightCount)) {
                    isAward = CommonConstant.PROGRAM_IS_AWARD_YES;
                }
                if (program.getBuyType().equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE) && !program.getRefundStatus
                        ().equals(CommonConstant.PROGRAM_IS_RETURN_COIN_YES)) {
                    if (isAward.equals(CommonConstant.PROGRAM_IS_AWARD_YES)) {
                        program.setRefundStatus(CommonConstant.PROGRAM_IS_RETURN_COIN_NO);
                    } else {
                        program.setRefundStatus(CommonConstant.PROGRAM_IS_RETURN_COIN_WAIT);
                    }
                }

                // 更新号码和isAward
                program.setIsAward(isAward);
                program.setUpdateTime(DateUtil.getCurrentTimestamp());
                programDao.update(program);
                res = true;
            }
        }
        return res;
    }

    @Override
    public List<Program> getProgramList(Long gameId, String periodId, Integer programType) {
        String key = RedisConstant.getSaleProgramList(gameId, periodId, programType);
        List<Program> result = redisService.kryoGet(key, ArrayList.class);
        if (result == null) {
            result = rebuildSaleProgramList(gameId, periodId, programType);
        }
        return result;
    }

    @Override
    public List<Program> rebuildSaleProgramList(Long gameId, String periodId, Integer programType) {
        List<Program> result = new ArrayList<>();
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        result = programDao.getProgramsByCondition(gameId, periodId, programType);
        saveProgram2Redis(gamePeriod, programType, result);
        return result;
    }

    @Override
    public Map<String, Object> getCurrentSalePrograms(Long gameId, Long userId, Integer programType) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> programDataList = new ArrayList<>();
        boolean isVip = vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        //1.判断是否预测中
        long leftTime = 0;
        GamePeriod currentPeriod = PeriodRedis.getAwardCurrentPeriod(gameId);
        Timestamp torrowTime = DateUtil.getIntervalDays(currentPeriod.getStartTime(), 1l);
        Timestamp currentProgramShowTime = CommonUtil.getSomeDateJoinTime(torrowTime, "06:00:00");

        Timestamp currentTime = DateUtil.getCurrentTimestamp();
        //2.已经预测直接返回data
        if (DateUtil.compareDate(currentProgramShowTime, currentTime)) {
            List<Program> programs = getProgramList(gameId, currentPeriod.getPeriodId(), programType);
            for (Program program : programs) {
                Map<String, Object> temp = new HashMap<>();
                //2.1check用户是否已购买
                Integer programBuyStatus = ProgramUtil.getProgramBuyStatus(program);
                if (userId != null) {
                    UserProgram userProgram = userProgramDao.getUserProgramByProgramId(userId, program.getProgramId()
                            , program.getPrice());
                    if (userProgram != null && userProgram.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
                        programBuyStatus = CommonConstant.PROGRAM_BUY_STATUS_PAYED;
                    }
                }
                //2.2 转化program展示
                temp.putAll(ProgramUtil.convertProgram2SaleMap(program, programBuyStatus, isVip));
                if (!temp.isEmpty()) {
                    programDataList.add(temp);
                }
            }
        } else {
            leftTime = DateUtil.getDiffSeconds(currentTime, currentProgramShowTime);
        }
        result.put("isVip", isVip);
        result.put("leftTime", leftTime);
        result.put("programAd", "以下号码预测仅供投注参考，购彩需到彩票店");
        result.put("programDataList", programDataList);
        return result;
    }

    @Override
    public Map<String, Object> getHistoryAwardProgramList(Long gameId, String lastPeriodId, Long userId, Integer
            isAward) {

        boolean hasNext = false;
        Integer pageSize = 10;
        String maxPeriodId = lastPeriodId;
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> programAwardList = new ArrayList<>();
        List<String> periodIds = programDao.getProgramPagePeriodId(gameId, lastPeriodId, isAward, pageSize + 1);
        if (periodIds.size() > 0) {
            if (periodIds.size() > pageSize) {
                hasNext = true;
                lastPeriodId = periodIds.get(pageSize - 1);
            } else {
                lastPeriodId = periodIds.get(periodIds.size() - 1);
            }
            List<Program> awardProgram = programDao.getProgramsByIntervalPeriodId(gameId, maxPeriodId, lastPeriodId,
                    isAward);
            Map<String, List<Map<String, Object>>> programMap = new HashMap<>();
            for (Program program : awardProgram) {
                List<Map<String, Object>> programDataList = null;
                if (programMap.containsKey(program.getPeriodId())) {
                    programDataList = programMap.get(program.getPeriodId());
                } else {
                    programDataList = new ArrayList<>();
                }
                Map<String, Object> tempMap = ProgramUtil.convertProgram2Map(program, CommonConstant
                        .PROGRAM_IS_RETURN_COIN_NO);
                if (!tempMap.isEmpty()) {
                    programDataList.add(tempMap);
                }
                programMap.put(program.getPeriodId(), programDataList);
            }

            //包装成list
            for (Map.Entry<String, List<Map<String, Object>>> temp : programMap.entrySet()) {
                Map<String, Object> programDateMap = new HashMap<>();
                programDateMap.put("periodText", GameConstant.PERIOD_NAME_MAP.get(GameCache.getGame(gameId).getGameEn
                        ()) + temp.getKey() + "期");
                programDateMap.put("periodId", temp.getKey());
                programDateMap.put("programDataList", temp.getValue());
                programAwardList.add(programDateMap);
            }

            programAwardList.sort((p1, p2) -> Integer.valueOf(p2.get("periodId").toString()).compareTo(Integer
                    .valueOf(p1.get("periodId").toString())));
        }

        result.put("hasNext", hasNext);
        result.put("lastPeriodId", lastPeriodId);
        result.put("programAwardList", programAwardList);
        return result;
    }

    @Override
    public Map<String, Object> getPurchaseProgramInfo(Long userId, String programId, Integer clientId, Integer
            versionCode) {
        Map<String, Object> res = new HashMap<>();
        //1.取得方案信息
        Program program = programDao.getProgramById(programId, false);
        if (program != null) {
            //1.1获取支付弹窗信息
            String programName = ProgramUtil.getProgramTypeCn(program.getProgramType()) + "，智慧指数" + program
                    .getWisdomScore();
            PrePayInfo prePayInfo = new PrePayInfo(programId, programName, program.getPrice(), program.getVipDiscount
                    ());
            prePayInfo.setVipDiscountWay(CommonConstant.VIP_DISCOUNT_WAY_LOW_PAY);
            prePayInfo.setVipPrice(program.getVipPrice());
            Map<String, Object> payInfo = payService.getConfirmPayPopInfo(userId, prePayInfo, clientId, versionCode);
            res.putAll(payInfo);
        }
        return res;
    }

    @Override
    public Map<String, Object> checkProgram(Long userId, String programId) {
        Map<String, Object> res = new HashMap<>();
        Program program = programDao.getProgramById(programId, false);
        if (program == null) {
            res.put("flag", -1);
            res.put("msg", "方案不存在");
            log.error("userId" + userId + " purchase not exist program " + programId);
            return res;
        }
        if (ProgramUtil.getProgramIsEnd(program)) {
            res.put("flag", -1);
            res.put("msg", "方案已过期");
            return res;
        }
        //
        String key = RedisConstant.getUserPurchaseProgramKey(program.getGameId(), program.getPeriodId(), userId,
                programId);
        Long purchaseUserId = redisService.kryoGet(key, Long.class);
        if (purchaseUserId != null && purchaseUserId.equals(userId)) {
            res.put("flag", -1);
            res.put("msg", "方案已购买");
            return res;
        }

        if (program.getBuyType().equals(CommonConstant.PROGRAM_BUY_TYPE_LIMIT) && program.getSaleCount() >= program
                .getTotalCount()) {
            res.put("flag", -1);
            res.put("msg", "方案已售罄");
            return res;
        }
        res.put("flag", 0);
        return res;
    }

    private Program productProgramsByType(String gameEn, String productType, GamePeriod period, Integer wisdomScore,
                                          Integer programType, Integer buyType, Integer limitBuyTypeCount) {

        // 根据购买类型生成多少住号码保存到数据库
        // 拆开
        String[] ballsArr = productType.split(CommonConstant.COMMON_COLON_STR);
        String redCount = ballsArr[0];
        String blueCount = ballsArr[1];
        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);

        // 红球号码 获取红球杀三码
        List<String> redList = Arrays.asList(GameEnum.getGameEnumById(GameCache.getGame(gameEn).getGameId())
                .getRedBalls());
        Collections.shuffle(redList);
        String killThreeCode = predictInfo.getKillThreeCode(period);
        String redNumber = getProgramNumberByCount(redCount, redList, killThreeCode);

        // 蓝球号码
        List<String> killThreeBlueBalls = predictInfo.getKillBlue(gameEn, PredictConstant.KILL_THREE_BLUE);
        String killBlue = killThreeBlueBalls.get(0);
        List<String> blueList = Arrays.asList(GameEnum.getGameEnumById(GameCache.getGame(gameEn).getGameId())
                .getBlueBalls());
        Collections.shuffle(blueList);
        String blueNumber = getProgramNumberByCount(blueCount, blueList, killBlue);

        // 保存到数据库
        Program program = new Program();
        // 生成方案ID
        String programId = generateProgramId(gameEn, period.getPeriodId());
        program.setProgramId(programId);
        program.setRedNumber(redNumber);
        program.setBlueNumber(blueNumber);
        program.setGameId(GameCache.getGame(gameEn).getGameId());
        program.setPeriodId(period.getPeriodId());
        // 获取wisdomScore 转换为小数
        float num = (float) wisdomScore / 10;
        DecimalFormat df = new DecimalFormat("0.0");
        program.setWisdomScore(df.format(num).toString());
        // 方案类型
        program.setProgramType(ProgramUtil.getProgramTypeByNums(productType));
        // 购买类型
        program.setBuyType(buyType);
        // 根据购买类型 为限购 是否设置限购个数和卖出个数 （确认是否已经售卖完毕）限购个数支持管理员调节
        if (buyType.equals(CommonConstant.PROGRAM_BUY_TYPE_LIMIT)) {
            program.setSaleCount(0);
            program.setTotalCount(limitBuyTypeCount);
        }
        Integer refundStatus = CommonConstant.PROGRAM_IS_RETURN_COIN_NO;
        if (buyType.equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE)) {
            refundStatus = CommonConstant.PROGRAM_IS_RETURN_COIN_INIT;
        }
        program.setRefundStatus(refundStatus);

        Long price = null;
        Long vipPrice = null;
        Integer discount = null;
        String iosMallId = null;
        String vipIosMallId = null;
        String key = gameEn + ActivityIniConstant.PROGRAM_PRICE_AFTER;
        String programPriceStr = ActivityIniCache.getActivityIniValue(key);
        if (StringUtils.isNotBlank(programPriceStr)) {
            Map<String, Map<String, Object>> programPrice = JSONObject.parseObject(programPriceStr, HashMap.class);
            Map<String, Object> discountInfo = programPrice.get(programType + "_" + buyType);
            if (discountInfo != null) {
                price = Long.valueOf(discountInfo.get("price").toString());
                vipPrice = Long.valueOf(discountInfo.get("vipPrice").toString());
                discount = Integer.valueOf(discountInfo.get("discount").toString());
                iosMallId = discountInfo.get("iosMallId").toString();
                vipIosMallId = discountInfo.get("vipIosMallId").toString();
            }
        }
        program.setPrice(price);
        program.setVipDiscount(discount);
        program.setVipPrice(vipPrice);
        program.setIosMallId(iosMallId);
        program.setVipIosMallId(vipIosMallId);
        program.setCreateTime(DateUtil.getCurrentTimestamp());
        program.setUpdateTime(DateUtil.getCurrentTimestamp());
        int res = programDao.insert(program);
        if (res <= 0) {
            return null;
        }
        return program;
    }

    private String getProgramNumberByCount(String count, List<String> numberList, String exceptNumber) {
        int redIndex = 0;
        StringBuffer numberS = new StringBuffer();
        List<String> removeNumberList = new ArrayList(numberList);
        while (redIndex < Integer.valueOf(count)) {
            String number = removeNumberList.remove(0);
            if (!exceptNumber.contains(number)) {
                redIndex++;
                numberS.append(number).append(CommonConstant.SPACE_SPLIT_STR);
            }
        }
        return numberS.toString().trim();
    }

    private String generateProgramId(String gameEn, String periodId) {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = programIdSequenceDao.getProgramIdSequence();
        String programId = timePrefix + gameEn + CommonUtil.formatSequence(seq) + periodId;
        return programId;
    }


    private List<Program> getProgramListByPeriod(GamePeriod period) {
        List<Program> programList = programDao.getProgramsByPeriod(period.getGameId(), period.getPeriodId());
        return programList;
    }

    /* 判断彩种方案是否中奖*/
    private boolean calculateNumIsAward(long gameId, Integer redRightCount, Integer blueRightCount) {
        boolean res = false;
        Game game = GameCache.getGame(gameId);
        if (game.getGameEn().equals(GameConstant.SSQ)) {
            if (redRightCount >= 4 || blueRightCount > 0 || (redRightCount >= 3 && blueRightCount > 0)) {
                res = true;
            }
        } else if (game.getGameEn().equals(GameConstant.DLT)) {
            if (redRightCount >= 3 || blueRightCount > 2 || (redRightCount >= 2 && blueRightCount >= 1)) {
                res = true;
            }
        }
        return res;
    }
}
