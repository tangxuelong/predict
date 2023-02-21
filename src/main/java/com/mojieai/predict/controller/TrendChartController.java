package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.PeriodScheduleDao;
import com.mojieai.predict.dao.PredictScheduleDao;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.AwardInfo;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.enums.PayChannelEnum;
import com.mojieai.predict.enums.PeriodEnum;
import com.mojieai.predict.enums.trend.TrendPeriodEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.cron.BaseScheduler;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by wangxiang on 2017/7/8.
 * 走势图
 */
@RequestMapping("/trendChart")
@Controller
public class TrendChartController extends BaseController {
    @Autowired
    private TrendChartService trendChartService;
    @Autowired
    private GetDataFrom500Service getDataFrom500Service;
    @Autowired
    private TrendService trendService;
    @Autowired
    private RedisService redisService;
    @Autowired
    protected BaseScheduler baseScheduler;
    @Autowired
    private AwardInfoService awardInfoService;
    @Autowired
    private CompatibleService compatibleService;
    @Autowired
    private PurchaseOrderStatisticService purchaseOrderStatisticService;
    @Autowired
    private SocialIntegralLogService socialIntegralLogService;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private BillService billService;

    /*客户端获取走势图数据*/
    @RequestMapping("/getChartInfo")
    @ResponseBody
    public Object getChartInfo(@RequestParam(required = false) String lotteryClass, @RequestParam(required = false)
            String gameEn, @RequestParam(required = false) String playType, @RequestParam String trendChartType, String
                                       periodNumber, HttpServletRequest request) {
        /* 走势图默认期次个数*/
        int periodNumberParam = TrendPeriodEnum.PERIOD100.getNum();

        /* 日常参数检查*/
        if ((StringUtils.isEmpty(lotteryClass) && StringUtils.isEmpty(gameEn)) || !StringUtils.isNumeric(playType)
                || !StringUtils.isNumeric(trendChartType)) {
            return buildErrJson(ResultConstant.PARAMS_ERROR);
        }

        /* 如果有传递个数，使用传递的期次个数*/
        if (StringUtils.isNotEmpty(periodNumber) && StringUtils.isNumeric(periodNumber)) {
            Set trendPeriodNums = new HashSet();
            for (TrendPeriodEnum validPeriodNumber : TrendPeriodEnum.values()) {
                trendPeriodNums.add(validPeriodNumber.getNum());
            }
            //periodNumber must in set
            if (trendPeriodNums.contains(Integer.valueOf(periodNumber))) {
                periodNumberParam = Integer.valueOf(periodNumber);
            }
        }
        String gameEnStr = StringUtils.isEmpty(lotteryClass) ? gameEn : lotteryClass;
        /* 获取走势图数据*/
        Map<String, Object> periodList = trendChartService.getTrendListData(gameEnStr, playType, trendChartType,
                periodNumberParam);

        /* 版本兼容 走势图连号*/
        compatibleService.continueNum(periodList, request);

        return buildSuccJson(periodList);
    }

    @RequestMapping("/getCurrentPeriod")
    @ResponseBody
    public Object getCurrentPeriod(@RequestParam String gameEn) {
        Map currentPeriodInfo = new HashMap();
        Game game = GameCache.getGame(gameEn);
        if (game != null) {
            GamePeriod gamePeriod = PeriodRedis.getCurrentPeriod(game.getGameId());
            if (gamePeriod != null) {
                currentPeriodInfo.put("currentPeriodId", gamePeriod.getPeriodId());
                int length = GameFactory.getInstance().getGameBean(game.getGameId()).getPeriodDateFormat().length();
                currentPeriodInfo.put("currentPeriodName", gamePeriod.getPeriodId().substring(length) + "期");
                currentPeriodInfo.put("awardTime", DateUtil.formatTime(gamePeriod.getAwardTime()));
                currentPeriodInfo.put("leftSecond", DateUtil.getDiffSeconds(DateUtil.getCurrentTimestamp(),
                        gamePeriod.getAwardTime()));
            }
        }
        return buildSuccJson(currentPeriodInfo);
    }

    /*冷热选号*/
    @RequestMapping("/getNumInHotColdTrend")
    @ResponseBody
    public Object getNumberInHotCold(@RequestParam String gameEn) {
        Game game = GameCache.getGame(gameEn);
        Map<String, Object> result = trendService.getHotColdSelectNum(game);
        return buildSuccJson(result);
    }

    @RequestMapping("/getBlueMatrixTrend")
    @ResponseBody
    public Object getBlueMatrixTrend(@RequestParam String token, @RequestParam String gameEn) {
        UserToken userToken = loginService.checkToken(token);
        if (userToken == null || userToken.getUserId() == null) {
            return buildErrJson("用户不存在");
        }
        if (!vipMemberService.checkUserIsVip(userToken.getUserId(), VipMemberConstant.VIP_MEMBER_TYPE_DIGIT)) {
            return buildErrJson("会员专享服务");
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("玩法不存在");
        }

        Map<String, Object> res = trendChartService.getBlueMatrixTrendChart(game.getGameId());
        return buildSuccJson(res);
    }

    /*抓取走势图数据*/
    @RequestMapping("/getDataFrom500")
    @ResponseBody
    public Object getDataFrom500(@RequestParam String grepType, String gameEn, String begin, String end) {
        int[] peroidArr = new int[2];
        long gameId = 2;
        if (gameEn.equals("dlt")) {
            peroidArr[0] = Integer.valueOf(begin);
            peroidArr[1] = Integer.valueOf(end);
            gameId = 1;

        } else {
            peroidArr[0] = Integer.valueOf(begin);
            peroidArr[1] = Integer.valueOf(end);
        }

        if (grepType.equals("1")) {
            List<GamePeriod> list = getDataFrom500Service.getDataFrom500(gameId, gameEn, peroidArr);
            return buildSuccJson(list);
        } else {
            List<AwardInfo> list = getDataFrom500Service.getAwordInfoFrom500(gameId, gameEn, peroidArr);
            return buildSuccJson(list);
        }
    }

