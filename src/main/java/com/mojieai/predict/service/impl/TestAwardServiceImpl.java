package com.mojieai.predict.service.impl;

import com.mojieai.predict.cache.PeriodCache;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.dao.AwardInfoDao;
import com.mojieai.predict.entity.bo.AwardDetail;
import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.enums.SsqGameEnum;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.service.TestAwardService;
import com.mojieai.predict.service.game.AbstractGame;
import com.mojieai.predict.service.game.GameFactory;
import com.mojieai.predict.thread.CalcAwardTask;
import com.mojieai.predict.thread.TestCalcAwardTask;
import com.mojieai.predict.thread.ThreadPool;
import com.mojieai.predict.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

@Service
public class TestAwardServiceImpl implements TestAwardService {
    protected Logger log = LogConstant.commonLog;

    @Autowired
    private AwardInfoDao awardInfoDao;
    @Autowired
    private RedisService redisService;

    @Override
    public List<AwardDetail> calcAwardDetail(Long gameId, int periodNum, int type) {
        List<AwardDetail> resultList = new ArrayList<>();
        if (periodNum < 0) {
            log.info("[查看历史中奖]期次数小于0。periodNum:" + periodNum);
            return resultList;
        }
        List<GamePeriod> periods = PeriodCache.getPeriodMap().get(CommonUtil.mergeUnionKey(gameId, RedisConstant
                .LAST_ALL_OPEN_PERIOD));
        if (periods == null) {
            log.error("periods is null. " + CommonUtil.mergeUnionKey(gameId, periodNum));
            return resultList;
        }
        int awardSize = periods.size();
        if (periodNum == 0 || periodNum > awardSize) {
            periodNum = awardSize; //0或者超过开奖号码记录数，都认为是查询所有
        }
        AbstractGame ag = GameFactory.getInstance().getGameBean(gameId);
        CompletionService<AwardDetail> completionService = new ExecutorCompletionService<>(ThreadPool.getInstance()
                .getCalcExec());
        //某彩种的某一期
        for (int i = 0; i <= periodNum - 1; i++) {
            completionService.submit(new TestCalcAwardTask(type, ag, periods.get(i), awardInfoDao, this, redisService));
        }
        for (int i = 0; i <= periodNum - 1; i++) {
            try {
                //主线程总是能够拿到最先完成的任务的返回值，而不管它们加入线程池的顺序
                Future<AwardDetail> f = completionService.take();
                resultList.add(f.get());
            } catch (Throwable e) {
                log.error("calcAwardDetail error. " + CommonUtil.mergeUnionKey(gameId, periodNum, type), e);
            }
        }
        Collections.sort(resultList, (o1, o2) -> -o1.getPeriodId().compareTo(o2.getPeriodId()));
        return resultList;
    }

