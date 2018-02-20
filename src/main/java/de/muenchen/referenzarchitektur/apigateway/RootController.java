/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.muenchen.referenzarchitektur.apigateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author roland
 */
@RestController
@Configuration
public class RootController {

    /**
     * Resource without content.
     */
    class RootLinksResource extends ResourceSupport {

        public RootLinksResource() {
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api")
    public @ResponseBody
    ResponseEntity<RootLinksResource> getActions() {
        //these lines need to be generated...
        //duplication to application.yml --> can this be somehow be avoided?        
        Link userServiceLink = linkTo(RootController.class)
                .slash("api").slash("user_service").withRel("user_service");
        Link adminServiceLink = linkTo(RootController.class)
                .slash("api").slash("admin_service").withRel("admin_service");

        RootLinksResource links = new RootLinksResource();
        links.add(userServiceLink);
        links.add(adminServiceLink);

        return new ResponseEntity<>(links, HttpStatus.OK);
    }

}
