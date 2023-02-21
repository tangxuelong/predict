package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.*;
import com.mojieai.predict.dao.UserAccountFlowDao;
import com.mojieai.predict.entity.bo.UserEncircleInfo;
import com.mojieai.predict.entity.bo.UserEncircleInfoPack;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.entity.vo.*;
import com.mojieai.predict.enums.CommonStatusEnum;
import com.mojieai.predict.redis.PeriodRedis;
import com.mojieai.predict.service.*;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.PayUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by tangxuelong on 2017/9/6.
 */
@Service
public class CompatibleServiceImpl implements CompatibleService {
    @Autowired
    private UserTitleService userTitleService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private UserSignService userSignService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserBankCardService userBankCardService;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;

    private static final Logger log = LogConstant.commonLog;

    @Override
    public void killThreeRed(Map<String, Object> resultMap, HttpServletRequest request) {
        String versionCode = request.getHeader(CommonConstant.VERSION_CODE);
        if (StringUtils.isBlank(versionCode) || Integer.valueOf(versionCode) <= 5) {
            List<Map<String, Object>> periodList = (List<Map<String, Object>>) resultMap.get("periodList");
            int index = 0;
            for (Map<String, Object> period : periodList) {
                if (index != 0) {// 第一期为预测，不动
                    String[] periodArr = period.get("killCode").toString().split(CommonConstant.SPACE_SPLIT_STR);
                    String[] newPeriodArr = new String[periodArr.length];
                    for (int i = 0; i < periodArr.length; i++) {
                        if (periodArr[i].contains(CommonConstant.COMMON_STAR_STR)) {
                            newPeriodArr[i] = periodArr[i].replace(CommonConstant.COMMON_STAR_STR, "");
                        } else {
                            newPeriodArr[i] = periodArr[i].replace(periodArr[i], CommonConstant.COMMON_STAR_STR +
                                    periodArr[i]);
                        }
                    }
                    period.put("killCode", String.join(CommonConstant.SPACE_SPLIT_STR, newPeriodArr));
                }
                index++;
            }
        }
    }

