package com.mojieai.predict.zk.client.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ZookeeperConstant;
import com.mojieai.predict.zk.client.AbstractClusterSyncProvider;
import com.mojieai.predict.zk.client.ClusterSyncProvider;
import com.mojieai.predict.zk.annotation.ClusterSync;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

public class ZKClusterSyncProvider extends AbstractClusterSyncProvider implements ClusterSyncProvider {
    private static final Logger logger = LogConstant.commonLog;

    @SuppressWarnings("unchecked")
    @Override
    public Object doBefore(ApplicationContext context, ProceedingJoinPoint pjp, ClusterSync clusterSync)
            throws Throwable {

        String rootDirectory = clusterSync.path();
        //判断是否已经创建过该路径的zk连接，第一次执行都是null
        ZKCandidate zkClient = (ZKCandidate) ZookeeperConstant.get(rootDirectory);
        if (null == zkClient || !zkClient.isAlive()) {
            try {
                zkClient = makeZKCandidate(context, clusterSync.path(), clusterSync.callbackRef());
            } catch (Exception e) {
                logger.error("exception", e);
                throw e;
            }
        }
        if (clusterSync.allowAcessAsFistTime()) {
            int count = zkClient.getAccessCount();
            if (count == 0) {
                zkClient.setAccessCount(count + 1);
                Object result = pjp.proceed();
                if (clusterSync.returnImmediate()) {
                    zkClient.destroy();
                }
                return result;
            }
        }
        if (null != zkClient && zkClient.isAlive() && zkClient.isMaster()) {
            //如果当前机器是master，则执行；否则，跳过
            int count = zkClient.getAccessCount();
            zkClient.setAccessCount(count + 1);
            try {
                Object result = pjp.proceed();
                if (clusterSync.returnImmediate()) {
                    zkClient.destroy();
                }
                return result;
            } catch (Throwable t) {
                if (clusterSync.rollbackFor().isAssignableFrom(t.getClass())) {
                    zkClient.destroy();
                }
                throw t;
            }
        }
        return returnDefaultValue(pjp);
    }

    @Override
    public void process(ApplicationContext context, ClusterSync clusterSync) {
    }
}