package com.axa.api.model.input;

import javax.validation.constraints.NotNull;

import com.axa.api.model.enumeration.ChannelEnum;

import io.swagger.annotations.ApiModelProperty;

/**
 * Model : Input expected containing userIdentifier, channel and schema
 *
 */
public class MinimalTokenInput {

	@NotNull(message = "userIdentifier is required")
	@ApiModelProperty(required = true)
	private String userIdentifier;
	@NotNull(message = "channel is required")
	@ApiModelProperty(required = true)
	private ChannelEnum channel;
	@NotNull(message = "schema is required")
	@ApiModelProperty(required = true)
	private String schema;
	
	public MinimalTokenInput() {		
	}
	
	public String getUserIdentifier() {
		return userIdentifier;
	}
	public void setUserIdentifier(String userIdentifier) {
		this.userIdentifier = userIdentifier;
	}
	public ChannelEnum getChannel() {
		return channel;
	}
	public void setChannel(ChannelEnum channel) {
		this.channel = channel;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
}