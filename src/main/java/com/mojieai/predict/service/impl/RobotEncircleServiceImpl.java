package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.SocialRobotCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.*;
import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.bo.ListMatchInfo;
import com.mojieai.predict.entity.po.*;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.enums.CronEnum;
import com.mojieai.predict.enums.SportsRobotEnum;
import com.mojieai.predict.service.*;
import com.mojieai.predict.thread.SportRobotTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.HttpServiceUtils;
import com.mojieai.predict.util.SportsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
public class RobotEncircleServiceImpl implements RobotEncircleService {
    private static final Logger log = CronEnum.PERIOD.getLogger();
    @Autowired
    private SocialRobotDao socialRobotDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserTokenDao userTokenDao;
    @Autowired
    private ThirdHttpService thirdHttpService;
    @Autowired
    private SportsRobotRecommendDao sportsRobotRecommendDao;
    @Autowired
    private SportSocialService sportSocialService;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private ExchangeMallDao exchangeMallDao;
    @Autowired
    private MatchInfoService matchInfoService;

    @Override
    public Map<String, Object> registerRobot(Integer count, Integer robotType) {
        Map<String, Object> result = new HashMap<>();

        int robotCount = 0;
        SocialRobotCache socialRobotCache = new SocialRobotCache();
        while (robotCount < count) {
            try {
                String mobile = CommonUtil.generateRobotMobile(robotType);
                String password = SocialEncircleKillConstant.SOCIAL_ROBOT_PASSWORD;
                //1.????????????
                UserLoginVo userLoginVo = loginService.userLogin(mobile, password, "robot", null, null, null);
                //2.?????????????????????
                SocialRobot robot = new SocialRobot();
                robot.setRobotUserId(userLoginVo.getUserId());
                robot.setRobotType(robotType);
                socialRobotDao.insert(robot);
                result.put("name" + count, "????????????");
            } catch (Exception e) {
                continue;
            }
            robotCount++;
        }
        socialRobotCache.refresh();
        return result;
    }

    @Override
    public Map<String, Object> modifyRobotName(Integer robotType) {
        Map result = new HashMap();
        String fileUrl = "/data/mojiecp/predict/src/test/resources/head_name.txt";

        List<Map<String, String>> nameImg = getNameAndImgFromFile(fileUrl);
        if (nameImg.size() <= 1000) {
            return null;
        }

        List<SocialRobot> robots = socialRobotDao.getAllSocialRobot(SocialEncircleKillConstant.SOCIAL_ROBOT_ENABLE,
                robotType);

        for (int i = 0; i < robots.size(); i++) {
            SocialRobot temp = robots.get(i);
            String name = nameImg.get(i).get("name");
            String url = nameImg.get(i).get("img");

            Long userId = temp.getRobotUserId();
            UserToken userToken = userTokenDao.getTokenByUserIdByShardType(userId, String.valueOf(userId).substring
                    (String.valueOf(userId).length() - 2));
            UserLoginVo userLoginVo = loginService.modifyHeadImgOrNickName(userToken.getToken(), userId, url, name);
            result.put("robot" + userLoginVo.getNickName(), "robotId:" + temp.getRobotId());
        }
        return result;
    }

