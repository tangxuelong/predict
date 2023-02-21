package com.mojieai.predict.cache;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.IniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.BannerDao;
import com.mojieai.predict.entity.po.Banner;
import com.mojieai.predict.entity.vo.BannerVo;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class BannerCache {
    //todo 改成Map类型 key gameId_type
    private static final Logger log = LogConstant.commonLog;

    private static List<Banner> bannerList = new ArrayList<>();
    private static Map<Integer, List<Banner>> bannerPositionMap = new HashMap<>();

    public static Integer ACTION_TYPE_H5 = 0;//跳转h5
    public static Integer ACTION_TYPE_NATIVE = 1;//本地

    public static Integer POSITION_TYPE_TOOL = 0;//工具banner
    public static Integer POSITION_TYPE_USER = 1;//个人中心banner
    public static Integer POSITION_TYPE_FOOTBALL_INDEX = 2;//足彩主页
    public static Integer POSITION_TYPE_DIGIT_SOCIAL_INDEX = 3;//数字彩社区主页
    public static Integer POSITION_TYPE_DIGIT_INDEX = 4;//数字彩主页

    @Autowired
    private BannerDao bannerDao;

    private BannerCache() {
    }


    public void init() {
        log.info("init BannerCache");
        refresh();
    }

    public void refresh() {
        bannerPositionMap.clear();
        bannerList = bannerDao.getAllBanners();
        for (Banner banner : bannerList) {
            List<Banner> banners = null;
            if (bannerPositionMap.containsKey(banner.getPositionType())) {
                banners = bannerPositionMap.get(banner.getPositionType());
            } else {
                banners = new ArrayList<>();
            }
            banners.add(banner);
            bannerPositionMap.put(banner.getPositionType(), banners);
        }
        log.info("refresh " + (bannerList == null ? 0 : bannerList.size()) + " banner");
    }

    public static List<Banner> getAllUsableBannerList() {
        // 过滤掉开始时间和结束时间
        List<Banner> banners = new ArrayList<>();
        for (Banner banner : bannerList) {
            if (DateUtil.compareDate(banner.getStartTime(), DateUtil.getCurrentTimestamp()) && DateUtil.compareDate
                    (DateUtil.getCurrentTimestamp(), banner.getEndTime())) {
                banners.add(banner);
            }
        }
        return banners;
    }

    @Deprecated
    public static List<BannerVo> getBannerVos(Integer positionType, Integer versionCode, Integer clientType) {

        List<Banner> banners = BannerCache.getAllUsableBannerList();
        List<BannerVo> bannerVos = new ArrayList<>();
        if (clientType == null) {
            clientType = CommonConstant.CLIENT_TYPE_ANDRIOD;
        }
        if (banners != null && !banners.isEmpty()) {
            int bannerCount = IniCache.getIniIntValue(IniConstant.BANNER_COUNT, 5);
            int counter = 0;
            for (Banner banner : banners) {
                if (counter > bannerCount) {
                    break;
                }
                if (clientType.equals(CommonConstant.CLIENT_TYPE_ANDRIOD) && versionCode < CommonConstant
                        .VERSION_CODE_4_3 && banner.getActionType().equals(ACTION_TYPE_NATIVE)) {
                    continue;
                }
                if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && versionCode < CommonConstant
                        .VERSION_CODE_4_1 && banner.getActionType().equals(ACTION_TYPE_NATIVE)) {
                    continue;
                }
                if (positionType != banner.getPositionType()) {
                    continue;
                }
                Integer[] ignoreId1 = {127, 128, 129, 130};
                List<Integer> ignoreIdList1 = Arrays.asList(ignoreId1);
                if (clientType.equals(CommonConstant.CLIENT_TYPE_IOS) && versionCode <= CommonConstant
                        .VERSION_CODE_4_4_1 && ignoreIdList1.contains(banner.getBannerId())) {
                    continue;
                }
                if (CommonUtil.getIosReview(versionCode).equals(0) && clientType.equals(CommonConstant.CLIENT_TYPE_IOS)) {
                    Integer[] ignoreId = {117, 115, 116, 103, 104, 107, 108, 109, 118, 119, 120, 121, 127, 122, 123,
                            124, 125, 126, 128, 129, 130, 132, 133, 136, 138, 139};
                    List<Integer> ignoreIdList = Arrays.asList(ignoreId);
                    if (ignoreIdList.contains(banner.getBannerId())) {
                        continue;
                    }
                }

                if (banner.getStartTime().before(DateUtil.getCurrentTimestamp()) && banner.getEndTime().after(DateUtil
                        .getCurrentTimestamp())) {
                    bannerVos.add(new BannerVo(banner.getBannerId(), banner.getTitle(), banner.getImgUrl(), banner
                            .getDetailUrl(), banner.getWeight(), banner.getGameId(), banner.getActionType()));
                    counter++;
                }
            }
        }
        return bannerVos;
    }

    /**
     * 获取banner
     * 4.6版本之后新接口
     *
     * @param positionType
     * @param versionCode
     * @param clientType
     * @return
     */
    public static List<BannerVo> getBannerVosV2(Integer positionType, Integer versionCode, Integer clientType) {
        List<Banner> banners = bannerPositionMap.get(positionType);
        if (banners == null || banners.size() == 0) {
            return null;
        }
        if (CommonUtil.getIosReview(versionCode, clientType).equals(CommonConstant.IOS_REVIEW_STATUS_WAIT)) {
            return null;
        }
        List<BannerVo> result = new ArrayList<>();
        int counter = 0;
        int bannerCount = IniCache.getIniIntValue(IniConstant.BANNER_COUNT, 5);
        for (Banner banner : banners) {
            if (counter > bannerCount) {
                break;
            }
            if (banner.getExclusiveClientId() != null && banner.getExclusiveClientId().contains(String.valueOf
                    (clientType))) {
                continue;
            }

            if (DateUtil.getCurrentTimestamp().before(banner.getStartTime()) || DateUtil.getCurrentTimestamp().after
                    (banner.getEndTime())) {
                continue;
            }

            result.add(new BannerVo(banner.getBannerId(), banner.getTitle(), banner.getImgUrl(), banner.getDetailUrl
                    (), banner.getWeight(), banner.getGameId(), banner.getActionType()));
            counter++;
        }

        return result;
    }
}
