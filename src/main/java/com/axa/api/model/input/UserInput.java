package com.axa.api.model.input;

import javax.validation.constraints.NotNull;

import com.axa.api.model.logging.UserInputLogging;

import io.swagger.annotations.ApiModelProperty;

/**
 * Input expected containing a login, password and schema
 *
 */
public class UserInput {
	
	@NotNull(message = "login is required")
	@ApiModelProperty(required = true)
	private String login;
	@NotNull(message = "password is required")
	@ApiModelProperty(required = true)
	private String password;
	@NotNull(message = "schema is required")
	@ApiModelProperty(required = true)
	private String schema;

	public UserInput() {
	}
	
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public UserInputLogging getWithoutPassword() {
		return new UserInputLogging(this.login, this.schema);
	}
}
