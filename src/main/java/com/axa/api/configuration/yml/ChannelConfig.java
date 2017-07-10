package com.axa.api.configuration.yml;

import org.springframework.stereotype.Component;

/**
 * Component containing channel informations for a specific schema
 *
 */
@Component
public class ChannelConfig {

	private Integer maxValidityTime;
	private Integer maxSuccessfulAttempt;
	private Integer maxFailedAttempt;
	private Integer lockDuration;
	private String sender;
	private String language;
	
	public Integer getMaxValidityTime() {
		return maxValidityTime;
	}
	public void setMaxValidityTime(Integer maxValidityTime) {
		this.maxValidityTime = maxValidityTime;
	}
	public Integer getMaxSuccessfulAttempt() {
		return maxSuccessfulAttempt;
	}
	public void setMaxSuccessfulAttempt(Integer maxSuccessfulAttempt) {
		this.maxSuccessfulAttempt = maxSuccessfulAttempt;
	}
	public Integer getMaxFailedAttempt() {
		return maxFailedAttempt;
	}
	public void setMaxFailedAttempt(Integer maxFailedAttempt) {
		this.maxFailedAttempt = maxFailedAttempt;
	}
	public Integer getLockDuration() {
		return lockDuration;
	}
	public void setLockDuration(Integer lockDuration) {
		this.lockDuration = lockDuration;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
}