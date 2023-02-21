package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.ColdHotAttrVo;
import com.mojieai.predict.entity.vo.ColdHotNumVo;
import com.mojieai.predict.entity.vo.SocialBigDataVo;
import com.mojieai.predict.enums.resonance.DltResonanceEnum;
import com.mojieai.predict.enums.resonance.GameResonance;
import com.mojieai.predict.enums.resonance.ResonanceEnum;
import com.mojieai.predict.enums.resonance.SsqResonanceEnum;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.ProgramUtil;
import com.mojieai.predict.util.SocialEncircleKillCodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by tangxuelong on 2017/11/7.
 */
@Service
public class SocialResonanceServiceImpl implements SocialResonanceService {
    private static final Logger log = LogConstant.commonLog;
    @Autowired
    private DltSocialResonanceDataDao dltSocialResonanceDataDao;
    @Autowired
    private SsqSocialResonanceDataDao ssqSocialResonanceDataDao;
    @Autowired
    private SocialCodeMissionDao socialCodeMissionDao;
    @Autowired
    private SocialEncircleCodeDao socialEncircleCodeDao;
    @Autowired
    private SocialKillCodeDao socialKillCodeDao;
    @Autowired
    private ExchangeMallDao exchangeMallDao;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private TrendService trendService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private UserResonanceInfoService userResonanceInfoService;
    @Autowired
    private PayService payService;
    @Autowired
    private RedisService redisService;


    @Override
    public void rebuildSocialResonance() {
        Game game = GameCache.getGame(GameConstant.SSQ);
        GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
        List<SocialEncircle> socialEncircleList = socialEncircleCodeDao.getSocialEncircleByPeriodId(game.getGameId(),
                currentPeriod.getPeriodId());
        for (SocialEncircle socialEncircle : socialEncircleList) {
            updateSocialResonance(socialEncircle);
        }

        List<SocialKillCode> socialKillCodeList = socialKillCodeDao.getKillNumsByPeriodId(game.getGameId(),
                currentPeriod.getPeriodId());
        for (SocialKillCode socialKillCode : socialKillCodeList) {
            updateSocialResonance(socialKillCode);
        }
    }

    @Override
    public void updateSocialResonance(SocialEncircle socialEncircle) {
        Game game = GameCache.getGame(socialEncircle.getGameId());
        // 区分双色球和大乐透
        GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(game.getGameId(), socialEncircle.getPeriodId());

        if (game.getGameEn().equals(GameConstant.DLT)) {
            //遍历号码 更新数据
            for (DltResonanceEnum k : DltResonanceEnum.values()) {
                if (k.getSocialType().equals(0)) { //围号
                    dltUpdateResonance(period, socialEncircle.getUserEncircleCode(), k, 0);
                }
            }
        }
        if (game.getGameEn().equals(GameConstant.SSQ)) {
            //遍历号码 更新数据
            for (SsqResonanceEnum k : SsqResonanceEnum.values()) {
                if (k.getSocialType().equals(0)) { //围号
                    ssqUpdateResonance(period, socialEncircle.getUserEncircleCode(), k, 0);
                }
            }
        }
    }

    @Override
    public void updateSocialResonance(SocialKillCode socialKillCode) {
        Game game = GameCache.getGame(socialKillCode.getGameId());
        // 区分双色球和大乐透
        GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(game.getGameId(), socialKillCode.getPeriodId());

        if (game.getGameEn().equals(GameConstant.DLT)) {
            //遍历号码 更新数据
            for (DltResonanceEnum k : DltResonanceEnum.values()) {
                if (k.getSocialType().equals(1)) { //杀号
                    dltUpdateResonance(period, socialKillCode.getUserKillCode(), k, 0);
                }
            }
        }
        if (game.getGameEn().equals(GameConstant.SSQ)) {
            //遍历号码 更新数据
            for (SsqResonanceEnum k : SsqResonanceEnum.values()) {
                if (k.getSocialType().equals(1)) { //杀号
                    ssqUpdateResonance(period, socialKillCode.getUserKillCode(), k, 0);
                }
            }
        }
    }

