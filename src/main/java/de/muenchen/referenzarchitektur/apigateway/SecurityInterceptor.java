package de.muenchen.referenzarchitektur.apigateway;

/**
 *
 * @author roland
 */
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;

/**
 * Wird verwendet, um auch im API-Gateway immer den aktuellen Access Token zu haben.
 * Auf diese Weise wird auch bei reinen API-Gateway-Requests (z.B. beim Lazy-Loading von HTML-Fragmenten)
 * immer überprüft, ob das aktuelle Access Token noch valide ist und falls nicht über den
 * Refresh Token ein neues geholt.
 * Bei den Backend-Zugriffe über Zuul wird zwar auch in der Klasse OAuth2TokenRelayFilter immer
 * das Access Token überprüft und wenn nötig ein aktuelleres verwendet. Dort wird aber das Token
 * im Frontend nicht ausgetauscht, so dass reine Frontend-Requests "unendlich" valide sind.
 * Auch wenn unser Frontend weniger sicherheitstechnisch relevant ist, so sollten wir es 
 * trotzdem entsprechend absichern und sicherstellen, dass sich der User neu anmeldet, wenn das 
 * Refresh Token abläuft.
 * Eigentlich sollte Spring OAuth2 das ja selbst machen, aber die Spring-Entwickler sind da anderer
 * Ansicht: https://github.com/spring-projects/spring-security-oauth2-boot/issues/5
 * @author roland
 */
@Component
public class SecurityInterceptor extends HandlerInterceptorAdapter {

    private static Logger log = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Autowired
    public OAuth2RestOperations restTemplate;

    @Autowired
    private TokenStore tokenStore;

    /**
     * Executed before actual handler is executed
     *
     * @param request
     * @param response
     * @param handler
     * @return 
     * @throws java.lang.Exception 
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        return this.refreshToken(request, response);
    }

    /**
     * Refreshes the current OAuth2 Token if necessary (i.e. if it is expired).
     * @param request The Servlet Request
     * @param response The Servlet Response
     * @return true if subsequent controller method should be executed, false otherwise 
     * (which is if a forward to OAuth Server is required)
     * @throws IOException 
     */
    private boolean refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("refreshToken called");
        //get current Token
        SecurityContext ctx = SecurityContextHolder.getContext();
        OAuth2Authentication a = (OAuth2Authentication) ctx.getAuthentication();
        if (a == null) {
            return true; //no authentication object --> no token to refresh
        }
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) a.getDetails();
        String tokenValue = details.getTokenValue();
        log.debug("Current token: " + tokenValue);

        try {
            //get new access token if renewal is necessary
            OAuth2AccessToken newAccessToken = restTemplate.getAccessToken();
            String newTokenValue = newAccessToken.getValue();
            
            if (!newTokenValue.equals(tokenValue)) {
                //if token refreshed need to put it into authentication object
                log.debug("Refreshed! New token: " + newTokenValue);

                //create new OAuth2Authentication object
                OAuth2Authentication newOAuth2Authentication = tokenStore.readAuthentication(newAccessToken);

                //create new OAuth2AuthenticationDetails object and attach to OAuth2Authentication
                request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, newTokenValue);
                request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, "Bearer");
                OAuth2AuthenticationDetails newDetails = new OAuth2AuthenticationDetails(request);
                newOAuth2Authentication.setDetails(newDetails);

                //put new OAuth2Authentication with new token into SecurityContext
                ctx.setAuthentication(newOAuth2Authentication);
                SecurityContextHolder.setContext(ctx);
            }

        } catch (UserRedirectRequiredException e) {
            log.info("Refresh token no longer valid. Have to redirect the user to the Auth Server.");
            //Refresh token not valid any more
            //now reset the SecurityContext, so redirect will work
            SecurityContext ctxNew = new SecurityContextImpl();
            SecurityContextHolder.setContext(ctxNew);
            //redirect to self (re-throwing the Exception will result in a forever-living token - don't know why)
            response.sendRedirect(request.getRequestURI());
            return false;
        }
        return true;
    }

}
