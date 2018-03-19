package de.muenchen.referenzarchitektur.apigateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@EnableZuulProxy
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class ApiGatewayApplication  extends WebMvcConfigurerAdapter {

    @Autowired
    private SecurityInterceptor securityInterceptor;
        
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public CustomHeaderFilter simpleFilter() {
        return new CustomHeaderFilter();
    }
    
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(securityInterceptor);
    }

}
