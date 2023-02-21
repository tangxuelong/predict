package com.mojieai.predict.controller;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.BannerCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.entity.dto.GameWeightDto;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.AwardInfoVo;
import com.mojieai.predict.entity.vo.BannerVo;
import com.mojieai.predict.enums.GameEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.AwardService;
import com.mojieai.predict.service.PushService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.service.historyaward.HistoryAward;
import com.mojieai.predict.service.historyaward.HistoryAwardFactory;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by tangxuelong on 2017/6/30.
 */
@RequestMapping("/award")
@Controller
public class AwardController extends BaseController {
    @Autowired
    private RedisService redisService;
    @Autowired
    private PushService pushService;
    @Autowired
    private AwardService awardService;

    /* 首页接口*/
    @RequestMapping("/")
    @ResponseBody
    public Object index(HttpServletRequest request, @RequestAttribute(required = false) Integer clientType,
                        @RequestAttribute Integer versionCode) {//客户端首页
        Map<String, Object> resultData = new HashMap<>();
        /* banner*/
        List<BannerVo> bannerVos = BannerCache.getBannerVosV2(BannerCache.POSITION_TYPE_TOOL, versionCode, clientType);
        // // 活动hack 代码 只在苹果显示 根据clientID
        if (bannerVos != null && (null == clientType || clientType.equals(CommonConstant.CLIENT_TYPE_IOS))) {
            int count = 0;
            for (int i = 0; i < bannerVos.size(); i++) {
                if (bannerVos.get(i).getBannerId() == 60 || bannerVos.get(i).getBannerId() == 61 || bannerVos.get(i)
                        .getBannerId() == 62 || bannerVos.get(i).getBannerId() == 63 || bannerVos.get(i).getBannerId()
                        == 81 || bannerVos.get(i).getBannerId() == 82) {
                    bannerVos.remove(i - count);
                    count++;
                }

            }
            for (int i = 0; i < bannerVos.size(); i++) {
                if (bannerVos.get(i).getBannerId() == 82) {
                    bannerVos.remove(i);
                    break;
                }
            }
        }
        /* 彩种开奖号码列表*/
        List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
        /* 彩种权重排序*/
        gameIdList = sortGameIdByWeight(gameIdList);
        //List<GamePeriod> lastPeriods = PeriodRedis.getLastPeriodsByGameIds(gameIdList);
//        if (lastPeriods == null || lastPeriods.isEmpty()) {
//            return buildErrJson("系统异常");
//        }
//        List<AwardInfoVo> awardInfoVos = new ArrayList<>();
//        for (GamePeriod lastPeriod : lastPeriods) {
//            if (lastPeriod == null) {
//                continue;
//            }
//            AwardInfoVo awardInfoVo = GameFactory.getInstance().getGameBean(lastPeriod.getGameId()).calcWinningNumber
//                    (lastPeriod);
//            awardInfoVos.add(awardInfoVo);
//        }
//        resultData.put("awardList", awardInfoVos);

        /* 工具顺序排序*/
        for (Long gameId : gameIdList) {
            Map<String, Object> map = new HashMap<>();
            List<String[]> toolsH = GameEnum.getGameEnumById(gameId).getIndexHTools();
            map.put("indexToolsH", toolsH);
            List<String[]> toolsV = GameEnum.getGameEnumById(gameId).getIndexVTools();
            map.put("indexToolsV", toolsV);
            List<BannerVo> bannerVoList = new ArrayList<>();
            for (BannerVo bannerVo : bannerVos) {
                if (bannerVo.getGameId().equals(gameId)) {
                    bannerVoList.add(bannerVo);
                }
            }
            map.put("banners", bannerVoList);
            resultData.put(GameCache.getGame(gameId).getGameEn() + "Tools", map);

        }
        return buildSuccJson(resultData);
    }

