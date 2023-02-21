package com.mojieai.predict.zk.client;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.annotation.PreDestroy;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.util.DateUtil;
import com.mojieai.predict.zk.ZkConf;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractZKClient implements Watcher {

    private static final Logger logger = LogConstant.commonLog;
    /**
     * 连接的实例
     */
    @Autowired
    protected ZooKeeper zk;

    @Autowired
    protected ZkConf conf;

    protected CountDownLatch connectedSignal;

    /**
     * 返回zk是否在正常连接，如果没有请应用自己判断如何处理
     *
     * @return
     */
    public boolean isAlive() {
        return zk == null ? false : zk.getState() == null ? false : zk.getState().isAlive();
    }

    protected void connect() throws IOException, NoSuchAlgorithmException, InterruptedException {
        Timestamp startTime = DateUtil.getCurrentTimestamp();

        // 如果之前连着，要先断掉
        if (null != zk && zk.getState().isAlive()) {
            zk.close();
        }
        //同步计数器, 初始计数器为1
        connectedSignal = new CountDownLatch(1);
        zk = new ZooKeeper(conf.getServer(), conf.getTimeout(), this);
        // 阻塞程序继续执行
        connectedSignal.await();
        if (null != conf.getScheme()) {
            zk.addAuthInfo(conf.getScheme(), conf.getAuth().getBytes());
        }
        if (0 != conf.getUseACL() && conf.getAcls() == Ids.OPEN_ACL_UNSAFE) {
            List<ACL> acls = conf.getAcls();
            Id authId = new Id("digest", DigestAuthenticationProvider.generateDigest(conf.getAuth()));
            Id anyId = new Id("world", "anyone");
            acls.clear();
            acls.add(new ACL(ZooDefs.Perms.ALL ^ ZooDefs.Perms.DELETE, anyId));
            acls.add(new ACL(ZooDefs.Perms.DELETE, authId));
            conf.setAcls(acls);
        }
        connectedSignal = null;
        Timestamp endTime = DateUtil.getCurrentTimestamp();
        Long seconds = DateUtil.getDiffSeconds(startTime, endTime);
        this.logger.info("[ConnectZK] - ok. cost " + seconds + " ms");
    }

    public void init() throws Exception {
        connect();
    }

    @PreDestroy
    public void destroy() {
        if (zk != null) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void delete(String path) throws InterruptedException, KeeperException {
        if (!isAlive()) {
            return;
        }
        zk.delete(path, -1);
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == KeeperState.SyncConnected) {
            if (null != connectedSignal) {
                connectedSignal.countDown();
            }
        } else if (event.getState() == KeeperState.Expired) {
            try {
                // 正常情況下一个zk连接不应该有session失效的情况，如果有则打出log来
                logger.info("[invoke init] session expired error, event=[" + event.toString() + "]");
                init();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else if (KeeperState.Disconnected == event.getState()) {
            //客户端处于断开连接状态，和ZK集群都没有建立连接,此时最好别重建连接，因为客户端会自动重连，防止羊群效应
            logger.info("[invoke init] 与ZK服务器断开连接, event=[" + event.toString() + "]");
        }
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public ZkConf getConf() {
        return conf;
    }

    public void setConf(ZkConf conf) {
        this.conf = conf;
    }

}