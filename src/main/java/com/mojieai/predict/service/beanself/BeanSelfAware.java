package com.mojieai.predict.service.beanself;

/**
 * 实现此接口的类可以得到代理类
 */
public interface BeanSelfAware {
    void setSelf(Object proxyBean);
}