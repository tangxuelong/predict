package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.GamePeriodDao;
import com.mojieai.predict.dao.PredictRedBallDao;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.bo.Task;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.PredictRedBall;
import com.mojieai.predict.enums.RedisPubEnum;
import com.mojieai.predict.enums.SsqGameEnum;
import com.mojieai.predict.enums.trend.TrendPeriodEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.AwardInfoRedisService;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.redis.refresh.handler.pub.RedisPublishHandler;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.TrendUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequestMapping("/admin")
@Controller
public class RefreshController extends BaseController {

    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private RedisPublishHandler redisPublishHandler;
    @Autowired
    private AwardInfoRedisService awardInfoRedisService;
    @Autowired
    private PredictRedBallService predictRedBallService;
    @Autowired
    private PredictRedBallDao predictRedBallDao;
    @Autowired
    private TrendChartService trendChartService;
    @Autowired
    private GamePeriodDao gamePeriodDao;
    @Autowired
    private DingTalkRobotService dingTalkRobotService;
    @Autowired
    private PredictNumService predictNumService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private UserSportSocialRecommendService userSportSocialRecommendService;
    @Autowired
    private StarUserMatchService starUserMatchService;

    @RequestMapping("/refresh")
    @ResponseBody
    public Object refresh(@RequestParam String channelKey, @RequestParam Integer type) {
        if (StringUtils.isBlank(channelKey) || type < 0 || type > 1) {
            return buildErrJson("参数格式不正确");
        }
        channelKey = channelKey.toUpperCase();
        String channel = RedisPubEnum.getChannel(channelKey, type);
        if (channel == null) {
            return buildErrJson("不存在的订阅频道:" + channelKey);
        }
        String message = "refresh channel:" + channel;
        long result = redisPublishHandler.publish(channel, message);
        if (result == ResultConstant.ERROR) {
            message = message + " fail.result=" + result;
            return buildErrJson(message);
        }
        return buildSuccJson();
    }

    //刷新大盘彩awardInfo in redis
    @RequestMapping("/refresh/awardInfo")
    @ResponseBody
    public Object refreshAwardInfo(@RequestParam Long gameId) {
        awardInfoRedisService.refreshAwardInfo(gameId);
        return buildSuccJson();
    }

    /*手动生成杀3码*/
    @RequestMapping("/manualGenerateKillCode")
    @ResponseBody
    public Object manualGenerateKillCode(@RequestParam long gameId, String beginPeriodId, String endPeriodId) {
        List<String> list = new ArrayList<>(SsqGameEnum.SSQ_RED_NUMBERS);

        int begin = Integer.valueOf(beginPeriodId);
        int end = Integer.valueOf(endPeriodId);
        for (int i = begin; i <= end; i++) {
            Collections.shuffle(list);
            Task task1 = new Task(Long.valueOf(gameId), i + "");
            PredictRedBall predictRedBall = predictRedBallDao.getPredictRedBall(task1.getGameId(), task1.getPeriodId(),
                    PredictConstant.PREDICT_RED_BALL_STR_TYPE_TWENTY);

            String redTwenty = "";
            if (predictRedBall != null) {
                redTwenty = predictRedBall.getNumStr();
            } else {
                redTwenty = list.get(0);
                for (int tempRed = 1; tempRed < 20; tempRed++) {
                    redTwenty = redTwenty + CommonConstant.SPACE_SPLIT_STR + list.get(tempRed);
                }
                redTwenty = TrendUtil.orderNum(redTwenty);
                int res = predictRedBallDao.insert(new PredictRedBall(Long.valueOf(gameId), task1.getPeriodId(),
                        PredictConstant
                                .PREDICT_RED_BALL_STR_TYPE_TWENTY, redTwenty));
                if (res <= 0) break;
            }

            String killNum = "";
            int count = 0;
            for (String temp : list) {
                if (count == 3) {
                    break;
                }
                if (!redTwenty.contains(temp)) {
                    killNum = killNum + CommonConstant.SPACE_SPLIT_STR + temp;
                    count++;
                }
            }
            killNum = TrendUtil.orderNum(killNum.trim());
            predictRedBallService.saveKillThreeCode(task1.getGameId(), task1.getPeriodId(), killNum);
//
        }
        return "";
    }

