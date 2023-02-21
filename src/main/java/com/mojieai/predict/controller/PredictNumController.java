package com.mojieai.predict.controller;


import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PredictConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.SubscribeProgramDao;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.SubscribeProgram;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.enums.predict.PickNumEnum;
import com.mojieai.predict.enums.predict.PickNumPredict;
import com.mojieai.predict.enums.predict.SsqPickNumEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.*;
import com.mojieai.predict.service.predict.AbstractPredictView;
import com.mojieai.predict.service.predict.PredictFactory;
import com.mojieai.predict.service.predict.PredictInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/analysis")
@Controller
public class PredictNumController extends BaseController {

    @Autowired
    private PredictNumService predictNumService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private PredictRedBallService predictRedBallService;
    @Autowired
    private CompatibleService compatibleService;
    @Autowired
    private SubScribeProgramService subScribeProgramService;
    @Autowired
    private UserSubscribeInfoLogService userSubscribeInfoLogService;
    @Autowired
    private SubscribeProgramDao subscribeProgramDao;

    @RequestMapping("/predictNum")
    @ResponseBody
    public Object predictNum(@RequestParam String gameEn, String token, @RequestParam String deviceId) {
        Map resultMap = null;
        String userIdStr = "";
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }
        userIdStr = exchangeUserId(token);
        if (StringUtils.isBlank(userIdStr) && StringUtils.isNotBlank(token)) {
            return buildJson(-1, "用户校验失败,请重新登录");
        }
        resultMap = predictNumService.getPredictNums(userIdStr, deviceId, game.getGameId());
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/predictIndex")
    @ResponseBody
    public Object predictIndexInfo(@RequestParam String gameEn, String token, @RequestParam String deviceId,
                                   @RequestAttribute String versionCode, @RequestAttribute(required = false) Integer
                                           clientType) {
        Map<String, Object> resultMap = null;
        String userIdStr = "";
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }
        userIdStr = exchangeUserId(token);
        if (StringUtils.isBlank(userIdStr) && StringUtils.isNotBlank(token)) {
            return buildJson(-1, "用户校验失败,请重新登录");
        }
        resultMap = predictNumService.getPredictIndexInfo(userIdStr, deviceId, game.getGameId(), versionCode);
        compatibleService.programChangeOrderBug(resultMap, clientType, versionCode);
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/predictHistory")
    @ResponseBody
    public Object predictNumHistory(@RequestParam String gameEn) {
        Map<String, Object> resultMap = new HashMap<>();

        Game game = GameCache.getGame(gameEn);
        if (game != null) {
            resultMap = predictNumService.getPredictHistoryList(game.getGameId());
        } else {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }
        return buildSuccJson(resultMap);
    }

