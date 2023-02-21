package com.mojieai.predict.service.httpasync;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpResponse;


/**
 * Created by Kyle Zhang on 2016/10/18.
 */
@Data
@NoArgsConstructor
public abstract class AsyncStringCallback {
    private String resultCharset = null;

    public AsyncStringCallback(String resultCharset) {
        this.resultCharset = resultCharset;
    }

    public abstract void completed(String result, HttpResponse response, HttpContext context);

    public abstract void failed(final Exception ex);

    //each implemented class knows which kind of object will be returned.
    //for instance, query ticket status will return a list object containing BetResult
    //query award code will return a PlatformAward object.
    public abstract Object parseResponse(String response);

}
