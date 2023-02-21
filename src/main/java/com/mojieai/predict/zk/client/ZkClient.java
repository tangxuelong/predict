package com.mojieai.predict.zk.client;

public interface ZkClient {

    void slaveToMasterSwitched();

    boolean isMaster();

    boolean isAlive();

    void init() throws Exception;

    void destroy();
}
