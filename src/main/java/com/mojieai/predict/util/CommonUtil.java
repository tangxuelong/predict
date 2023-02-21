package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.IdSequenceBaseDao;
import com.mojieai.predict.entity.bo.PaginationInfo;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.bo.RealNameInfo;
import com.mojieai.predict.entity.dto.HttpParamDto;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PayClientChannel;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.quartz.CronExpression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommonUtil {
    private static final char[] bcdLookup = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};
    protected static Logger log = LogConstant.commonLog;

    protected static Map<Integer, Integer> defaultPushMap = new HashMap<>();

    static {
        defaultPushMap.put(CommonConstant.PUSH_TYPE_DIGIT_SOCIAL_KILL_NUM, 1);
        defaultPushMap.put(CommonConstant.PUSH_TYPE_DIGIT_SOCIAL_GOD_ENCIRCLE_NUM, 1);
        defaultPushMap.put(CommonConstant.PUSH_TYPE_SPORTS_SOCIAL_GOD_RECOMMEND, 1);
    }

    public static <T> Map<String, T> asMap(Object... args) {
        if (args.length % 2 != 0)
            throw new IllegalArgumentException("args.length = " + args.length);

        Map<String, T> map = new HashMap<String, T>();
        for (int i = 0; i < args.length - 1; i += 2)
            map.put(String.valueOf(args[i]), (T) args[i + 1]);
        return map;
    }

    public static String mergeUnionKey(Object... args) {
        if (args.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (Object arg : args) {
            sb.append(arg).append(CommonConstant.COMMON_VERTICAL_STR);
        }
        String result = sb.toString();
        return result.substring(0, result.length() - 1);
    }

    /**
     * 计算排列组合的值
     *
     * @param total
     * @param select
     * @return
     */
    public static int combine(int total, int select) {
        if (select > total) {
            return 0;
        } else if (select == total) {
            return 1;
        } else if (total == 0) {
            return 1;
        } else {
            if (select > total / 2)
                select = total - select;

            long result = 1;
            for (int i = total; i > total - select; i--) {
                result *= i;
                if (result < 0)
                    return -1;
            }
            for (int j = select; j > 0; j--) {
                result /= j;
            }
            if (result > Integer.MAX_VALUE)
                return -1;
            return (int) result;
        }
    }

    /**
     * 从n个对象中选择m个的所有排列
     *
     * @param a
     * @param m
     * @return
     */
    public static List<String[]> combine(String[] a, int m) {
        List<String[]> result = new ArrayList<String[]>();
        int n = a.length;
        int[] bs = new int[n];
        if (m > n) {
            throw new RuntimeException("Can not get " + n + " elements from " + m + " elements!");
        } else if (m == n) {
            result.add(a);
            return result;
        }

        for (int i = 0; i < n; i++) {
            bs[i] = 0;
        }
        //初始化
        for (int i = 0; i < m; i++) {
            bs[i] = 1;
        }
        boolean flag = true;
        boolean tempFlag = false;
        int pos = 0;
        int sum = 0;
        //首先找到第一个10组合，然后变成01，同时将左边所有的1移动到数组的最左边
        do {
            sum = 0;
            pos = 0;
            tempFlag = true;
            result.add(getElement(bs, a, m));

            for (int i = 0; i < n - 1; i++) {
                if (bs[i] == 1 && bs[i + 1] == 0) {
                    bs[i] = 0;
                    bs[i + 1] = 1;
                    pos = i;
                    break;
                }
            }
            //将左边的1全部移动到数组的最左边

            for (int i = 0; i < pos; i++) {
                if (bs[i] == 1) {
                    sum++;
                }
            }
            for (int i = 0; i < pos; i++) {
                if (i < sum) {
                    bs[i] = 1;
                } else {
                    bs[i] = 0;
                }
            }

            //检查是否所有的1都移动到了最右边
            for (int i = n - m; i < n; i++) {
                if (bs[i] == 0) {
                    tempFlag = false;
                    break;
                }
            }
            if (tempFlag == false) {
                flag = true;
            } else {
                flag = false;
            }

        }
        while (flag);
        result.add(getElement(bs, a, m));

        return result;
    }

    private static String[] getElement(int[] bs, String[] a, int m) {
        String[] result = new String[m];
        int pos = 0;
        for (int i = 0; i < bs.length; i++) {
            if (bs[i] == 1) {
                result[pos] = a[i];
                pos++;
            }
        }
        return result;
    }

    /**
     * 对序列号进行初始化，如果>8位则不管，如果<8位，则左补0
     *
     * @param numberToFormat
     * @return
     */
    public static String formatSequence(long numberToFormat) {
        DecimalFormat format = new DecimalFormat("00000000");
        return format.format(numberToFormat);
    }

    /*
     * 将16进制字符串转换为字符数组
     */
    public static final byte[] hexStrToBytes(String s) {
        byte[] bytes;

        bytes = new byte[s.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }

        return bytes;
    }

    /*
     * 将字符数组转换为16进制字符串
     */
    public static final String bytesToHexStr(byte[] bcd) {
        StringBuffer s = new StringBuffer(bcd.length * 2);

        for (int i = 0; i < bcd.length; i++) {
            s.append(bcdLookup[(bcd[i] >>> 4) & 0x0f]);
            s.append(bcdLookup[bcd[i] & 0x0f]);
        }

        return s.toString();
    }

    /**
     * 判断是否未数字
     */
    public static Boolean isNumeric(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static Boolean isNumericeFloat(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[0-9]+(.[0-9]+)?$");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static Long getNextDelayTime(Game game, CronEnum cronEnum) {
        Date nextFire = getNextFire(game, cronEnum);
        if (null == nextFire) {
            return null;
        }
        GamePeriod period = PeriodRedis.getCurrentPeriod(game.getGameId());
        long now = System.currentTimeMillis();
        long delay = nextFire.getTime() - now;
        if (null == period || null == period.getEndTime()) {
            return delay;
        }
        long time2Deadline = period.getEndTime().getTime() - now;
        if (time2Deadline < CronEnum.DEFAULT_TIME_TO_DEADLINE) {
            return delay;
        }
        return Math.min(delay, time2Deadline - CronEnum.DEFAULT_TIME_TO_DEADLINE);
    }

    public static Date getNextFire(Game game, CronEnum cronEnum) {
        if (null == game) {
            log.info("[CronManager]getNextFire gameId is null. game = " + game);
            return null;
        }
        List<CronExpression> cronExpressions = new ArrayList<>();
        String cronString = cronEnum.getCronString(game);
        String[] cronArr = cronString.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_VERTICAL_STR);
        for (String cron : cronArr) {
            try {
                CronExpression ce = new CronExpression(cron);
                cronExpressions.add(ce);
            } catch (ParseException e) {
                log.warn("[cronManager] invalid cronExpression " + cron);
            }
        }
        return getNextFire(cronExpressions);
    }

    public static Date getNextFire(List<CronExpression> cronExpressions) {
        if (cronExpressions == null) {
            return null;
        }
        if (cronExpressions.size() == 0) {
            log.info("[resendManager] getNextFire cronExpressions size is 0");
        }
        Date now = new Date();
        Date result = null;
        for (CronExpression cron : cronExpressions) {
            Date nextValidTime = cron.getNextValidTimeAfter(now);
            if (nextValidTime == null) {
                log.error("[resendManager] getNextFire nextValidTime is null");
                continue;
            }
            if (result == null || nextValidTime.before(result)) {
                result = nextValidTime;
            }
        }
        return result;
    }

    public static int[] numberStr2IntArray(String numberStr, String splitStr) {
        boolean b = numberStr.contains(splitStr);
        if (!b) {
            throw new BusinessException("号码格式有误");
        }
        String[] numbers = StringUtils.split(numberStr.trim(), splitStr);
        int length = numbers.length;
        int[] numArray = new int[length];
        for (int i = 0; i < length; i++) {
            numArray[i] = Integer.valueOf(numbers[i]);
        }
        return numArray;
    }

    public static String[] getBalls(String winningNumber, int type) {
        String[] balls = winningNumber.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMON_COLON_STR);
        return balls[type].split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.SPACE_SPLIT_STR);
    }

    public static String[] getFc3dBall(String winningNumber, int type) {
        String[] balls = winningNumber.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.SPACE_SPLIT_STR);
        if (type == CommonConstant.FC_ALL_DIGIT) {
            return balls;
        }
        return new String[]{balls[type]};
    }

    public static String getBallStr(int i) {
        String ball = String.valueOf(i);
        if (i < 10) {
            ball = "0" + i;
        }
        return ball;
    }

    public static String convertStrNum(int i) {
        String numStr = null;
        if (i < 10) {
            numStr = "0" + i;
        } else {
            numStr = i + "";
        }
        return numStr;
    }

    public static Boolean pageHasNext(PaginationList pageList) {
        Boolean hasNext = Boolean.FALSE;
        PaginationInfo paginationInfo = pageList.getPaginationInfo();
        if (paginationInfo.getCurrentPage() < paginationInfo.getTotalPage()) {
            hasNext = Boolean.TRUE;
        }
        return hasNext;
    }

    public static Integer getIntegerArrSum(String[] numArr) {
        Integer result = 0;
        if (numArr == null || numArr.length <= 0) {
            return result;
        }
        result = Arrays.stream(numArr).filter(n -> isNumeric(n)).mapToInt(n -> Integer.valueOf(n)).sum();
        return result;
    }

    public static String convertFen2YuanAndRemoveZero(Long price) {
        return CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(price).toString());
    }

    public static BigDecimal convertFen2Yuan(Long price) {
        if (price == null) {
            return null;
        }
        BigDecimal temp = new BigDecimal(100);
        BigDecimal bigDecimal = new BigDecimal(price);
        BigDecimal res = bigDecimal.divide(temp, 2, BigDecimal.ROUND_UP);
        return res;
    }

    public static BigDecimal convertFen2Yuan(Integer price) {
        BigDecimal temp = new BigDecimal(100);
        BigDecimal bigDecimal = new BigDecimal(price);
        BigDecimal res = bigDecimal.divide(temp, 2, BigDecimal.ROUND_UP);
        return res;
    }

    public static BigDecimal convertFen2Yuan(String price) {
        BigDecimal temp = new BigDecimal(100);
        BigDecimal bigDecimal = new BigDecimal(price);
        BigDecimal res = bigDecimal.divide(temp, 2, BigDecimal.ROUND_UP);
        return res;
    }

    public static BigDecimal convertYuan2Fen(String price) {
        BigDecimal temp = new BigDecimal(100);
        BigDecimal bigDecimal = new BigDecimal(price);
        return bigDecimal.multiply(temp);
    }

    public static String generateRobotMobile(Integer robotType) {
        String number = "888";//定义电话号码以139开头
        if (robotType.equals(SocialEncircleKillConstant.SOCIAL_ROBOT_TYPE_SPORT)) {
            number = "777";//定义电话号码以139开头
        } else if (robotType.equals(SocialEncircleKillConstant.SOCIAL_ROBOT_TYPE_CELEBRITY)) {
            number = "666";//定义电话号码以139开头
        }
        number = generateMoile(number);
        return number;
    }

    public static String generateTouristMoile() {
        String number = "999";//定义电话号码以139开头
        number = generateMoile(number);
        return number;
    }

    //任意3位开头
    public static String generateMoile(String begin) {
        String number = begin;//定义电话号码以139开头
        Random random = new Random(System.currentTimeMillis());//定义random，产生随机数
        for (int j = 0; j < 8; j++) {
            //生成0~9 随机数
            number += random.nextInt(10);
        }
        return number;
    }

    public static String getUserNumBookNumType(Integer numType) {
        String res = "";
        if (numType.equals(CommonConstant.USER_NUMBER_TYPE_SELF)) {
            res = "自选";
        } else if (numType.equals(CommonConstant.USER_NUMBER_TYPE_PREDICT)) {
            res = "智慧";
        }

        return res;
    }

    public static String getUserNumBookAwardInfo(Integer ifAward) {
        if (ifAward.equals(CommonConstant.USER_NUMBER_IF_AWARD_YES)) {
            return "已开奖";
        } else if (ifAward.equals(CommonConstant.USER_NUMBER_IF_AWARD_NO)) {
            return "待开奖";
        } else {
            return "";
        }
    }

    public static String addStart2WinNum(String winningNumbers) {
        if (StringUtils.isBlank(winningNumbers)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] winNum = winningNumbers.split(CommonConstant.COMMON_COLON_STR);
        String[] redBalls = winNum[0].split(CommonConstant.SPACE_SPLIT_STR);
        String[] blueBalls = winNum[1].split(CommonConstant.SPACE_SPLIT_STR);
        for (int i = 0; i < redBalls.length; i++) {
            sb.append(CommonConstant.COMMON_STAR_STR).append(redBalls[i]);
            if (i < redBalls.length - 1) {
                sb.append(CommonConstant.SPACE_SPLIT_STR);
            }
        }
        sb.append(CommonConstant.COMMON_COLON_STR);
        for (int i = 0; i < blueBalls.length; i++) {
            sb.append(CommonConstant.COMMON_STAR_STR).append(blueBalls[i]);
            if (i < blueBalls.length - 1) {
                sb.append(CommonConstant.SPACE_SPLIT_STR);
            }
        }
        return sb.toString();
    }

    public static String getUserMoblieDefaultName(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return null;
        }
        StringBuffer mobileStr = new StringBuffer(mobile);
        return mobileStr.replace(3, 7, "****").toString();
    }

    public static String divideByBigDecimal(Integer div1, Integer div2) {
        if (div2 == null || div2 == 0) {
            return "0";
        }
        return new BigDecimal(div1).divide(new BigDecimal(div2), 2, RoundingMode.HALF_UP).toString();
    }

    public static String addByBigDecimal(String num1, String num2) {
        if (StringUtils.isBlank(num1)) {
            num1 = "0";
        }
        if (StringUtils.isBlank(num2)) {
            num2 = "0";
        }
        return new BigDecimal(num1).add(new BigDecimal(num2)).toString();
    }

    public static String getUnitCnByPayType(Integer payType) {
        if (payType.equals(CommonConstant.PAY_TYPE_GOLD_COIN)) {
            return CommonConstant.GOLD_COIN_MONETARY_UNIT;
        } else if (payType.equals(CommonConstant.PAY_TYPE_CASH)) {
            return CommonConstant.CASH_MONETARY_UNIT_YUAN;
        } else if (payType.equals(CommonConstant.PAY_TYPE_WISDOM_COIN)) {
            return CommonConstant.GOLD_WISDOM_COIN_MONETARY_UNIT;
        }
        return "";
    }

    public static String getMoneyStr(Long payAmount, Integer payType) {
        if (payType.equals(CommonConstant.PAY_TYPE_GOLD_COIN)) {
            return String.valueOf(payAmount);
        } else {
            return new BigDecimal(payAmount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).toString();
        }
    }

    public static String generateStrId(Long userId, String idType, IdSequenceBaseDao baseDao) {
        String timePrefix = DateUtil.formatDate(new Date(), DateUtil.DATE_FORMAT_YYMMDDHH);
        String wisdomStr = userId + "";
        Long seq = baseDao.insertIdSeq();
        if (seq == null) {
            return null;
        }
        return Long.parseLong(timePrefix) + idType + CommonUtil.formatSequence(seq) + wisdomStr.substring(wisdomStr
                .length() - 2);
    }

    //除法
    public static String divide(String div1, String div2, Integer scale) {
        if (StringUtils.isBlank(div2)) {
            return "";
        }
        BigDecimal big1 = new BigDecimal(div1);
        BigDecimal big2 = new BigDecimal(div2);
        return divide(big1, big2, scale).toString();
    }

    public static BigDecimal divide(BigDecimal div1, BigDecimal div2, Integer scale) {
        return div1.divide(div2, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiply(String num1, String num2) {
        BigDecimal big1 = new BigDecimal(num1);
        BigDecimal big2 = new BigDecimal(num2);
        return big1.multiply(big2);
    }

    /* 减法*/
    public static BigDecimal subtract(String num1, String num2) {
        if (StringUtils.isBlank(num1)) {
            num1 = "0";
        }
        if (StringUtils.isBlank(num2) || !isNumericeFloat(num2)) {
            num2 = "0";
        }
        BigDecimal big1 = new BigDecimal(num1);
        BigDecimal big2 = new BigDecimal(num2);
        return big1.subtract(big2);
    }

    /* 获取某一天的指定时间*/
    public static Timestamp getSomeDateJoinTime(Timestamp date, String time) {
        String timeStr = DateUtil.formatTime(date, "yyyy-MM-dd") + " " + time;
        return DateUtil.formatString(timeStr, "yyyy-MM-dd HH:mm:ss");
    }

    public static Timestamp getSomeDateJoinTime(Timestamp date, String time, String format) {
        String timeStr = DateUtil.formatTime(date, "yyyy-MM-dd") + " " + time;
        return DateUtil.formatString(timeStr, format);
    }

    public static String removeZeroAfterPoint(double num) {
        if (num % 1.0 == 0) {
            return String.valueOf((long) num);
        }
        return String.valueOf(num);
    }

    public static String removeZeroAfterPoint(String num) {
        return removeZeroAfterPoint(Double.valueOf(num));
    }

    public static Integer getIntOfTime(Timestamp createTime) {
        return Integer.valueOf(DateUtil.formatTime(createTime, "yyyyMMdd"));
    }

    public static String calculatePercent(Integer part, Integer all) {
        if (part == null || all == null || all == 0) {
            return 0 + CommonConstant.PERCENT_SPLIT_STR;
        }
        String person = divide(part + "", all + "", 0);
        return multiply(person, "100").toString() + CommonConstant.PERCENT_SPLIT_STR;
    }

    public static String markerStarByModelNum(String modelNum, String waitBalls) {
        if (StringUtils.isBlank(modelNum) || StringUtils.isBlank(waitBalls) || waitBalls.contains(CommonConstant
                .COMMON_STAR_STR)) {
            return waitBalls;
        }
        String[] waitArr = waitBalls.split(CommonConstant.SPACE_SPLIT_STR);
        for (String num : waitArr) {
            if (modelNum.contains(num)) {
                waitBalls = waitBalls.replaceAll(num, CommonConstant.COMMON_STAR_STR + num);
            }
        }
        return waitBalls;
    }

    public static String getWeekIdByDate(Date date) {
        Calendar current = Calendar.getInstance();
        return new StringBuffer().append(current.get(Calendar.YEAR)).append(DateUtil.getWeekOfYear(date) - 1)
                .toString();
    }

    public static String getMonthIdByDate(Date date) {
        return new StringBuffer().append(DateUtil.getMonth(new Timestamp(date.getTime()))).toString();
    }

    public static String getSportSocialRecommendDetailPushUrl(String programId) {
        return "mjlottery://mjnative?page=recommendFootballDetail&programId=" + programId;
    }

    public static String getSportSocialGodPersonCenterPushUrl(Long godUserId) {
        return "mjlottery://mjnative?page=footballUserMain&userId=" + godUserId;
    }

    public static String getPurchaseRecommendMarqueeTitle(String userName, String payAmount) {
        return "预祝红单！！" + userName + "花" + payAmount + "智慧币购买了预测！";
    }

    public static String getGoldRecommendMarqueeTitle(String rankName, String userName) {
        return rankName + "单大神" + userName + "发预测了，速去围观~";
    }

    public static Long getUserIdSuffix(String keyInfo) {
        if (StringUtils.isBlank(keyInfo)) {
            return null;
        }
        return Long.valueOf(keyInfo.substring(keyInfo.length() - 2, keyInfo.length()));
    }

    public static String getSignMoJieData(Map<String, Object> params) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            content.append(entry.getKey()).append("=").append(entry.getValue()).append(CommonConstant.COMMON_AND_STR);
        }

        //验签必填字段
        content.append("caiqr_timestamp=").append(DateUtil.getCurrentTimeMillis());
        content.append(CommonConstant.COMMON_AND_STR).append("caiqr_version=").append("1.2");
        content.append(CommonConstant.COMMON_AND_STR).append("client_type=").append("2002");

        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );
        String keys = PropertyUtils.getProperty("mojiePrivateKey");
        String sign = RSAUtil.sign(content.toString(), keys);
        Map<String, String> header = new HashMap<>();
        header.put("caiqr-signature", sign);
        return HttpServiceUtils.sendHttpPostRequestAndSetHeader(CommonConstant.MOJIE_SPORTS_MATCH_DATA_URL, header,
                content.toString(), "UTF-8");
    }

    public static Map<Integer, Integer> getUserPushMap(String pushInfo) {
        if (StringUtils.isBlank(pushInfo)) {
            return defaultPushMap;
        }
        return JSONObject.parseObject(pushInfo, HashMap.class);
    }

    public static Integer getRandomIndexByOccupy(Integer[] ratioArr) {
        Integer[] occupyRatio = new Integer[ratioArr.length];
        for (int i = 0; i < ratioArr.length; i++) {
            occupyRatio[i] = (i == 0) ? ratioArr[i] : (ratioArr[i] + occupyRatio[i - 1]);
        }
        Integer element = new Random().nextInt(occupyRatio[occupyRatio.length - 1] + 1);
        for (int i = 0; i < occupyRatio.length; i++) {
            if (occupyRatio[i] >= element) {
                return i;
            }
        }
        return null;
    }

    public static Integer getRandomIndexByOccupy(String[] ratioArr) {
        if (ratioArr.length == 0) {
            return null;
        }
        Integer[] intOccupyArr = new Integer[ratioArr.length];
        for (int i = 0; i < ratioArr.length; i++) {
            intOccupyArr[i] = Integer.valueOf(ratioArr[i]);
        }
        return getRandomIndexByOccupy(intOccupyArr);
    }

    public static void main(String[] args) {
//        Integer[] ar = {1, 1, 3};
//        Map<Integer, Integer> temp = new HashMap();
//        for (int i = 0; i < 1000000; i++) {
//            Integer a = getRandomIndexByOccupy(ar);
//            if (temp.containsKey(a)) {
//                temp.put(a, temp.get(a) + 1);
//            } else {
//                temp.put(a, 1);
//            }
//        }
//
        System.out.print(extractNumFromString("专业的足球分析，带大家赢球车牌号123456"));
    }

    public static Integer getUserSignTypeByVersion(Integer clientType, Integer versionCode) {
        Integer result = CommonConstant.USER_SIGN_TYPE_DAILY;
        if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && versionCode >= CommonConstant.VERSION_CODE_4_1) {
            result = CommonConstant.USER_SIGN_TYPE_CYCLE;
        } else {
            if (versionCode != null && versionCode >= CommonConstant.VERSION_CODE_4_2) {
                result = CommonConstant.USER_SIGN_TYPE_CYCLE;
            }
        }
        return result;
    }

    public static Integer getIosReview(Integer versionCode) {
        Integer iosReview = CommonConstant.IOS_REVIEW_STATUS_PASSED;
        if (versionCode >= CommonConstant.VERSION_CODE_4_4_1) {
            iosReview = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.IOS_REVIEW_FLAG, CommonConstant
                    .IOS_REVIEW_STATUS_WAIT);
        }
        return iosReview;
    }

    public static Integer getIosReview(Integer versionCode, Integer clientType) {
        if (clientType == null || clientType.equals(CommonConstant.CLIENT_TYPE_IOS)) {
            return getIosReview(versionCode);
        }
        if (clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD)) {
            return CommonConstant.IOS_REVIEW_STATUS_PASSED;
        }
        if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS_1)) {
            return ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.IOS_REVIEW_FLAG + ":" +
                    clientType, CommonConstant.IOS_REVIEW_STATUS_WAIT);
        }
        if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS_WISDOM_PREDICT)) {
            return ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.IOS_REVIEW_FLAG + ":" +
                    clientType, CommonConstant.IOS_REVIEW_STATUS_WAIT);
        }

        Integer iosReview = CommonConstant.IOS_REVIEW_STATUS_PASSED;
        if (versionCode >= CommonConstant.VERSION_CODE_4_6) {
            iosReview = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.IOS_REVIEW_FLAG + ":" +
                    clientType, CommonConstant.IOS_REVIEW_STATUS_WAIT);
        }
        return iosReview;
    }

    public static List<Long> extractNumFromString(String target) {
        if (StringUtils.isBlank(target)) {
            return null;
        }
        List<Long> result = new ArrayList<>();

        Pattern p = Pattern.compile("[^0-9]");
        Matcher m = p.matcher(target);
        String replaceTarget = m.replaceAll(" ");
        if (StringUtils.isBlank(replaceTarget)) {
            return null;
        }
        StringBuffer numStr = new StringBuffer();
        for (int i = 0; i < replaceTarget.length(); i++) {
            String charStr = replaceTarget.substring(i, i + 1);
            if (StringUtils.isBlank(charStr)) {
                if (StringUtils.isNotBlank(numStr.toString())) {
                    result.add(Long.valueOf(numStr.toString()));
                    numStr = new StringBuffer();
                }
            } else {
                numStr.append(charStr);
            }
        }
        if (StringUtils.isNotBlank(numStr.toString())) {
            result.add(Long.valueOf(numStr.toString()));
        }
        return result;
    }

    public static List<Map<String, Object>> CollectionsSortedByWeight(List<Map<String, Object>> target, String
            weightName) {
        if (target == null || target.size() == 0 || StringUtils.isBlank(weightName)) {
            return target;
        }
        if (!target.get(0).containsKey(weightName)) {
            throw new IllegalArgumentException("weightName not exist in target");
        }

        Collections.sort(target, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Integer weight2 = Integer.valueOf(o2.get(weightName).toString());
                Integer weight1 = Integer.valueOf(o1.get(weightName).toString());
                return weight2.compareTo(weight1);
            }
        });
        return target;
    }

    public static String packageColorHtmlTag2Str(String tagStr, String color) {
        if (StringUtils.isBlank(tagStr)) {
            return "";
        }
        if (StringUtils.isBlank(color)) {
            return tagStr;
        }
        return "<font color='" + color + "'>" + tagStr + "</font>";
    }

    public static String getImgUrlWithDomain(String imgName) {
        if (StringUtils.isBlank(imgName)) {
            return "";
        }
        return getWebDomain() + imgName;
    }

    public static String getWebDomain() {
        return IniCache.getIniValue(IniConstant.QI_NIU_DOMAIN_NAME);
    }

    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes = keyBytes = Base64.decodeBase64(key);
