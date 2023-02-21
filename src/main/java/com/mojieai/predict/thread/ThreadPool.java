package com.mojieai.predict.thread;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.IniConstant;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Singal
 */
public class ThreadPool {
    //后台业务公用线程池
    private static ExecutorService exec = new ThreadPoolExecutor(30, 30, 5 * 60, TimeUnit.SECONDS, new
            LinkedBlockingQueue<>());

    //刷新期次专用
    private static ExecutorService periodExec = new ThreadPoolExecutor(5, 5, 5 * 60, TimeUnit.SECONDS, new
            LinkedBlockingQueue<>());

    //NIO专用线程池
    private static ExecutorService nioExec = new ThreadPoolExecutor(10, 10, 5 * 60, TimeUnit.SECONDS, new
            LinkedBlockingQueue<>());

    //计算号码中奖情况
    private static ExecutorService calcExec = new ThreadPoolExecutor(20, 20, 5 * 60, TimeUnit.SECONDS, new
            LinkedBlockingQueue<>());

    //推送
    private static ExecutorService pushExec = new ThreadPoolExecutor(3, 3, 5 * 60, TimeUnit.SECONDS, new
            LinkedBlockingQueue<>());

    //更新设备信息
    private static ExecutorService updateDeviceInfoExec = new ThreadPoolExecutor(3, 3, 5 * 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());
    //发送邮件
    private static ExecutorService sendEmailExec = new ThreadPoolExecutor(3, 3, 5 * 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

    //统计社区大数据专用
    private static ExecutorService statisticSocialExec = new ThreadPoolExecutor(5, 5, 5 * 60, TimeUnit.SECONDS, new
            LinkedBlockingQueue<>());

    //任务派发奖励
    private static ExecutorService getUserSocialTaskExec = new ThreadPoolExecutor(5, 5, 5 * 60, TimeUnit.SECONDS, new
            LinkedBlockingQueue<>());

    //产品统计
    private static ExecutorService allProductBillExec = new ThreadPoolExecutor(5, 5, 5 * 60, TimeUnit.SECONDS, new
            LinkedBlockingQueue<>());

    //机器人推荐
    private static ExecutorService sportsRobotRecommendExec = new ThreadPoolExecutor(3, 3, 5 * 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

    private static ExecutorService godPredictTaskExec = new ThreadPoolExecutor(3, 3, 5 * 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

    private static Map<String, ThreadPoolExecutor> taskExecutorMap = new HashMap<>();

    private static ThreadPool instance = new ThreadPool();

    private ThreadPool() {
    }

    public static ThreadPool getInstance() {
        return instance;
    }

    public ExecutorService getPeriodExec() {
        return periodExec;
    }

    public ExecutorService getUpdateDeviceInfoExec() {
        return updateDeviceInfoExec;
    }

    public ExecutorService getCalcExec() {
        return calcExec;
    }

    public ExecutorService getPushExec() {
        return pushExec;
    }

    public ExecutorService getSendEmailExec() {
        return sendEmailExec;
    }

    public ExecutorService getStatisticSocialExec() {
        return statisticSocialExec;
    }

    public ExecutorService getUserSocialTaskExec() {
        return getUserSocialTaskExec;
    }

    public ExecutorService getAllProductBillExec() {
        return allProductBillExec;
    }

    public static ExecutorService getSportsRobotRecommendExec() {
        return sportsRobotRecommendExec;
    }

    public static ExecutorService getGodPredictTaskExec() {
        return godPredictTaskExec;
    }

    public void execute(Runnable runnable) {
        exec.execute(runnable);
    }

    public void executeParseTask(Runnable runnable) {
        nioExec.execute(runnable);
    }

    public ThreadPoolExecutor getTaskExecutor(Long gameId) {
        String gameIdStr = gameId.toString();
        if (taskExecutorMap.containsKey(gameIdStr)) {
            return taskExecutorMap.get(gameIdStr);
        }
        synchronized (gameIdStr.concat("TaskExecutor").intern()) {
            if (!taskExecutorMap.containsKey(gameIdStr)) {
                taskExecutorMap.put(gameIdStr, new ThreadPoolExecutor(IniCache.getIniIntValue(IniConstant
                        .TASK_THREAD_POOL_SIZE, 5),
                        IniCache.getIniIntValue(IniConstant.TASK_THREAD_POOL_SIZE, 5), 60, TimeUnit.SECONDS, new
                        LinkedBlockingQueue<>(), new
                        ThreadPoolExecutor.AbortPolicy()));
            }
        }
        return taskExecutorMap.get(gameIdStr);
    }
}
