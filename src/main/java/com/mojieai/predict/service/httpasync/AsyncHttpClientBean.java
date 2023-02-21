package com.mojieai.predict.service.httpasync;

/**
 * Created by Kyle Zhang on 2016/10/18.
 */
public class AsyncHttpClientBean {

    private static AsyncHttpClientManager asyncHttpClientManager;

    public void init() {
        asyncHttpClientManager = new AsyncHttpClientManager();
    }

    public static AsyncHttpClientManager getAsyncHttpClientManager() {
        return asyncHttpClientManager;
    }
}
