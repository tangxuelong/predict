package com.mojieai.predict.controller;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.dao.IniDao;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.PeriodRedisService;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.MatchInfoService;
import com.mojieai.predict.service.PushService;
import com.mojieai.predict.service.SocialEncircleCodeService;
import com.mojieai.predict.service.SocialService;
import com.mojieai.predict.service.predict.PredictFactory;
import com.mojieai.predict.service.predict.PredictInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Singal
 */
@RequestMapping("/cron")
@Controller
public class CronController extends BaseController {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private RedisService redisService;
    @Autowired
    private IniDao iniDao;
    @Autowired
    private IniCache iniCache;
    @Autowired
    private PeriodRedisService periodRedisService;
    @Autowired
    private PushService pushService;
    @Autowired
    private SocialEncircleCodeService socialEncircleCodeService;
    @Autowired
    private SocialService socialService;
    @Autowired
    private MatchInfoService matchInfoService;

    /*
     * refresh and rebuild 一级缓存和二级缓存
     * */
    @RequestMapping("/redis")
    @ResponseBody
    public Object redis(@RequestParam String action, @RequestParam Long gameId) throws
            Exception {
        if (StringUtils.isBlank(action) || null == gameId) {
            return buildErrJson("参数有误");
        }
        switch (action) {
            // 构建一级缓存
            case "refreshTimeline":
                periodRedisService.refreshTimeline(gameId);
                break;
            // 构建2级缓存
            case "refreshPeriodInfo":
                periodRedisService.refreshPeriodInfo(gameId);
                break;
            // 刷新超过3秒的缓存period
            case "refreshExpirePeriodInfo":
                periodRedisService.refreshExpirePeriodInfo(gameId);
                break;
            // 重构1级缓存
            case "rebuildTimeline":
                periodRedisService.rebuildTimeline(gameId);
                break;
            // 重构2级缓存
            case "rebuildPeriodInfo":
                periodRedisService.rebuildPeriodInfo(gameId);
                break;
            case "rebuildWiningNumberPush":
                pushService.rebuildClientIdList();
                break;
            case "rebuildRedKillThree":
                Game game = GameCache.getGame(gameId);
                PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(game.getGameEn());
                predictInfo.rebuildRedKillThree(gameId);
                break;
            case "rebuildSocialKillNumList":
                socialEncircleCodeService.rebuildKillNumListRedis(gameId, 29, "");
                break;
            case "redistributeSSQPredictNums":
                GamePeriod period = PeriodRedis.getLastOpenPeriodByGameId(GameCache.getGame(GameConstant.SSQ)
                        .getGameId());
                socialService.setFivePredictNums(period.getGameId(), period.getPeriodId());
                socialService.distributeKillPredictNums(period.getGameId(), period.getPeriodId());
                socialService.distributeEnCirclePredictNums(period.getGameId(), period.getPeriodId());
                break;
            case "rebuildPeriodHotEncircle":
                GamePeriod currentPeriod = PeriodRedis.getCurrentPeriod(gameId);
                socialEncircleCodeService.rebuildPeriodHotEncircle(gameId, currentPeriod.getPeriodId());
                break;
            case "rebuildTagMatchListTimeLine":
                matchInfoService.rebuildTagMatchListTimeLine();
                break;
            case "buildNewMatchTagTimeLine":
                matchInfoService.buildNewMatchTagTimeLine(gameId.intValue());
                break;
        }
        return buildSuccJson();
    }


}
