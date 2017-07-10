package com.axa.api.model.response.api;

import com.axa.api.model.response.openam.UserInfo;

/**
 * Model : Output sent by the API with the registered user's informations (AUTHENTICATE)
 *
 */
public class User {
	private String uid;
	private String login;
	private String sn;
	private String givenName;
	private String mail;
	private String phoneNumber;

	public User() {
	}
	
	public User(UserInfo info) {
		this.uid = info.getUid()[0];
		this.login = info.getLogin()[0];
		this.sn = info.getSn()[0];
		this.givenName = info.getGivenName()[0];
		this.mail = info.getMail()[0];
		this.phoneNumber = info.getPhoneNumber()[0];
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
