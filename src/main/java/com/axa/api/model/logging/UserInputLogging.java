package com.axa.api.model.logging;

/**
 * Model : Generation of an object (UserInput) to log
 *
 */
public class UserInputLogging {
	private String login;
	private String schema;

	public UserInputLogging() {
	}
	public UserInputLogging(String login, String schema) {
		this.login = login;
		this.schema = schema;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
}