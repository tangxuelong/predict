package com.mojieai.predict.service.httpasync;

import org.apache.http.HttpResponse;

/**
 * Created by Kyle Zhang on 2016/10/17.
 */
public interface CommonAsyncCallbackInterface {
    void doCompleted(HttpResponse response);

    void doFailed(Exception ex);

    void doCancelled();
}