    //暂时都生成双色球的
    @Override
    public List<String> generateNumberList(int type, GamePeriod period) {
        List<String> numberList = new ArrayList<>();
        List<String> redList = new ArrayList<>(SsqGameEnum.SSQ_RED_NUMBERS);
        List<String> blueList = new ArrayList<>(SsqGameEnum.SSQ_BLUE_NUMBERS);
        switch (type) {
            //每一期都是不同的随机号码 1w注
            case 0:
                for (int i = 0; i < 10000; i++) {
                    Collections.shuffle(redList, new Random(System.currentTimeMillis()));
                    Collections.shuffle(blueList, new Random(System.currentTimeMillis()));
                    String number = redList.get(0) + " " + redList.get(1) + " " + redList.get(2) + " " + redList.get
                            (3) + " " + redList.get(4) + " " + redList.get(5) + ":" + blueList.get(0);
                    numberList.add(number);
                }
                break;
            //每一期都是相同的随机号码 1w注
            case 1:
                for (int i = 0; i < 10000; i++) {
                    String ranStr = String.valueOf(System.currentTimeMillis());
                    Collections.shuffle(redList, new Random(Long.parseLong(ranStr.substring(0, ranStr.length() - 5))));
                    Collections.shuffle(blueList, new Random(0));
                    String number = redList.get(0) + " " + redList.get(1) + " " + redList.get(2) + " " + redList.get
                            (3) + " " + redList.get(4) + " " + redList.get(5) + ":" + blueList.get(0);
                    numberList.add(number);
                }
                break;
            //33个红球全包，保6中5 18621注
            case 2:
                String urlSeparator = String.valueOf(File.separatorChar);
                String path = getClass().getResource(".").getPath();
                String subPath = path.substring(0, path.indexOf("WEB-INF") + 8);
                String rootPath = subPath.substring(0, subPath.lastIndexOf(urlSeparator));
                rootPath = rootPath + File.separatorChar + "classes" + File.separatorChar + "filterfile" + File
                        .separatorChar + "ssq" + File.separatorChar + "ssq33z6b5.txt";
                File file = new File(rootPath);
                try {
                    InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt;
                    while ((lineTxt = bufferedReader.readLine()) != null) {
                        Collections.shuffle(blueList, new Random(System.currentTimeMillis()));
                        numberList.add(lineTxt + ":" + blueList.get(0));
                    }
                    read.close();
                } catch (Throwable e) {
                    log.error("read 33 file error" + period, e);
                }
                break;
            //30个红球，保6中5，随机杀3号 10978注
            case 3:
                Collections.shuffle(redList, new Random(System.currentTimeMillis()));
                String allNumber = redList.get(0);
                for (int i = 1; i < 30; i++) {
                    allNumber = allNumber + " " + redList.get(i);
                }
                String[] redBalls = allNumber.split(" ");
                String urlSeparator30 = String.valueOf(File.separatorChar);
                String path30 = getClass().getResource(".").getPath();
                String subPath30 = path30.substring(0, path30.indexOf("WEB-INF") + 8);
                String rootPath30 = subPath30.substring(0, subPath30.lastIndexOf(urlSeparator30));
                rootPath30 = rootPath30 + File.separatorChar + "classes" + File.separatorChar + "filterfile" + File
                        .separatorChar + "ssq" + File.separatorChar + "ssq30z6b5.txt";
                File file30 = new File(rootPath30);
                List<String> tempList = new ArrayList<>();
                try {
                    InputStreamReader read = new InputStreamReader(new FileInputStream(file30));
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt;
                    while (StringUtils.isNotBlank(lineTxt = bufferedReader.readLine())) {
                        Collections.shuffle(blueList, new Random(System.currentTimeMillis()));
                        String[] placeArray = lineTxt.split(" ");
                        String number30 = "";
                        for (String place : placeArray) {
                            number30 = number30 + redBalls[Integer.parseInt(place) - 1] + " ";
                        }
                        tempList.add(number30.substring(0, number30.length() - 1) + ":" + blueList.get(0));
                    }
                    read.close();
                } catch (Throwable e) {
                    log.error("read 30 file error" + period, e);
                }
                List<Integer> orderList = new ArrayList<>();
                for (int i = 0; i < tempList.size(); i++) {
                    orderList.add(i);
                }
                Collections.shuffle(orderList, new Random(System.currentTimeMillis()));
                for (int i = 0; i < 10000; i++) {
                    numberList.add(tempList.get(orderList.get(i)));
                }
//                numberList = tempList;
                break;
            //每期随机20个红球 38760注
            case 4:
                Collections.shuffle(redList, new Random(System.currentTimeMillis()));
                String number20 = redList.get(0);
                for (int i = 1; i < 20; i++) {
                    number20 = number20 + " " + redList.get(i);
                }
                number20 = number20 + ":" + blueList.get(0);
                numberList.add(number20);
                break;
            //每期随机16个红球 8008注
            case 5:
                Collections.shuffle(redList, new Random(System.currentTimeMillis()));
                String number16 = redList.get(0);
                for (int i = 1; i < 16; i++) {
                    number16 = number16 + " " + redList.get(i);
                }
                number16 = number16 + ":" + blueList.get(0);
                numberList.add(number16);
                break;
        }
        return numberList;
    }
}