    /*根据期次中奖信息，置换trendDB数据，内置给自己用*/
    @RequestMapping("/save2Db")
    @ResponseBody
    public Object manulSave2Db(@RequestParam String gameId, @RequestParam String periodId, String periodIdEnd) {

        for (int begin = Integer.valueOf(periodId); begin < Integer.valueOf(periodIdEnd); begin++) {
            String endNum = begin + "";
            int num = Integer.valueOf(endNum.substring(4));//2017001
            if (num > 200) {
                continue;
            }
            trendService.saveTrend2DbManul(Long.valueOf(gameId), begin + "");
        }
        return "success";
    }

//    /*手工生成走势图缓存*/
//    @RequestMapping("/manualProduceChart")
//    @ResponseBody
//    public Object manualProduceChartToRedis(String gameId, String beginPeriod,
//                                            String endPeriodId, String num, String currentPeriodId) {
//        if (StringUtils.isEmpty(gameId) || StringUtils.isEmpty(beginPeriod) || StringUtils.isEmpty(endPeriodId)
//                || StringUtils.isEmpty(endPeriodId) || StringUtils.isEmpty(num)) {
//            return buildErrJson(ResultConstant.PARAMS_ERROR);
//        }
//
//        GamePeriod currentPeriod = null;
//        if (StringUtils.isEmpty(currentPeriodId)) {
//            currentPeriod = PeriodRedis.getCurrentPeriod(Long.valueOf(gameId));
//        } else {
//            currentPeriod = new GamePeriod();
//            currentPeriod.setPeriodId(currentPeriodId);
//        }
//        if (currentPeriod != null) {
//            trendChartService.generate100ChartData(Long.valueOf(gameId), currentPeriod.getPeriodId(),
//                    Integer.valueOf(beginPeriod), Integer.valueOf(endPeriodId), Integer.valueOf(num));
//        }
//        return buildSuccJson("");
//    }
//
//    /*手工生成走势图缓存的连号*/
//    @RequestMapping("/manualProduceChartContinue")
//    @ResponseBody
//    public Object manualProduceChartContinueToRedis(String gameId, String beginPeriod, String endPeriodId, String
//            currentPeriodId) {
//        if (StringUtils.isBlank(gameId)) {
//            return buildErrJson(ResultConstant.PARAMS_ERROR);
//        }
//        Map res = new HashMap();
//        for (TrendPeriodEnum tpe : TrendPeriodEnum.values()) {
//            Map temp = trendChartService.generate100ChartContinueToRedis(Long.valueOf(gameId), beginPeriod,
//                    endPeriodId, tpe.getNum(), currentPeriodId);
//            res.put("trend" + tpe.getNum(), temp);
//        }
//        return buildSuccJson(res);
//    }

//    @RequestMapping("/manuanlProduceChartFc3d")
//    @ResponseBody
//    public Object manuanlProduceChartFc3d() {
//        Game game = GameCache.getGame(GameConstant.FC3D);
//        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
//        for (TrendPeriodEnum te : TrendPeriodEnum.values()) {
//            trendChartService.generate3LevelChartData(game.getGameId(), lastOpenPeriod.getPeriodId(), te.getNum());
//        }
//        return buildSuccJson("success");
//    }

    /**/
//    @RequestMapping("/manualDelPredictHistory")
//    @ResponseBody
//    public Object manualDelPredictHistory(@RequestParam long gameId, String delFlag) {

//        redisService.kryoZAddSet("testSortSet", 1L, "a1");
//        redisService.kryoZAddSet("testSortSet", 2L, "a2");
//        redisService.kryoZAddSet("testSortSet", 3L, "a3");
//        redisService.kryoZAddSet("testSortSet", 4L, "a4");
//
//        List<String> test1 = redisService.kryoZRevRangeByScoreGet("testSortSet", Long.MIN_VALUE,4L,
//                 0, 100, String.class);
//
//        redisService.kryoZRemRangeByRank("testSortSet", -1, -1);
//
//        List<String> test2 = redisService.kryoZRevRangeByScoreGet("testSortSet", 4L,
//                Long.MIN_VALUE, 0, 100, String.class);

//        String history100PredictWinkey = RedisConstant.getPredictNumsKey(gameId, "", RedisConstant
//                .HISTORY_100_PREDICT_WIN, null);
//
//        long time = System.currentTimeMillis();
//        List<AwardDetail> awardDetails = redisService.kryoZRevRangeByScoreGet(history100PredictWinkey, time + 1,
//                Long.MIN_VALUE, 0, 100, AwardDetail.class);

//        if (StringUtils.isNotBlank(delFlag) && delFlag.equals("yes")) {
//            redisService.kryoZRemRangeByRank(history100PredictWinkey, -1, -1);
//            List<AwardDetail> awardDetails1 = redisService.kryoZRevRangeByScoreGet(history100PredictWinkey, time + 1,
//                    Long.MIN_VALUE, 0, 100, AwardDetail.class);
//        }

//        return "";
//    }

    @RequestMapping("/testWithOutSign")
    @ResponseBody
    public Object test() {
//        Map res = PayChannelEnum.WX.getsignkey();
        billService.downloadJDBill();
        return buildSuccJson("");
    }

}
