package com.mojieai.predict.zk.client.impl;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.ZookeeperConstant;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.zk.client.ZkClient;

import com.mojieai.predict.zk.client.AbstractZKClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.MethodInvoker;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.AsyncCallback;

public class ZKCandidate extends AbstractZKClient implements AsyncCallback.VoidCallback, ZkClient {
    private static final Logger logger = LogConstant.commonLog;
    /**
     * 创建的临时节点的全路径
     */
    private String path;

    private String rootDirectory;
    /**
     * 是否是master
     */

    private volatile boolean isMaster;

    private int accessCount = 0;

    private MethodInvoker invoker;

    /**
     * 通过这个方法取是否是master<br>
     * 如果设置选取master方法是0（默认），且设置监听的path已经存在，则直接返回isMaster值<br>
     * 否则需要wait指定时间（默认3s），待确认后再返回，如果没有取到自己设定的path则认为自己不是master，否则直接返回isMaster值<br>
     *
     * @return
     */
    @Override
    public boolean isMaster() {
        synchronized (this) {
            try {
                doLeaderElection();
                logger.info("doLeaderElection - after_isMaster->" + isMaster);
            } catch (KeeperException.ConnectionLossException e) {
                long timeout = this.conf.getTimeout();
                while (timeout > 0) {
                    Timestamp startTimeStamp = DateUtil.getCurrentTimestamp();
                    // 当zookeeper链接中断尝试重新链接
                    try {
                        Thread.sleep(ZookeeperConstant.WAIT_FOR_CONNECTION);
                        doLeaderElection();
                        break;
                    } catch (ConnectionLossException e1) {
                        logger.error("ConnectionLossException while doLeaderElection:", e1);
                    } catch (InterruptedException e2) {
                        logger.error("InterruptedException while doLeaderElection:", e2);
                    }

                    timeout -= DateUtil.getDiffSeconds(startTimeStamp, DateUtil.getCurrentTimestamp());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return Boolean.FALSE;
            }
            return null == path ? Boolean.FALSE : isMaster;
        }
    }

    /**
     * 返回zk是否在正常连接，如果没有请应用自己判断如何处理
     *
     * @return
     */
    @Override
    public boolean isAlive() {
        return zk == null ? Boolean.FALSE : zk.getState() == null ? Boolean.FALSE : zk.getState().isAlive();
    }

    @Override
    @PostConstruct
    public void init() throws Exception {
        // 先重置，确保isMaster方法取到的是新的决议
        isMaster = Boolean.FALSE;
        try {
            connect();
            // 如果没有rootDirectoty节点，则创建该目录, 该目录的数据类型是持久的
            if (null == zk.exists(rootDirectory, Boolean.FALSE)) {
                try {
                    zk.create(rootDirectory, new byte[0], conf.getAcls(), CreateMode.PERSISTENT);
                } catch (KeeperException.NodeExistsException e) {
                    logger.error("KeeperException.NodeExistsException:", e);
                }
            }

            //创建zk临时序列化节点：对于zk创建临时节点不能进行重试，避免创建过多临时节点
            path = zk.create(rootDirectory + "/" + ZookeeperConstant.znodePrefix, getZnodeContent().getBytes(), conf.getAcls(),
                    CreateMode.EPHEMERAL_SEQUENTIAL);

            logger.debug("create path " + path + " and sync success");
            // 异步调用, 使得读操作的连接所连的zk实例能与leader进行同步, 从而能读到最新的类容
            zk.sync(rootDirectory, this, null);

        } catch (KeeperException.ConnectionLossException e) {
            long timeout = this.conf.getTimeout();
            while (timeout > 0) {
                Timestamp startTimeStamp = DateUtil.getCurrentTimestamp();
                Thread.sleep(ZookeeperConstant.WAIT_FOR_CONNECTION);
                // 当zookeeper链接中断尝试重新链接
                if (this.checkMyEphemeralSeqExist()) {
                    zk.sync(path, this, null);
                    return;
                }
                timeout -= DateUtil.getDiffSeconds(startTimeStamp, DateUtil.getCurrentTimestamp());
            }
            throw e;
        } catch (Exception e) {
            if (this.isAlive()) {
                this.destroy();
            }
            logger.error("init exception:", e);
            throw e;
        }
    }

    /**
     * check if the node is created used in catch
     *
     * @return
     */
    private boolean checkMyEphemeralSeqExist() throws Exception {
        List<String> znodeList;
        try {
            znodeList = zk.getChildren(rootDirectory, Boolean.FALSE);
            Collections.sort(znodeList);
            // 检查
            logger.info("checkMyEphemeralSeqExist, znodeList=[" + znodeList + "]");
            if (null == znodeList || znodeList.size() == 0) {
                return Boolean.FALSE;
            }

            for (String s : znodeList) {
                String curPath = rootDirectory + "/" + s;
                byte[] data = zk.getData(curPath, null, null);
                if (null != data && this.getZnodeContent().equalsIgnoreCase(new String(data))) {
                    path = curPath;
                    return Boolean.TRUE;
                }
            }

            return Boolean.FALSE;
        } catch (KeeperException e) {
            logger.error("KeeperException.NodeExistsException:", e);
        } catch (InterruptedException e) {
            logger.error("ThreadName: " + Thread.currentThread().getName()
                    + "|InterruptedException while doLeaderElection:" + e.getMessage(), e);
            return Boolean.FALSE;
        } finally {
            return Boolean.FALSE;
        }

    }

    /**
     * 这个方法的最大作用是notify在isMaster()方法中等待的线程
     *
     * @param isMaster
     */
    private void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    private String getZnodeContent() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "0";
        }
    }

    /**
     * 处理sync方法的异步返回
     */
    @SuppressWarnings("deprecation")
    @Override
    public void processResult(int rc, String path, Object ctx) {
        switch (rc) {
            case Code.Ok:
                logger.debug("sync success");
                break;
            default:
                break;
        }
    }

    @Override
    public void process(WatchedEvent event) {
        super.process(event);
        if (Watcher.Event.EventType.NodeDeleted == event.getType()
                ) {
            logger.info("find " + event.getPath() + " was deleted, it'm my turn!" + path);
            setMaster(Boolean.TRUE);
            slaveToMasterSwitched();
        }
    }

    /**
     * 选取master
     *
     * @throws ConnectionLossException
     */
    private void doLeaderElection() throws ConnectionLossException {
        try {
            if (StringUtils.isBlank(path)) {
                init();
            }

            List<String> znodeList = zk.getChildren(rootDirectory, Boolean.FALSE);
            Collections.sort(znodeList);
            int size = znodeList.size();

            // delete the rabbish node created by the same machine.
            if (size > ZookeeperConstant.MAX_DUPLICATE_NODE) {
                //去除同一机器产生的重复节点
                StringBuilder sb = new StringBuilder();
                for (String s : znodeList) {
                    sb.append(s).append(",");

                    String curPath = rootDirectory + "/" + s;
                    byte[] data = zk.getData(curPath, null, null);
                    if (null != data && this.getZnodeContent().equalsIgnoreCase(new String(data))
                            && !this.path.equalsIgnoreCase(curPath)) {
                        logger.info("znodelist delete curPath=[" + curPath + "] nodeContent" + new String(data));
                        zk.delete(curPath, -1);
                    }
                }
                logger.info("znodeList=[" + sb.toString() + "], path=[" + path + "]");
            }
            boolean isPathExist = Boolean.FALSE;
            for (int i = 0; i < size; i++) {
                if (path.equals(rootDirectory + "/" + znodeList.get(i))) {
                    isPathExist = Boolean.TRUE;
                    if (i == 0) {
                        //如果第一个就是自己，那他就是节点最小的机器，设为master
                        setMaster(Boolean.TRUE);
                    } else {
                        // 从getChildren方法获取的列表中选择前一个自己的节点，设置watcher，
                        // 这里要考虑获取时存在，但设置watcher时已经不存在的znode的情形
                        int j = i;
                        Stat stat = null;
                        do {
                            // 这里先不设置watcher
                            stat = zk.exists(rootDirectory + "/" + znodeList.get(--j), Boolean.FALSE);
                        }
                        while (j > 0 && null == stat);
                        // 如果没有前一个节点，自己就是master
                        if (null == stat) {
                            setMaster(Boolean.TRUE);
                        } else {
                            // 设置对前一个节点的监听
                            stat = zk.exists(rootDirectory + "/" + znodeList.get(j), this);
                            setMaster(Boolean.FALSE);
                        }
                    }

                    break;
                }
            }
            if (!isPathExist) {
                init();
                return;
            }
        } catch (KeeperException.ConnectionLossException e) {
            logger.error(zk.getSessionId() + e.getMessage(), e);
            throw e;
        } catch (InterruptedException e) {
            logger.error("ThreadName: " + Thread.currentThread().getName()
                    + "|InterruptedException while doLeaderElection:" + e.getMessage(), e);
        } catch (Exception e) {
            logger.error(
                    "ThreadName: " + Thread.currentThread().getName() + "|exception while doLeaderElection:"
                            + e.getMessage(), e);
        }

    }

    /**
     * 由slave变成leader
     */
    @Override
    public void slaveToMasterSwitched() {
        if (null == invoker) {
            return;
        }
        try {
            invoker.setArguments(new String[]
                    {ZookeeperConstant.scheduleRootDirectory});
            invoker.prepare();
            invoker.invoke();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        ZookeeperConstant.remove(rootDirectory);
    }

    public void setRootDirectory(String rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }
    // the follows are set methods
    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    public void setInvoker(MethodInvoker invoker) {
        this.invoker = invoker;
    }
}
