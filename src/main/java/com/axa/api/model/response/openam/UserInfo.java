package com.axa.api.model.response.openam;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model : User informations gotten in a RetrieveInfoResult
 *
 */
public class UserInfo {

	@JsonProperty("cn")
	private String[] uid;
	@JsonProperty("b2CSRlogin")
	private String[] login;
	@JsonProperty("sn")
	private String[] sn;
	@JsonProperty("givenName")
	private String[] givenName;
	@JsonProperty("mail")
	private String[] mail;
	@JsonProperty("b2CSRmobile")
	private String[] phoneNumber;
	
	public String[] getUid() {
		return uid;
	}
	public void setUid(String[] uid) {
		this.uid = uid;
	}
	public String[] getLogin() {
		return login;
	}
	public void setLogin(String[] login) {
		this.login = login;
	}
	public String[] getSn() {
		return sn;
	}
	public void setSn(String[] sn) {
		this.sn = sn;
	}
	public String[] getGivenName() {
		return givenName;
	}
	public void setGivenName(String[] givenName) {
		this.givenName = givenName;
	}
	public String[] getMail() {
		return mail;
	}
	public void setMail(String[] mail) {
		this.mail = mail;
	}
	public String[] getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String[] phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
