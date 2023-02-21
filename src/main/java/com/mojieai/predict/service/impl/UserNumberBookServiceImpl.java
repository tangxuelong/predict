package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.UserNumBookIdSequenceDao;
import com.mojieai.predict.dao.UserNumberBookDao;
import com.mojieai.predict.entity.bo.Email;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.UserNumberBook;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SendEmailService;
import com.mojieai.predict.service.UserNumberBookService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.thread.SendEmailTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserNumberBookServiceImpl implements UserNumberBookService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private UserNumBookIdSequenceDao userNumSeqDao;
    @Autowired
    private UserNumberBookDao userNumberBookDao;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SendEmailService sendEmailService;

    @Override
    public String generateNumId(Long userId) {
        String userIdStr = userId + "";
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        long seq = userNumSeqDao.insertUserNumBookIdSeq();
        String numBookId = Long.parseLong(timePrefix) + "NUMBOOK" + CommonUtil.formatSequence(seq) + userIdStr
                .substring(userIdStr.length() - 2);
        return numBookId;
    }

    @Override
    public Map<String, Object> getUserNumbers(long gameId, Long userId, String lastNumId) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(lastNumId)) {
            lastNumId = null;
        }
        //1.查询期次信息
        boolean hasNext = true;
        List<Map> datas = new ArrayList();
        Map lastPeriodInfo = getCurrentPageLastPeriodId(gameId, lastNumId, userId);
        if (lastPeriodInfo != null) {
            String lastPeriodId = (String) lastPeriodInfo.get("lastPeriodId");
            hasNext = (boolean) lastPeriodInfo.get("hasNext");
            //2.依据期次从数据库中查询数据
            List<UserNumberBook> userNumberBooks = userNumberBookDao.getUserNumsByUserIdAndLastNumId(gameId, userId,
                    lastNumId, lastPeriodId);

            if (userNumberBooks != null && userNumberBooks.size() > 0) {
                lastNumId = userNumberBooks.get(userNumberBooks.size() - 1).getNumId();
                datas = convertUserNumBook2MapShow(userNumberBooks);
            }
        }
        result.put("datas", datas);
        result.put("isVip", vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));
        result.put("hasNext", hasNext);
        result.put("lastNumId", lastNumId);
        return result;
    }

    private Map<String, Object> getCurrentPageLastPeriodId(long gameId, String lastNumId, Long userId) {
        String periodId = null;
        boolean hasNext = false;
        Integer periodSize = CommonConstant.USER_NUMBER_BOOK_PERIOD_COUNT;
        List<String> periodIds = userNumberBookDao.getCurrentPageLastPeriodId(gameId, lastNumId, userId, periodSize);
        if (periodIds != null && periodIds.size() > 0) {
            if (periodIds.size() == CommonConstant.USER_NUMBER_BOOK_PERIOD_COUNT) {
                periodId = periodIds.get(periodIds.size() - 2);
                hasNext = true;
            } else {
                periodId = periodIds.get(periodIds.size() - 1);
            }
        }
        Map result = new HashMap();
        result.put("lastPeriodId", periodId);
        result.put("hasNext", hasNext);
        return result;
    }

    private List<Map> convertUserNumBook2MapShow(List<UserNumberBook> userNumBooks) {
        List<Map> result = new ArrayList<>();
        if (userNumBooks == null || userNumBooks.size() <= 0) {
            return null;
        }
        Map<String, Map<String, Object>> tempMap = new HashMap<>();
        for (UserNumberBook userNumberBook : userNumBooks) {
            Map<String, Object> numBookMap = null;
            List<Map> datas = null;
            if (tempMap.containsKey(userNumberBook.getPeriodId())) {
                numBookMap = (Map<String, Object>) tempMap.get(userNumberBook.getPeriodId());
                datas = (List<Map>) numBookMap.get("datas");
            } else {
                numBookMap = new HashMap<>();
                String winninnNum = "";
                String awardStatus = "待开奖";
                GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(userNumberBook.getGameId(),
                        userNumberBook.getPeriodId());
                if (gamePeriod != null && StringUtils.isNotEmpty(gamePeriod.getWinningNumbers())) {
                    awardStatus = "已开奖";
                    if (userNumberBook.getGameId().equals(GameCache.getGame(GameConstant.FC3D).getGameId())) {
                        String[] numArr = gamePeriod.getWinningNumbers().split(CommonConstant.SPACE_SPLIT_STR);
                        StringBuffer sb = new StringBuffer();
                        for (String num : numArr) {
                            sb.append(CommonConstant.COMMON_STAR_STR).append(num).append(CommonConstant
                                    .COMMA_SPLIT_STR);
                        }
                        winninnNum = sb.toString().trim();
                        winninnNum = winninnNum.substring(0, winninnNum.length() - 1);
                    } else {
                        winninnNum = CommonUtil.addStart2WinNum(gamePeriod.getWinningNumbers());
                    }
                }
                numBookMap.put("winninnNum", winninnNum);
                numBookMap.put("periodId", userNumberBook.getPeriodId());
//                numBookMap.put("awardStatus", CommonUtil.getUserNumBookAwardInfo(userNumberBook.getIfAward()));
                numBookMap.put("awardStatus", awardStatus);
                datas = new ArrayList<>();
            }
            datas.add(convertUserNumBook2Map(userNumberBook));
            numBookMap.put("datas", datas);
            tempMap.put(userNumberBook.getPeriodId(), numBookMap);
        }
        tempMap.entrySet().stream().sorted(Map.Entry.<String, Map<String, Object>>comparingByKey().reversed())
                .forEach(e -> result.add(e.getValue()));
        return result;
    }

    private Map<String, Object> convertUserNumBook2Map(UserNumberBook userNumberBook) {
        Map<String, Object> result = new HashMap<>();
        if (userNumberBook == null) {
            return null;
        }
        String awardDesc = "";
        String numType = CommonUtil.getUserNumBookNumType(userNumberBook.getNumType());
        if (userNumberBook.getIfAward().equals(CommonConstant.USER_NUMBER_IF_AWARD_YES)) {
            awardDesc = userNumberBook.getAwardDesc();
        }
        int numCount = userNumberBook.getNumCount() == null ? 0 : userNumberBook.getNumCount();
        String numCountDesc = "";
        if (numCount > 0) {
            numCountDesc = numCount + "注";
        }
        // 福彩3d 直选 组三 组六
        if (userNumberBook.getGameId().equals(GameCache.getGame(GameConstant.FC3D).getGameId()) && numType.equals
                (CommonConstant.USER_NUMBER_TYPE_SELF_MSG)) {
            numType = "组六";
            if (userNumberBook.getNums().contains(CommonConstant.COMMON_COLON_STR)) {
                numType = "直选";
            }
            if (userNumberBook.getNums().contains(CommonConstant.COMMON_ADD_STR)) {
                numType = "组三";
            }
        }

        result.put("numId", userNumberBook.getNumId());
        result.put("numType", numType);
        result.put("awardDesc", awardDesc);
        result.put("number", userNumberBook.getNums());
        result.put("numCount", numCountDesc);
        return result;
    }

    @Override
    public Map<String, Object> saveUserNumber(long gameId, String periodId, Long userId, String nums, Integer numType) {
        Map<String, Object> result = new HashMap<>();
        boolean successFlag = false;
        String msg = "保存失败";
        //用户每天最多1W
        String date = DateUtils.formatDate(new Date(), "yyyyMMdd");
        String key = RedisConstant.getUserOneDaySaveNumBooKTimesKey(userId, date);
        long max = redisService.incr(key);
        if (redisService.ttl(key).equals(-1L)) {
            int expireTime = TrendUtil.getExprieSecond(DateUtil.getEndOfOneDay(DateUtil.getCurrentTimestamp()), 60);
            redisService.expire(key, expireTime);
        }
        if (max > CommonConstant.NUMBER_BOOK_MAX_COUNT) {
            result.put("successFlag", false);
            result.put("msg", "保存超过当前限制");
            Email email = new Email();
            email.setTitle("号码本用户异常行为");
            email.setContent(userId + "保存号码过多，请核实。期次：" + periodId);
            SendEmailTask sendEmailTask = new SendEmailTask(sendEmailService, email);
            ThreadPool.getInstance().getSendEmailExec().submit(sendEmailTask);
            return result;
        }

        boolean hasSpace = checkUserNumBookSpace(gameId, userId);
        //1.if there is no space delete last
        if (!hasSpace) {
            String bookNumId = userNumberBookDao.getUserMostRomoteDateId(gameId, userId);
            userNumberBookDao.updateUserNumEnable(bookNumId, userId, CommonConstant.USER_NUMBER_BOOK_DISENABLE);
        }
        Integer numCount = calculateNumCount(gameId, nums);
        //2.save
        UserNumberBook userNumberBook = new UserNumberBook();
        userNumberBook.setGameId(gameId);
        userNumberBook.setIfAward(CommonConstant.USER_NUMBER_IF_AWARD_NO);
        userNumberBook.setIsEnable(CommonConstant.USER_NUMBER_BOOK_ENABLE);
        userNumberBook.setNumCount(numCount);
        userNumberBook.setNumId(generateNumId(userId));
        userNumberBook.setNums(nums);
        userNumberBook.setNumType(numType);
        userNumberBook.setPeriodId(periodId);
        userNumberBook.setUserId(userId);
        int saveRes = userNumberBookDao.insert(userNumberBook);
        if (saveRes > 0) {
            successFlag = true;
            msg = "保存成功";
        }
        result.put("successFlag", successFlag);
        result.put("msg", msg);
        return result;
    }

    @Override
    public Map<String, Object> deleteUserNum(Long userId, String numId) {
        Map<String, Object> res = new HashMap<>();
        boolean successFlag = false;
        String msg = "删除失败";
        Integer resUpdate = userNumberBookDao.updateUserNumEnable(numId, userId, CommonConstant
                .USER_NUMBER_BOOK_DISENABLE);
        if (resUpdate > 0) {
            successFlag = true;
            msg = "删除成功";
        }
        res.put("msg", msg);
        res.put("successFlag", successFlag);
        return res;
    }

    @Override
    public void calculateAward2NumBook(Long gameId, String periodId) {
        for (int i = 1; i <= 100; i++) {
            //1.获取某一表所有该期数据
            List<UserNumberBook> userNumberBooks = userNumberBookDao.getOneTaleAllDataByPeriodId(i * 1L, gameId,
                    periodId);
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (gamePeriod != null && StringUtils.isNotBlank(gamePeriod.getWinningNumbers())) {
                String[] winNum = gamePeriod.getWinningNumbers().split(CommonConstant.COMMON_COLON_STR);
                String[] redWin = winNum[0].split(CommonConstant.SPACE_SPLIT_STR);
                String[] blueWin = winNum[1].split(CommonConstant.SPACE_SPLIT_STR);
                for (UserNumberBook temp : userNumberBooks) {
                    String awardNum = temp.getNums();
                    if (awardNum.contains(CommonConstant.COMMON_STAR_STR)) {
                        continue;
                    }
                    String[] nums = temp.getNums().split(CommonConstant.COMMON_COLON_STR);
                    String redNums = nums[0];
                    awardNum = addStar2WinNum(redNums, redWin);
                    awardNum = awardNum + CommonConstant.COMMON_COLON_STR;
                    String blueNums = null;
                    if (nums.length == 2) {
                        blueNums = nums[1];
                        awardNum = awardNum + addStar2WinNum(blueNums, blueWin);
                    }
                    temp.setNums(awardNum);
                    if (awardNum.contains(CommonConstant.COMMON_STAR_STR)) {
                        temp.setAwardDesc("您围中了一些球");
                    }
                    userNumberBookDao.updateUserNumBookNumsAndAwardDesc(temp.getNumId(), temp.getUserId(), temp
                            .getNums(), temp.getAwardDesc());
                }
            }
        }
    }

    private String addStar2WinNum(String nums, String[] wins) {
        if (StringUtils.isBlank(nums) || wins == null || wins.length == 0) {
            return "";
        }
        for (String ball : wins) {
            if (nums.contains(ball)) {
                nums = nums.replace(ball, CommonConstant.COMMON_STAR_STR + ball);
            }
        }
        return nums;
    }

    private Integer calculateNumCount(long gameId, String nums) {
        if (gameId == GameCache.getGame(GameConstant.FC3D).getGameId()) {
            if (nums.contains(CommonConstant.COMMON_COLON_STR)) {
                //numType = "直选";
                String[] numsArr = nums.split(CommonConstant.COMMON_COLON_STR);
                return numsArr[0].split(CommonConstant.COMMA_SPLIT_STR).length * numsArr[1].split(CommonConstant
                        .COMMA_SPLIT_STR).length * numsArr[2].split(CommonConstant.COMMA_SPLIT_STR).length;
            }
            if (nums.contains(CommonConstant.COMMON_ADD_STR)) {
                //numType = "组三";
                String[] numsArr = nums.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_ADD_STR);
                return numsArr[0].split(CommonConstant.COMMA_SPLIT_STR).length * numsArr[1].split(CommonConstant
                        .COMMA_SPLIT_STR).length;
            }
            //numType = "组六";
            return CommonConstant.factorial(nums.split(CommonConstant.COMMA_SPLIT_STR).length, 3);
        }
        String[] numsArr = nums.split(CommonConstant.COMMON_COLON_STR);
        if (numsArr.length != 2) {
            return null;
        }
        GameEnum ge = GameEnum.getGameEnumById(gameId);
        String[] redBalls = numsArr[0].split(CommonConstant.SPACE_SPLIT_STR);
        String[] blueBalls = numsArr[1].split(CommonConstant.SPACE_SPLIT_STR);
        int redCount = CommonConstant.factorial(redBalls.length, ge.oneNumRedCount());
        int blueCount = CommonConstant.factorial(blueBalls.length, ge.oneNumBlueCount());
        return redCount * blueCount;
    }

    private boolean checkUserNumBookSpace(long gameId, Long userId) {
        boolean hasSpace = false;
        Integer maxNumCount = VipMemberConstant.NOT_VIP_MAX_CLOUD_NUMBER_BOOK;
        boolean isVip = vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        if (isVip) {
            maxNumCount = VipMemberConstant.VIP_MAX_CLOUD_NUMBER_BOOK;
        }
        Integer count = userNumberBookDao.getUserNumBookCount(gameId, userId);
        if (count < maxNumCount) {
            hasSpace = true;
        }
        return hasSpace;
    }
}
