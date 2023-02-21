package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.BannerCache;
import com.mojieai.predict.cache.ButtonOrderCache;
import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.ButtonOrderedDao;
import com.mojieai.predict.dao.UserFeedbackDao;
import com.mojieai.predict.entity.bo.DigitNavParams;
import com.mojieai.predict.entity.bo.Email;
import com.mojieai.predict.entity.po.ButtonOrdered;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.po.UserFeedback;
import com.mojieai.predict.entity.vo.IndexShowVo;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.AttachService;
import com.mojieai.predict.service.DingTalkRobotService;
import com.mojieai.predict.service.SendEmailService;
import com.mojieai.predict.thread.SendEmailTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by tangxuelong on 2017/7/24.
 */
@Service
public class AttachServiceImpl implements AttachService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private UserFeedbackDao userFeedbackDao;
    @Autowired
    private SendEmailService sendEmailService;
    @Autowired
    private DingTalkRobotService dingTalkRobotService;
    @Autowired
    private RedisService redisService;

    @Override
    public Boolean versionCheck(String versionCode) {
        if (Integer.valueOf(versionCode) >= Integer.valueOf(ActivityIniCache.getActivityIniValue(ActivityIniConstant
                .VERSION, "1"))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public IndexShowVo getIndexShow(Integer type) {
        IndexShowVo indexShowVo;
        try {
            String indexShow = "";
            if (type == null || type.equals(CommonConstant.PUSH_CENTER_NOTICE_TYPE_DIGIT)) {
                indexShow = ActivityIniCache.getActivityIniValue(ActivityIniConstant.INDEX_SHOW,
                        "{\"url\":\"mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq" +
                                ".com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k" +
                                "%3DkmAQNEe32XcJ4DPo6ZDhspRXJ0FZtgk8\"," +
                                "\"showText\":\"加入QQ讨论群：159879058\",\"isShow\":\"1\"}");
            } else {
                indexShow = "{\"isShow\": 0,\"showText\":\"加入QQ讨论群：791986342\",\"url\": " +
                        "\"mqqapi://card/show_pslcard?src_type=internal&version=1&uin=791986342&key" +
                        "=7c6c1474dce6d8e3e5b18f45aed09caf013dd0e2e6a60fffc29d573f6f014de0&card_type=group&source" +
                        "=external\"}";
            }

            Map<String, Object> indexShowMap = (Map<String, Object>) JSONObject.parse(indexShow);
            indexShowVo = new IndexShowVo(indexShowMap.get("url").toString(), indexShowMap.get
                    ("showText").toString(), new Integer(indexShowMap.get("isShow").toString()));

        } catch (Exception e) {
            throw new BusinessException("get index show error" + e.getMessage());
        }
        return indexShowVo;
    }

    @Override
    public Map<String, Object> getCommunications(Integer type) {
        Map<String, Object> weiXinShowMap = null;
        try {
            String weiXinShow = "";
            if (type != null && type.equals(1)) {
                weiXinShow = "{\"qqShow\": {\"isShow\": 1,\"btnText\": \"\",\"url\": " +
                        "\"mqqapi://card/show_pslcard?src_type=internal&version=1&uin=791986342&key" +
                        "=7c6c1474dce6d8e3e5b18f45aed09caf013dd0e2e6a60fffc29d573f6f014de0&card_type=group&source" +
                        "=external\"},\"weiXinShow\": {\"isShow\": 1,\"btnText\": \"微信号zhihuicp，点击复制\"," +
                        "\"weiXinCode\": \"zhihuicp\",\"alterMsg\": \"已复制微信号\",\"beforeJoinMsg\": " +
                        "\"进“微信群”需先加客服智慧微信\"},\"adMsg\": \"加入智慧预测群，畅谈双色球技巧\"}";
            } else {
                weiXinShow = ActivityIniCache.getActivityIniValue(ActivityIniConstant.COMMUNICATION_INFO,
                        "{\"qqShow\": {\"isShow\": 1,\"btnText\": \"加入QQ讨论群：592012648\",\"url\": " +
                                "\"mqqapi://card/show_pslcard?src_type=internal&version=1&uin=592012648&key" +
                                "=8aaabbbfa4e2dfa9e8dc42cb010292dfa3f8ae777262cf55a3b6a2ca7c5c0245&card_type=group&source" +
                                "=external\"},\"weiXinShow\": {\"isShow\": 1,\"btnText\": \"微信号zhihuicp，点击复制\"," +
                                "\"weiXinCode\": \"zhihuicp\",\"alterMsg\": \"已复制微信号\",\"beforeJoinMsg\": " +
                                "\"进“微信群”需先加客服智慧微信\"},\"adMsg\": \"加入智慧预测群，畅谈双色球技巧\"}");
            }

            weiXinShowMap = (Map<String, Object>) JSONObject.parse(weiXinShow);
        } catch (Exception e) {
            throw new BusinessException("get weiXin show error", e);
        }
        return weiXinShowMap;
    }

    @Override
    public void userFeedback(String content, String contact, String token) {
        try {
            String encodeContent = URLEncoder.encode(content, "utf-8");
            UserFeedback userFeedback = new UserFeedback();
            userFeedback.setContent(encodeContent);
            userFeedback.setUserToken(token);
            userFeedback.setContact(contact);
            userFeedbackDao.insert(userFeedback);

            userFeedBackCheck();
        } catch (Exception e) {
            log.error("userFeedback error" + e.getMessage());
            throw new BusinessException("userFeedback error");
        }
    }

    @Override
    public void userFeedBackCheck() {
        try {
            List<UserFeedback> userFeedbackList = userFeedbackDao.getUnSendContent();
            StringBuffer sb = new StringBuffer("<table border=\"1\">\n  <tr>\n    " +
                    "<th>联系方式</th>\n<th>内容</th>\n</tr>\n");
            for (UserFeedback userFeedback : userFeedbackList) {
                sb.append("<tr>\n");
                sb.append("    <td>").append(userFeedback.getContact()).append("</td>\n");
                sb.append("    <td>").append(URLDecoder.decode(userFeedback.getContent(), "utf-8")).append("</td>\n");
                sb.append("</tr>\n");
                userFeedbackDao.update(userFeedback.getFeedbackId());
            }
            sb.append("</table>");
            Email email = new Email();
            email.setContent(sb.toString());
            email.setTitle("智慧彩票用户反馈");
            SendEmailTask sendEmailTask = new SendEmailTask(sendEmailService, email);
            ThreadPool.getInstance().getSendEmailExec().submit(sendEmailTask);
        } catch (Exception e) {
            log.error("userFeedBackCheck error" + e.getMessage());
            throw new BusinessException("userFeedBackCheck error" + e.getMessage());
        }
    }

    @Override
    public void getThirdWinningNumberUpdate() {
        try {
            log.info("监控开奖号码数据更新");
            /* 监控开奖号码变化情况*/
            List<Long> gameIdList = new ArrayList<>(GameCache.getAllGameMap().keySet());
            for (Long gameId : gameIdList) {
                Game game = GameCache.getGame(gameId);
                if (null == game) {
                    log.error("getThirdWinningNumberUpdate get game is null");
                    throw new BusinessException("getThirdWinningNumberUpdate get game is null");
                }
                // 大盘彩
                if (game.getGameType().equals(Game.GAME_TYPE_COMMON)) {
                    /* 当前为开奖的期次 如果处于开奖状态*/
                    GamePeriod period = PeriodRedis.getLastOpenPeriodByGameId(gameId);
                    GamePeriod nextPeriod = PeriodRedis.getNextPeriodByGameIdAndPeriodId(gameId, period.getPeriodId());
                    // 开奖后一小时持续执行
                    String stopTime = redisService.kryoGet(gameId + "stopTime", String.class);
                    Boolean awardTimeStart = Boolean.FALSE;
                    /* 如果已经处于开奖后一个小时 期次切换到开奖期次*/
                    if (StringUtils.isNotBlank(stopTime)) {
                        awardTimeStart = Boolean.TRUE;
                        nextPeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, stopTime);
                    }
                    /* 处于开奖后一个小时，或者当前开奖的期次处于开奖状态*/
                    if (DateUtil.compareDate(nextPeriod.getAwardTime(), new Date())) {
                        /* 只有当前开奖的期次处于开奖状态 才设置开奖后一个小时的缓存*/
                        if (!awardTimeStart) {
                            redisService.kryoSetEx(gameId + "stopTime", 60 * 60, nextPeriod.getPeriodId());
                        }
                        log.info(game.getGameEn() + "第" + nextPeriod.getPeriodId() + "期" + "正在开奖，正在监控三方开奖号码是否更新");
                        /* 三方是否更新标志*/
                        Boolean winningFlag = Boolean.FALSE;
                        String winningNumber = ""; //开奖号码
                        String getThirdName = "";  //三方名字
                        /* 网易*/
                        String wangyi = redisService.kryoGet("wangyi", String.class);
                        /* 如果已经抓取到开奖号，则停止*/
                        if (StringUtils.isBlank(wangyi)) {
                            String url = new StringBuffer().append(CommonConstant.AWARD_163_DOWNLOAD_URL_PREFIX).append
                                    (game.getGameEn()).append(CommonConstant.URL_SPLIT_STR).toString();
                            Document doc = Jsoup.connect(url).timeout(CommonConstant.AWARD_163_DOWNLOAD_TIMEOUT_MSEC)
                                    .get();
                            if (doc != null) {
                                Element content = doc.getElementsByClass(CommonConstant
                                        .AWARD_163_DOWNLOAD_ELEMENT_CLASS).get(1).getElementsByTag("a").get(0);
                                String text = content.text();
                                /* 期次相等 号码不为空，开启通知，设置号码已更新的缓存*/
                                if (Objects.equals(text, nextPeriod.getPeriodId())) {
                                    Elements allElements = content.getAllElements();
                                    winningNumber = allElements.attr("matchBall");
                                    if (StringUtils.isNotBlank(winningNumber)) {
                                        redisService.kryoSetEx("wangyi", 60 * 60, "1");
                                        getThirdName = "网易";
                                        winningFlag = Boolean.TRUE;
                                    }
                                }
                            }
                        }

                        /* 腾讯 和网易 互不影响*/
                        String tecent = redisService.kryoGet("tecent", String.class);
                        if (StringUtils.isBlank(tecent)) {
                            String tecentUrl = new StringBuffer().append(CommonConstant.AWARD_QQ_DOWNLOAD_URL_PREDIX)
                                    .append(CommonConstant.QQ_OPEN_AWARD_PAGE).append(game.getGameEn()).append
                                            (CommonConstant.SUFFIX_JS).toString();
                            Document tecentDoc = Jsoup.connect(tecentUrl).ignoreContentType(true).timeout(CommonConstant
                                    .AWARD_163_DOWNLOAD_TIMEOUT_MSEC).get();
                            if (tecentDoc != null) {
                                Element element = tecentDoc.getElementsByTag("body").first();
                                Map openAward = JSONObject.parseObject(element.text(), HashMap.class);
                                if (openAward != null) {
                                    List kaijiangList = (List) openAward.get("kaijiang_list");
                                    if (kaijiangList != null && kaijiangList.size() > 0) {
                                        Map<String, Object> lastInfo = (Map<String, Object>) kaijiangList.get(0);
                                        if (lastInfo != null && nextPeriod.getPeriodId().contains(lastInfo.get("qihao")
                                                .toString())) {
                                            redisService.kryoSetEx("tecent", 60 * 60, "1");
                                            getThirdName = "腾讯";
                                            winningFlag = Boolean.TRUE;
                                            winningNumber = lastInfo.get("kjhm").toString().replace(CommonConstant
                                                    .COMMA_SPLIT_STR, CommonConstant.SPACE_SPLIT_STR).replace
                                                    (CommonConstant.COMMON_VERTICAL_STR, CommonConstant
                                                            .COMMON_COLON_STR);
                                        }
                                    }
                                }
                            }
                        }


                        if (winningFlag) {
                            String markdown = "#### 监控服务 \n" + "> " + "监控到" + getThirdName + "的开奖号码更新," + game
                                    .getGameEn() + "第" + nextPeriod.getPeriodId() + "期开奖号码:" + winningNumber + " " +
                                    "@18301552530\n" + "> " + "###### " + DateUtil.formatNowTime(15) + "发布 \n";
                            List<String> at = new ArrayList<>();
                            at.add("18301552530");
                            dingTalkRobotService.sendMassageToAll("监控服务", markdown, at);
                        }

                    }
                }


            }
        } catch (Exception e) {
            //log.error("开奖号码监控异常", e);
        }
    }

    @Override
    public Map<String, Object> getDigitalLotteryHomePage(Integer clientType, Integer versionCode, DigitNavParams
            digitNavParams) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> lotteryNavigation = new ArrayList<>();

        lotteryNavigation.add(getGameNavigation(GameConstant.SSQ, "#FF5050", digitNavParams.getSsqNavIds(),
                "mjlottery://mjnative?page=kjlb&gameName=双色球&gameEn=ssq"));
        lotteryNavigation.add(getGameNavigation(GameConstant.DLT, "#FFAF17", digitNavParams.getDltNavIds(),
                "mjlottery://mjnative?page=kjlb&gameName=大乐透&gameEn=dlt"));
        lotteryNavigation.add(getGameNavigation(GameConstant.FC3D, "#3F9ADF", digitNavParams.getFc3dNavIds(),
                "mjlottery://mjnative?page=kjlb&gameName=福彩3d&gameEn=fc3d"));

        result.put("banner", BannerCache.getBannerVosV2(BannerCache.POSITION_TYPE_DIGIT_INDEX, versionCode, clientType));
        result.put("lotteryNavigation", lotteryNavigation);
        return result;
    }

    @Override
    public Map<String, Object> getAllDigitNav(Game game, String navIds) {
        Map<String, Object> result = new HashMap<>();
        Map<Integer, Map<String, Object>> sortedMap = new HashMap<>();
        List<ButtonOrdered> buttons = ButtonOrderCache.getButtonOrdered(game.getGameId(), ButtonOrderedConstant
                .BUTTON_TYPE_DIGIT_HOME_PAGE_DYNAMIC_NAV);
        if (StringUtils.isBlank(navIds) || StringUtils.isBlank(navIds.replaceAll(CommonConstant.COMMA_SPLIT_STR,
                CommonConstant.SPACE_NULL_STR))) {
            navIds = ActivityIniCache.getActivityIniValue(ActivityIniConstant.getDigitIndexDefaultNav(game.getGameId
                    ()));
        }
        if (StringUtils.isBlank(navIds)) {
            log.error("数字彩编辑导航异常");
        }
        List<String> navIdList = Arrays.asList(navIds.split(CommonConstant.COMMA_SPLIT_STR));
        List<Map<String, Object>> defaultNav = new ArrayList<>();
        List<Map<String, Object>> dynamicNav = new ArrayList<>();

        for (ButtonOrdered button : buttons) {
            Map<String, Object> buttonMap = convertButton2DigitHomeToolNav(button);
            if (buttonMap == null || buttonMap.isEmpty()) {
                continue;
            }
            String tempNavId = String.valueOf(button.getBtnId());
            if (navIds.contains(tempNavId)) {
                sortedMap.put(navIdList.indexOf(tempNavId), buttonMap);
            } else {
                dynamicNav.add(buttonMap);
            }
        }

        if (!sortedMap.isEmpty()) {
            for (int i = 0; i < sortedMap.size(); i++) {
                if (sortedMap.get(i) != null) {
                    defaultNav.add(i, sortedMap.get(i));
                }
            }
        }


        result.put("staticNav", getToolNavigation(game.getGameId(), ButtonOrderedConstant
                .BUTTON_TYPE_DIGIT_HOME_PAGE_FIXED_NAV, null));
        result.put("defaultNav", defaultNav);
        result.put("dynamicNav", dynamicNav);
        return result;
    }

    private Map<String, Object> getGameNavigation(String gameEn, String tagColor, String customNav, String
            winningNumJumpUrl) {
        Map<String, Object> navigation = new HashMap<>();
        Game game = GameCache.getGame(gameEn);

        Map<String, Object> winningNumMap = new HashMap<>();
        GamePeriod gamePeriod = PeriodRedis.getLastOpenPeriodByGameId(game.getGameId());

        String redBall = "<font color='#ff5050'>" + gamePeriod.getWinningNumbers().split(":")[0] + "</font>";
        String blueBall = "";
        if (!gameEn.equals(GameConstant.FC3D)) {
            blueBall = " <font color='#5B8BF0'>" + gamePeriod.getWinningNumbers().split(":")[1] + "</font>";
        }


        String testNum = "";
        if (gameEn.equals(GameConstant.FC3D)) {
            testNum = "试机号 : ";
            String num = "- - -";

            JSONObject jsonObject = JSONObject.parseObject(gamePeriod.getRemark());
            if (jsonObject != null && jsonObject.containsKey("testNum")) {
                num = jsonObject.getString("testNum");
            }
            testNum = testNum + num;
        }

        winningNumMap.put("testNum", testNum);
        winningNumMap.put("winningNum", redBall + blueBall);
        winningNumMap.put("jumpUrl", winningNumJumpUrl);
        winningNumMap.put("umStatistics", game.getGameEn() + "LotteryOpenAward");
        winningNumMap.put("periodName", gamePeriod.getPeriodId() + "期开奖号：");

        navigation.put("tagColor", tagColor);
        navigation.put("gameId", game.getGameId());
        navigation.put("winningNumInfo", winningNumMap);
        navigation.put("lotteryName", game.getGameName());
        navigation.put("toolNavigation", getToolNavigation(game.getGameId(), ButtonOrderedConstant
                .BUTTON_TYPE_DIGIT_HOME_PAGE_FIXED_NAV, null));
        navigation.put("fixToolNavigation", getToolNavigation(game.getGameId(), ButtonOrderedConstant
                .BUTTON_TYPE_DIGIT_HOME_PAGE_DYNAMIC_NAV, customNav));
        return navigation;
    }

    private List<Map<String, Object>> getToolNavigation(Long gameId, Integer type, String customNav) {
        List<Map<String, Object>> result = null;
        List<ButtonOrdered> buttonOrderedList = null;
        List<String> customNavList = null;
        Map<Integer, Map<String, Object>> sortedMap = null;
        if (type.equals(ButtonOrderedConstant.BUTTON_TYPE_DIGIT_HOME_PAGE_FIXED_NAV)) {
            buttonOrderedList = ButtonOrderCache.getButtonOrdered(gameId, type);
            result = new ArrayList<>();
        } else {
            if (StringUtils.isBlank(customNav)) {
                customNav = ActivityIniCache.getActivityIniValue(ActivityIniConstant.getDigitIndexDefaultNav(gameId));
                if (StringUtils.isBlank(customNav)) {
                    throw new IllegalArgumentException("customNav error");
                }
            }
            customNavList = Arrays.asList(customNav.split(CommonConstant.COMMA_SPLIT_STR));
            result = new ArrayList<>();
            sortedMap = new HashMap<>();
            buttonOrderedList = ButtonOrderCache.getButtonOrdered(gameId, type, customNav);
        }
        if (buttonOrderedList == null || buttonOrderedList.size() == 0) {
            return null;
        }

        for (ButtonOrdered button : buttonOrderedList) {
            Map<String, Object> temp = convertButton2DigitHomeToolNav(button);
            if (temp != null) {
                if (customNavList != null) {
                    sortedMap.put(customNavList.indexOf(button.getBtnId() + ""), temp);
                } else {
                    result.add(temp);
                }
            }
        }
        if (StringUtils.isBlank(customNav)) {
            result = CommonUtil.CollectionsSortedByWeight(result, "weight");
        } else {
            if (sortedMap == null || sortedMap.isEmpty()) {
                throw new IllegalArgumentException("customNav error");
            }
            for (int i = 0; i < sortedMap.size(); i++) {
                if (sortedMap.get(i) != null) {
                    result.add(i, sortedMap.get(i));
                }
            }
        }

        return result;
    }

    private Map<String, Object> convertButton2DigitHomeToolNav(ButtonOrdered button) {
        Map<String, Object> result = new HashMap<>();

        String cornerImg = "";
        Boolean checkVip = false;
        if (StringUtils.isNotBlank(button.getMemo())) {
            Map<String, Object> memoMap = JSONObject.parseObject(button.getMemo(), HashMap.class);
            if (memoMap.containsKey("cornerImg")) {
                cornerImg = memoMap.get("cornerImg").toString();
            }
            if (memoMap.containsKey("vipFunction")) {
                checkVip = (Boolean) memoMap.get("vipFunction");
            }
        }

        result.put("navId", button.getBtnId());
        result.put("iconImg", button.getImg());
        result.put("cornerImg", cornerImg);
        result.put("name", button.getName());
        result.put("jumpUrl", button.getJumpUrl());
        result.put("weight", button.getWeight());
        result.put("umStatistics", "digithome" + button.getUniqueStr());
        result.put("checkVip", checkVip);
        return result;
    }
}
