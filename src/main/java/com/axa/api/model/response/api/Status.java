package com.axa.api.model.response.api;

/**
 * Model : Output sent by the API with the status of the action and its message (UNLOCK and VALIDATE)
 *
 */
public class Status {
	private int code;
	private String message;

	public Status() {
	}
	
	public Status(int code, String message) {
		this.message = message;
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}