package com.axa.api.configuration.yml;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.axa.api.exception.InvalidSchemaException;

/**
 * Component containing the list of different schemas
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class SchemaListConfig {

	private Map<String, SchemaItemConfig> schemas = new HashMap<>();

	public SchemaListConfig() {
	}

	public Map<String, SchemaItemConfig> getSchemas() {
		return schemas;
	}
	
	public SchemaItemConfig getSchemaItemConfig(String schema) throws InvalidSchemaException {
    	SchemaItemConfig schemaItemConfig = schemas.get(schema);

    	if (schemaItemConfig != null && (schemaItemConfig.getProperties() != null) && !schemaItemConfig.getProperties().isEmpty()) {
    		return schemaItemConfig;
    	}

    	throw new InvalidSchemaException("schema does not exist or is not configured");
    }
}
