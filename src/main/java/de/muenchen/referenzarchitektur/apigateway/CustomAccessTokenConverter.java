/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.muenchen.referenzarchitektur.apigateway;

import java.util.Map;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

/**
 * Wird benötigt, um das JWT so zu erweitern, wie Spring Boot und Zuul es benötigen.
 * Hintergrund: Der OAuth2TokenRelayFilter in Zuul benötigt in Methode getAccessToken
 * die client_id in auth.getOAuth2Request().getClientId(), und die wird
 * gesetzt in DefaultAccessTokenConverter.extractAuthentication.
 * Dabei wird hartcodiert nach dem Feld "client_id" gesucht. KeyCloak
 * setzt dieses aber nicht, jedoch findet man die ClientId z.B. in Feld
 * "azp". Deshalb gibt es nun einen angepassten AccessTokenConverter,
 * der die clientId von "azp" nach "client_id" konvertiert.
 * 
 * @author roland
 */
public class CustomAccessTokenConverter extends DefaultAccessTokenConverter {
    
    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        converterHelper(map);
        return super.extractAuthentication(map);
    }
    
    // Helper method created so that the wildcard can be captured
    // through type inference.
    private <T> void converterHelper(Map<String, T> map) {

        T clientId = map.get("azp");
        map.put("client_id", clientId);
    }
    
}
