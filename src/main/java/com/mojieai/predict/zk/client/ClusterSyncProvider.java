package com.mojieai.predict.zk.client;

import com.mojieai.predict.zk.annotation.ClusterSync;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;

public interface ClusterSyncProvider {
    public Object doBefore(ApplicationContext context, ProceedingJoinPoint pjp, ClusterSync clusterSync)
            throws Throwable;

    public void process(ApplicationContext context, ClusterSync clusterSync);
}
