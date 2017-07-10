package com.axa.api.configuration.yml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Component containing OpenAM server informations
 *
 */
@Configuration
@ConfigurationProperties(prefix = "openam")
public class OpenAMConfig {

	private String url;
	private String port;
	private Boolean ssl;
	private String deploymentPath;
	private String cookieName;
	private String adminUser;
	private String adminPwd;
	private String adminServiceChain;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public Boolean getSsl() {
		return ssl;
	}
	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}
	public String getDeploymentPath() {
		return deploymentPath;
	}
	public void setDeploymentPath(String deploymentPath) {
		this.deploymentPath = deploymentPath;
	}
	public String getAdminUser() {
		return adminUser;
	}
	public void setAdminUser(String adminUser) {
		this.adminUser = adminUser;
	}
	public String getAdminPwd() {
		return adminPwd;
	}
	public void setAdminPwd(String adminPwd) {
		this.adminPwd = adminPwd;
	}
	public String getAdminServiceChain() {
		return adminServiceChain;
	}
	public void setAdminServiceChain(String adminServiceChain) {
		this.adminServiceChain = adminServiceChain;
	}
	public String getCookieName() {
		return cookieName;
	}
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}
	
	public String getOpenAMUrl() {
		if (this.ssl.booleanValue())
			return "https://" + this.url + ":" + this.port + "/" + this.deploymentPath;
		else
			return "http://" + this.url + ":" + this.port + "/" + this.deploymentPath;
	}
	
	public String getUserAuthenticationUrl(String scope) {				
		return this.getOpenAMUrl() + "/json/authenticate?authIndexType=module&authIndexValue=" + scope + "&noSession=true";
	}
	public String getAdminAuthenticationUrl() {
		return this.getOpenAMUrl() + "/json/authenticate?authIndexType=service&authIndexValue=" + this.adminServiceChain;
	}
	public String getSessionExpirationUrl(String tokenId) {
		return this.getOpenAMUrl() + "/json/sessions?_action=getTimeLeft&tokenId=" + tokenId;
	}
	public String getUserAttributesUrl(String login, String scope) {
		return this.getOpenAMUrl() + "/json/users?_queryFilter=b2CSRlogin eq \"" + login + "\" and scope eq \"" + scope + "\"";	
	}
}