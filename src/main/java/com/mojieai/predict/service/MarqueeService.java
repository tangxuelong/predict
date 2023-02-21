package com.mojieai.predict.service;

import java.util.List;
import java.util.Map;

public interface MarqueeService {

    List<Map<String, Object>> getRecentMarqueeInfo();

    void saveContent2Marquee(String marqueeTitle, String pushUrl);
}
