package com.axa.api.model.response.openam;

/**
 * Model : Response gotten when the user is authenticated (OpenAMDAO.authenticate())
 *
 */
public class AuthenticationResponse {
	private String message;
	private String successUrl;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSuccessUrl() {
		return successUrl;
	}
	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}
}
