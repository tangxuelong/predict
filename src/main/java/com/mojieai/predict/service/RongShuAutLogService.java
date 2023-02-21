package com.mojieai.predict.service;

import java.util.Map;

public interface RongShuAutLogService {

    Boolean addRongShuAutLog(Long userId, Integer autType, Integer autStatus, String rongShuResult);
}
