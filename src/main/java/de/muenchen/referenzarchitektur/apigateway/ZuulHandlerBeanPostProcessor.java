/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.muenchen.referenzarchitektur.apigateway;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author roland
 */
@Configuration
public class ZuulHandlerBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

    @Autowired
    private SecurityInterceptor securityInterceptor;

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
        if (bean instanceof ZuulHandlerMapping) {
            ZuulHandlerMapping zuulHandlerMapping = (ZuulHandlerMapping) bean;
            Object[] objects = {securityInterceptor};
            zuulHandlerMapping.setInterceptors(objects);
        }
        return super.postProcessAfterInstantiation(bean, beanName);
    }

}