    /* 彩种近100期开奖号码以及期次开奖详情*/
    @RequestMapping("/{gameEn}")
    @ResponseBody
    public Object queryAwards(@PathVariable String gameEn, @RequestParam(required = false) String clientId) {
        Long startTime = System.currentTimeMillis();
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("彩种不存在");
        }
        /* 近100期的期次*/
        List<GamePeriod> gamePeriods = PeriodRedis.getHistory100AwardPeriod(game.getGameId());
        if (gamePeriods == null || gamePeriods.isEmpty()) {
            return buildErrJson("数据不存在");
        }
        List<AwardInfoVo> awardInfoVos = new ArrayList<>();
        AbstractGame gameBean = GameFactory.getInstance().getGameBean(gameEn);
        for (GamePeriod gamePeriod : gamePeriods) {
            if (gamePeriod == null || StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
                continue;
            }
            AwardInfoVo awardInfoVo = GameFactory.getInstance().getGameBean(gamePeriod.getGameId()).calcWinningNumber
                    (gamePeriod);
            Map<String, Object> playTypeAndBonus = gameBean.getPlayTypeAndBonus(redisService, gamePeriod.getPeriodId(),
                    gameEn);
            if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                awardInfoVo.setPeriodSale((String) playTypeAndBonus.get("periodSale"));
                awardInfoVo.setPoolBonus((String) playTypeAndBonus.get("poolBonus"));
                awardInfoVo.setTestNum((String) playTypeAndBonus.get("testNum"));
            }
            awardInfoVo.setPlayTypeAndBonus((List<String>) playTypeAndBonus.get("awardInfoList"));
            awardInfoVos.add(awardInfoVo);
        }
        /* 添加文案*/
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(ActivityIniCache.getActivityIniValue(ActivityIniConstant.getAwardTitle(gameEn)));
        stringBuffer.append(CommonConstant.SPACE_SPLIT_STR);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("awardInfoVos", awardInfoVos);
        if (StringUtils.isNotBlank(clientId)) {
            if (!pushService.checkPush(clientId, game.getGameId())) {
                stringBuffer.append("，开奖通知已关闭");
            }
        }
        resultMap.put("showText", stringBuffer.toString());