    @Override
    public void expireSocialResonance() {
        for (ResonanceEnum re : ResonanceEnum.values()) {
            for (GameResonance gameResonance : re.getGameResonance()) {
                if (gameResonance.getResonanceTypeTime() == 0) {
                    continue;
                }
                Game game = GameCache.getGame(re.getGameEn());

                GamePeriod period = PeriodRedis.getCurrentPeriod(game.getGameId());

                List<Long> socialCodeMissionIds = socialCodeMissionDao.getSlaveMissionIdsByTaskType(gameResonance
                        .getSocialType(), gameResonance.getResonanceType(), game.getGameEn(), period.getStartTime
                        ());


                // 0 第一阶段 1 第二阶段 2 第三阶段 3 第4阶段
                for (Long socialCodeMissionId : socialCodeMissionIds) {
                    SocialCodeMission socialCodeMission = socialCodeMissionDao.getSlaveBakMissionById
                            (socialCodeMissionId);
                    String periodId = null;
                    String socialCode = null;
                    //圈号
                    if (socialCodeMission.getMissionType().equals(SocialCodeMission.MISSION_TYPE_ENCIRCLE)) {
                        if (DateUtil.getDiffMinutes(socialCodeMission.getCreateTime(), DateUtil.getCurrentTimestamp()
                        ) > gameResonance.getResonanceTypeTime()) {
                            SocialEncircle socialEncircle = socialEncircleCodeDao.getSocialEncircleByEncircleId(game
                                    .getGameId(), period.getPeriodId(), Long.parseLong(socialCodeMission.getKeyInfo()
                                    .toString()));
                            if (null == socialEncircle) {
                                continue;
                            }
                            periodId = socialEncircle.getPeriodId();
                            socialCode = socialEncircle.getUserEncircleCode();
                        }
                    }

                    //杀号
                    if (socialCodeMission.getMissionType().equals(SocialCodeMission.MISSION_TYPE_KILL)) {
                        if (DateUtil.getDiffMinutes(socialCodeMission.getCreateTime(), DateUtil.getCurrentTimestamp()
                        ) > gameResonance.getResonanceTypeTime()) {
                            // 围号
                            SocialKillCode socialKillCode = socialKillCodeDao.getKillNumsByKillCodeId(Long.parseLong
                                    (socialCodeMission.getKeyInfo().toString()), period.getPeriodId());
                            if (null == socialKillCode) {
                                continue;
                            }
                            periodId = socialKillCode.getPeriodId();
                            socialCode = socialKillCode.getUserKillCode();
                        }
                    }
                    if (null != periodId) {
                        if (DateUtil.getDiffMinutes(socialCodeMission.getCreateTime(), DateUtil.getCurrentTimestamp()
                        ) > gameResonance.getResonanceTypeTime()) {
                            // 减少掉共振数据
                            expireGameResonance(period, socialCode, gameResonance, 1, re);

                            // 更新为下一个状态
                            socialCodeMissionDao.updateMissionStatus(socialCodeMissionId, gameResonance
                                    .getResonanceType() + 1, gameResonance.getResonanceType());
                        }
                    }
                }
            }
        }
    }

    private void dltUpdateResonance(GamePeriod period, String socialCode, GameResonance gameResonance, Integer
            operateType) {
        DltSocialResonanceData dltSocialResonanceData = dltSocialResonanceDataDao.getTypeResonanceCurrentPeriod
                (period.getPeriodId(), gameResonance.getSocialType(), gameResonance.getResonanceType());
        Boolean insert = Boolean.FALSE;
        if (null == dltSocialResonanceData) {
            // 插入
            dltSocialResonanceData = new DltSocialResonanceData(period.getPeriodId(), gameResonance.getSocialType(),
                    gameResonance.getResonanceType());
            insert = Boolean.TRUE;
        }
        // 号码 + 1
        for (String number : socialCode.split(CommonConstant.COMMA_SPLIT_STR)) {
            Field[] fields = dltSocialResonanceData.getClass().getDeclaredFields();
            updateResonance(dltSocialResonanceData, fields, number, operateType);
        }
        dltSocialResonanceData.setPeriodId(period.getPeriodId());
        dltSocialResonanceData.setSocialType(gameResonance.getSocialType());
        dltSocialResonanceData.setResonanceType(gameResonance.getResonanceType());
        if (insert) {
            dltSocialResonanceDataDao.insert(dltSocialResonanceData);
        } else {
            dltSocialResonanceDataDao.update(dltSocialResonanceData);
        }
    }

