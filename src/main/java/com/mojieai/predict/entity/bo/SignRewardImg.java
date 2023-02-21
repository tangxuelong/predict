package com.mojieai.predict.entity.bo;

import com.mojieai.predict.cache.SignRewardCache;
import com.mojieai.predict.util.CommonUtil;
import lombok.Data;

import java.io.Serializable;

@Data
public class SignRewardImg implements Serializable {
    private static final long serialVersionUID = 9128808107479501347L;

    private String vipImg = CommonUtil.getWebDomain() + "user_sign_vip.png";
    private String moreGoldCoinImg = CommonUtil.getWebDomain() + "user_sign_more_gold_coin.png";
    private String oneGoldCoinImg = CommonUtil.getWebDomain() + "user_sign_single_gold_coin.png";
    private String redPacketImg = CommonUtil.getWebDomain() + "user_sign_hong_bao.png";
    private String moreRedPacketImg = CommonUtil.getWebDomain() + "user_sign_hong_bao.png";


    public String getUserSignRewardImg(Integer rewardType, Integer rewardCount) {
        if (rewardType.equals(SignRewardCache.REWARD_SIGN_TYPE_VIP)) {
            return vipImg;
        } else if (rewardType.equals(SignRewardCache.REWARD_SIGN_TYPE_WISDOM)) {
            if (rewardCount > 1000) {
                return moreRedPacketImg;
            }
            return redPacketImg;
        } else if (rewardType.equals(SignRewardCache.REWARD_SIGN_TYPE_GOLD_COIN)) {
            if (rewardCount > 2) {
                return moreGoldCoinImg;
            }
            return oneGoldCoinImg;
        }
        return "";
    }
}
