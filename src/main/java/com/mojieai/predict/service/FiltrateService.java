package com.mojieai.predict.service;

import java.util.List;

/**
 * Created by tangxuelong on 2017/8/7.
 */
public interface FiltrateService {
    List<String> FiltrateNumber(String lotteryNumber);

    List<String> FiltrateNumberAC(String lotteryNumber);
}
