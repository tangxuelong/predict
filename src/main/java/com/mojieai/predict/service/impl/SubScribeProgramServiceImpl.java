package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.dao.SubscribeProgramDao;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.enums.predict.PickNumEnum;
import com.mojieai.predict.enums.predict.PickNumPredict;
import com.mojieai.predict.enums.predict.SsqPickNumEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.SubScribeProgramService;
import com.mojieai.predict.service.UserSubscribeInfoService;
import com.mojieai.predict.service.predict.AbstractPredictView;
import com.mojieai.predict.service.predict.PredictFactory;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.PredictUtil;
import com.mojieai.predict.util.ProgramUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SubScribeProgramServiceImpl implements SubScribeProgramService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;
    @Autowired
    private UserSubscribeInfoService userSubscribeInfoService;
    @Autowired
    private SubscribeProgramDao subscribeProgramDao;
    @Autowired
    private PredictRedBallDao predictRedBallDao;

    @Override
    public Map<String, Object> getSubScribeProgram(Long userId, long gameId, Integer programType) {
        Map<String, Object> res = new HashMap<>();
        Integer firstBuy = userSubscribeInfoService.checkUserFirstBuyStatus(gameId, userId, programType);
        Integer periodStatus = 0;
        List<Map<String, Object>> predictNums = new ArrayList<>();
        programType = PredictUtil.getProgramTypeByFirstBuy(firstBuy, programType);
        String subscribeIndexKey = RedisConstant.getSubscribeIndexKey(gameId, programType);
        predictNums = redisService.kryoGet(subscribeIndexKey, ArrayList.class);
        if (predictNums == null) {
            predictNums = rebuildSubScribeIndex(gameId, programType, firstBuy);
        }

        predictNums = checkUserHasPurchase(gameId, userId, predictNums);

        predictNums.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Integer res = Integer.valueOf(o1.get("goodsType").toString()).compareTo(Integer.valueOf(o2.get
                        ("goodsType").toString()));
                if (res != 0) {
                    return res;
                }
                Integer status = Integer.valueOf(o2.get("status").toString()).compareTo(Integer.valueOf(o1.get("status")
                        .toString()));
                if (status != 0) {
                    return status;
                }
                return Integer.valueOf(o2.get("continueHit").toString()).compareTo(Integer.valueOf(o1.get("continueHit")
                        .toString()));
            }
        });

        res.put("firstBuy", firstBuy);
        res.put("periodStatus", periodStatus);
        res.put("predictNums", predictNums);
        res.put("numTitle", "基础态 冷态 热态 回补态");
        res.put("adInfo", "智慧采用4种全新杀号模型分别对100期、200期和500期开奖数据进行分析和预测，不同模型的预测结果会有冲突，请慎重参考");
        return res;
    }

    private List<Map<String, Object>> checkUserHasPurchase(long gameId, Long userId, List<Map<String, Object>>
            predictNums) {
        if (predictNums == null || predictNums.size() == 0) {
            return predictNums;
        }
        for (Map<String, Object> temp : predictNums) {
            Boolean status = userSubscribeInfoService.checkUserSubscribePredict(userId, Integer.valueOf(temp
                    .get("goodsId").toString()));
            if (status) {
                temp.put("status", 1);
                temp.put("priceMsg", "");
                temp.put("btnMsg", "订阅中");
            }

            if (!status && Integer.valueOf(temp.get("goodsType").toString()) == 1) {
                String num[] = temp.get("nums").toString().split(CommonConstant.SPACE_SPLIT_STR);
                StringBuilder numShow = new StringBuilder();
                for (int i = 0; i < num.length; i++) {
                    numShow.append(CommonConstant.COMMON_QUESTION_STR).append(CommonConstant.SPACE_SPLIT_STR);
                }
                temp.put("nums", numShow.toString().trim());
            }
        }
        return predictNums;
    }

    private List<Map<String, Object>> rebuildSubScribeIndex(long gameId, Integer programType, Integer firstBuy) {
        List<Map<String, Object>> res = new ArrayList<>();

        List<SubscribeProgram> subscribePrograms = subscribeProgramDao.getSubscribeProgramByProgramType(gameId,
                programType);
        if (subscribePrograms == null) {
            log.error("programType:" + programType + " 未查询到订阅信息请确认");
            return res;
        }

        for (SubscribeProgram program : subscribePrograms) {
            PickNumEnum pickNumEnum = PickNumEnum.getPickNumEnum(GameCache.getGame(gameId).getGameEn());
            if (pickNumEnum == null) {
                log.error("programType " + programType + " has no pickNumEnum");
                continue;
            }
            PickNumPredict spn = pickNumEnum.getGamePickNumEnum(program.getPredictType());
            PredictRedBall predictRedBall = predictRedBallDao.getLatestPredictRedBall(gameId, spn.getStrType());
            Map<String, Object> temp = new HashMap<>();

            String priceMsg = "";
            String btnMsg = "免费";
            if (program.getPayType().equals(PredictConstant.SUBSCRIBE_PROGRAM_PAY_TYPE_PAY)) {
                btnMsg = "订阅";
                priceMsg = "<font>" + CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(program.getAmount())
                        .toString()) + "</font>智慧币/<font>" + program.getSubscribeNum() + "</font>期";
                if (program.getProgramType().equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_RED_FIRST) || program
                        .getProgramType().equals(PredictConstant.PREDICT_STATE_PROGRAM_TYPE_BLUE_FIRST)) {
                    btnMsg = "首购免单";
                }
            }

            String front = "";
            String analysisNum = spn.getColdHotStateNum() + "期";
            if (spn.getColdHotStateNum() < 0) {
                analysisNum = "全期";
                front = "综合";
            }

            Map<String, Object> achieve = getProgramAchieve(program);
            temp.putAll(achieve);

            temp.put("goodsId", program.getProgramId());
            temp.put("name", spn.getPredictName());
            temp.put("goodsDesc", front + "分析<font color='#ff5050'>" + analysisNum + "</font>开奖数据");
            temp.put("goodsType", program.getPayType());
            temp.put("status", 0);
            temp.put("nums", predictRedBall.getNumStr());
            temp.put("priceMsg", priceMsg);
            temp.put("btnMsg", btnMsg);
            temp.put("predictType", program.getPredictType());
            res.add(temp);
        }

        //保存redis
        GamePeriod currentPeriod = PeriodRedis.getAwardCurrentPeriod(gameId);
        String subscribeIndexKey = RedisConstant.getSubscribeIndexKey(gameId, programType);
        int expireTime = TrendUtil.getExprieSecond(currentPeriod.getAwardTime(), 3600);
        redisService.kryoSetEx(subscribeIndexKey, expireTime, res);

        return res;
    }

    private Map<String, Object> getProgramAchieve(SubscribeProgram program) {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> achieve = new ArrayList<>();
        //1.近10期
        Integer continueHit = 0;
        if (program.getPayType().equals(PredictConstant.SUBSCRIBE_PROGRAM_PAY_TYPE_PAY)) {
            Game game = GameCache.getGame(program.getGameId());
            Map<String, String> predictNumbers = getPredictRedNum(game, program.getPredictType());
            Integer tempContinueHit = 0;
            Integer recentHit = 0;
            Integer maxCount = 100;
            Integer maxRecentCount = 10;
//            boolean continueHitFlag = true;
            if (predictNumbers != null && predictNumbers.size() != 0) {
                for (Map.Entry<String, String> temp : predictNumbers.entrySet()) {
                    if (maxCount <= 0) {
                        break;
                    } else if (maxCount == 100) {
                        maxCount--;
                        continue;
                    }
                    //2.1 近期连中情况
                    if (temp.getValue().contains(CommonConstant.COMMON_STAR_STR)) {
                        tempContinueHit = 0;
                    } else {
                        tempContinueHit++;
                        if (tempContinueHit > continueHit) {
                            continueHit = tempContinueHit;
                        }
                    }

                    //2.2 10期情况
                    if (!temp.getValue().contains(CommonConstant.COMMON_STAR_STR) && maxRecentCount > 0) {
                        recentHit++;
                    }
                    maxRecentCount--;
                    maxCount--;
                }
            }

            Map<String, Object> continueMap = new HashMap<>();
            continueMap.put("name", "<font color='#FF762B'>最高" + continueHit + "连中</font>");
            continueMap.put("bgUrl", "http://sportsimg.mojieai.com/subscribe_program_continue_hit_bg1.png");
            achieve.add(continueMap);

            Map<String, Object> allHit10 = new HashMap<>();
            allHit10.put("name", "<font color='#FF5050'>近10中" + recentHit + "</font>");
            allHit10.put("bgUrl", "http://sportsimg.mojieai.com/subscribe_program_10_get_10_bg.png");
            achieve.add(allHit10);

        }

        if (program.getVipDiscount() != null && program.getVipDiscount() != 0 && program.getVipDiscount() < 100) {
            Map<String, Object> vipMap = new HashMap<>();
            vipMap.put("name", "<font color='#FF8D00'>会员" + ProgramUtil.getProgramDiscountTxt(program.getVipDiscount
                    ()) + "折</font>");
            vipMap.put("bgUrl", "http://sportsimg.mojieai.com/subscribe_program_vip_discount_bg.png");
            achieve.add(vipMap);
        }

        if (program.getBuyType().equals(CommonConstant.PROGRAM_BUY_TYPE_COMPENSATE)) {
            Map<String, Object> buyTypeMap = new HashMap<>();
            buyTypeMap.put("name", "<font color='#5588F4'>不中包赔</font>");
            buyTypeMap.put("bgUrl", "http://sportsimg.mojieai.com/subscribe_program_compensate_bg.png");
            achieve.add(buyTypeMap);
        }
        res.put("achieve", achieve);
        res.put("continueHit", continueHit);
        return res;
    }

    private Map<String, String> getPredictRedNum(Game game, Integer predictType) {
        Map<String, String> res = new HashMap<>();
        String redisKey = RedisConstant.getPredictTypeKey(game.getGameEn(), predictType);
        res = redisService.kryoGet(redisKey, TreeMap.class);
        if (res == null) {
            res = PredictFactory.getInstance().getPredictView(game.getGameEn()).getPredictRedBallNum(predictType);
        }
        return res;
    }
}
