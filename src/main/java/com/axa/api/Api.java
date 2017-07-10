package com.axa.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Main : Starting the API
 *
 */
@SpringBootApplication
public class Api extends SpringBootServletInitializer {

    private static Class<Api> applicationClass = Api.class;
	
    /* 
     * Creates and runs a SpringBoot application
     */
    public static void main(String[] args) {
        SpringApplication.run(Api.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }
}
