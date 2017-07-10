package com.axa.api.model.input;

import com.axa.api.validator.UniqueOptionalIdentifier;

/**
 * Model : Input expected containing MinimalTokenInput and channels' informations (mail and phone)
 *
 */
@UniqueOptionalIdentifier(message = "only one attribute between mail and phone can be used at a time")
public class TokenInput extends MinimalTokenInput {

	private String mail;
	private String phone;

	public TokenInput() {
	}
	
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
}