/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.muenchen.referenzarchitektur.apigateway;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.util.Map;

/**
 * Diese Klasse ist nötig, so lange Bug
 * https://github.com/spring-cloud/spring-cloud-netflix/issues/1895 noch nicht
 * gefixt ist. Das Problem ist, dass der PreDecorationFilter.java in Projekt
 * spring-cloud-netflix standardmäßig Port, Host und Proto als CSV aneinander
 * reiht. Verschiedene Methoden im Backend kommen damit aber nicht klar und
 * erwarten einen einzelnen Wert anstatt einer CSV. Was wir also machen ist die
 * CSV zerlegen und nur den ersten Wert weiterreichen.
 *
 * @author roland
 */
public class CustomHeaderFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        int FILTER_ORDER = org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        String PORT_HEADER = org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_PORT_HEADER.toLowerCase();
        String HOST_HEADER = org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_HOST_HEADER.toLowerCase();
        String PROTO_HEADER = org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_PROTO_HEADER.toLowerCase();
        overwriteZuulRequestHeader(PORT_HEADER);
        overwriteZuulRequestHeader(HOST_HEADER);
        overwriteZuulRequestHeader(PROTO_HEADER);

        return null;
    }

    private void overwriteZuulRequestHeader(String header) {
        RequestContext ctx = RequestContext.getCurrentContext();
        Map<String, String> zuulRequestHeader = ctx.getZuulRequestHeaders();
        if (zuulRequestHeader != null) {
            String value = ctx.getZuulRequestHeaders().get(header);
            if (value != null && value.length() > 0) {
                if (value.indexOf(",") > 0) {
                    //value enthält mind. ein Komma. An den Kommata in ein Array zerlegen
                    //und nur den ersten Wert (der erhalten bleiben soll) übernehmen
                    String[] portList = value.split(",");
                    String newPort = portList[0];
                    ctx.addZuulRequestHeader(header, newPort);
                }
            }
        }
    }

}
