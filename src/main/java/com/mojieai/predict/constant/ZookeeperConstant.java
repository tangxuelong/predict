package com.mojieai.predict.constant;

import com.mojieai.predict.zk.client.ZkClient;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ZookeeperConstant {
    private static final Logger log = LogConstant.commonLog;
    public final static String scheduleRootDirectory = "/schedule";
    public final static String cronScheduleRootDirectory = "/cron";
    public final static String znodePrefix = "cluster_lock";
    public static final String CLUSTERSYNC_ANNOTATION = "@annotation(com.mojieai.predict.zk.annotation.ClusterSync) " +
            "&& @annotation(clusterSync)";
    public final static int WAIT_FOR_CONNECTION = 500;
    public final static int MAX_DUPLICATE_NODE = 2;
    private volatile static Map<String, ZkClient> zkMap = new ConcurrentHashMap<String, ZkClient>();

    public static String getKey(String rootDirectory) {
        return new StringBuilder(rootDirectory).append(",").append(znodePrefix).toString();
    }

    public static ZkClient remove(String rootDirectory) {
        return zkMap.remove(ZookeeperConstant.getKey(rootDirectory));
    }

    public static ZkClient get(String rootDirectory) {
        return zkMap.get(ZookeeperConstant.getKey(rootDirectory));
    }

    public static void put(ZkClient client, String rootDirectory) {
        String key = ZookeeperConstant.getKey(rootDirectory);
        ZkClient oldZkCli = zkMap.get(key);

        if (null != oldZkCli && oldZkCli.isAlive()) {
            log.error("ZkPool close oldZkCli before put new, key:" + key + "; oldZkPath:" + oldZkCli.toString());
            oldZkCli.destroy();
        }
        zkMap.put(key, client);
    }

}
