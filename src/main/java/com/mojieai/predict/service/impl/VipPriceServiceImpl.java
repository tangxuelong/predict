package com.mojieai.predict.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.cache.VipPriceCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.dao.ActivityInfoDao;
import com.mojieai.predict.dao.ActivityUserInfoDao;
import com.mojieai.predict.entity.po.ActivityInfo;
import com.mojieai.predict.entity.po.ActivityUserInfo;
import com.mojieai.predict.entity.po.VipMember;
import com.mojieai.predict.entity.po.VipPrice;
import com.mojieai.predict.service.ActivityService;
import com.mojieai.predict.service.VipMemberService;
import com.mojieai.predict.service.VipPriceService;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.util.VipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Service
public class VipPriceServiceImpl implements VipPriceService {
    private static final org.apache.logging.log4j.Logger log = LogConstant.commonLog;

    @Autowired
    private VipMemberService vipMemberService;
    @Autowired
    private ActivityUserInfoDao activityUserInfoDao;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityInfoDao activityInfoDao;

    @Override
    public Map<String, Object> getVipSaleList(Long userId, Integer clientType, Integer versionCode, Integer vipType) {
        if (vipType == null) {
            vipType = VipMemberConstant.VIP_MEMBER_TYPE_DIGIT;
        }
        String vipDeadLine = "您还不是" + VipUtils.getVipCnByType(vipType) + "VIP";
        boolean isVip = false;
        Map<String, Object> result = new HashMap<>();
        VipMember userVip = vipMemberService.getUserVipMemberRedisAndDb(userId, vipType);
        if (userVip != null && userVip.getStatus().equals(VipMemberConstant.VIP_MEMBER_STATUS_ENABLE) && DateUtil
                .compareDate(new Date(), userVip.getEndTime())) {
            vipDeadLine = "会员有效期至：" + DateUtil.formatTime(userVip.getEndTime(), "yyyy-MM-dd");
            isVip = true;
        }
        List<VipPrice> vipPrices = VipPriceCache.getAllRealMoneyPriceByClientType(clientType, vipType);
        List<Map> vipPriceList = packageVipPrice(vipPrices, isVip, userId, vipType);
        //vip特权
        List<Map> vipPrivileges = getVipPrivilegeByType(vipType);

        if (checkVersionClientTypeActivityStatus(clientType, versionCode)) {
            activityPricePackage(vipPriceList, userId, vipType);
        }

        result.put("isVip", vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_DIGIT));
        result.put("isSportsVip", vipMemberService.checkUserIsVip(userId, VipMemberConstant.VIP_MEMBER_TYPE_SPORTS));
        result.put("vipDeadLine", vipDeadLine);
        result.put("vipPriceList", vipPriceList);
        result.put("vipPrivilege", vipPrivileges);
        return result;
    }

    private boolean checkVersionClientTypeActivityStatus(Integer clientType, Integer versionCode) {
        boolean activityStatus = true;
        if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && versionCode < CommonConstant.VERSION_CODE_4_1) {
            return false;
        }

        if (clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) && versionCode < CommonConstant.VERSION_CODE_4_3) {
            return false;
        }

        if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && CommonUtil.getIosReview(versionCode) == 0) {
            activityStatus = false;
        }

        return activityStatus;
    }

    private List<Map> getVipPrivilegeByType(Integer vipType) {
        List<Map> result = null;
        try {
            String key = ActivityIniConstant.VIP_PRIVILEGE_KEYS;
            String afterKey = ActivityIniConstant.VIP_PRIVILEGE_IMG_AFTER;
            if (vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
                key = ActivityIniConstant.VIP_SPORTS_PRIVILEGE_KEYS;
                afterKey = ActivityIniConstant.VIP_SPORTS_PRIVILEGE_IMG_AFTER;
            }
            String vipPrivilege = ActivityIniCache.getActivityIniValue(key);
            result = JSONObject.parseObject(vipPrivilege, ArrayList.class);
            int count = 1;
            for (Map temp : result) {
                String img = ActivityIniCache.getActivityIniValue(temp.get("key") + afterKey);
                temp.put("img", img);
                temp.put("desc", count + CommonConstant.COMMON_PAUSE_STR_CN + temp.get("desc"));
                count++;
            }
        } catch (Exception e) {
            log.error("vipPrivilege异常", e);
        }
        return result;
    }

    private void activityPricePackage(List<Map> vipPriceList, Long userId, Integer vipType) {
        //201803001 新手活动一个月10块钱
        Boolean activityPermission = false;
        Integer activityId = 201803001;
        if (vipType != null && vipType.equals(VipMemberConstant.VIP_MEMBER_TYPE_SPORTS)) {
            activityId = 201806004;
        }
        ActivityInfo activityInfo = activityInfoDao.getActivityInfo(activityId);
        ActivityUserInfo activityUserInfo = activityUserInfoDao.getUserTotalTimes(activityId, userId);
        if ((null == activityUserInfo || activityUserInfo.getTotalTimes() != 2) && activityService
                .checkActivityIsEnabled(activityId)) {
            activityPermission = true;
        }
        for (Map price : vipPriceList) {
            String activityImg = "";
            String ratio = "";
            Integer activityStatus = 0;
            String activityDiscount = null;
            String activityName = null;
            if (Integer.valueOf(price.get("originDate").toString()).equals(30) && activityPermission) {
//                activityImg = "http://sportsimg.mojieai.com/vip_activity_title_new.png";
//                ratio = "142:35";
                VipPrice vipPrice = VipPriceCache.getVipPriceById(Integer.valueOf(price.get("vipPriceId").toString()));
                Map<String, Object> remarkMap = JSONObject.parseObject(activityInfo.getRemark(), HashMap.class);

                String discountAmount = remarkMap.get("discountAmount").toString();
                activityImg = remarkMap.get("img").toString();
                ratio = remarkMap.get("ratio").toString();

                BigDecimal activityPrice = CommonUtil.subtract(vipPrice.getOriginPrice().toString(), discountAmount);
//                price.put("originPrice", convertBig2Str(CommonUtil.convertFen2Yuan(activityPrice.toString())));
//                price.put("originPriceStr", "￥" + convertBig2Str(CommonUtil.convertFen2Yuan(activityPrice.toString())));
                price.put("price", convertBig2Str(CommonUtil.convertFen2Yuan(activityPrice.toString())));
                price.put("imgUrl", "");
                activityStatus = 1;
                activityDiscount = "-" + CommonUtil.removeZeroAfterPoint(CommonUtil.convertFen2Yuan(discountAmount)
                        .toString());
                activityName = "首购优惠";
            }
            Map<String, Object> tags = new HashMap<>();
            tags.put("img", activityImg);
            tags.put("ratio", ratio);
            price.put("tags", tags);
            price.put("activityStatus", activityStatus);
            price.put("activityDiscount", activityDiscount);
            price.put("activityName", activityName);
        }
    }

    private List<Map> packageVipPrice(List<VipPrice> vipPriceList, boolean isVip, Long userId, Integer vipType) {
        List<Map> packageVipPrice = null;
        if (vipPriceList == null) {
            return packageVipPrice;
        }
        packageVipPrice = new ArrayList<>();
        for (VipPrice vipPrice : vipPriceList) {
            Map temp = new HashMap();
            temp.put("vipPriceId", vipPrice.getVipPriceId());
            temp.put("purchaseDate", VipUtils.convertDateShow(vipPrice.getVipDate()));
            temp.put("originDate", vipPrice.getVipDate());
            temp.put("originPrice", convertBig2Str(CommonUtil.convertFen2Yuan(vipPrice.getOriginPrice())));
            temp.put("originPriceStr", "￥" + convertBig2Str(CommonUtil.convertFen2Yuan(vipPrice.getOriginPrice())));
            if (Objects.equals(vipPrice.getOriginPrice(), vipPrice.getPrice())) {
                temp.put("originPriceStr", "");
            }
            temp.put("price", convertBig2Str(CommonUtil.convertFen2Yuan(vipPrice.getPrice())));
            temp.put("imgUrl", vipPrice.getDiscountImg());
            String payBtnMsg = "开通";
            Timestamp validityDate = DateUtil.getCurrentTimestamp();
            if (isVip) {
                VipMember vipMember = vipMemberService.getUserVipMemberRedisAndDb(userId, vipType);
                validityDate = vipMember.getEndTime();
                payBtnMsg = "续费";
            }
            String validityDateStr = DateUtil.formatTime(DateUtil.getIntervalDays(validityDate, vipPrice.getVipDate()),
                    "yyyy-MM-dd");
            temp.put("payBtnMsg", payBtnMsg);
            temp.put("validityDate", validityDateStr);
            packageVipPrice.add(temp);
        }
        return packageVipPrice;
    }

    private String convertBig2Str(BigDecimal num) {
        if (num == null) {
            return "";
        }
        if (num.doubleValue() % 1.0 == 0) {
            return num.longValue() + "";
        }
        return num.toString();
    }
}
