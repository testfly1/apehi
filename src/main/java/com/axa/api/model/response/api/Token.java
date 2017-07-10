package com.axa.api.model.response.api;

/**
 * Model : Output sent by the API with the created token informations (GENERATE)
 *
 */
public class Token extends Status {
	private String token;

	public Token() {
	}
	
	public Token(String token) {
		this.token = token;
	}

	public Token(String token, Integer code, String message) {
		super(code, message);
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}