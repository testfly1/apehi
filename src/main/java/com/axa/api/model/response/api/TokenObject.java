package com.axa.api.model.response.api;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

/**
 * Model : Output sent by the API with all the stored token informations (GET)
 *
 */
@Entry(objectClasses = { "axaOtp", "top" })
public class TokenObject {
	
	@Id
	@JsonIgnore
	private Name dn;
	
	@ApiModelProperty(hidden = true)
	@Attribute(name = "uid")
	@DnAttribute(value = "uid", index = 0)
	private String uid;
	
	@Attribute(name = "userIdentifier")
	private String userIdentifier;
	@Attribute(name = "phone")
	private String phone;
	@Attribute(name = "mail")
	private String mail;
	
	@Attribute(name = "generationTime")
	private Long generationTime;
	@ApiModelProperty(hidden = true)
	@Attribute(name = "code")
	private String code;
	@Attribute(name = "channel")
	private String channel;
	@Attribute(name = "status")
	private String status;
	
	@Attribute(name = "validityTime")
	private Integer validityTime;
	@Attribute(name = "failedAttempt")
	private Integer failedAttempt;
	@Attribute(name = "maxFailedAttempt")
	private Integer maxFailedAttempt;
	@Attribute(name = "successfulAttempt")
	private Integer successfulAttempt;
	@Attribute(name = "maxSuccessfulAttempt")
	private Integer maxSuccessfulAttempt;
	@Attribute(name = "schema")
	private String schema;
	@Attribute(name = "lockedTime")
	private Long lockedTime;
	@Attribute(name = "lockDuration")
	private Integer lockDuration;
	
	public TokenObject() {
		
	}
	
	public TokenObject(String uid, String userIdentifier, String phone, String mail, Long generationTime, 
			String code, String channel, String status, Integer validityTime, Integer failedAttempt, Integer maxFailedAttempt, 
			Integer successfulAttempt, Integer maxSuccessfulAtempt, String schema, Integer lockDuration) {

		this.uid = uid;
		this.userIdentifier = userIdentifier;
		this.phone = phone;
		this.mail = mail;
		this.generationTime = generationTime;
		this.code = code;
		this.channel = channel;
		this.status = status;
		this.validityTime = validityTime;
		this.failedAttempt = failedAttempt;
		this.maxFailedAttempt = maxFailedAttempt;
		this.successfulAttempt = successfulAttempt;
		this.maxSuccessfulAttempt = maxSuccessfulAtempt;
		this.schema = schema;
		this.lockDuration = lockDuration;
	}
	
	public Name getDn() {
		return dn;
	}
	public void setDn(Name dn) {
		this.dn = dn;
	}
	@JsonIgnore
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUserIdentifier() {
		return userIdentifier;
	}
	public void setUserIdentifier(String userIdentifier) {
		this.userIdentifier = userIdentifier;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public Long getGenerationTime() {
		return generationTime;
	}
	public void setGenerationTime(Long generationTime) {
		this.generationTime = generationTime;
	}
	@JsonIgnore
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getValidityTime() {
		return validityTime;
	}
	public void setValidityTime(Integer validityTime) {
		this.validityTime = validityTime;
	}
	public Integer getFailedAttempt() {
		return failedAttempt;
	}
	public void setFailedAttempt(Integer failedAttempt) {
		this.failedAttempt = failedAttempt;
	}
	public Integer getMaxFailedAttempt() {
		return maxFailedAttempt;
	}
	public void setMaxFailedAttempt(Integer maxFailedAttempt) {
		this.maxFailedAttempt = maxFailedAttempt;
	}
	public Integer getSuccessfulAttempt() {
		return successfulAttempt;
	}
	public void setSuccessfulAttempt(Integer successfulAttempt) {
		this.successfulAttempt = successfulAttempt;
	}
	public Integer getMaxSuccessfulAttempt() {
		return maxSuccessfulAttempt;
	}
	public void setMaxSuccessfulAttempt(Integer maxSuccessfulAttempt) {
		this.maxSuccessfulAttempt = maxSuccessfulAttempt;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public Long getLockedTime() {
		return lockedTime;
	}
	public void setLockedTime(Long lockedTime) {
		this.lockedTime = lockedTime;
	}
	public Integer getLockDuration() {
		return lockDuration;
	}
	public void setLockDuration(Integer lockDuration) {
		this.lockDuration = lockDuration;
	}
}