    private void expireGameResonance(GamePeriod period, String socialCode, GameResonance gameResonance, Integer
            operateType, ResonanceEnum resonanceEnum) {
        if (resonanceEnum.getGameEn().equals(GameConstant.SSQ)) {
            ssqUpdateResonance(period, socialCode, gameResonance, operateType);
        } else if (resonanceEnum.getGameEn().equals(GameConstant.DLT)) {
            dltUpdateResonance(period, socialCode, gameResonance, operateType);
        }

    }

    private void ssqUpdateResonance(GamePeriod period, String socialCode, GameResonance gameResonance, Integer
            operateType) {
        SsqSocialResonanceData ssqSocialResonanceData = ssqSocialResonanceDataDao
                .getTypeResonanceCurrentPeriod(period.getPeriodId(), gameResonance.getSocialType(), gameResonance
                        .getResonanceType());
        Boolean insert = Boolean.FALSE;
        if (null == ssqSocialResonanceData) {
            // 插入
            ssqSocialResonanceData = new SsqSocialResonanceData(period.getPeriodId(), gameResonance.getSocialType(),
                    gameResonance.getResonanceType());
            insert = Boolean.TRUE;
        }
        // 号码 + 1
        for (String number : socialCode.split(CommonConstant
                .COMMA_SPLIT_STR)) {
            Field[] fields = ssqSocialResonanceData.getClass().getDeclaredFields();
            updateResonance(ssqSocialResonanceData, fields, number, operateType);
        }
        ssqSocialResonanceData.setPeriodId(period.getPeriodId());
        ssqSocialResonanceData.setSocialType(gameResonance.getSocialType());
        ssqSocialResonanceData.setResonanceType(gameResonance.getResonanceType());
        if (insert) {
            ssqSocialResonanceDataDao.insert(ssqSocialResonanceData);
        } else {
            ssqSocialResonanceDataDao.update(ssqSocialResonanceData);
        }
    }

    private void updateResonance(Object ssqSocialResonanceData, Field[] fields, String number,
                                 Integer operateType) {
        for (Field field : fields) {
            if (field.getName().contains(number)) {
                // int resNum = new Random().nextInt(3) + 1;
                int resNum = 3;
                try {
                    String name = field.getName();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    Method set = ssqSocialResonanceData.getClass().getMethod("set" + name, Integer.class);
                    Method get = ssqSocialResonanceData.getClass().getMethod("get" + name);
                    Integer value = (Integer) get.invoke(ssqSocialResonanceData);//调用getter方法获取属性值

                    if (operateType == 0) {
                        if (null == value) {
                            value = resNum;
                        } else {
                            value = value + resNum;
                        }
                    }
                    if (operateType == 1) {
                        if (null == value) {
                            value = 0;
                        } else {
                            if (value < resNum) {
                                resNum = 1;
                            }
                            value = value - resNum;
                        }
                    }
                    set.invoke(ssqSocialResonanceData, value);    //调用setter方法获取属性值
                } catch (Exception e) {
                    throw new BusinessException("field", e);
                }
            }
        }
    }

    // 接口逻辑数据
    @Override
    public Map<String, Object> getResonanceData(Game game, Integer resonanceType, Long userId, Integer clientId) {
        Map<String, Object> resultMap = new HashMap<>();
        boolean vipFlag = vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT);
        Map<String, Object> itemInfo = payService.getAccessIdByType(CommonConstant.ACCESS_BIG_DATA, game.getGameId());
        Integer itemId = (Integer) itemInfo.get("itemId");
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(game.getGameId(), lastOpenPeriod
                .getPeriodId());
        boolean isPayed = payService.checkUserAccess(userId, game.getGameId(), nextPeriod.getPeriodId(), itemId);
        boolean bigDataPermission = false;
        if (isPayed || vipFlag) {
            bigDataPermission = true;
        }

