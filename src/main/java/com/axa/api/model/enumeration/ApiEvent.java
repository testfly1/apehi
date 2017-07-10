package com.axa.api.model.enumeration;

/**
 * Model : Enumeration of the different actions performed by the API
 *
 */
public enum ApiEvent {
	AUTHENTICATE("authenticate"),
	GETTOKEN("OTP status"),
	GENERATETOKEN("OTP generation"),
	DELETETOKEN("OTP deletion"),
	VALIDATETOKEN("OTP validation"),
	UNLOCKTOKEN("OTP unlock");
	
	private String value;
	
	ApiEvent() {
	}
	
	ApiEvent(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
