package com.axa.api.model.logging;

/**
 * Model : Generation of an object (MinimalTokenInput) to log
 *
 */
public class MinimalTokenInputLogging {

	private String userIdentifier;
	private String channel;
	private String schema;
	
	public MinimalTokenInputLogging() {		
	}

	public MinimalTokenInputLogging(String userIdentifier, String channel, String schema) {
		this.userIdentifier = userIdentifier;
		this.channel = channel;
		this.schema = schema;
	}
	
	public String getUserIdentifier() {
		return userIdentifier;
	}
	public void setUserIdentifier(String userIdentifier) {
		this.userIdentifier = userIdentifier;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
}