        // 基础数据
        Map<String, Object> bigData = socialEncircleCodeService.getSocialBigData(game.getGameId());
        List<SocialBigDataVo> socialBigDataVos = (List<SocialBigDataVo>) bigData.get("socialData");
        Map<String, Object> baseData = getOnePeriodBigDate(socialBigDataVos, bigDataPermission);
        resultMap.put("baseData", baseData);
        Map<String, Object> resonance = new HashMap<>();

        List<ExchangeMall> exchangeMalls = exchangeMallDao.getExchangeMallList(ExchangeMallConstant
                .EXCHANGE_MALL_ITEM_TYPE_RESONANCE, game.getGameId(), clientId);
        String vipDiscount = "";
        for (ExchangeMall exchangeMall : exchangeMalls) {
            if (exchangeMall.getItemCount() == 1) {
                resonance.put("oneGoodsId", exchangeMall.getItemId());
                resonance.put("onePeriodBtnMsg", exchangeMall.getItemName());
            } else if (exchangeMall.getItemCount() > 1) {
                resonance.put("monthGoodsId", exchangeMall.getItemId());
                resonance.put("monthBtnMsg", exchangeMall.getItemName());
            }
            vipDiscount = ProgramUtil.getProgramDiscountTxt(exchangeMall.getVipDiscount());
        }

        Integer isPermission = userResonanceInfoService.checkUserResonanceInfoPayStatus(game.getGameId(), userId,
                Integer.valueOf(nextPeriod.getPeriodId()));

        Long countDownSecond = (Long) bigData.get("countDownSecond");
        List<SocialBigDataVo> bigDataVos = (List<SocialBigDataVo>) bigData.get("socialData");
        if (bigDataVos.size() > 0) {
            countDownSecond = 0l;
        }

        String vipDiscountMsg = "";
        if (StringUtils.isNotBlank(vipDiscount) && Integer.valueOf(vipDiscount) > 0 && Integer.valueOf(vipDiscount) <
                10) {
            vipDiscountMsg = "会员享<font color='#ff5050'>" + vipDiscount + "</font>折优惠";
            if (!vipFlag) {
                vipDiscountMsg = vipDiscountMsg + "，<font color='#ff5050'>成为会员</font>";
            }
        }

        // 共振数据
        resonance.put("isPermission", isPermission);
        resonance.put("name", "共振数据");
        resonance.put("dataDesc", "包括围号共振分布、杀号共振分布，和热门围号、杀号的走势表现");
        resonance.put("vipDiscountMsg", vipDiscountMsg);

        //过滤条件
        List<Map<String, Object>> filterCondition = getAllResonanceFilterCondition(game.getGameEn(), resonanceType);
        resonance.put("filterCondition", filterCondition);

        String dataPrepareMsg = "第一批数据生成中";
        Map<String, Object> dataInstruction = (Map<String, Object>) bigData.get("dataInstruction");

        List<Map<String, Object>> resonanceData = getOnePeriodResonanceDate(game, resonanceType, userId, isPermission);

        resultMap.put("isHaveResonanceData", getIsHaveResonanceData(game, nextPeriod));