    private List<Map<String, String>> getNameAndImgFromFile(String fileUrl) {
        List<Map<String, String>> res = new ArrayList<>();
        File file = new File(fileUrl);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line = "";
            while ((line = reader.readLine()) != null) {
                Map<String, String> tempRes = new HashMap<>();

                String[] nameImg = line.split("####");
                tempRes.put("name", nameImg[0]);
                tempRes.put("img", nameImg[1]);
                res.add(tempRes);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void sportRobotAddRecommendTiming() {
        //1.???????????????
        SportsRobotEnum sportsRobotEnum = SportsRobotEnum.getCurrentTimeRobot();
        if (sportsRobotEnum == null) {
            return;
        }
        //2.????????????
        List<Map<String, Object>> matchInfos = getRobotPredictMatch();
        if (matchInfos.size() == 0) {
            log.error("sportRobotAddRecommendTiming ??????????????????");
            return;
        }
        //3.????????????????????????
        List<Integer> robotIds = sportsRobotEnum.getRobotIdByRandom();
        for (Integer robotId : robotIds) {
            Integer matchId = randomSelectMatchId(matchInfos);
            SportRobotTask task = new SportRobotTask(robotId, matchId, this);
            ExecutorService taskExec = ThreadPool.getInstance().getSportsRobotRecommendExec();
            taskExec.submit(task);
        }
    }

    private Integer randomSelectMatchId(List<Map<String, Object>> matchInfos) {
        //1.????????????????????????
        Map<Integer, List<Map<String, Object>>> paragraphMatch = getParagraphMatch(matchInfos);

        //2.??????????????????????????????????????????????????????
        String robotSelectRatioStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                .SPORTS_ROBOT_SELECT_MATCH_RATIO, "1:1:1");
        String[] selectRatioArr = robotSelectRatioStr.split(CommonConstant.COMMON_COLON_STR);
        Integer robotMatchSectionIndex = CommonUtil.getRandomIndexByOccupy(selectRatioArr);
        if (selectRatioArr.length != paragraphMatch.size() || robotMatchSectionIndex == null) {
            log.error("????????????????????????????????????????????????, ??????????????????????????????" + paragraphMatch.size() + " ??????????????????" + selectRatioArr.length);
            robotMatchSectionIndex = new Random().nextInt(paragraphMatch.size());
        }

        //3.???????????????????????????
        List<Map<String, Object>> subMatchInfo = paragraphMatch.get(robotMatchSectionIndex);
        Collections.shuffle(subMatchInfo);

        return Integer.valueOf(subMatchInfo.get(0).get("matchId").toString());
    }

    private Map<Integer, List<Map<String, Object>>> getParagraphMatch(List<Map<String, Object>> matchInfos) {
        //1.????????????????????????
        String matchSplitRatioStr = ActivityIniCache.getActivityIniValue(ActivityIniConstant.SPORTS_ROBOT_MATCH_RATIO,
                "1:1:1");
        String[] matchSplitRatioArr = matchSplitRatioStr.split(CommonConstant.COMMON_COLON_STR);

        //2.????????????????????????index??????
        Integer[] matchMidIndexArr = new Integer[matchSplitRatioArr.length - 1];
        Integer sumRatio = 0;
        for (String tempRatio : matchSplitRatioArr) {
            sumRatio += Integer.valueOf(tempRatio);
        }
        Integer partRatio = 0;
        for (int i = 0; i < matchSplitRatioArr.length - 1; i++) {
            partRatio += Integer.valueOf(matchSplitRatioArr[0]);
            matchMidIndexArr[i] = matchInfos.size() * partRatio / sumRatio;
        }

        Map<Integer, List<Map<String, Object>>> result = new HashMap<>();
        for (int i = 0; i <= matchMidIndexArr.length; i++) {
            List<Map<String, Object>> tempMatchInfo = new ArrayList<>();
            if (i == 0) {
                tempMatchInfo = matchInfos.subList(0, matchMidIndexArr[i]);
            } else if (i == matchMidIndexArr.length) {
                tempMatchInfo = matchInfos.subList(matchMidIndexArr[matchMidIndexArr.length - 1], matchInfos.size());
            } else {
                tempMatchInfo = matchInfos.subList(matchMidIndexArr[i - 1], matchMidIndexArr[i]);
            }
            result.put(i, tempMatchInfo);
        }

        return result;
    }

    private List<Map<String, Object>> getRobotPredictMatch() {
        List<Map<String, Object>> res = new ArrayList<>();

        TreeSet<Map<String, Object>> dateMatchList = matchInfoService.getAllMatchInfoFromRedis();

        //????????????????????????list???
        for (Map<String, Object> dateMatch : dateMatchList) {
            //???????????????????????????????????????????????????
            Timestamp currentDate = DateUtil.getCurrentTimestamp();
            Timestamp worldCupEndDate = DateUtil.formatString("2018-07-15 23:59:59", "yyyy-MM-dd HH:mm:ss");
//            if (DateUtil.compareDate(currentDate, worldCupEndDate) && dateMatch.containsKey("matchName")) {
//                String matchName = dateMatch.get("matchName").toString();
//                if (matchName.equals("?????????") || matchName.equals("??????") || matchName.equals("??????") || matchName.equals
//                        ("??????") || matchName.equals("??????") || matchName.equals("??????") || matchName.equals("??????") ||
//                        matchName.equals("K2??????") || matchName.equals("?????????") || matchName.equals("?????????") || matchName
//                        .equals("??????") || matchName.equals("?????????")) {
//                    continue;
//                }
//            }
            res.add(dateMatch);
        }
        return res;
    }

    @Override
    public Boolean divideRobotRecommend(Integer robotType) {
        List<SocialRobot> robots = socialRobotDao.getAllSocialRobot(1, robotType);
        int count = 0;
        for (SocialRobot robot : robots) {
            SportRobotRecommend robotRecommend = new SportRobotRecommend();
            Integer recommendDate = SportsRobotEnum.BATCH_TWENTY_THREE_ROBOT.getBatchNum();
            if (count < 300) {
                recommendDate = SportsRobotEnum.BATCH_TWELVE_ROBOT.getBatchNum();
            } else if (count < 700) {
                recommendDate = SportsRobotEnum.BATCH_EIGHTEEN_ROBOT.getBatchNum();
            }
            robotRecommend.setRecommendDate(recommendDate);
            robotRecommend.setUserId(robot.getRobotUserId());
            sportsRobotRecommendDao.insert(robotRecommend);
            count++;
        }
        return true;
    }

    @Override
    public Boolean sportRobotRecommend(Integer robotId, Integer matchId) {
        Boolean res = Boolean.FALSE;
        SportRobotRecommend recommendRobot = sportsRobotRecommendDao.getRobotRecommendById(robotId);
        SportsRobotEnum sre = SportsRobotEnum.getSportsRobotEnum(recommendRobot.getRecommendDate());
        if (sre == null) {
            log.error("sports robot SportsRobotEnum not exsit" + recommendRobot.getRecommendDate());
            return res;
        }
        if (recommendRobot.getRecommendTimes() >= sre.getRecommendTimes()) {
            SocialRobotCache robotCache = new SocialRobotCache();
            robotCache.refresh();
            return res;
        }
        Map<String, DetailMatchInfo> matchMap = thirdHttpService.getMatchMapByMatchIds(String.valueOf(matchId));
        DetailMatchInfo matchInfo = matchMap.get(String.valueOf(matchId));
        if (DateUtil.compareDate(matchInfo.getEndTime(), DateUtil.getCurrentTimestamp())) {
            return false;
        }
        List<Integer> playTypes = new ArrayList<>();
        if (matchInfo.getSpf() != null && !matchInfo.getSpf().isEmpty()) {
            playTypes.add(SportsProgramConstant.FOOTBALL_PLAY_TYPE_SPF);
        }
        if (matchInfo.getRqSpf() != null && !matchInfo.getRqSpf().isEmpty()) {
            playTypes.add(SportsProgramConstant.FOOTBALL_PLAY_TYPE_RQSPF);
        }
        if (matchInfo.getAsia() != null && !matchInfo.getAsia().isEmpty()) {
            playTypes.add(SportsProgramConstant.FOOTBALL_PLAY_TYPE_ASIA);
        }
        if (playTypes.size() == 0) {
            return res;
        }
        Collections.shuffle(playTypes);
        Integer playType = playTypes.get(0);
        String options = getPredictOption(matchId, playType);
        if (StringUtils.isBlank(options)) {
            return res;
        }
        //robot????????????
        int delaySecond = 180;
        long second = (new Random().nextInt(delaySecond) + 1) * 1000;
        try {
            Thread.sleep(second);
        } catch (InterruptedException e) {
            log.error("??????????????????????????????", e);
        }
        ExchangeMall exchangeMall = randomGetExchangeMall(recommendRobot.getUserId());
        Integer itemId = exchangeMall.getItemId();
        Long price = exchangeMall.getItemPrice();
        String recommendId = sportSocialService.generateRecommendId(recommendRobot.getUserId());
        UserSportSocialRecommend userSportSocialRecommend = new UserSportSocialRecommend(recommendId, recommendRobot
                .getUserId(), matchId + "", 200, playType, matchInfo.getRecommendInfo(playType, options), itemId,
                price, "", "", null, 0, matchInfo.getHandicap(playType), matchInfo.getEndTime(), "");
        userSportSocialRecommend.setRecommendTitle("");
        sportSocialService.saveUserRecommend(userSportSocialRecommend, "127.0.0.1", 1000);
        int times = recommendRobot.getRecommendTimes() + 1;
        recommendRobot.setRecommendTimes(times);
        sportsRobotRecommendDao.updateByPrimaryKeySelective(recommendRobot);
        res = Boolean.TRUE;
        return res;
    }

    //???????????????????????????????????????????????????
    private ExchangeMall randomGetExchangeMall(Long userId) {
        UserInfo userInfo = userInfoDao.getUserInfo(userId);
        if (userInfo.getIsReMaster() == null || !userInfo.getIsReMaster().equals(1)) {
            return exchangeMallDao.getExchangeMall(19);
        }
        Integer goodsId = null;
        Map<Integer, ExchangeMall> goodsMap = getRecommendGoods();
        if (goodsMap.isEmpty()) {
            log.error("randomGetExchangeMall ???????????????????????????????????????");
            return null;
        }
        //1.???????????????????????????????????????
        String robotRecommendPriceRatio = ActivityIniCache.getActivityIniValue(ActivityIniConstant
                .ROBOT_RECOMMEND_PRICE_RATIO, "20:1,21:1,23:1");
        String[] tempArr = robotRecommendPriceRatio.split(CommonConstant.COMMA_SPLIT_STR);
        if (tempArr.length >= 3) {
            String[] goodsIds = new String[tempArr.length];
            String[] goodsRatios = new String[tempArr.length];
            for (int i = 0; i < tempArr.length; i++) {
                String[] idRatioTempArr = tempArr[i].split(CommonConstant.COMMON_COLON_STR);
                if (goodsMap.containsKey(Integer.valueOf(idRatioTempArr[0]))) {
                    goodsIds[i] = idRatioTempArr[0];
                    goodsRatios[i] = idRatioTempArr[1];
                }
            }

            Integer goodsIdIndex = CommonUtil.getRandomIndexByOccupy(goodsRatios);
            if (goodsIdIndex != null) {
                goodsId = Integer.valueOf(goodsIds[goodsIdIndex]);
            }
        }
        if (goodsId == null) {
            goodsId = new Random().nextInt(goodsMap.size());
        }

        return goodsMap.get(goodsId);
    }

    private Map<Integer, ExchangeMall> getRecommendGoods() {
        Map<Integer, ExchangeMall> result = new HashMap<>();
        List<ExchangeMall> exchangeMalls = exchangeMallDao.getExchangeMallList(ExchangeMallConstant
                .EXCHANGE_MALL_RECOMMEND);
        for (ExchangeMall exchangeMall : exchangeMalls) {
            if (!result.containsKey(exchangeMall.getItemId())) {
                result.put(exchangeMall.getItemId(), exchangeMall);
            }
        }
        return result;
    }

    //???????????????????????????
    private String getPredictOption(Integer matchId, Integer playType) {

        return thirdHttpService.getMatchPredictOption(matchId, playType);
    }
}
