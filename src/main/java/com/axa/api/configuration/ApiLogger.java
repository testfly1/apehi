package com.axa.api.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creation of a Logger
 *
 */
public class ApiLogger{

	public static Logger log = LoggerFactory.getLogger("API");
	
	public ApiLogger(String className) {
		log = LoggerFactory.getLogger(className);
	}
}