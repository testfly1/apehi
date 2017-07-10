package com.axa.api.configuration.yml;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.axa.api.exception.InvalidSchemaException;

/**
 * Component containing schema informations
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class SchemaItemConfig {

	private String scope;
	private Map<String, ChannelConfig> properties = new HashMap<>();

	public SchemaItemConfig() {
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Map<String, ChannelConfig> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, ChannelConfig> properties) {
		this.properties = properties;
	}
	
	public ChannelConfig getChannelConfig(String channel) throws InvalidSchemaException {
    	ChannelConfig channelConfig = properties.get(channel);

    	if (channelConfig != null) {
    		return channelConfig;
    	}
    	
    	throw new InvalidSchemaException("channel does not exist or is not configured for this schema");
    }
}