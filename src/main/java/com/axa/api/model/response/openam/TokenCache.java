package com.axa.api.model.response.openam;

/**
 * Model : Cache to store the admin token needed to retrieve user information and its expiration time
 *
 */
public class TokenCache {

	private String tokenId;
	private Long sessionExpirate;
	
	public TokenCache() {
	}

	public String getTokenId() {
		return tokenId;
	}
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}
	public Long getSessionExpirate() {
		return sessionExpirate;
	}
	public void setSessionExpirate(Long sessionExpirate) {
		this.sessionExpirate = sessionExpirate;
	}
}