//        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(key);
//        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    public static PublicKey loadPublicKey(String publicKeyStr) throws Exception {
//        BASE64Decoder base64Decoder= new BASE64Decoder();
//        byte[] buffer= base64Decoder.decodeBuffer(publicKeyStr);
        byte[] buffer = Base64.decodeBase64(publicKeyStr);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    public static String appendKey2MapString(String remark, Map<String, Object> userReal) {
        if (StringUtils.isBlank(remark) && userReal == null) {
            return null;
        }
        Map<String, Object> resMap = new HashMap<>();
        if (StringUtils.isNotBlank(remark)) {
            resMap.putAll(JSONObject.parseObject(remark, HashMap.class));
        }
        if (userReal != null) {
            resMap.putAll(userReal);
        }
        return JSONObject.toJSONString(resMap);
    }

    public static String signString(TreeMap<String, Object> signMap, List<String> unSignKeyList) throws
            IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        Iterator iterator = unSignKeyList.iterator();

        String result;
        while (iterator.hasNext()) {
            result = (String) iterator.next();
            signMap.remove(result);
        }

        iterator = signMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry) iterator.next();
            if (entry.getValue() != null && ((String) entry.getValue()).trim().length() > 0) {
                sb.append((String) entry.getKey() + "=" + (String) entry.getValue() + "&");
            }
        }

        result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    public static String signStringWithOutKey(TreeMap<String, Object> signMap, List<String> unSignKeyList) throws
            IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        Iterator iterator = unSignKeyList.iterator();

        String result;
        while (iterator.hasNext()) {
            result = (String) iterator.next();
            signMap.remove(result);
        }

        iterator = signMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry) iterator.next();
            if (entry.getValue() != null && ((String) entry.getValue()).trim().length() > 0) {
                sb.append((String) entry.getValue());
            }
        }
        return sb.toString();
    }

    public static RealNameInfo getUserRealNameInfo(String remark) {
        if (StringUtils.isBlank(remark)) {
            return null;
        }
        Map<String, Object> remarkMap = JSONObject.parseObject(remark, HashMap.class);
        if (remarkMap == null || !remarkMap.containsKey("realName")) {
            return null;
        }
        return new RealNameInfo(remarkMap.get("realName").toString(), remarkMap.get("idCard").toString(), Integer
                .valueOf(remarkMap.get("authenticateCode").toString()));
    }

    /**
     * 1. 对需要传递的参数(除去Appid和Sign两个参数)的Key值进 字典顺序排序
     * 2. 按Key值的顺序拼接参数的值
     * 3. 在第2步拼接结果的末尾拼接上私有Key
     * 4. 对第3步得到的字符 进 MD5加密,并对得到的结果取 写
     * 5. 将第4步获得的加密 作为参数中Sign的值进
     *
     * @param params
     * @param key
     * @return
     */
    public static String getRongShuSign(TreeMap<String, Object> params, String key) {
        List<String> unSignKeyList = new ArrayList<>();
        unSignKeyList.add("Appid");
        unSignKeyList.add("Sign");
        TreeMap<String, Object> signMap = new TreeMap<>();
        signMap.putAll(params);
        String signStr = CommonUtil.signStringWithOutKey(signMap, unSignKeyList);
        signStr = signStr + key;
        return Md5Util.getMD5String(signStr).toLowerCase();
    }

    public static String getSendVerifyCodePrefix(String type) {
        if (StringUtils.isBlank(type) || type.equals(CommonConstant.SMS_TYPE_LOGIN) || type.equals(CommonConstant
                .SMS_TYPE_FORGET_PASSWORD)) {
            return RedisConstant.PREFIX_SEND_VERIFY_CODE;
        } else if (type.equals(CommonConstant.SMS_TYPE_BIND_BANK)) {
            return RedisConstant.PREFIX_SEND_BIND_BANK_VERIFY_CODE;
        }
        return "";
    }

    public static Integer getWebPayStatus(Integer channelId) {
        Integer webPay = 0;
        if (channelId.equals(CommonConstant.JD_PAY_CHANNEL_ID) || channelId.equals(CommonConstant.YOP_PAY_CHANNEL_ID)
                || channelId.equals(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID)) {
            webPay = 1;
        }
        return webPay;
    }

    public static String hiddenNum(String numStr) {
        if (StringUtils.isBlank(numStr)) {
            return null;
        }
        StringBuilder stringBuild = new StringBuilder(numStr);
        StringBuilder hiddenStar = new StringBuilder();
        for (int i = 0; i < stringBuild.length() - 1; i++) {
            hiddenStar.append(CommonConstant.COMMON_STAR_STR);
        }
        return stringBuild.replace(1, stringBuild.length() - 1, hiddenStar.toString()).toString();
    }

    public static String getCardTypeCn(Integer cardType) {
        if (cardType.equals(CommonConstant.BANK_CARD_TYPE_DEBIT)) {
            return "储蓄卡";
        }
        return "信用卡";
    }

    public static String packageBankName(String bankCn, String bankCard) {
        if (StringUtils.isBlank(bankCard)) {
            return bankCn;
        }
        return bankCn + "(" + bankCard.substring(bankCard.length() - 4, bankCard.length()) + ")";
    }

    public static String getWxOpenid(String wxCode, PayClientChannel payClientChannel) {
        Map<String, String> payKeyStr = (Map<String, String>) JSONObject.parse(payClientChannel.getPayKeyStr());

        String appid = payKeyStr.get(IniConstant.WX_JSAPI_PAY_APP_ID); //微信开放平台审核通过的应用APPID
        String secret = payKeyStr.get(IniConstant.WX_JSAPI_SECRET);

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?";
        StringBuilder sb = new StringBuilder(url);
        sb.append("appid=").append(appid).append("&secret=").append(secret).append("&code=").append(wxCode).append
                ("&grant_type=authorization_code");

        String openidRes = HttpServiceUtils.sendHttpsPostRequest(sb.toString(), "", HttpParamDto.DEFAULT_CHARSET);

        if (StringUtils.isBlank(openidRes)) {
            return null;
        }
        Map<String, String> openidMap = JSONObject.parseObject(openidRes, HashMap.class);
        if (!openidMap.containsKey("openid") || openidMap.get("openid") == null) {
            return "";
        }
        return openidMap.get("openid");
    }

    public static String removeQuotationMark(String target) {
        if (StringUtils.isBlank(target)) {
            return target;
        }
        return target.replaceAll("\"", "");
    }

    public static String removeJDBillQuotationMark(String target) {
        if (StringUtils.isBlank(target)) {
            return target;
        }
        String temp = target.replaceAll("=", "");
        temp = temp.replaceAll("\\t", "");
        return temp.replaceAll("\"", "");
    }

    public static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values == null || values.length == 0) {
            result = false;
        } else {
            for (String value : values) {
                result &= !StringUtils.isEmpty(value);
            }
        }
        return result;
    }

    /**
     * 将emoji表情替换成空串
     *
     * @param source
     * @return 过滤后的字符串
     */
    public static String filterEmoji(String source) {
        if (source != null && source.length() > 0) {
            return source.replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "");
        } else {
            return source;
        }
    }

    public static String getValueFromMap(Map<String, String> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        return map.get(key);
    }

    public static String getValueFromMap(String key, Map<String, Object> map) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }
        return map.get(key).toString();
    }

    public static Object getValueFromJSONMap(String jsonStr, String key) {
        if (StringUtils.isBlank(jsonStr) || StringUtils.isBlank(key)) {
            return null;
        }
        Map<String, Object> jsonMap = JSONObject.parseObject(jsonStr, HashMap.class);
        if (jsonMap == null || jsonMap.isEmpty() || !jsonMap.containsKey(key)) {
            return null;
        }

        return jsonMap.get(key);
    }
}
