package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BannerDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.Banner;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BannerDaoImpl extends BaseDao implements BannerDao {
    @Override
    public List<Banner> getAllUsableBanners() {
        return sqlSessionTemplate.selectList("Banner.getAllUsableBanners");
    }

    @Override
    public List<Banner> getAllBanners() {
        return sqlSessionTemplate.selectList("Banner.getAllBanners");
    }

    @Override
    public void insertBanner(Banner banner) {
        sqlSessionTemplate.insert("Banner.insert", banner);
    }

    @Override
    public int updateBanner(Banner banner) {
        return sqlSessionTemplate.update("Banner.update", banner);
    }
}
