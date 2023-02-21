package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.service.FiltrateService;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tangxuelong on 2017/8/7.
 */
@Service
public class FiltrateServiceImpl implements FiltrateService {
    protected Logger log = LogConstant.commonLog;

    @Override
    public List<String> FiltrateNumber(String lotteryNumber) {
        String[] lotteryNumberArr = lotteryNumber.split(CommonConstant.SPACE_SPLIT_STR);
        int count = 0;
        //
        List<String> hezhiResultNumbers = new ArrayList<>();


        /*if (lotteryNumberArr.length == 0) {
            return resultNumbers;
        }*/
        Long startTime = System.currentTimeMillis();
        log.info("start filter hezhi>>>>>>>>>>>>>>>>>>>>");
        Integer[] arr = new Integer[6];
        for (int a = 0; a <= 27; a++) {
            for (int b = a + 1; b <= 28; b++) {
                for (int c = b + 1; c <= 29; c++) {
                    for (int d = c + 1; d <= 30; d++) {
                        for (int e = d + 1; e <= 31; e++) {
                            for (int f = e + 1; f <= 32; f++) {
                                count++;
                                arr[0] = Integer.valueOf(lotteryNumberArr[a]);
                                arr[1] = Integer.valueOf(lotteryNumberArr[b]);
                                arr[2] = Integer.valueOf(lotteryNumberArr[c]);
                                arr[3] = Integer.valueOf(lotteryNumberArr[d]);
                                arr[4] = Integer.valueOf(lotteryNumberArr[e]);
                                arr[5] = Integer.valueOf(lotteryNumberArr[f]);
                                if (arr[1] == arr[2] || arr[1] == arr[3] || arr[1] == arr[4] || arr[1] == arr[5] || arr[2] == arr[3
                                        ] || arr[2] == arr[4] || arr[2] == arr[5] || arr[3] == arr[4] || arr[3] == arr[5] || arr[4
                                        ] == arr[5]) {
                                    log.info(arr);
                                } else {
                                    int sum = arr[0] + arr[1] + arr[2] + arr[3] + arr[4] + arr[5];
                                    if (sum >= 100 && sum <= 120) {
                                        hezhiResultNumbers.add(arr.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Long endTime = System.currentTimeMillis() - startTime;
        log.info("end filter hezhi>>>>>>>>>>>>>>>>>>>>" + endTime);
        log.info(count);

        return hezhiResultNumbers;
    }

    @Override
    public List<String> FiltrateNumberAC(String lotteryNumber) {
        String[] lotteryNumberArr = lotteryNumber.split(CommonConstant.SPACE_SPLIT_STR);
        int count = 0;
        //
        List<String> ACResultNumbers = new ArrayList<>();


        /*if (lotteryNumberArr.length == 0) {
            return resultNumbers;
        }*/

        Long startTime = System.currentTimeMillis();
        log.info("start filter AC>>>>>>>>>>>>>>>>>>>>");
        Integer[] arr = new Integer[6];
        for (int a = 0; a <= 27; a++) {
            for (int b = a + 1; b <= 28; b++) {
                for (int c = b + 1; c <= 29; c++) {
                    for (int d = c + 1; d <= 30; d++) {
                        for (int e = d + 1; e <= 31; e++) {
                            for (int f = e + 1; f <= 32; f++) {
                                arr[0] = Integer.valueOf(lotteryNumberArr[a]);
                                arr[1] = Integer.valueOf(lotteryNumberArr[b]);
                                arr[2] = Integer.valueOf(lotteryNumberArr[c]);
                                arr[3] = Integer.valueOf(lotteryNumberArr[d]);
                                arr[4] = Integer.valueOf(lotteryNumberArr[e]);
                                arr[5] = Integer.valueOf(lotteryNumberArr[f]);
                                Set ACSet = new HashSet();//AC值
                                for (int i = 0; i < 5; i++) {
                                    for (int j = i + 1; j <= 5; j++) {
                                        ACSet.add(Math.abs(arr[j] - arr[1]));
                                    }
                                }
                                if ((ACSet.size() - (6 - 1)) == 9) {
                                    ACResultNumbers.add(arr.toString());
                                }
                                count++;
                            }
                        }
                    }
                }
            }
        }
        Long endTime = System.currentTimeMillis() - startTime;
        log.info("end filter AC>>>>>>>>>>>>>>>>>>>>" + endTime);
        log.info(count);

        return ACResultNumbers;
    }
}
