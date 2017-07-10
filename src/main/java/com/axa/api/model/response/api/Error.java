package com.axa.api.model.response.api;

/**
 * Model : Output sent by the API when an error occurs
 *
 */
public class Error {
	private int code;
	private String[] message;

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String[] getMessage() {
		return message;
	}
	public void setMessage(String[] message) {
		this.message = message;
	}
}

