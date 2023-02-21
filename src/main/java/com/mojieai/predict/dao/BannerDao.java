package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.Banner;

import java.util.List;

/**
 * Created by Ynght on 2016/10/24.
 */
public interface BannerDao {
    List<Banner> getAllUsableBanners();

    List<Banner> getAllBanners();

    void insertBanner(Banner banner);

    int updateBanner(Banner banner);
}