//        log.info("开奖列表返回内容" + (resultMap.toString()));
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/awardDetail")
    @ResponseBody
    public Object getAwardDetail(@RequestParam String gameEn, @RequestParam String periodId) {
        String awardArea = "";
        String poolBonus = "";
        String periodSale = "";
        String testNum = "";
        String poolBonusStr = "";
        String periodSaleStr = "";
        List awardAreas = null;
        Map<String, Object> result = new HashMap<>();

        Game game = GameCache.getGame(gameEn);
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(game.getGameId(), periodId);

        HistoryAward historyAward = HistoryAwardFactory.getInstance().getHistoryAward(gameEn);
        String winningNum = gamePeriod.getWinningNumbers();
        if (gameEn.equals(GameConstant.FC3D)) {
            winningNum = winningNum.replaceAll(CommonConstant.SPACE_SPLIT_STR, CommonConstant.COMMON_COLON_STR);
        }
        List<Map<String, String>> numProperty = historyAward.getNumberProperties(winningNum, gameEn);
        AbstractGame gameBean = GameFactory.getInstance().getGameBean(gameEn);
        if (StringUtils.isNotBlank(gamePeriod.getRemark())) {
            Map remarkMap = JSONObject.parseObject(gamePeriod.getRemark(), HashMap.class);
            awardArea = (String) remarkMap.get("area");
            poolBonus = (String) remarkMap.get("pool");
            periodSale = (String) remarkMap.get("sale");
            testNum = (String) remarkMap.get("testNum");
            if (StringUtils.isNotBlank(awardArea)) {
                awardArea = awardArea.replaceAll(CommonConstant.COMMA_SPLIT_STR, CommonConstant
                        .SPACE_SPLIT_STR + CommonConstant.SPACE_SPLIT_STR + CommonConstant.SPACE_SPLIT_STR);
                awardAreas = gameBean.splitWinArea(remarkMap.get("area").toString());
            }
        }

        Map<String, Object> playTypeAndBonus = gameBean.getPlayTypeAndBonus(redisService, gamePeriod.getPeriodId(),
                gameEn);
        List<String> playTypeAndBonusStr = (List<String>) playTypeAndBonus.get("awardInfoList");
        List<String> playTypeAndBonusStrMore = null;
        if (playTypeAndBonus.containsKey("awardInfoListMore")) {
            playTypeAndBonusStrMore = (List<String>) playTypeAndBonus.get("awardInfoListMore");
        }
        if (poolBonus.contains("亿")) {
            poolBonusStr = poolBonus;
        }
        if (periodSale.contains("亿")) {
            periodSaleStr = periodSale;
        }
        if (periodSale.contains("元")) {
            periodSale = periodSale.replace("元", "");
        }
        if (poolBonus.contains("元")) {
            poolBonus = poolBonus.replace("元", "");
        }
        poolBonusStr = poolBonus;
        periodSaleStr = periodSale;
        if (StringUtils.isNotBlank(poolBonus) && !poolBonus.contains("亿") && Double.parseDouble(poolBonus.trim()) >
                Double.parseDouble(String.valueOf(10000 * 10000))) {
            BigDecimal b = new BigDecimal(poolBonus);
            BigDecimal one = new BigDecimal((10000 * 10000));
            poolBonusStr = String.valueOf(b.divide(one, 2, BigDecimal.ROUND_HALF_UP).doubleValue()) + "亿";
        }

        if (StringUtils.isNotBlank(periodSale) && !periodSale.contains("亿") && Double.parseDouble(periodSale.trim())
                > Double.parseDouble(String.valueOf(10000 * 10000))) {
            BigDecimal b = new BigDecimal(periodSale);
            BigDecimal one = new BigDecimal((10000 * 10000));
            periodSaleStr = String.valueOf(b.divide(one, 2, BigDecimal.ROUND_HALF_UP).doubleValue()) + "亿";
        }

        result.put("winningNumbers", gamePeriod.getWinningNumbers());
        result.put("awardArea", awardArea);
        result.put("awardAreas", awardAreas);
        result.put("testNum", testNum);
        result.put("periodSale", periodSale);
        result.put("periodSaleStr", periodSaleStr);
        result.put("poolBonus", poolBonus);
        result.put("poolBonusStr", poolBonusStr);
        result.put("periodId", periodId);
        result.put("gameName", game.getGameName());
        result.put("playTypeAndBonus", playTypeAndBonusStr);
        result.put("playTypeAndBonusStrMore", playTypeAndBonusStrMore);
        result.put("numberProperties", numProperty);
        result.put("awardTime", DateUtil.getCommonGameAwardTime(gamePeriod.getAwardTime()));
        return buildSuccJson(result);
    }

    private List<Long> sortGameIdByWeight(List<Long> gameIdList) {
        String indexGameWeightStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant.INDEX_GAME_WEIGHT);
        Map<String, Object> gameWeightMap = (Map<String, Object>) JSONObject.parse(indexGameWeightStr);
        List<Object> gameWeightList = (List<Object>) gameWeightMap.get("gameWeightList");
        List<GameWeightDto> gameWeightDtos = new ArrayList<>();
        for (Object obj : gameWeightList) {
            Map<String, Object> map = (Map<String, Object>) obj;
            GameWeightDto gameWeightDto = new GameWeightDto();
            gameWeightDto.setGameId(Long.parseLong(map.get("gameId").toString()));
            gameWeightDto.setWeight(new Integer(map.get("weight").toString()));
            gameWeightDtos.add(gameWeightDto);
        }
        Collections.sort(gameWeightDtos, (o1, o2) -> {
            if (o1.getWeight() > o2.getWeight()) {
                return -1;
            } else if (o1.getWeight() < o2.getWeight()) {
                return 1;
            } else {
                return 0;
            }
        });
        List<Long> sortedGameIds = new ArrayList<>();
        List<Long> notContains = new ArrayList<>();
        for (GameWeightDto gameWeightDto : gameWeightDtos) {
            if (gameIdList.contains(gameWeightDto.getGameId())) {
                sortedGameIds.add(gameWeightDto.getGameId());
            } else {
                notContains.add(gameWeightDto.getGameId());
            }
        }
        sortedGameIds.addAll(notContains);
        return sortedGameIds;
    }

    @RequestMapping("/banners")
    @ResponseBody
    public Object banners(@RequestParam(required = false) String clientId, @RequestAttribute Integer versionCode,
                          @RequestAttribute Integer clientType) {
        List<BannerVo> bannerVos = BannerCache.getBannerVosV2(BannerCache.POSITION_TYPE_TOOL, versionCode, clientType);
        List<BannerVo> result = new ArrayList<>();
        Game game = GameCache.getGame(GameConstant.SSQ);
        if (bannerVos != null) {
            for (BannerVo bannerVo : bannerVos) {
                if (bannerVo.getGameId().equals(game.getGameId())) {
                    result.add(bannerVo);
                }
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("banners", result);
        return buildSuccJson(resultMap);
    }

    //todo 工具首页不应该放倒开奖页面，找时间修改
    @RequestMapping("/tools")
    @ResponseBody
    public Object tools(@RequestParam String gameEn, @RequestParam(required = false) String clientId, @RequestAttribute
            String versionCode, @RequestAttribute Integer clientType) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("tools", awardService.getSortedTools(GameCache.getGame(gameEn), versionCode, clientType));
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/awardTable")
    @ResponseBody
    public Object awardTable(@RequestParam String gameEn, @RequestParam String numberCount) {
        Map<String, Object> resultMap = awardService.getAwardTable(GameCache.getGame(gameEn), numberCount);
        return buildSuccJson(resultMap);
    }
}
