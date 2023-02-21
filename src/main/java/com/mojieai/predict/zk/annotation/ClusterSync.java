package com.mojieai.predict.zk.annotation;

import com.mojieai.predict.constant.ZookeeperConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClusterSync {
    public String path() default ZookeeperConstant.scheduleRootDirectory;

    public String callbackRef() default "";

    /* 设置为true，第一次调用无视Cluster Lock */
    public boolean allowAcessAsFistTime() default false;

    @SuppressWarnings("rawtypes")
    public Class rollbackFor() default RuntimeException.class;

    public boolean returnImmediate() default false;
}
