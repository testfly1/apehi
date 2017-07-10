package com.axa.api.exception;

/**
 * Exception : Invalid Schema Exception
 */
public class InvalidSchemaException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private String message;

	public InvalidSchemaException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
