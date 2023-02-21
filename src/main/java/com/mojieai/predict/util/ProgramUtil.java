package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.Program;
import com.mojieai.predict.redis.PeriodRedis;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramUtil {

    public static Map<String, Object> convertProgram2SaleMap(Program program, Integer programBuyStatus, boolean isVip) {
        Map<String, Object> res = new HashMap<>();
        String programDesc = "";
        if (program.getBuyType().equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE)) {
            programDesc = "<font color='#999999'>此推单如不中奖，返还花费的智慧币</font>";
        } else if (program.getBuyType().equals(CommonConstant.PROGRAM_BUY_TYPE_LIMIT)) {
            programDesc = "<font color='#999999'>限购推单只开放部分购买名额，保证号码的稀缺性</font>";
        }

        res.put("programScoreText", "智慧指数 " + program.getWisdomScore());
        res.put("programBuyType", program.getBuyType());
        res.put("programBuyTypeText", getProgramBuyTypeText(program.getBuyType(), program.getSaleCount(), program
                .getTotalCount(), program.getIsAward()));
        res.put("programBuyTypeBgUrl", getProgramBuyTypeBgUrl(program.getBuyType()));

        String price = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(program.getPrice()).doubleValue());
        res.put("programPriceText", "价格：<font color='#FF5050'>" + price + "</font>" + CommonConstant
                .GOLD_WISDOM_COIN_MONETARY_UNIT);
