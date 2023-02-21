package com.mojieai.predict.entity.po;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户体育推荐
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class UserSportSocialRecommend {
    private String recommendId;
    private Long userId;
    private Integer lotteryCode;
    private String matchId;
    private Integer playType;
    private String recommendInfo;
    private Timestamp endTime;
    private Integer itemId;
    private Long price;
    private String baseOn;
    private String reason;
    private Integer isRight;
    private Integer isDistribute;
    private String handicap;
    private Integer awardAmount;
    private Integer saleCount;
    private Integer couponSaleCount;
    private String recommendTitle;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;

    public UserSportSocialRecommend(String recommendId, Long userId, String matchId, Integer lotteryCode, Integer
            playType, String recommendInfo, Integer itemId, Long price, String baseOn, String reason, Integer isRight,
                                    Integer isDistribute, String handicap, Timestamp endTime, String remark) {
        this.recommendId = recommendId;
        this.userId = userId;
        this.matchId = matchId;
        this.playType = playType;
        this.recommendInfo = recommendInfo;
        this.itemId = itemId;
        this.price = price;
        this.baseOn = baseOn;
        this.reason = CommonUtil.filterEmoji(reason);
        this.isRight = isRight;
        this.isDistribute = isDistribute;
        this.lotteryCode = lotteryCode;
        this.handicap = handicap;
        this.endTime = endTime;
        this.couponSaleCount = 0;
        this.createTime = DateUtil.getCurrentTimestamp();
        this.remark = StringUtils.isBlank(remark) ? "" : remark;
    }

    public Map<String, Object> remark2marks() {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (!StringUtils.isBlank(remark)) {
                Map<String, Object> remarkMap = JSONObject.parseObject(remark, HashMap.class);
                List<Object> marks = new ArrayList<>();
                if (null != remarkMap.get("singleRecommend")) {
                    Map<String, Object> singleRecommend = new HashMap<>();
                    singleRecommend.put("img", CommonConstant.SINGLE_RECOMMEND_IMG_URL);
                    singleRecommend.put("ratio", CommonConstant.SINGLE_RECOMMEND_IMG_REDIO);
                    marks.add(singleRecommend);
                }
                if (null != remarkMap.get("analysis")) {
                    Map<String, Object> analysis = new HashMap<>();
                    analysis.put("img", CommonConstant.ANY_RECOMMEND_IMG_URL);
                    analysis.put("ratio", CommonConstant.ANY_RECOMMEND_IMG_REDIO);
                    marks.add(analysis);
                }
                resultMap.put("marks", marks);
                if (null != remarkMap.get("mainRecommend")) {
                    resultMap.put("mainRecommend", remarkMap.get("mainRecommend"));
                }
            }
        } catch (Exception e) {
            throw new BusinessException("remark2marks is error remark:" + remark);
        }
        return resultMap;
    }
}