package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.TouristUser;

public interface TouristUserDao {
    TouristUser getUserIdByDeviceId(String deviceId);

    TouristUser getUserIdByToken(String userToken);

    TouristUser getUserByUserId(Long userId);

    Integer insert(TouristUser touristUser);
}
