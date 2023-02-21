package com.mojieai.predict.cache;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.dao.UserSignRewardDao;
import com.mojieai.predict.entity.po.UserSignReward;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class SignRewardCache {
    public static Integer SIGN_TYPE_COMMON = 0;
    public static Integer SIGN_TYPE_CYCLE = 1;
    public static Integer SIGN_TYPE_CYCLE_ACTIVITY = 2;
    public static Integer REWARD_SIGN_TYPE_GOLD_COIN = 0;
    public static Integer REWARD_SIGN_TYPE_CASH = 1;
    public static Integer REWARD_SIGN_TYPE_WISDOM = 2;
    public static Integer REWARD_SIGN_TYPE_VIP = 3;
    public static Integer IF_REWARD_NO = 0;
    public static Integer IF_REWARD_YES = 1;

    private static Map<String, Integer> signRewardMap = new HashMap<>();
    private static List<UserSignReward> userSignRewards;
    public static String defaultKey = SIGN_TYPE_COMMON + CommonConstant.COMMON_COLON_STR + REWARD_SIGN_TYPE_GOLD_COIN +
            CommonConstant.COMMON_COLON_STR + 1;

    @Autowired
    private UserSignRewardDao userSignRewardDao;

    public void init() {
        refresh();
    }

    public void refresh() {
        userSignRewards = userSignRewardDao.getAllSignReward();
        for (UserSignReward signReward : userSignRewards) {
            signRewardMap.put(signReward.getSignType() + CommonConstant.COMMON_COLON_STR + signReward.getRewardType()
                    + CommonConstant.COMMON_COLON_STR + signReward.getSignCount(), signReward.getSignReward());
        }
    }

    public static Integer getSignReward(Integer signType, Integer rewardType, Integer signCount) {
        String keys = signType + CommonConstant.COMMON_COLON_STR + rewardType + signCount;
        Integer result = signRewardMap.get(keys);
        if (result == null) {
            result = signRewardMap.get(defaultKey);
        }
        return result;
    }

    public static List<UserSignReward> getSignReward(Integer signType) {
        List<UserSignReward> result = new ArrayList<>();
        for (UserSignReward reward : userSignRewards) {
            if (reward.getSignType().equals(signType)) {
                result.add(reward);
            }
        }
        result = result.stream().sorted(Comparator.comparing(UserSignReward::getSignCount)).collect(Collectors.toList
                ());
        return result;
    }

    public static UserSignReward getSignReward(Integer signType, Integer signCount) {
        for (UserSignReward reward : userSignRewards) {
            if (reward.getSignType().equals(signType) && reward.getSignCount().equals(signCount)) {
                return reward;
            }
        }
        return null;
    }

    public static String getRewardImg(int signCount) {
        return "";
    }
}
