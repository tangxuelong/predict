package com.mojieai.predict.service.httpasync;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.exception.BusinessException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by Kyle Zhang on 2016/10/17.
 */
public class AsyncHttpClientManager {

    protected Logger log = LogConstant.commonLog;

    public static int DEFAULT_CONNECTION_TIMEOUT = 120000;
    public static int DEFAULT_SOCKET_TIMEOUT = 120000;
    public static int DEFAULT_SELECT_THREAD_COUNT = 10;
    public static int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 120000;
    public static int DEFAULT_MAX_CONNECTION_COUNT = 1;
    public static int DEFAULT_MAX_PERROUTE_COUNT = 1;
    public static int DEFAULT_REQUEST_PAUSE = 0;
    public static int DEFAULT_SELECT_INTERVALT = 200;

    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int selectThreadCount = DEFAULT_SELECT_THREAD_COUNT;
    private int connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;
    private int maxConnectionNum = DEFAULT_MAX_CONNECTION_COUNT;
    private int defaultMaxPerRoute = DEFAULT_MAX_PERROUTE_COUNT;
    private int requestPause = DEFAULT_REQUEST_PAUSE;
    private int selectInterval = DEFAULT_SELECT_INTERVALT;

    private CloseableHttpAsyncClient closeableHttpAsyncClient;
    PoolingNHttpClientConnectionManager connManager;
    RequestConfig requestConfig;
    IOReactorConfig ioReactorConfig;
    ConnectingIOReactor ioReactor;

    public AsyncHttpClientManager() {
        initialize();
    }

    public AsyncHttpClientManager(int connectionTimeout, int socketTimeout, int selectThreadCount,
                                  int selectInterval, int connectionRequestTimeout, int maxConnectionNum,
                                  int maxPerRoute) {
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        this.selectThreadCount = selectThreadCount;
        this.selectInterval = selectInterval;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.maxConnectionNum = maxConnectionNum;
        this.defaultMaxPerRoute = maxPerRoute;
        initialize();
    }

    private void initialize() {
        try {
            requestConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout)
                    .setConnectionRequestTimeout(connectionRequestTimeout).build();

            ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(selectThreadCount).setSoKeepAlive(true)
                    .setSelectInterval(selectInterval).setConnectTimeout(connectionTimeout).build();

            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

            connManager = new PoolingNHttpClientConnectionManager(ioReactor);
            connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
            connManager.setMaxTotal(maxConnectionNum);

            closeableHttpAsyncClient = HttpAsyncClients.custom()
                    .setConnectionManager(connManager)
                    .setDefaultRequestConfig(requestConfig).build();

            if (closeableHttpAsyncClient == null) {
                throw new BusinessException("http client initialization fails!");
            }
            // AsyncHttpClient start
            closeableHttpAsyncClient.start();
            log.info("http client initialization succeed!");
        } catch (Exception e) {
            log.error("[Async]http client initialization fails", e);
            throw new BusinessException("http client initialization fails!");
        }
    }

    public void close() throws IOException {
        closeableHttpAsyncClient.close();
    }

    private org.apache.http.protocol.HttpContext generateHttpClientContext(
            HttpContext context) {

        HttpClientContext clientContext = new HttpClientContext();

        if (context.getConnectionTimeout() <= 0) {
            context.setConnectionTimeout(connectionTimeout);
        }

        if (context.getRequestTimeout() <= 0) {
            context.setRequestTimeout(this.connectionRequestTimeout);
        }

        if (context.getSocketTimeout() <= 0) {
            context.setSocketTimeout(this.socketTimeout);
        }

        RequestConfig config = RequestConfig.custom().setConnectTimeout(context.getConnectionTimeout())
                .setSocketTimeout(context.getSocketTimeout()).setConnectionRequestTimeout(context.getRequestTimeout())
                .build();
        clientContext.setRequestConfig(config);
        return clientContext;
    }

    public void execute(HttpContext context, AsyncStringCallback callback)
            throws BusinessException {
        RequestBuilder builder = new RequestBuilder(context, context.getContent(), callback);
        HttpUriRequest r = builder.buildRequest();
        HttpHost host = builder.buildHost(r);
        FutureCallback<HttpResponse> d = builder.buildFutureCallback();
        org.apache.http.protocol.HttpContext clientContext = generateHttpClientContext(context);
        context.setStartTimeStamp(System.currentTimeMillis());
        closeableHttpAsyncClient.execute(host, r, clientContext, d);
    }
}
