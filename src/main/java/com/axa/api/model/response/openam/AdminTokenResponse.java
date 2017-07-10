package com.axa.api.model.response.openam;

/**
 * Model : Response gotten when an admin token is needed (OpenAMDAO.retrieveUserAttributes())
 *
 */
public class AdminTokenResponse {
	private String tokenId;
	private String successUrl;
	
	public String getTokenId() {
		return tokenId;
	}
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}
	public String getSuccessUrl() {
		return successUrl;
	}
	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}
}
