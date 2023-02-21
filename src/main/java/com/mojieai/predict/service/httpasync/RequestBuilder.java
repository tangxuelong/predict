package com.mojieai.predict.service.httpasync;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.exception.BusinessException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Kyle Zhang on 2016/10/17.
 */
public class RequestBuilder {
    Logger log = LogConstant.commonLog;

    protected HttpUriRequest request;
    protected HttpContext context;
    protected String content;
    protected AsyncStringCallback callback;

    public RequestBuilder(HttpContext context) {
        this.context = context;
    }

    public RequestBuilder(HttpContext context, String content, AsyncStringCallback callback) {
        this.context = context;
        this.content = content;
        this.callback = callback;
    }

    protected String buildGetUrl() throws BusinessException {
        String url = this.context.getUrl();
        String charset = getCharset(context.getCharset());
        try {
            if (this.content != null && this.content.trim().length() != 0) {
                url += "?" + URLEncoder.encode(content, charset);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("[AsyncRequestBuilder]build url charset error! ", e);
            throw new BusinessException("charset error!", e);
        }
        return url;
    }

    protected HttpEntity buildPostEntity() throws BusinessException {
        String charset = getCharset(context.getCharset());
        HttpEntity entity;

        try {
            if (content == null) {
                content = "";
            }
            if (this.context.isNeedCompress()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream gos = new GZIPOutputStream(bos);
                gos.write(this.content.getBytes(charset));
                gos.flush();
                gos.close();
                entity = new InputStreamEntity(new ByteArrayInputStream(bos.toByteArray()));
            } else {
                entity = new StringEntity(content, charset);
            }
        } catch (Exception e) {
            log.error("[AsyncRequestBuilder]build entity body charset error! ", e);
            throw new BusinessException("charset error!", e);
        }
        return entity;
    }

    private HttpGet buildGet() throws BusinessException {
        HttpGet t = new HttpGet(buildGetUrl());
        return t;
    }

    private HttpPost buildPost() throws BusinessException {
        HttpPost t = new HttpPost(this.context.getUrl());
        t.setEntity(this.buildPostEntity());
        return t;
    }

    public HttpUriRequest buildRequest() throws BusinessException {
        if (this.context.getMethod() == HttpContext.HTTP_GET) {
            request = buildGet();
        } else if (this.context.getMethod() == HttpContext.HTTP_POST) {
            request = buildPost();
        } else {
            log.error("[AsyncRequestBuilder]httpasync http request method error!");
            throw new BusinessException("http request method error");
        }
        this.header(request);
        return request;
    }

    public HttpHost buildHost(HttpUriRequest request) throws BusinessException {
        Args.notNull(request, "HTTP request");
        HttpHost target = null;
        final URI requestURI = request.getURI();
        int port = 0;
        if (requestURI.isAbsolute()) {
            target = URIUtils.extractHost(requestURI);
            if (target == null) {
                throw new BusinessException("URI does not specify a valid host name: " + requestURI);
            }
            port = target.getPort();
            if (port <= 0) {
                try {
                    port = DefaultSchemePortResolver.INSTANCE.resolve(target);
                } catch (UnsupportedSchemeException use) {
                    throw new BusinessException("URI does not specify a valid host name: " + requestURI + use);
                }
            }
        }
        return new HttpHost(target.getHostName(), port, context.getRoutePoolKey());
    }

    protected String getCharset(String charset) {
        if (charset == null || charset.trim().length() == 0 || !Charset.forName(charset).canEncode()) {
            return HttpContext.DEFAULT_CHARSET;
        }
        return charset;
    }

    protected void header(HttpUriRequest request) {
        String charset = getCharset(context.getCharset());
        this.context.setCharset(charset);
        if (this.context.isNeedCompress()) {
            request.addHeader("Content-Type", "application/x-gzip");
            request.addHeader("Accept", "application/x-gzip");
            request.addHeader("Accept-Encoding", "gzip, deflate");
        } else {
            request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=" + charset);
        }
    }

    public FutureCallback<HttpResponse> buildFutureCallback() {
        CommonAsyncCallbackInterface callback = buildAsyncCallback();
        FutureCallbackAdapter adapter = new FutureCallbackAdapter(callback, context);
        return adapter;
    }

    //we can make this method to abstract if we need more kinds of CallbackAdapter
    //for instance, we may need stream call back
    protected CommonAsyncCallbackInterface buildAsyncCallback() {
        return new CommonAsyncStringCallbackInterfaceAdapter();
    }

    public class CommonAsyncStringCallbackInterfaceAdapter implements CommonAsyncCallbackInterface {

        @Override
        public void doCompleted(HttpResponse response) {
            try {
                HttpEntity entity = response.getEntity();
                if (context.isNeedCompress()) {
                    entity = new GzipDecompressingEntity(entity);
                }
                String r = null;
                String resultCharset = callback.getResultCharset();
                if (resultCharset != null) {
                    r = EntityUtils.toString(entity, resultCharset);
                } else {
                    r = EntityUtils.toString(entity);
                }
                callback.completed(r, response, context);
            } catch (Exception e) {
                log.error("[AsyncRequestBuilder]requestBuilder doCompleted! ", e);
                callback.failed(e);
            }
        }

        @Override
        public void doFailed(Exception ex) {
            log.error("[AsyncRequestBuilder]requestBuilder doFailed! ", ex);
            callback.failed(ex);
        }

        @Override
        public void doCancelled() {
            log.error("[AsyncRequestBuilder]requestBuilder doCancelled ");
            callback.failed(new BusinessException("request cancelled"));
        }

    }
}