    /* 手动刷新预测历史*/
    @RequestMapping("/refreshPredictHistory")
    @ResponseBody
    public Object refreshPredictHistory(Long gameId) {

        Boolean res = predictNumService.rebuild100History(gameId);
        String key = RedisConstant.getPeriodDetailKey(gameId, RedisConstant.LAST_100_PREDICT_HISTORY);
        redisService.del(key);
        return buildSuccJson(res);
    }

    /* 手动刷新走势图*/
    @RequestMapping("/manualRefreshTrendRedis")
    public Object manualRefreshTrendRedis(@RequestParam String gameEn) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("彩种不存在");
        }
        GamePeriod lastOpenPeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());
        long begin = System.currentTimeMillis();
        String endPeriodId = lastOpenPeriod.getPeriodId();
        //1.生成TrendPeriodEnum的各种基本一级走势图
        for (TrendPeriodEnum tpe : TrendPeriodEnum.values()) {
            int periodNum = tpe.getNum();
            //1.1获取开始期次和结束期次
            String beginPeriod = gamePeriodDao.getIntervalPeriod(game.getGameId(), lastOpenPeriod.getPeriodId(),
                    periodNum);
            if (StringUtils.isBlank(beginPeriod)) {
                return buildErrJson("beginPeriod is null");
            }
            log.info(periodNum + "Profile begin generateChart use time");
            //1.2计算基本走势
            trendChartService.generate100ChartData(game.getGameId(), lastOpenPeriod.getPeriodId(), Integer.valueOf
                    (beginPeriod), Integer.valueOf(lastOpenPeriod.getPeriodId()), periodNum);
        }
        log.info("一级走势图刷新成功 ");
        //2.依据一级走势图计算连号
        for (TrendPeriodEnum tpe : TrendPeriodEnum.values()) {
            int periodNum = tpe.getNum();
            //2.1获取开始期次
            String beginPeriod = gamePeriodDao.getIntervalPeriod(game.getGameId(), lastOpenPeriod.getPeriodId(),
                    periodNum);
            //2.2计算连号
            Map temp = trendChartService.generate100ChartContinueToRedis(game.getGameId(), beginPeriod, endPeriodId,
                    tpe.getNum(), endPeriodId);
            log.info(JSONObject.toJSONString(temp));
        }
        log.info("二级走势图刷新成功 ");

        //3.依据连号数据组合三级redis走势
        for (TrendPeriodEnum te : TrendPeriodEnum.values()) {
            trendChartService.generate3LevelChartData(game.getGameId(), lastOpenPeriod.getPeriodId(), te.getNum());
        }
        log.info("三级走势图刷新成功 ");
        long end = System.currentTimeMillis();

        String markdown = "#### 缓存刷新服务 \n" + "> " + gameEn + endPeriodId + " 期所有走势图刷新成功了。\n 总计耗时" + (end - begin)
                / 1000 + "秒";
        List<String> at = new ArrayList<>();
        at.add("18301552530");
        dingTalkRobotService.sendMassageToAll("缓存刷新服务", markdown, at);

        return "";
    }

    @RequestMapping("/refreshSportsRank")
    @ResponseBody
    public Object refreshSportsRank() {
        sportSocialService.updateUserRankRedis();
        return buildSuccJson();
    }

    @RequestMapping("/refreshSportRecommend")
    @ResponseBody
    public Object refreshSportRecommend() {
        userSportSocialRecommendService.rebuildSportRecommendList();
        return buildSuccJson();
    }

    @RequestMapping("/refresh_user_star")
    @ResponseBody
    public Object refreshUserStar() {
        starUserMatchService.reSaveIndexRecommend2StarUser();
        return buildSuccJson();
    }
}
