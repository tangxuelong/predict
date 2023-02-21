package com.mojieai.predict.service;

import java.util.List;
import java.util.Map;

public interface IndependPopService {

    List<Map<String, Object>> getSocialPopup(Long userId);

    List<Map<String, Object>> getSportsSocialPopup(Long userId);
}
