package com.mojieai.predict.zk.client;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ZookeeperConstant;
import com.mojieai.predict.zk.ZkConf;
import com.mojieai.predict.zk.client.impl.ZKCandidate;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.util.MethodInvoker;

public abstract class AbstractClusterSyncProvider {
    private static final Logger logger = LogConstant.commonLog;

    protected ZkConf zkConf = null;

    protected Object returnDefaultValue(ProceedingJoinPoint pjp) {
        if (null == pjp) {
            return null;
        }
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        if (null == methodSignature) {
            return null;
        }
        Class<?> returnType = methodSignature.getMethod().getReturnType();
        if (returnType.equals(int.class)) {
            return 0;
        } else if (returnType.equals(double.class)) {
            return 0d;
        } else if (returnType.equals(float.class)) {
            return 0f;
        } else if (returnType.equals(long.class)) {
            return 0l;
        } else if (returnType.equals(short.class)) {
            return 0;
        } else if (returnType.equals(boolean.class)) {
            return false;
        } else if (returnType.equals(byte.class)) {
            return 0;
        } else if (returnType.equals(char.class)) {
            return 0;
        }
        return null;
    }

    protected ZKCandidate makeZKCandidate(ApplicationContext context, String rootDirectory, String callbackRef)
            throws Exception {
        // 取得生成zkClient的两要素，另外一个rootDirectory从参数获取

        if (zkConf == null) {
            // 从服务器获取
            zkConf = (ZkConf) context.getBean("zkConf");
        }
        ZKCandidate zkCandidate = new ZKCandidate();
        zkCandidate.setRootDirectory(rootDirectory);
        zkCandidate.setConf(zkConf);
        // 设置回调函数
        if (null != callbackRef && !"".equals(callbackRef)) {
            MethodInvoker invoker = (MethodInvoker) context.getBean(callbackRef);
            if (null == invoker) {
                logger.error("callbackRef [" + callbackRef
                        + "] not found in spring context, may cause unable to use this func!");
            } else {
                zkCandidate.setInvoker(invoker);
            }
        }
        zkCandidate.init();//创建zk连接及临时节点
        ZookeeperConstant.put(zkCandidate, rootDirectory);//将创建的zk连接存入全局map
        return zkCandidate;
    }

    public ZkConf getZkConf() {
        return zkConf;
    }

    public void setZkConf(ZkConf zkConf) {
        this.zkConf = zkConf;
    }
}