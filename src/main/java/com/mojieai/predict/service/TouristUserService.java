package com.mojieai.predict.service;

import java.util.Map;

public interface TouristUserService {
    Map<String, Object> checkDeviceId(String deviceId, String channelType);

    boolean checkUserIdIsTourist(Long userId);
}
