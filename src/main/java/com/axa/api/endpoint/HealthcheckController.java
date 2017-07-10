package com.axa.api.endpoint;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller : Sends back the status of the API platform
 *
 */
@RestController
@Api
public class HealthcheckController {

	/*
	 * Request : 
	 * 
	 * $ curl \
	 * --request GET \
	 * --header "Content-Type: application/json" \
	 * URL + /isAlive
	 * {}
	 */
    /**
     * Recovery of API status
     * @return status
     */
    @RequestMapping(value = "/isAlive", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "Get API platform Status", 
    			  notes = "Retrieve the status of the API Service",
    			  response = String.class,
    			  tags = { "Healthcheck" }
    			  )
    public String isAlive() {
    	return "{\"status\" : \"OK\"}";
    }
}
