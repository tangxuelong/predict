package com.mojieai.predict.zk.client.impl;

import com.mojieai.predict.constant.ZookeeperConstant;
import com.mojieai.predict.zk.annotation.ClusterSync;
import com.mojieai.predict.zk.client.ClusterSyncProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Aspect
public class ZKClusterBeanPostProcessor implements ApplicationContextAware {
    private ApplicationContext context;
    private ClusterSyncProvider clusterSyncProvider;

    @Around(ZookeeperConstant.CLUSTERSYNC_ANNOTATION)
    public Object doBeforeClusterSync(ProceedingJoinPoint pjp, ClusterSync clusterSync) throws Throwable {
        return this.clusterSyncProvider.doBefore(this.context, pjp, clusterSync);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public void setClusterSyncProvider(ClusterSyncProvider clusterSyncProvider) {
        this.clusterSyncProvider = clusterSyncProvider;
    }
}
