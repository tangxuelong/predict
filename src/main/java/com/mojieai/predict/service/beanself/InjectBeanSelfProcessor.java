package com.mojieai.predict.service.beanself;

import com.mojieai.predict.constant.LogConstant;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * bean初始化完毕后调用
 */
@Repository
public class InjectBeanSelfProcessor implements BeanPostProcessor, ApplicationContextAware {
    private static final Logger log = LogConstant.commonLog;

    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof BeanSelfAware) {
            BeanSelfAware myBean = (BeanSelfAware) bean;
            //			log.info("BeanName:" + beanName);

            Class cls = bean.getClass();
            /**
             * 直接从context中获取bean，确保获取的是代理类（建立事务）如果申明的是@Service，但没有Proxy的类，将从Context中获取，
             * 如果仍然获取不到代理类，或者获取出现BeanCurrentlyInCreationException异常，使用原有的类 。出现此问题的原因还是serivce互相引用
             */

            if (!AopUtils.isAopProxy(bean)) {
                //if @Service
                Class c = bean.getClass();
                Service serviceAnnotation = (Service) c.getAnnotation(Service.class);
                if (serviceAnnotation != null) {
                    //com.mojieai.predict.service 互相引用，需要从Context从获取代理后的Bean
                    try {
                        //						log.info("No Proxy,retrive from context "+bean.getClass());
                        bean = context.getBean(beanName);
                        if (!AopUtils.isAopProxy(bean)) {
                            //仍然不是Proxy
                            log.error("No Proxy Bean for com.mojieai.predict.service " + bean.getClass());
                        }
                    } catch (BeanCurrentlyInCreationException ex) {
                        //告警，但仍然在一个No Proxy的情况正常运行
                        log.error("No Proxy Bean for com.mojieai.predict.service " + bean.getClass() + " " + ex.getMessage());
                    } catch (Exception ex) {
                        //告警，但仍然在一个No Proxy的情况正常运行
                        log.error("No Proxy Bean for com.mojieai.predict.service " + bean.getClass() + " " + ex.getMessage(), ex);
                    }
                }
            }

            myBean.setSelf(bean);
            return myBean;
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