    /*
    * 福彩3d预测号码
    * */
    @RequestMapping("/predictNumber")
    @ResponseBody
    public Object predictNumber(@RequestParam String gameEn, @RequestParam String token, @RequestParam(required = false)
            String timeSpan, @RequestParam Integer predictType) {
        // 校验用户信息
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildJson(-1, "用户校验失败,请重新登录");
        }
        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);

        Map<String, Object> resultMap = predictInfo.getPredictNumber(userToken.getUserId(), game.getGameId(),
                timeSpan, predictType);
        return buildSuccJson(resultMap);
    }

    /*
    * 福彩3d预测号码
    * */
    @RequestMapping("/rebuildPredictMarquee")
    @ResponseBody
    public Object rebuildPredictMarquee(@RequestParam String gameEn, @RequestParam String periodId) {
        // 校验用户信息
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }
        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);

        predictInfo.analysisPredictNumbers(game.getGameId(), periodId);
        return buildSuccJson();
    }

    /*
    * 福彩3d预测号码首页
    * */
    @RequestMapping("/predictNumberIndex")
    @ResponseBody
    public Object predictNumberIndex(@RequestParam String gameEn, @RequestParam(required = false) String token) {
        // 校验用户信息
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }
        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            UserToken userToken = loginService.checkToken(token);
            if (null == userToken) {
                return buildJson(-1, "用户校验失败,请重新登录");
            }
            userId = userToken.getUserId();
        }

        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);

        Map<String, Object> resultMap = predictInfo.predictNumbersIndex(userId, game.getGameId());
        return buildSuccJson(resultMap);
    }

    /*
    * 福彩3d预测号码首页历史
    * */
    @RequestMapping("/predictNumberHistory")
    @ResponseBody
    public Object predictNumberHistory(@RequestParam String gameEn, @RequestParam(required = false) String periodId) {
        // 校验用户信息
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }

        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);

        Map<String, Object> resultMap = predictInfo.predictNumbersHistory(game.getGameId(), periodId);
        return buildSuccJson(resultMap);
    }

    /*
    * 福彩3d预测号码定位杀一码
    * */
    @RequestMapping("/positionKill")
    @ResponseBody
    public Object positionKill(@RequestParam String gameEn) {
        // 校验用户信息
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }

        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);

        Map<String, Object> resultMap = predictInfo.positionKill(game.getGameId());
        return buildSuccJson(resultMap);
    }

    /*
    * 福彩3d预测号码3胆码
    * */
    @RequestMapping("/threeDanCode")
    @ResponseBody
    public Object threeDanCode(@RequestParam String gameEn) {
        // 校验用户信息
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }

        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);

        Map<String, Object> resultMap = predictInfo.threeDanCode(game.getGameId());
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/redTwentyNums")
    @ResponseBody
    public Object predictRedTwentyNums(@RequestParam String gameEn) {
        Map<String, Object> resultMap = new HashMap<>();

        Game game = GameCache.getGame(gameEn);
        if (game != null) {
            resultMap = predictRedBallService.getRedTwentyNumsByGameId(game.getGameId());
        } else {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }

        return buildSuccJson(resultMap);
    }

    @RequestMapping("/killThreeCode")
    @ResponseBody
    public Object killThreeCode(HttpServletRequest request, @RequestParam String gameEn) {
        Map<String, Object> resultMap = new HashMap<>();

        Game game = GameCache.getGame(gameEn);
        if (game != null) {
            resultMap = predictRedBallService.getKillThreeCodeByGameId(game.getGameId());
        } else {
            return buildJson(ResultConstant.GAME_NOT_EXIST, ResultConstant.GAME_NOT_EXIST_INFO);
        }

        /* 版本兼容*/
        compatibleService.killThreeRed(resultMap, request);
        return buildSuccJson(resultMap);
    }

    /*
    * 绝杀码
    * */
    @RequestMapping("/lastKillCode")
    @ResponseBody
    public Object lastKillCode(@RequestParam(required = false) String token, @RequestParam String gameEn) {
        Long userId = null;
        if (StringUtils.isNotBlank(token)) {
            UserToken userToken = loginService.checkToken(token);
            userId = userToken.getUserId();
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson(ResultConstant.GAME_NOT_EXIST_INFO);
        }
        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);

        Map<String, Object> resultMap = predictInfo.getLastKillCode(userId, gameEn, PredictConstant.LAST_KILL_CODE);
        resultMap.put("showText", predictInfo.getLastKillCodeShowText(gameEn));
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/killThreeBlue")
    @ResponseBody
    public Object killThreeBlue(@RequestParam String gameEn, Integer type) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson(ResultConstant.GAME_NOT_EXIST_INFO);
        }
        if (type == null) {
            type = PredictConstant.KILL_THREE_BLUE;
        }
        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);
        List<String> killThreeBlueBalls = predictInfo.getKillBlue(gameEn, type);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("killThreeBlue", killThreeBlueBalls);
        resultMap.put("showText", predictInfo.getShowText(gameEn));
        return buildSuccJson(resultMap);
    }

    @RequestMapping("/killThreeBlueRebuild")
    @ResponseBody
    public Object killThreeBlueRebuild(@RequestParam String gameEn) {
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson(ResultConstant.GAME_NOT_EXIST_INFO);
        }
        PredictInfo predictInfo = PredictFactory.getInstance().getPredictInfo(gameEn);
        predictInfo.killBluePredictRebuild(gameEn);
        return buildSuccJson();
    }

    @RequestMapping("/pickNumPredict")
    @ResponseBody
    public Object pickNumPredict(@RequestParam String gameEn, @RequestParam String token, @RequestParam Integer type) {
        Long userId = exchangeUserIdLong(token);
        if (userId == null) {
            return buildErrJson("用户不存在");
        }
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("彩种不存在");
        }

        AbstractPredictView predictView = PredictFactory.getInstance().getPredictView(gameEn);
        if (predictView == null) {
            return buildErrJson("彩种不存在");
        }
        PickNumPredict pickNumEnum = PickNumEnum.getPickNumEnum(gameEn).getGamePickNumEnum(type);
        Map res = pickNumEnum.getPredictInfo(predictView, userId);
        return buildSuccJson(res);
    }

    @RequestMapping("/predictStateNum")
    @ResponseBody
    public Object predictStateNum(String token, @RequestParam String gameEn, @RequestParam Integer type) {
        Long userId = exchangeUserIdLong(token);
        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("彩种不存在");
        }

        Map res = subScribeProgramService.getSubScribeProgram(userId, game.getGameId(), type);
        return buildSuccJson(res);
    }

    @RequestMapping("/givePredictStateNum")
    @ResponseBody
    public Object givePredictStateNum(@RequestParam String token, @RequestParam Integer goodsId) {
        Long userId = exchangeUserIdLong(token);
        if (userId == null) {
            return buildErrJson("用户不存在");
        }
        //2.订阅
        SubscribeProgram program = subscribeProgramDao.getSubscribePredictByProgramId(goodsId);
        Map<String, Object> res = userSubscribeInfoLogService.givePredictStateNum2User(userId, program);
        return buildSuccJson(res);
    }

    @RequestMapping("/clearTimes")
    @ResponseBody
    public Object clearPredictTimes(@RequestParam long gameId, @RequestParam String mobile, @RequestParam String type) {
        Map resultMap = new HashMap();

        boolean res = predictNumService.clearTimes(gameId, mobile, type);

        if (res) {
            resultMap.put("msg", mobile + " 已经帮您清除成功，尽情体验去吧。^_^!!!");
        } else {
            resultMap.put("msg", mobile + " 未能帮您清除成功。-_-!!!");
        }

        return buildSuccJson(resultMap);
    }

    @RequestMapping("/showGame")
    @ResponseBody
    public Object showGame(@RequestParam int versionCode) {
        Map<String, Integer> result = new HashMap<>();
        //展示游戏
        int showGame = ActivityIniCache.getActivityIniIntValue(ActivityIniConstant.APP_SHOW_GAME_FLAG,
                ActivityIniConstant.APP_SHOW_GAME);

        result.put("showGameFlag", showGame);
        return buildSuccJson(result);
    }

    private String exchangeUserId(String token) {
        String userIdStr = "";
        if (StringUtils.isNotBlank(token)) {
            UserToken userToken = loginService.checkToken(token);
            if (userToken != null) {
                userIdStr = userToken.getUserId() + CommonConstant.SPACE_NULL_STR;
            }
        }
        return userIdStr;
    }

    private Long exchangeUserIdLong(String token) {
        Long userIdStr = null;
        if (StringUtils.isNotBlank(token)) {
            UserToken userToken = loginService.checkToken(token);
            if (userToken != null) {
                userIdStr = userToken.getUserId();
            }
        }
        return userIdStr;
    }
}
