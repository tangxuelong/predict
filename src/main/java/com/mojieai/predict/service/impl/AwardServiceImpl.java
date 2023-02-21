package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ButtonOrderCache;
import com.mojieai.predict.cache.PeriodCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.AwardInfoDao;
import com.mojieai.predict.dao.ButtonOrderedDao;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.ButtonOrdered;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.ButtonOrderNewVo;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.AwardService;
import com.mojieai.predict.service.filter.Filter;
import com.mojieai.predict.service.filter.FilterFactory;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.thread.CalcAwardDetailTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

@Service
public class AwardServiceImpl implements AwardService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private AwardInfoDao awardInfoDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ButtonOrderedDao buttonOrderedDao;

    @Override
    public List<AwardDetail> calcAwardDetail(Long gameId, int periodNum, List<String> numberList) {
        //历史上所选号码列表的中奖信息,key为奖级，Object[]第一项为奖级中奖次数，第二项为中奖明细list，list有长度限制
        //当key为-1时，回传当前计算期次信息
        List<AwardDetail> resultList = new ArrayList<>();
        if (periodNum < 0) {
            log.info("[查看历史中奖]期次数小于0。periodNum:" + periodNum);
            return resultList;
        }
        List<GamePeriod> periods = PeriodCache.getPeriodMap().get(CommonUtil.mergeUnionKey(gameId, RedisConstant
                .LAST_ALL_OPEN_PERIOD));
        if (periods == null) {
            log.error("periods is null. " + CommonUtil.mergeUnionKey(gameId, periodNum));
            return resultList;
        }
        int awardSize = periods.size();
        if (periodNum == 0 || periodNum > awardSize) {
            periodNum = awardSize; //0或者超过开奖号码记录数，都认为是查询所有
        }
        AbstractGame ag = GameFactory.getInstance().getGameBean(gameId);
        CompletionService<AwardDetail> completionService = new ExecutorCompletionService<>(ThreadPool.getInstance()
                .getCalcExec());
        //某彩种的某一期
        for (int i = 0; i <= periodNum - 1; i++) {
            completionService.submit(new CalcAwardDetailTask(ag, periods.get(i), awardInfoDao, numberList));
        }
        for (int i = 0; i < periodNum - 1; i++) {
            Future<AwardDetail> f;
            try {
                f = completionService.take();//主线程总是能够拿到最先完成的任务的返回值，而不管它们加入线程池的顺序
                resultList.add(f.get());
            } catch (Throwable e) {
                log.error("calcAwardDetail error. " + CommonUtil.mergeUnionKey(gameId, periodNum, numberList));
            }
        }
        Collections.sort(resultList, (o1, o2) -> -o1.getPeriodId().compareTo(o2.getPeriodId()));

//        for (int i = 0; i <= periodNum - 1; i++) {
//            long t1 = System.currentTimeMillis();
//            int[] totalAward = null;
//            GamePeriod period = periods.get(i);
//            for (String lotteryNumber : numberList) {
//                int[] result = ag.analyseBidAwardLevels(lotteryNumber, period);
//                if (totalAward == null) {
//                    totalAward = result;
//                } else {
//                    for (int t = 0; t < totalAward.length; t++) {
//                        totalAward[t] = totalAward[t] + result[t];
//                    }
//                }
//            }
//            log.info("analyseBidAwardLevels cost " + (System.currentTimeMillis() - t1));
//            List<AwardInfo> awardInfos = AwardInfoCache.getAwardInfoList(period.getGameId(), period.getPeriodId());;
//            if (awardInfos == null) {
//                awardInfos = awardInfoDao.getAwardInfos(gameId, period.getPeriodId());
//            }
//            //包装奖级信息，有些期次对应奖级并没有奖金需要按照官方法则再次计算
//            BigDecimal bonus = ag.processAwardInfo(totalAward, period, awardInfos);
//            AwardDetail detail = new AwardDetail(period.getGameId(), period.getPeriodId(), bonus, totalAward);
//            resultList.add(detail);
//            log.info("loop cost " + (System.currentTimeMillis() - t1));
//        }
        return resultList;
    }

    @Override
    public List<ButtonOrderNewVo> getSortedTools(Game game, String versionCode, Integer clientType) {
        List<ButtonOrderNewVo> buttonOrderNewVos = new ArrayList<>();
        List<ButtonOrdered> buttonOrdereds = ButtonOrderCache.getButtonOrdered(game.getGameId(),
                ButtonOrderedConstant.BUTTON_TYPE_PROFIT_LOSS_TABLE);
        for (ButtonOrdered buttonOrdered : buttonOrdereds) {
            if (buttonOrdered.getVersionCode() != null && Integer.valueOf(versionCode) < buttonOrdered.getVersionCode
                    ()) {
                continue;
            }
            ButtonOrderNewVo buttonOrderNewVo = new ButtonOrderNewVo();
            buttonOrderNewVo.setToolName(buttonOrdered.getName());
            buttonOrderNewVo.setToolType(buttonOrdered.getUniqueStr());
            buttonOrderNewVo.setToolImgUrl(buttonOrdered.getImg());
            buttonOrderNewVo.setToolWeight(buttonOrdered.getWeight());
            buttonOrderNewVo.setToolIcon(buttonOrdered.getMemo());
            buttonOrderNewVo.setJumpUrl(buttonOrdered.getJumpUrl());
            buttonOrderNewVo.setJumpFlag(0);
            if (StringUtils.isNotBlank(buttonOrdered.getJumpUrl())) {
                buttonOrderNewVo.setJumpFlag(1);
            }

            if (buttonOrdered.getUniqueStr().equals("ykb") || buttonOrdered.getUniqueStr().equals("lqjz")) {
                buttonOrderNewVo.setCheckVip(Boolean.TRUE);
            }
            if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && buttonOrdered.getUniqueStr().equals("lqjz")) {
                continue;
            }
            if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS_1) && buttonOrdered.getUniqueStr().equals("lqjz")) {
                continue;
            }
            if (clientType.equals(1031) && buttonOrdered.getUniqueStr().equals("lqjz")) {
                continue;
            }
            buttonOrderNewVos.add(buttonOrderNewVo);
        }
        return buttonOrderNewVos;
    }

    @Override
    public Map<String, Object> getAwardTable(Game game, String numberCount) {
        Map<String, Object> resultMap = new HashMap<>();
        String redNumberCount = numberCount.split(CommonConstant.COMMON_COLON_STR)[0];
        String blueNumberCount = numberCount.split(CommonConstant.COMMON_COLON_STR)[1];

        // 计算注数 一共有多少注
        Filter filter = FilterFactory.getInstance().getFilter(game.getGameEn());
        int blueNum = Integer.valueOf(blueNumberCount);
        if (game.getGameEn().equals(GameConstant.DLT)) {
            blueNum = CommonUtil.combine(blueNum, 2);
        }
        Integer takeOff = filter.getBallsCombine(Integer.valueOf(redNumberCount), blueNum);
        resultMap.put("takeOff", takeOff + "注，" + (takeOff * 2) + "元");

        AbstractGame ag = GameFactory.getInstance().getGameBean(game.getGameEn());

        List<String> awardCondition = ag.getGameAwardCondition();

        List<Object> awardList = new ArrayList<>();
        for (String condition : awardCondition) {
            Map<String, Object> awardItem = new HashMap<>();
            // levelName
            awardItem.put("levelName", condition);


            Integer redBallCount = Integer.valueOf(condition.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .COMMON_ADD_STR)[0]);
            Integer blueBallCount = Integer.valueOf(condition.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant
                    .COMMON_ADD_STR)[1]);


            int[] awardLevels = ag.analyseBidAwardLevels(0, Integer.valueOf(redNumberCount), 0, Integer.valueOf
                    (blueNumberCount), 0, redBallCount, 0, blueBallCount, PeriodRedis.getCurrentPeriod(game.getGameId
                    ()).getPeriodId());

            List<Object> levelDetail = new ArrayList<>();
            String[] levelNameArr = new String[]{"一等奖", "二等奖", "三等奖", "四等奖", "五等奖", "六等奖"};

            String levelAmountStr = "";
            Integer levelAmount = 0;
            Boolean isBigBonus = Boolean.FALSE;
            for (int i = 0; i < awardLevels.length; i++) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("levelName", levelNameArr[i]);
                detail.put("bidNum", awardLevels[i] + "注");
                levelDetail.add(detail);
                if (i == 0 && awardLevels[i] > 0) {
                    levelAmountStr += awardLevels[i] + "A+";
                    isBigBonus = Boolean.TRUE;
                }
                if (i == 1 && awardLevels[i] > 0) {
                    levelAmountStr += awardLevels[i] + "B+";
                    isBigBonus = Boolean.TRUE;
                }

                if (i != 0 && i != 1 && awardLevels[i] > 0) {
                    if (i == 2 && awardLevels[i] > 0 && game.getGameEn().equals(GameConstant.DLT)) {
                        levelAmountStr += awardLevels[i] + "C+";
                        isBigBonus = Boolean.TRUE;
                    } else {
                        List<AwardInfo> awardInfos = ag.getAwardAmountList();
                        levelAmount += awardLevels[i] * awardInfos.get(i).getBonus().intValue();
                    }
                }
            }
            if (levelAmount > 0) {
                levelAmountStr += levelAmount + "元";
            }
            awardItem.put("levelDetail", levelDetail);
            if (levelAmountStr.endsWith(CommonConstant.COMMON_ADD_STR)) {
                levelAmountStr = levelAmountStr.substring(0, levelAmountStr.length() - 1);
            }
            awardItem.put("levelAmount", levelAmountStr);
            if (isBigBonus) {
                awardItem.put("levelDesc", "<font color=\"#FF5050\">盈</font>");
            } else {
                if ((levelAmount - takeOff * 2) >= 0) {
                    awardItem.put("levelDesc", "<font color=\"#FF5050\">盈" + (levelAmount - takeOff * 2) + "元</font>");
                } else {
                    awardItem.put("levelDesc", "<font color=\"#1FBF43\">亏" + (takeOff * 2 - levelAmount) + "元</font>");
                }
            }
            awardList.add(awardItem);
        }

        resultMap.put("awardList", awardList);

        resultMap.put("awardDesc", ag.getAwardDesc());

        resultMap.put("awardCondition", ag.awardCondition());

        return resultMap;
    }
}