        resonance.put("resonanceData", resonanceData);
        resultMap.put("resonance", resonance);
        resultMap.put("countDownSecond", countDownSecond);
        resultMap.put("dataPrepareMsg", dataPrepareMsg);
        resultMap.put("dataInstruction", dataInstruction);
        return resultMap;
    }

    private Boolean getIsHaveResonanceData(Game game, GamePeriod period) {
        if (game.getGameEn().equals(GameConstant.SSQ)) {

            List<SsqSocialResonanceData> socialResonanceDatas = ssqSocialResonanceDataDao
                    .getAllTypeResonanceCurrentPeriod(period.getPeriodId(), 0);
            if (socialResonanceDatas == null || socialResonanceDatas.size() == 0) {
                return Boolean.FALSE;
            }
            List<SsqSocialResonanceData> socialResonanceDataK = ssqSocialResonanceDataDao
                    .getAllTypeResonanceCurrentPeriod(period.getPeriodId(), 1);
            if (socialResonanceDataK == null || socialResonanceDataK.size() == 0) {
                return Boolean.FALSE;
            }
        }
        if (game.getGameEn().equals(GameConstant.DLT)) {
            List<DltSocialResonanceData> dltSocialResonanceDatas = dltSocialResonanceDataDao
                    .getAllTypeResonanceCurrentPeriod(period.getPeriodId(), 0);
            if (dltSocialResonanceDatas == null || dltSocialResonanceDatas.size() == 0) {
                return Boolean.FALSE;
            }
            List<DltSocialResonanceData> dltSocialResonanceDataK = dltSocialResonanceDataDao
                    .getAllTypeResonanceCurrentPeriod(period.getPeriodId(), 1);
            if (dltSocialResonanceDataK == null || dltSocialResonanceDataK.size() == 0) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean livingBuildEncircleResonance(SocialEncircle socialEncircle) {
        try {
            //1.插入mission表 待过期处理
            SocialCodeMission socialCodeMission = new SocialCodeMission(socialEncircle.getEncircleCodeId() + "",
                    SocialCodeMission.MISSION_TYPE_ENCIRCLE, 0, DateUtil.getCurrentTimestamp(), GameCache.getGame
                    (socialEncircle.getGameId()).getGameEn());
            socialCodeMissionDao.insert(socialCodeMission);
            //2.
            updateSocialResonance(socialEncircle);
        } catch (Exception e) {
            log.error("livingBuildEncircleResonance error encircleId:" + socialEncircle.getEncircleCodeId(), e);
        }
        return true;
    }

    @Override
    public Boolean livingBuildKillResonance(SocialKillCode socialKillCode) {
        try {
            //1.插入mission表 待过期处理
            SocialCodeMission socialCodeMission = new SocialCodeMission(socialKillCode.getKillCodeId() + "",
                    SocialCodeMission.MISSION_TYPE_KILL, 0, DateUtil.getCurrentTimestamp(), GameCache.getGame
                    (socialKillCode.getGameId()).getGameEn());
            socialCodeMissionDao.insert(socialCodeMission);
            //2.更新
            updateSocialResonance(socialKillCode);
        } catch (Exception e) {
            log.error("livingBuildKillResonance error killCodeId:" + socialKillCode.getKillCodeId(), e);
        }
        return true;
    }

    private List<Map<String, Object>> getOnePeriodResonanceDate(Game game, Integer resonanceType, Long userId,
                                                                Integer isPermission) {
        // 围号和杀号数据
        List<Map<String, Object>> resonanceData = new ArrayList<>();

        //1.获取围号共振数据
        //1.1 根据类型找到围号的共振值
        Integer socialType = SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_ENCIRCLE_RED;
        Map<String, Integer> resonanceMap = getEncircleResonanceByGame(game.getGameId(), socialType, resonanceType);

        //1.2 转成展示形式
        Map<String, Object> socialEncircleData = getResonanceDataDistributeAndTrend(game, resonanceMap, socialType,
                isPermission);

        //2.获取杀号共振数据
        //2.1 根据类型找到杀号的共振值
        socialType = SocialEncircleKillConstant.SOCIAL_OPERATE_NUM_KILL_RED;
        Map<String, Integer> killResonanceMap = getEncircleResonanceByGame(game.getGameId(), socialType, resonanceType);

        //2.2 转成展示形式
        Map<String, Object> socialKillData = getResonanceDataDistributeAndTrend(game, killResonanceMap, socialType,
                isPermission);

        resonanceData.add(socialEncircleData);
        resonanceData.add(socialKillData);
        return resonanceData;
    }

    private Map<String, Object> getResonanceDataDistributeAndTrend(Game game, Map<String, Integer> resonanceMap,
                                                                   Integer socialType, Integer isPermission) {
        Map<String, Object> distributeTrendMap = new HashMap<>();
        boolean noData = false;
        if (resonanceMap == null || resonanceMap.isEmpty()) {
            noData = true;
        }
        String socialName = SocialEncircleKillCodeUtil.getSocialNameBySocialType(socialType);
        //1.排序前六个号码 (特殊处理给他们颜色)
        Map<String, Integer> resonanceSortedMap = sortByValue(resonanceMap);
        List<String> bestSix = new ArrayList<>();  //前6的号码
        if (!noData) {
            for (Map.Entry entry : resonanceSortedMap.entrySet()) {
                bestSix.add(entry.getKey().toString());
            }
            Collections.reverse(bestSix);
            bestSix = bestSix.subList(0, 6);
            resonanceSortedMap.putAll(resonanceMap);//todo 不知道是干嘛的
        }

        //2.获取共振--分布数据
        Map<String, Object> distribute = new HashMap<>();
        List<Object> distributes = getResonanceDataDistributes(game, bestSix, resonanceMap, isPermission);

        distribute.put("name", socialName + "共振分布");
        distribute.put("data", distributes);
        /*社区围号共振情况，号码共振值大的属于热门围号。*/
        distribute.put("adInfo", "社区" + socialName + "共振情况，号码共振值大的属于热门" + socialName);

        distributeTrendMap.put("distribute", distribute);

        //3.获取共振--热门围号及走势分析数据
        Map<String, Object> trend = new HashMap<>();
        List<Object> trends = getResonanceDataTrends(game, bestSix, isPermission);

        trend.put("num", bestSix);
        trend.put("data", trends);
        trend.put("name", "热门" + socialName + "及走势分析");

        distributeTrendMap.put("trend", trend);
        return distributeTrendMap;
    }

    private List<Object> getResonanceDataTrends(Game game, List<String> bestSix, Integer isPermission) {
        if (isPermission.equals(CommonConstant.PROGRAM_IS_PAY_NO) || bestSix == null || bestSix.isEmpty()) {
            return null;
        }
        List<Object> dataList = new ArrayList<>();
        //1.获取冷热号的出现次数
        Map<String, Object> coldHotNums = trendService.getHotColdSelectNum(game);
        List<ColdHotNumVo> coldHotNumVoList = (List<ColdHotNumVo>) coldHotNums.get("dataList");

        GamePeriod lastPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());

        //2.遍历热门号并包装
        for (String betsNumber : bestSix) {
            Map<String, Object> dataItem = new HashMap<>();
            dataItem.put("num", betsNumber);
            dataItem.put("color", SocialEncircleKillCodeUtil.getSocialResonanceBestNumColor(bestSix.indexOf
                    (betsNumber)));
            StringBuffer hotCold = new StringBuffer();
            StringBuffer omit = new StringBuffer();
            int j = 1;

            List<Integer> averageOmitArr = null;
            String charName = "RED";
            if (game.getGameEn().equals(GameConstant.DLT)) {
                charName = "FRONT";
            }


            for (ColdHotNumVo coldHotNumVo : coldHotNumVoList) {
                List<ColdHotAttrVo> redColdHot = coldHotNumVo.getRedColdHotAttrVoList();
                hotCold.append("近").append(coldHotNumVo.getPeriodNum()).append(":");
                omit.append("近").append(coldHotNumVo.getPeriodNum()).append(":");
                for (ColdHotAttrVo coldHotAttrVo : redColdHot) {
                    if (coldHotAttrVo.getBallNum().equals(betsNumber)) {
                        String chartKey = RedisConstant.getCurrentChartKey(game.getGameId(), lastPeriod.getPeriodId()
                                , charName, Integer.valueOf(coldHotNumVo.getPeriodNum().substring(0, coldHotNumVo
                                        .getPeriodNum().length() - 1)));
                        HashMap chartData = redisService.kryoGet(chartKey, HashMap.class);
                        if (chartData != null) {
                            List<Map> statistic = (List<Map>) chartData.get("statisticsList");
                            for (Map temp : statistic) {
                                if ("平均遗漏".equals(temp.get("periodName").toString())) {
                                    averageOmitArr = (List<Integer>) temp.get("omitNum");
                                }
                            }
                        }
                        /*出号3次，出号频率0.22，属<font color='#ff5050'>冷号</font>*/
                        hotCold.append("出现").append(coldHotAttrVo.getColdHeatVal()).append("次,").append("出现频率")
                                .append(coldHotAttrVo.getColdHeatPercent()).append(",属").append(getColdHotColor
                                (coldHotAttrVo.getColdHeatName()));
                        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
                        //String avgOmit = df.format((float) coldHotAttrVo.getOmitNum() / Integer.valueOf(coldHotNumVo
                        //        .getPeriodNum().substring(0, 2)));//返回的是String类型

                        Integer bestNumInt = Integer.valueOf(betsNumber);
                        String outPecent = CommonUtil.divide(coldHotAttrVo.getOmitNum() + "", averageOmitArr.get
                                (bestNumInt - 1) + "", 2); //
                        // 欲出几率 本期遗漏除以平均遗漏

                        omit.append("当前遗漏").append(coldHotAttrVo.getOmitNum()).append("次,").append("平均遗漏")
                                .append(averageOmitArr.get(bestNumInt - 1)).append(",欲出几率").append(outPecent);
                        if (j < coldHotNumVoList.size()) {
                            hotCold.append("<br>");
                            omit.append("<br>");
                        }
                    }
                }
                j++;
            }
            dataItem.put("hotCold", hotCold.toString());
            dataItem.put("omit", omit.toString());
            dataList.add(dataItem);
        }
        return dataList;
    }

    private String getColdHotColor(String name) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("冷号", "<font color=\"#4596FF\">冷号</font>");
        resultMap.put("温号", "<font color=\"#FFA850\">温号</font>");
        resultMap.put("热号", "<font color=\"#FF5050\">热号</font>");
        return resultMap.get(name);
    }

    private List<Object> getResonanceDataDistributes(Game game, List<String> bestSix, Map<String, Integer> resonanceMap,
                                                     Integer isPermission) {
        List<Object> dataList = new ArrayList<>();

        if (isPermission.equals(CommonConstant.PROGRAM_IS_PAY_NO) || resonanceMap == null || resonanceMap.isEmpty()) {
            return null;
        }
        AbstractGame abstractGame = GameFactory.getInstance().getGameBean(game.getGameEn());

        for (String num : abstractGame.getAllRedNums()) {
            Map<String, Object> dataItem = new HashMap<>();
            dataItem.put("num", num);
            dataItem.put("resonance", resonanceMap.get(num));
            dataItem.put("numColor", "#FF5050");
            dataItem.put("bgColor", "#FFFFFF");
            dataItem.put("isHollow", 1);
            if (bestSix.contains(num)) {
                dataItem.put("isHollow", 0);
                dataItem.put("numColor", "#FFFFFF");
                String bgColor = SocialEncircleKillCodeUtil.getSocialResonanceBestNumColor(bestSix.indexOf(num));
                dataItem.put("bgColor", bgColor);
            }
            dataList.add(dataItem);
        }
        return dataList;
    }

    private List<Map<String, Object>> getAllResonanceFilterCondition(String gameEn, Integer resonanceType) {
        List<Map<String, Object>> filterCondition = new ArrayList<>();
        Map<String, Object> filterConditionA = new HashMap<>();
        filterConditionA.put("name", "全部");
        filterConditionA.put("type", "3");
        filterConditionA.put("isDefault", "0");
        if (resonanceType == 3) {
            filterConditionA.put("isDefault", "1");
        }
        filterCondition.add(filterConditionA);
        Map<String, Object> filterConditionB = new HashMap<>();
        filterConditionB.put("name", "近24小时");
        filterConditionB.put("type", "2");
        filterConditionB.put("isDefault", "0");
        if (resonanceType == 2) {
            filterConditionA.put("isDefault", "1");
        }
        filterCondition.add(filterConditionB);
        Map<String, Object> filterConditionC = new HashMap<>();
        filterConditionC.put("name", "近12小时");
        filterConditionC.put("type", "1");
        filterConditionC.put("isDefault", "0");
        if (resonanceType == 1) {
            filterConditionA.put("isDefault", "1");
        }
        filterCondition.add(filterConditionC);
        Map<String, Object> filterConditionD = new HashMap<>();
        filterConditionD.put("name", "近6小时");
        filterConditionD.put("type", "0");
        filterConditionD.put("isDefault", "0");
        if (resonanceType == 0) {
            filterConditionA.put("isDefault", "1");
        }
        filterCondition.add(filterConditionD);
        return filterCondition;
    }

    private Map<String, Object> getOnePeriodBigDate(List<SocialBigDataVo> socialBigDataVos, boolean bigDataPermission) {
        Map<String, Object> baseData = new HashMap<>();
        if (socialBigDataVos != null && socialBigDataVos.size() > 0) {
            String dateNum = socialBigDataVos.get(0).getHotEncircleData();
            if (!bigDataPermission) {
                String[] arr = dateNum.split(CommonConstant.COMMA_SPLIT_STR);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arr.length; i++) {
                    sb.append(CommonConstant.COMMON_QUESTION_STR);
                    if (i < arr.length - 1) {
                        sb.append(CommonConstant.COMMA_SPLIT_STR);
                    }
                }
                dateNum = sb.toString();
            }
            dateNum = dateNum.replaceAll(CommonConstant.COMMA_SPLIT_STR, CommonConstant.SPACE_SPLIT_STR);
            String bigDataAd = socialBigDataVos.get(0).getStatisticDate().split(CommonConstant.COMMON_DASH_STR)[0] + "月"
                    + socialBigDataVos.get(0).getStatisticDate().split(CommonConstant.COMMON_DASH_STR)[1] + "日 " + " " +
                    socialBigDataVos.get(0).getStatisticHour() + "最热围号";
            baseData.put("name", "基础数据");
            baseData.put("title", "会员免费");
            baseData.put("titleBg", "http://sportsimg.mojieai.com/social_big_data_vip_free.png");
            baseData.put("adInfo", "6小时更新一次");
            baseData.put("bigDataAd", bigDataAd);
            baseData.put("dateNum", dateNum);
        }
        return baseData;
    }

    private Map<String, Integer> getEncircleResonanceByGame(Long gameId, Integer socialType, Integer resonanceType) {
        Map<String, Integer> resultMap = new TreeMap<>();
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(gameId);
        GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, lastOpenPeriod.getPeriodId());
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.SSQ)) {
            SsqSocialResonanceData ssqSocialResonanceData = ssqSocialResonanceDataDao.getTypeResonanceCurrentPeriod
                    (nextPeriod.getPeriodId(), socialType, resonanceType);
            resultMap = getNumResonanceByReflex(ssqSocialResonanceData);
        }
        if (GameCache.getGame(gameId).getGameEn().equals(GameConstant.DLT)) {
            DltSocialResonanceData dltSocialResonanceData = dltSocialResonanceDataDao.getTypeResonanceCurrentPeriod
                    (nextPeriod.getPeriodId(), socialType, resonanceType);
            resultMap = getNumResonanceByReflex(dltSocialResonanceData);
        }
        return resultMap;
    }

    private Map<String, Integer> getNumResonanceByReflex(Object socialResonanceData) {
        Map<String, Integer> resultMap = new TreeMap<>();
        if (socialResonanceData == null) {
            return resultMap;
        }

        Field[] fields = socialResonanceData.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().contains("number")) {
                try {
                    String name = field.getName();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    Method get = socialResonanceData.getClass().getMethod("get" + name);
                    Integer value = (Integer) get.invoke(socialResonanceData);//调用getter方法获取属性值
                    resultMap.put(field.getName().substring(field.getName().length() - 2, field.getName()
                            .length()), value);
                } catch (Exception e) {
                    throw new BusinessException("field error", e);
                }
            }
        }
        return resultMap;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return map;
        }
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }
}
