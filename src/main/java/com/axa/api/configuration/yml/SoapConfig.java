package com.axa.api.configuration.yml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Component containing SOAP server informations
 *
 */
@Configuration
@ConfigurationProperties(prefix = "soap")
public class SoapConfig {

	private String url;
	private Integer port;
	private Boolean ssl;
	private String deploymentPath;
	private String application;
	private String organization;
	private String login;
	private String password;
	private Boolean bypassSendingToken;
	private Boolean bypassAuthorization;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Boolean getSsl() {
		return ssl;
	}
	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Boolean getBypassSendingToken() {
		return bypassSendingToken;
	}
	public void setBypassSendingToken(Boolean bypassSendingToken) {
		this.bypassSendingToken = bypassSendingToken;
	}
	public Boolean getBypassAuthorization() {
		return bypassAuthorization;
	}
	public void setBypassAuthorization(Boolean bypassAuthorization) {
		this.bypassAuthorization = bypassAuthorization;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getDeploymentPath() {
		return deploymentPath;
	}
	public void setDeploymentPath(String deploymentPath) {
		this.deploymentPath = deploymentPath;
	}
	
	public String getSoapServerUrl() {
		if (this.ssl.booleanValue())
			return "https://" + this.url + ":" + this.port + "/" + this.deploymentPath;
		else
			return "http://" + this.url + ":" + this.port + "/" + this.deploymentPath;
	}
}