//        String vipDiscount = CommonUtil.removeZeroAfterPoint(getProgramDiscountTxt(program.getVipDiscount()));
        String vipPrice = ProgramUtil.getVipPrice(program.getPrice(), program.getVipDiscount()).toString();
        vipPrice = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(vipPrice).toString());
        if (program.getVipPrice() != null) {
            vipPrice = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(program.getVipPrice()).doubleValue());
        }

        res.put("programVipPriceText", "<font color='#FF8E50'>会员" + vipPrice + CommonConstant.WISDOM_COIN_PAY_NAME +
                "</font>");
        res.put("programDesc", programDesc);
        res.put("programId", program.getProgramId());
        res.put("programBuyStatus", programBuyStatus);
        String programRedNumber = program.getRedNumber();
        String programBlueNumber = program.getBlueNumber();
        if (!programBuyStatus.equals(CommonConstant.PROGRAM_BUY_STATUS_PAYED)) {
            programRedNumber = getProgramNumShow(0, program.getProgramType(), programRedNumber);
            programBlueNumber = getProgramNumShow(1, program.getProgramType(), programBlueNumber);
        } else {
            programRedNumber = TrendUtil.orderNum(programRedNumber);
            programBlueNumber = TrendUtil.orderNum(programBlueNumber);
        }
        res.put("programRedNumber", programRedNumber);
        res.put("programBlueNumber", programBlueNumber);
        return res;
    }

    /**
     * @param ballType    0 红球 1蓝球
     * @param programType
     * @param redNum
     * @return
     */
    public static String getProgramNumShow(Integer ballType, Integer programType, String redNum) {
        int showCount = getShowCount(ballType, programType);
        String redNumArr[] = redNum.split(CommonConstant.SPACE_SPLIT_STR);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < redNumArr.length; i++) {
            if (i < showCount) {
                sb.append(redNumArr[i]);
            } else {
                sb.append(CommonConstant.COMMON_QUESTION_STR);
            }
            sb.append(CommonConstant.SPACE_SPLIT_STR);
        }
        return sb.toString().trim();
    }

    private static int getShowCount(Integer ballType, Integer programType) {
        int showNumArr[] = new int[2];
        if (programType.equals(CommonConstant.PROGRAM_TYPE_15_5)) {
            showNumArr[0] = 4;
            showNumArr[1] = 2;
        } else if (programType.equals(CommonConstant.PROGRAM_TYPE_12_3)) {
            showNumArr[0] = 3;
            showNumArr[1] = 1;
        } else if (programType.equals(CommonConstant.PROGRAM_TYPE_9_3)) {
            showNumArr[0] = 2;
            showNumArr[1] = 1;
        } else if (programType.equals(CommonConstant.PROGRAM_TYPE_8_3)) {
            showNumArr[0] = 2;
            showNumArr[1] = 0;
        }
        if (showNumArr.length == 0) {
            return 0;
        }
        return showNumArr[ballType];
    }

    private static String getProgramBuyTypeBgUrl(Integer buyType) {
        String bgImg = "";
        if (buyType.equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE)) {
            bgImg = "http://sportsimg.mojieai.com/program_compensate_bg.png";
        } else if (buyType.equals(CommonConstant.PROGRAM_BUY_TYPE_LIMIT)) {
            bgImg = "http://sportsimg.mojieai.com/program_limit_bg.png";
        }
        return bgImg;
    }

    public static Map<String, Object> convertProgram2Map(Program program, Integer isReturnCoin) {
        Map<String, Object> res = new HashMap<>();
        Integer awardStatus = CommonConstant.PROGRAM_IS_AWARD_WAIT;//待开奖
        if (program == null) {
            return res;
        }
        if (program.getIsAward() != null) {
            awardStatus = program.getIsAward();
        }

        String programTitle = getProgramTypeCn(program.getProgramType()) + "，智慧指数 " + program.getWisdomScore();
        res.put("programTitle", programTitle);
        res.put("programBuyType", program.getBuyType());
        res.put("programBuyTypeBgUrl", getProgramBuyTypeBgUrl(program.getBuyType()));
        res.put("programBuyTypeText", getProgramBuyTypeText(program.getBuyType(), program.getSaleCount(), program
                .getTotalCount(), CommonConstant.PROGRAM_IS_AWARD_YES));
        String price = CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(program.getPrice()).doubleValue());
        res.put("programPriceText", "<font color='#999999'>价格：" + price + CommonConstant
                .GOLD_WISDOM_COIN_MONETARY_UNIT + "</font>");
        res.put("programRedNumber", TrendUtil.orderNum(program.getRedNumber()));
        res.put("programBlueNumber", TrendUtil.orderNum(program.getBlueNumber()));
        res.put("programAwardStatus", awardStatus);
        res.put("programAwardText", getProgramReturnTxt(isReturnCoin));

        return res;
    }

    public static List<Map<String, Object>> getProgramListFromActivityIni(long gameId) {
        String key = ActivityIniConstant.PROGRAM_LIST;
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            key = GameConstant.DLT + CommonConstant.COMMON_COLON_STR + key;
        }
        List<Map<String, Object>> programList = JSONObject.parseObject(ActivityIniCache.getActivityIniValue(key),
                ArrayList.class);
        return programList;
    }


    public static String getProgramBuyTypeText(Integer buyType, Integer saleCount, Integer totalCount, Integer
            isAward) {
        String res = "";
        if (buyType.equals(CommonConstant.PROGRAM_BUY_TYPE_LIMIT)) {
            saleCount = saleCount > totalCount ? totalCount : saleCount;
            if (isAward != null && (isAward.equals(CommonConstant.PROGRAM_IS_AWARD_NO) || isAward.equals(CommonConstant
                    .PROGRAM_IS_AWARD_YES))) {
                res = "限购";
            } else {
                Integer restCount = totalCount - saleCount;
                res = "仅剩" + restCount + "个";
            }
        } else if (buyType.equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE)) {
            res = "不中包赔";
        }
        return res;
    }

    public static String getProgramTypeCn(Integer programType) {
        if (programType.equals(CommonConstant.PROGRAM_TYPE_15_5)) {
            return "15红5蓝";
        } else if (programType.equals(CommonConstant.PROGRAM_TYPE_12_3)) {
            return "12红3蓝";
        } else if (programType.equals(CommonConstant.PROGRAM_TYPE_9_3)) {
            return "9红3蓝";
        } else if (programType.equals(CommonConstant.PROGRAM_TYPE_8_3)) {
            return "8红3蓝";
        }
        return "";
    }

    public static String getProgramReturnTxt(Integer isReturnCoin) {
        if (isReturnCoin.equals(CommonConstant.PROGRAM_IS_RETURN_COIN_YES)) {
            return "<font color='#333333'>已赔付</font>";
        }
        return "";
    }

    public static Integer getProgramBuyTypeNums(String buyType) {
        // 可以修改配置到activityIni
        Map<String, Integer> map = new HashMap<>();
        // 15:5:0 方案类型
        map.put("15:5:0", 1);
        map.put("15:5:1", 2);
        map.put("15:5:2", 3);
        map.put("12:3:0", 1);
        map.put("12:3:1", 2);
        map.put("12:3:2", 3);
        map.put("9:3:0", 1);
        map.put("9:3:1", 3);
        map.put("9:3:2", 6);
        map.put("8:3:0", 1);
        map.put("8:3:1", 3);
        map.put("8:3:2", 6);
        return map.get(buyType);
    }

    public static Integer getProgramTypeByNums(String programType) {
        // 可以修改配置到activityIni
        Map<String, Integer> map = new HashMap<>();
        map.put("15:5", CommonConstant.PROGRAM_TYPE_15_5);
        map.put("12:3", CommonConstant.PROGRAM_TYPE_12_3);
        map.put("9:3", CommonConstant.PROGRAM_TYPE_9_3);
        map.put("8:3", CommonConstant.PROGRAM_TYPE_8_3);
        return map.get(programType);
    }

    public static String getVipDisCountPrice(Long price, Integer vipDiscount) {
        //1.获取vip的折扣后的钱
        BigDecimal vipPrice = getVipPrice(price, vipDiscount);
        //2.原价减去现价
        BigDecimal discountPrice = new BigDecimal(price).subtract(vipPrice);
        return discountPrice.toString();
    }

    public static BigDecimal getVipPrice(Long price, Integer vipDiscount) {
        BigDecimal vipPrice = CommonUtil.divide(CommonUtil.multiply(price + "", vipDiscount + ""), new BigDecimal
                (100), 2);
        return vipPrice;
    }

    public static String getProgramDiscountTxt(Integer vipDiscount) {
        String discountStr = CommonUtil.divide(vipDiscount + "", "10", 1).toString();
        return CommonUtil.removeZeroAfterPoint(discountStr);
    }

    public static String getRemark(Long originAmount, Integer vipDiscount, Long amount) {
        Map<String, Object> res = new HashMap<>();
        String vipDiscountTxt = "";
        if (!originAmount.equals(amount)) {
            String vipDiscountStr = getProgramDiscountTxt(vipDiscount);
            vipDiscountTxt = "会员折扣:" + vipDiscountStr + "折";
        }
        res.put("originAmount", originAmount + CommonConstant.CASH_MONETARY_UNIT_FEN);
        res.put("purchaseAmount", amount + CommonConstant.CASH_MONETARY_UNIT_FEN);
        res.put("vipDiscount", vipDiscountTxt);
        return JSONObject.toJSONString(res);
    }

    public static boolean getProgramIsEnd(Program program) {
        Boolean res = Boolean.TRUE;
        GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(program.getGameId(), program.getPeriodId());
        Timestamp endTime = CommonUtil.getSomeDateJoinTime(period.getEndTime(), "20:00:00");
        if (DateUtil.compareDate(DateUtil.getCurrentTimestamp(), endTime)) {
            res = Boolean.FALSE;
        }
        return res;
    }

    public static Integer getProgramBuyStatus(Program program) {
        Integer programBuyStatus = CommonConstant.PROGRAM_BUY_STATUS_NO_PURCHASE;
        if (getProgramIsEnd(program)) {
            programBuyStatus = CommonConstant.PROGRAM_BUY_STATUS_EXPORED;
        } else if (program.getBuyType().equals(CommonConstant.PROGRAM_BUY_TYPE_LIMIT) && program
                .getTotalCount() <= program.getSaleCount()) {
            programBuyStatus = CommonConstant.PROGRAM_BUY_STATUS_SALE_END;
        }
        return programBuyStatus;
    }
}
