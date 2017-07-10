package com.axa.api.exception;

/**
 * Exception : Internal Server Error
 */
public class InternalServerErrorException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private String message;

	public InternalServerErrorException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
