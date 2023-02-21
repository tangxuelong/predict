package com.mojieai.predict.enums;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.entity.po.AwardInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum D11GameEnum {
    QIAN_1("QIAN_1") {
        @Override
        public int getNumberCount() {
            return 1;
        }
    }, QIAN_2_ZUXUAN("QIAN_2_ZUXUAN") {
        @Override
        public int getNumberCount() {
            return 2;
        }
    }, QIAN_2_ZHIXUAN("QIAN_2_ZHIXUAN") {
        @Override
        public int getNumberCount() {
            return 2;
        }
    }, QIAN_3_ZUXUAN("QIAN_3_ZUXUAN") {
        @Override
        public int getNumberCount() {
            return 3;
        }
    }, QIAN_3_ZHIXUAN("QIAN_3_ZHIXUAN") {
        @Override
        public int getNumberCount() {
            return 3;
        }
    }, REN_2("REN2") {
        @Override
        public int getNumberCount() {
            return 2;
        }
    }, REN_3("REN3") {
        @Override
        public int getNumberCount() {
            return 3;
        }
    }, REN_4("REN4") {
        @Override
        public int getNumberCount() {
            return 4;
        }
    }, REN_5("REN5") {
        @Override
        public int getNumberCount() {
            return 5;
        }
    }, REN_6("REN6") {
        @Override
        public int getNumberCount() {
            return 6;
        }
    }, REN_7("REN7") {
        @Override
        public int getNumberCount() {
            return 7;
        }
    }, REN_8("REN8") {
        @Override
        public int getNumberCount() {
            return 8;
        }
    };

    public static final String[] D11_EXTRA = {QIAN_1.getExtra(), QIAN_2_ZUXUAN.getExtra(), QIAN_2_ZHIXUAN.getExtra(),
            QIAN_3_ZUXUAN.getExtra(), QIAN_3_ZHIXUAN.getExtra(), REN_2.getExtra(), REN_3.getExtra(), REN_4.getExtra()
            , REN_5.getExtra(), REN_6.getExtra(), REN_7.getExtra(), REN_8.getExtra()};

    public final static List<AwardInfo> D11_AWARD_INFO_LIST = new ArrayList<>();
    public final static Map<String, String> extraCnMap = new HashMap<>();
    public final static List<String> D11_NUMBERS = new ArrayList<>();
    public final static Integer HBD11_DAILY_PERIOD = 81;
    public final static Integer JXD11_DAILY_PERIOD = 84;
    public final static Integer SDD11_DAILY_PERIOD = 87;
    public final static Integer GDD11_DAILY_PERIOD = 84;
    public final static Integer XJD11_DAILY_PERIOD = 97;
    public final static Integer HLJD11_DAILY_PERIOD = 88;
    public final static Integer SXD11_DAILY_PERIOD = 88;
    /* 格式:
        -1,22:28:00,08:58:00,08:58:20_ 下横杠结尾,生成期次的第一条数据(-1昨天,0当天,1明天,以此类推),开始时间,结束时间,开奖时间
        08:58:30,09:08:00,09:08:20_ 下横杠结尾,第二期次数据, 用于生成标准期次的基础数据,开始时间,结束时间,开奖时间
        yyyyMMdd,000 生成期次ID的格式,前者: ID的时间日期前缀, 后者ID的自增序号格式
    */
    public final static String HBD11_INIT_PERIOD_FORMAT = "-1,21:55:00,08:35:00,08:36:00_08:35:00," +
            "08:45:00,08:46:00_yyMMdd,00";
    public final static String JXD11_INIT_PERIOD_FORMAT = "-1,22:59:30,09:09:30,09:10:00_09:09:30," +
            "09:19:30,09:20:00_yyMMdd,00";
    public final static String SDD11_INIT_PERIOD_FORMAT = "-1,22:55:20,08:35:20,08:36:00_08:35:20,08:45:20," +
            "08:46:00_yyMMdd,00";
    public final static String GDD11_INIT_PERIOD_FORMAT = "-1,22:59:50,09:09:50,09:10:30_09:09:50,09:19:50," +
            "09:20:30_yyMMdd,00";

    public final static String XJD11_INIT_PERIOD_FORMAT = "0,01:59:30,09:59:30,10:00:00_09:59:30,10:09:30," +
            "10:10:00_yyMMdd,00";
    public final static String HLJD11_INIT_PERIOD_FORMAT = "-1,22:34:00,08:04:00,08:04:00_08:04:00,08:14:00," +
            "08:14:00_yyMMdd,00";
    public final static String SXD11_INIT_PERIOD_FORMAT = "-1,22:59:30,08:29:30,08:30:00_08:29:30,08:39:30," +
            "08:40:00_yyMMdd,00";


    public final static Integer HBD11_TIME_INTERVAL = 600;
    public final static Integer JXD11_TIME_INTERVAL = 600;
    public final static Integer SDD11_TIME_INTERVAL = 600;
    public final static Integer GDD11_TIME_INTERVAL = 600;
    public final static Integer XJD11_TIME_INTERVAL = 600;
    public final static Integer HLJD11_TIME_INTERVAL = 600;
    public final static Integer SXD11_TIME_INTERVAL = 600;

    static {
        extraCnMap.put(QIAN_1.getExtra(), "前一");
        extraCnMap.put(QIAN_2_ZUXUAN.getExtra(), "前二组选");
        extraCnMap.put(QIAN_2_ZHIXUAN.getExtra(), "前二直选");
        extraCnMap.put(QIAN_3_ZUXUAN.getExtra(), "前三组选");
        extraCnMap.put(QIAN_3_ZHIXUAN.getExtra(), "前三直选");
        extraCnMap.put(REN_2.getExtra(), "任选二");
        extraCnMap.put(REN_3.getExtra(), "任选三");
        extraCnMap.put(REN_4.getExtra(), "任选四");
        extraCnMap.put(REN_5.getExtra(), "任选五");
        extraCnMap.put(REN_6.getExtra(), "任选六");
        extraCnMap.put(REN_7.getExtra(), "任选七");
        extraCnMap.put(REN_8.getExtra(), "任选八");

        D11_AWARD_INFO_LIST.add(new AwardInfo("1", "前一直选", new BigDecimal(13)));//前一
        D11_AWARD_INFO_LIST.add(new AwardInfo("2", "前二组选", new BigDecimal(65)));//前二组选
        D11_AWARD_INFO_LIST.add(new AwardInfo("3", "前二直选", new BigDecimal(130)));//前二直选
        D11_AWARD_INFO_LIST.add(new AwardInfo("4", "前三组选", new BigDecimal(195)));//前三组选
        D11_AWARD_INFO_LIST.add(new AwardInfo("5", "前三直选", new BigDecimal(1170)));//前三直选
        D11_AWARD_INFO_LIST.add(new AwardInfo("6", "任选二", new BigDecimal(6)));//任选二
        D11_AWARD_INFO_LIST.add(new AwardInfo("7", "任选三", new BigDecimal(19)));//任选三
        D11_AWARD_INFO_LIST.add(new AwardInfo("8", "任选四", new BigDecimal(78)));//任选四
        D11_AWARD_INFO_LIST.add(new AwardInfo("9", "任选五", new BigDecimal(540)));//任选五
        D11_AWARD_INFO_LIST.add(new AwardInfo("10", "任选六", new BigDecimal(90)));//任选六
        D11_AWARD_INFO_LIST.add(new AwardInfo("11", "任选七", new BigDecimal(26)));//任选七
        D11_AWARD_INFO_LIST.add(new AwardInfo("12", "任选八", new BigDecimal(9)));//任选八


        D11_NUMBERS.add("01");
        D11_NUMBERS.add("02");
        D11_NUMBERS.add("03");
        D11_NUMBERS.add("04");
        D11_NUMBERS.add("05");
        D11_NUMBERS.add("06");
        D11_NUMBERS.add("07");
        D11_NUMBERS.add("08");
        D11_NUMBERS.add("09");
        D11_NUMBERS.add("10");
        D11_NUMBERS.add("11");
    }

    private String extra;

    D11GameEnum(String extra) {
        this.extra = extra;
    }

    public static D11GameEnum getByExtra(String extra) {
        for (D11GameEnum k : D11GameEnum.values()) {
            if (extra.equals(k.getExtra())) {
                return k;
            }
        }
        return null;
    }

    public String getExtra() {
        return extra;
    }

    abstract public int getNumberCount();

    public String calcRenSameNumbers(String lotteryNumber, String winningNumbers) {
        String[] winArray = winningNumbers.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                .SPACE_SPLIT_STR);
        for (String winNumber : winArray) {
            if (lotteryNumber.contains(winNumber)) {
                lotteryNumber = lotteryNumber.replace(winNumber, CommonConstant.UP_ARROW_STR + winNumber);
            }
        }
        return lotteryNumber;
    }
}