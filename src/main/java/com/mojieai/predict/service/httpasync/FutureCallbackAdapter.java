package com.mojieai.predict.service.httpasync;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.thread.ThreadPool;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.logging.log4j.Logger;

//1* FutureCallbackAdapter implements the FutureCallback<HttpResponse>
//2* FutureCallbackAdapter contains a CommonAsyncCallbackInterface implementation.
//3* RequestBuilder.CommonAsyncStringCallbackInterfaceAdapter implement CommonAsyncCallbackInterface
//4* if we need stream process, we just need to create a CommonAsyncStreamCallbackInterfaceAdapter implementing
//      CommonAsyncCallbackInterface
//5* CommonAsyncStingCallbackInterfaceAdapter will delegate the method call to AsyncStringCallback created for
//      each httpasync call(queryTicketStatus)
public class FutureCallbackAdapter implements FutureCallback<HttpResponse> {

    private CommonAsyncCallbackInterface commonAsyncCallbackInterface;
    public HttpContext context;
    Logger log = LogConstant.commonLog;

    public FutureCallbackAdapter(CommonAsyncCallbackInterface commonAsyncCallbackInterface,
                                 HttpContext context) {
        super();
        this.commonAsyncCallbackInterface = commonAsyncCallbackInterface;
        this.context = context;
    }

    @Override
    public void completed(HttpResponse response) {
        long startTime = System.currentTimeMillis();
        ThreadPool.getInstance().executeParseTask(() -> commonAsyncCallbackInterface.doCompleted(response));
        long endTime = System.currentTimeMillis();
        long diff = endTime - startTime;
//        if (diff >= IniCache.getIniIntValue(IniConstant.HTTP_RESPONSE_PARSE_TIME_DIFF, 2)) {
        log.error("[Async]process httpasync http response time exceeds 2 ms, actual time cost is : " + diff);
//        }
    }

    @Override
    public void failed(Exception ex) {
        commonAsyncCallbackInterface.doFailed(ex);
    }

    @Override
    public void cancelled() {
        commonAsyncCallbackInterface.doCancelled();
    }

}