    @Override
    public void continueNum(Map<String, Object> periodMap, HttpServletRequest request) {
        String versionCode = request.getHeader(CommonConstant.VERSION_CODE);
        if (StringUtils.isBlank(versionCode) || Integer.valueOf(versionCode) < 5) {
            try {
                List<Map<String, Object>> periodList = (List<Map<String, Object>>) periodMap.get("periodList");
                if (periodList != null && periodList.size() > 0) {
                    for (int i = 0; i < periodList.size(); i++) {
                        Map<String, Object> tempMap = periodList.get(i);
                        if (tempMap.containsKey("omitNum") && tempMap.get("omitNum") != null && (tempMap.get
                                ("omitNum")) instanceof List) {
                            List<Integer> omitNums = (List<Integer>) tempMap.get("omitNum");
                            for (int j = 0; j < omitNums.size(); j++) {
                                if (omitNums.get(j) != null && omitNums.get(j) == -2) {
                                    omitNums.set(j, 0);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("连号数据转化异常", e);
            }
        }
    }

    @Override
    public void getKillNumListByPeriodId(Map<String, Object> result, long gameId, String periodId, String versionCode) {
        if (result == null || !result.containsKey("encircles") || result.get("encircles") == null || Integer.valueOf
                (versionCode) < CommonConstant.VERSION_CODE_3_0) {
            return;
        }
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        if (gamePeriod != null && StringUtils.isNotBlank(gamePeriod.getWinningNumbers())) {
            return;
        }
        List<MyEncircleVo> res = (List<MyEncircleVo>) result.get("encircles");
        for (MyEncircleVo tempVo : res) {
            String nums = tempVo.getEncircleNum();
            if (!nums.contains(CommonConstant.COMMON_STAR_STR)) {
                tempVo.setEncircleNum(addStar2Balls(nums));
            }
        }
    }

    @Override
    public void killNumListCompateUserId(Map<String, Object> result, String versionCode, Integer clientType, Long
            gameId) {
        if (result == null || !result.containsKey("encircles") || result.get("encircles") == null || Integer.valueOf
                (versionCode) < CommonConstant.VERSION_CODE_3_2 || clientType == null) {
            return;
        }
        List<MyEncircleVo> myEncircleVos = (List<MyEncircleVo>) result.get("encircles");
        List<MyEncircleVoPack> encircles = new ArrayList<>();

        for (MyEncircleVo myEncircleVo : myEncircleVos) {
            MyEncircleVoPack tempPack = new MyEncircleVoPack(String.valueOf(myEncircleVo.getEncircleUserId()),
                    myEncircleVo);

            // 大神
            List<String> godList = userTitleService.getUserGodList(gameId, myEncircleVo.getEncircleUserId(),
                    versionCode);
            tempPack.setGodList(godList);
            encircles.add(tempPack);
        }

        result.put("encircles", encircles);
    }

    @Override
    public void followsKillNumList(Map<String, Object> result, Long gameId, String versionCode) {
        if (result == null || !result.containsKey("encircles") || result.get("encircles") == null || Integer.valueOf
                (versionCode) < CommonConstant.VERSION_CODE_3_0) {
            return;
        }
        List<UserEncircleInfo> res = (List<UserEncircleInfo>) result.get("encircles");
        for (UserEncircleInfo tempInfo : res) {
            String nums = tempInfo.getEncircleNum();
            if (!nums.contains(CommonConstant.COMMON_STAR_STR)) {
                tempInfo.setEncircleNum(addStar2Balls(nums));
            }
        }
    }

    @Override
    public void followsKillNumListCompateUserId(long gameId, Map<String, Object> result, String versionCode, Integer
            clientType) {
        if (result == null || !result.containsKey("encircles") || result.get("encircles") == null || Integer.valueOf
                (versionCode) < CommonConstant.VERSION_CODE_3_2) {
            return;
        }
        List<UserEncircleInfo> res = (List<UserEncircleInfo>) result.get("encircles");
        List<UserEncircleInfoPack> userEncircleInfoPacks = new ArrayList<>();

        for (UserEncircleInfo tempInfo : res) {
            List<String> godList = userTitleService.getUserGodList(gameId, tempInfo.getEncircleUserId(), versionCode);

            UserEncircleInfoPack tempPack = new UserEncircleInfoPack(String.valueOf(tempInfo.getEncircleUserId()),
                    tempInfo, godList);
            userEncircleInfoPacks.add(tempPack);
        }
        result.put("encircles", userEncircleInfoPacks);
    }

    @Override
    public void classicEncircleListCompateUserId(long gameId, Map<String, Object> result, String versionCode) {
        if (result == null || !result.containsKey("encircles") || result.get("encircles") == null || Integer.valueOf
                (versionCode) < CommonConstant.VERSION_CODE_3_2) {
            return;
        }
        List<HashMap> res = (List<HashMap>) result.get("encircles");

        for (HashMap tempMap : res) {
            List<UserEncircleInfo> encircles = (List<UserEncircleInfo>) tempMap.get("encircles");
            List<UserEncircleInfoPack> userEncircleInfoPacks = new ArrayList<>();
            for (UserEncircleInfo tempInfo : encircles) {
                List<String> godList = userTitleService.getUserGodList(gameId, tempInfo.getEncircleUserId(),
                        versionCode);

                UserEncircleInfoPack tempPack = new UserEncircleInfoPack(String.valueOf(tempInfo.getEncircleUserId()),
                        tempInfo, godList);
                userEncircleInfoPacks.add(tempPack);
            }
            tempMap.put("encircles", userEncircleInfoPacks);
        }
    }

    @Override

    public void myEncirclesV2_3(Map result, long gameId, String versionCode) {
        if (result == null || !result.containsKey("encircles") || result.get("encircles") == null || Integer.valueOf
                (versionCode) < CommonConstant.VERSION_CODE_3_0) {
            return;
        }

        List<Map<String, Object>> encircles = (List<Map<String, Object>>) result.get("encircles");
        for (Map<String, Object> tempMap : encircles) {
            if (tempMap == null || !tempMap.containsKey("periodId") || tempMap.get("periodId") == null) {
                continue;
            }
            String periodId = tempMap.get("periodId").toString();
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
                List<Map<String, Object>> encircleDetails = (List<Map<String, Object>>) tempMap.get("encircleDetails");
                for (Map encircle : encircleDetails) {
                    String nums = encircle.get("encircleNum").toString();
                    if (!nums.contains(CommonConstant.COMMON_STAR_STR)) {
                        encircle.put("encircleNum", addStar2Balls(nums));
                    }
                }
            }
        }
    }

    @Override
    public void killNumDetail(Long gameId, String periodId, Map<String, Object> result, String versionCode) {
        if (result == null || result.get("datas") == null || Integer.valueOf(versionCode) < CommonConstant
                .VERSION_CODE_3_0) {
            return;
        }
        GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        if (StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
            List<EncircleKillNumVo> encircleKillNumVos = (List<EncircleKillNumVo>) result.get("datas");
            for (EncircleKillNumVo userKillCode : encircleKillNumVos) {
                String nums = userKillCode.getUserKillCode();
                if (!nums.contains(CommonConstant.COMMON_STAR_STR) && !nums.equals("杀号后可见")) {
                    userKillCode.setUserKillCode(addStar2Balls(nums));
                    userKillCode.setUserKillCode_3_2(addStar2Balls(nums));
                }
            }
        }
    }

    @Override
    public void killNumDetailCompateUserId(long gameId, Map<String, Object> result, String versionCode) {
        if (result == null || !result.containsKey("datas") || result.get("datas") == null) {
            return;
        }
        List<EncircleKillNumVo> res = (List<EncircleKillNumVo>) result.get("datas");

        List<EncircleKillNumVoPack> res1 = new ArrayList<>();
        for (EncircleKillNumVo tempMap : res) {
            EncircleKillNumVoPack pack = new EncircleKillNumVoPack(tempMap);
            res1.add(pack);
        }
        result.put("datas", res1);
    }

    @Override
    public void myKillNumsV2_3(Map result, Long gameId, String versionCode) {
        if (result == null || !result.containsKey("killNums") || result.get("killNums") == null || Integer.valueOf
                (versionCode) < CommonConstant.VERSION_CODE_3_0) {
            return;
        }

        List<Map<String, Object>> encircles = (List<Map<String, Object>>) result.get("killNums");
        for (Map<String, Object> tempMap : encircles) {
            if (tempMap == null || !tempMap.containsKey("periodId") || tempMap.get("periodId") == null) {
                continue;
            }
            String periodId = tempMap.get("periodId").toString();
            GamePeriod gamePeriod = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
            if (StringUtils.isBlank(gamePeriod.getWinningNumbers())) {
                List<Map<String, Object>> encircleDetails = (List<Map<String, Object>>) tempMap.get("killNumDetails");
                for (Map encircle : encircleDetails) {
                    String nums = encircle.get("killNumBack").toString();
                    Integer numType = (Integer) encircle.get("numType");
                    if (!nums.contains(CommonConstant.COMMON_STAR_STR) && numType == SocialEncircleKillConstant
                            .SOCIAL_PERSON_KILL_LIST_CODE_TYPE) {
                        encircle.put("killNumBack", addStar2Balls(nums));
                    }
                }
            }
        }
    }

    @Override
    public void followListCompate(Map<String, Object> resultMap, String versionCode, Integer clientType) {
        if (resultMap == null || !resultMap.containsKey("followList") || resultMap.get("followList") == null || Integer
                .valueOf(versionCode) < CommonConstant.VERSION_CODE_3_2 || clientType == null || clientType.equals
                (CommonConstant.CLIENT_TYPE_ANDRIOD)) {
            return;
        }
        List<FollowInfoVo> followInfoVos = (List<FollowInfoVo>) resultMap.get("followList");
        List<FollowInfoVoPack> followList = new ArrayList<>();

        for (FollowInfoVo followInfoVo : followInfoVos) {
            FollowInfoVoPack tempPack = new FollowInfoVoPack(String.valueOf(followInfoVo.getUserId()), followInfoVo);
            followList.add(tempPack);
        }
        resultMap.put("followList", followList);
    }

    @Override
    public Map<String, Object> exchangeDefaultPayChannel(Long userId, Integer clientType, Integer versionCode, String
            payAmount, Map<String, Object> res) {
        if (StringUtils.isBlank(payAmount) || (!clientType.equals(CommonConstant.CLIENT_TYPE_IOS_WISDOM_PREDICT) &&
                versionCode <= CommonConstant.VERSION_CODE_3_3) || !res.containsKey("paymentList") || res.get
                ("paymentList") == null) {
            return res;
        }
        List<Map<String, Object>> paymentList = (List<Map<String, Object>>) res.get("paymentList");
        if (paymentList != null && paymentList.size() > 0 && (versionCode >= CommonConstant.VERSION_CODE_4_0_1) ||
                clientType.equals(CommonConstant.CLIENT_TYPE_IOS_WISDOM_PREDICT)) {
            for (int i = 0; i < paymentList.size(); i++) {
                Map<String, Object> channel = paymentList.get(i);
                if (Integer.valueOf(channel.get("channelId").toString()).equals(CommonConstant.APPLE_PAY_CHANNEL_ID)) {
                    paymentList.remove(channel);
                }
            }
            res.put("paymentList", paymentList);
        }

        Long price = CommonUtil.multiply(payAmount, "100").longValue();

        if (paymentList.size() > 0) {
            for (Map<String, Object> channel : paymentList) {
                Integer channelAuthStatus = PayConstant.CHANNEL_AUTHENTICATE_NO_NEED;
                Integer cardFront = 0;
                Integer bindCard = 0;

                Long upperLimit = null;
                Long lowerLimit = null;
                String upperLimitStr = CommonUtil.getValueFromMap("upperLimit", channel);
                String lowerLimitStr = CommonUtil.getValueFromMap("lowerLimit", channel);
                if (StringUtils.isNotBlank(upperLimitStr)) {
                    upperLimit = Long.valueOf(upperLimitStr);
                }
                if (upperLimit != null && price > upperLimit) {
                    channel.put("status", 0);
                }

                if (StringUtils.isNotBlank(lowerLimitStr)) {
                    lowerLimit = Long.valueOf(lowerLimitStr);
                }
                if (lowerLimit != null && price < lowerLimit) {
                    channel.put("status", 0);
                }
                if (Integer.valueOf(channel.get("status").toString()) == 0) {
                    String imgUrl = PayUtil.getDisableChannelIcon(Integer.valueOf(channel.get("channelId").toString()
                    ), versionCode);
                    if (StringUtils.isNotBlank(imgUrl)) {
                        channel.put("channelIcon", imgUrl);
                    }
                }

                if (channel.containsKey("bindCard")) {
                    bindCard = Integer.valueOf(channel.get("bindCard").toString());
                }
                Integer realNameAuthenticate = 0;
                if (channel.containsKey("realNameAuthenticate")) {
                    realNameAuthenticate = Integer.valueOf(channel.get("realNameAuthenticate").toString());
                }
                if (bindCard.equals(CommonStatusEnum.YES.getStatus())) {
                    channelAuthStatus = PayConstant.CHANNEL_AUTHENTICATE_BIND_BANK;
                    if (userBankCardService.checkUserIfBankCard(userId)) {
                        channelAuthStatus = PayConstant.CHANNEL_AUTHENTICATE_SUCCESS;
                    }
                    cardFront = 1;
                } else if (realNameAuthenticate.equals(CommonStatusEnum.YES.getStatus())) {
                    channelAuthStatus = PayConstant.CHANNEL_AUTHENTICATE_REAL_NAME;
                    if (userInfoService.checkUserIfAuthenticate(userId)) {
                        channelAuthStatus = PayConstant.CHANNEL_AUTHENTICATE_SUCCESS;
                    }
                }
                channel.put("channelAuthStatus", channelAuthStatus);
                channel.put("cardFront", cardFront);
            }
        }

        res.put("paymentList", setDefaultChannel(userId, paymentList, price));

        if (paymentList != null) {
            PayUtil.sortedPaymentList(paymentList);
        }

        if (paymentList.size() == 1) {
            Map<String, Object> channel = paymentList.get(0);
            channel.put("isDefault", 1);
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> setDefaultChannel(Long userId, List<Map<String, Object>> paymentList, Long price) {
        //获取用户最近支付成功的支付渠道
        Integer useChannel = userAccountFlowDao.getUserRecentAccountFlow(userId, CommonConstant.PAY_TYPE_CASH,
                CommonConstant.PAY_STATUS_HANDLED);
        Boolean checkBalance = Boolean.TRUE;
        Boolean channelExist = Boolean.FALSE;
        for (Map<String, Object> payment : paymentList) {
            if (Integer.valueOf(payment.get("channelId").toString()).equals(useChannel)) {
                channelExist = Boolean.TRUE;
                if (payment.containsKey("status") && payment.get("status") != null && Integer.valueOf(payment.get
                        ("status").toString()) == 0) {
                    useChannel = null;
                }
            }
        }
        if (!channelExist) {
            useChannel = null;
        }

        if (useChannel == null) {
            checkBalance = userAccountService.checkUserBalance(userId, CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, price);
            if (checkBalance) {
                useChannel = CommonConstant.WISDOM_COIN_CHANNEL_ID;
            }
        }

        Boolean setDefault = Boolean.FALSE;
        if (useChannel != null) {
            setDefault = Boolean.TRUE;
        }
        for (Map<String, Object> channel : paymentList) {
            if (channel.containsKey("status") && channel.get("status") != null) {
                Integer status = Integer.valueOf(channel.get("status").toString());
                Integer channeId = Integer.valueOf(channel.get("channelId").toString());
                if (status.equals(0) || (!checkBalance && channeId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID))) {
                    channel.put("isDefault", 0);
                    continue;
                }
                if (!setDefault) {
                    channel.put("isDefault", 1);
                    setDefault = Boolean.TRUE;
                } else {
                    if (channeId.equals(useChannel)) {
                        channel.put("isDefault", 1);
                    } else {
                        channel.put("isDefault", 0);
                    }
                }

                if (!checkBalance && Integer.valueOf(channel.get("channelId").toString()).equals(CommonConstant
                        .WISDOM_COIN_CHANNEL_ID)) {
                    channel.put("tags", PayUtil.getPayChannelNotEnoughTags(CommonConstant.WISDOM_COIN_CHANNEL_ID));
                }
            }
        }

        return paymentList;
    }

    @Override
    public Map<String, Object> programChangeOrderBug(Map<String, Object> res, Integer clientId, String versionCode) {
        //客户端3.5不支持排序所以做版本兼容
        if (res == null || clientId == null || versionCode == null) {
            return res;
        }
        if (!res.containsKey("programList") || !CommonConstant.CLIENT_TYPE_ANDRIOD.equals(clientId) || Integer
                .valueOf(versionCode) > CommonConstant.VERSION_CODE_3_5 || res.get("programList") == null) {
            return res;
        }
        List<Map<String, Object>> tempRes = (List<Map<String, Object>>) res.get("programList");
        for (int i = 0; i < tempRes.size(); i++) {
            Map<String, Object> temp = tempRes.get(i);
            temp.put("programType", i);
        }
        return res;
    }

    @Override
    public void temporaryIosSignControl(Map<String, Object> result, Integer clientType, Integer versionCode, Long
            userId) {
        if (clientType == null) {
            return;
        }
        Map<String, Object> popContent = (Map<String, Object>) result.get("popContent");
        Boolean userSign = userSignService.checkUserSign(userId, DateUtil.formatDate(DateUtil.getCurrentTimestamp(),
                DateUtil.formatTab[DateUtil.FMT_DATE_SPECIAL]), CommonUtil.getUserSignTypeByVersion(clientType,
                versionCode));
        if (userSign) {
            popContent.put("btnMsg", "已签到");
        } else {
            popContent.put("jumpUrl", "");
        }
        result.put("popContent", popContent);
    }

    @Override
    public void sportsRecommendRemunerationControl(Map<String, Object> result, Integer versionCode) {
        if (result == null || !result.containsKey("remuneration")) {
            return;
        }

        List<Map<String, Object>> remunerations = (List<Map<String, Object>>) result.get("remuneration");
        List<Map<String, Object>> tempRemunerations = new ArrayList<>();
        if (remunerations != null) {
            for (int i = 0; i < remunerations.size(); i++) {
                Map<String, Object> tempRemuneration = remunerations.get(i);
                if (tempRemuneration != null) {
                    Boolean vipFlag = (Boolean) tempRemuneration.get("vipFlag");
                    if (vipFlag && versionCode < CommonConstant.VERSION_CODE_4_4) {
                        continue;
                    }
                    tempRemunerations.add(tempRemuneration);
                }
            }
        }
        result.put("remuneration", tempRemunerations);
    }

    @Override
    public Map<String, Object> iosReviewNotShowWorldCup(Map<String, Object> data, Integer versionCode, Integer
            clientType) {
        if (clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) || !data.containsKey("dateMatchList")) {
            return data;
        }
        boolean iosReview = CommonUtil.getIosReview(versionCode) == 0;
        if (!iosReview) {
            return data;
        }
        List<Map<String, Object>> dateMatchList = (List<Map<String, Object>>) data.get("dateMatchList");
        for (Map<String, Object> temp : dateMatchList) {
            List<Map<String, Object>> matches = (List<Map<String, Object>>) temp.get("match");
            for (Map<String, Object> matchInfo : matches) {
                if (matchInfo.get("matchName") != null && matchInfo.get("matchName").toString().equals("世界杯")) {
                    matchInfo.put("matchName", "");
                    matchInfo.put("tags", null);
                }
            }
        }
        data.put("dateMatchList", dateMatchList);
        return data;
    }

    @Override
    public void recommendListWorldCup(Map<String, Object> res, Integer versionCode, Integer clientType) {
        if (clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) || !res.containsKey("datas")) {
            return;
        }
        boolean iosReview = CommonUtil.getIosReview(versionCode) == 0;
        if (!iosReview) {
            return;
        }
        List<Map<String, Object>> dataAll = (List<Map<String, Object>>) res.get("datas");

        for (Map<String, Object> matchInfo : dataAll) {
            if (matchInfo.get("leagueMatch") != null && matchInfo.get("leagueMatch").toString().equals("世界杯")) {
//                matchInfo.put("matchName", "");
                matchInfo.put("tags", null);
                matchInfo.put("matchDesc", matchInfo.get("matchDesc").toString().replaceAll("世界杯", ""));
            }
        }
    }

    @Override
    public void iosReviewMatchPredict(Map<String, Object> res, Integer versionCode, Integer clientType) {
        if (clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) || !res.containsKey("predicts")) {
            return;
        }
        boolean iosReview = CommonUtil.getIosReview(versionCode) == 0;
        if (!iosReview) {
            return;
        }

        List<Map<String, Object>> matches = (List<Map<String, Object>>) res.get("predicts");
        if (matches == null || matches.size() == 0) {
            return;
        }
        for (Map<String, Object> match : matches) {
            if (match.get("leagueMatch") != null && match.get("leagueMatch").toString().equals("世界杯")) {
                match.put("tags", null);
                match.put("matchDesc", match.get("matchDesc").toString().replaceAll("世界杯", ""));
            }
        }
    }

    @Override
    public void userPurchaseSportsIosReview(Map<String, Object> res, Integer versionCode, Integer clientType) {
        if (clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) || !res.containsKey("datas")) {
            return;
        }
        boolean iosReview = CommonUtil.getIosReview(versionCode) == 0;
        if (!iosReview) {
            return;
        }
        List<Map<String, Object>> matches = (List<Map<String, Object>>) res.get("datas");
        for (Map<String, Object> temp : matches) {
            if (temp.get("matchDesc") != null && temp.get("matchDesc").toString().contains("世界杯")) {
                temp.put("matchDesc", temp.get("matchDesc").toString().replaceAll("世界杯", ""));
                temp.put("tags", null);
            }
        }
    }

    @Override
    public void sportsSocialPersonCenterIosReview(Map<String, Object> res, Integer versionCode, Integer clientType) {
        if (clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) || !res.containsKey("recommend")) {
            return;
        }
        boolean iosReview = CommonUtil.getIosReview(versionCode) == 0;
        if (!iosReview) {
            return;
        }

        List<Map<String, Object>> matches = (List<Map<String, Object>>) res.get("recommend");
        for (Map<String, Object> temp : matches) {
            if (temp.get("matchDesc") != null && temp.get("matchDesc").toString().contains("世界杯")) {
                temp.put("matchDesc", temp.get("matchDesc").toString().replaceAll("世界杯", ""));
                temp.put("tags", null);
            }
        }
    }

    private String addStar2Balls(String balls) {
        if (StringUtils.isBlank(balls)) {
            return balls;
        }
        balls = balls.replaceAll(CommonConstant.COMMA_SPLIT_STR, CommonConstant.COMMA_SPLIT_STR + CommonConstant
                .COMMON_STAR_STR);
        balls = CommonConstant.COMMON_STAR_STR + balls;
        return balls;
    }
}
