package com.mojieai.predict.service.httpasync;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Kyle Zhang on 2016/10/17.
 */
@Data
@NoArgsConstructor
public class HttpContext {
    public static int HTTP_GET = 0;
    public static int HTTP_POST = 1;
    public static String DEFAULT_CHARSET = "UTF-8";
    public static boolean DEFAULT_COMPRESS = false;
    private String url;
    private int method;
    private String charset = DEFAULT_CHARSET;
    private boolean needCompress = DEFAULT_COMPRESS;

    private String content;
    private String routePoolKey = "";
    private long startTimeStamp;
    private long endTimeStamp;
    private int requestTimeout = 0;
    private int connectionTimeout = 0;
    private int socketTimeout = 0;
}
