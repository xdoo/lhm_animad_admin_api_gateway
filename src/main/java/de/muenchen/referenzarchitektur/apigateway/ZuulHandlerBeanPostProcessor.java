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
 * Wenn man will, dass der MVCInterceptor auch beim Aufruf der Zuul Routen angesteuert wird,
 * braucht man diese Klasse (vgl. https://stackoverflow.com/questions/37553487/interceptor-not-getting-called-when-zuul-routes-configured-in-gateway).
 * Werde ich aber abschalten, da der Zuul Gateway sich selbst darum kümmert, immer das aktuelle Security
 * Token zu verwenden.
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
