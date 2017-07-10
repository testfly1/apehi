package com.axa.api.configuration.yml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Component containing LDAP server informations
 *
 */
@Configuration
@ConfigurationProperties(prefix = "ldap")
public class LdapConfig {

	private String url;
	private String port;
	private Boolean ssl;
	private String baseDN;
	private String ou;
	private String userName;
	private String password;

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
	public String getBaseDN() {
		return baseDN;
	}
	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}
	public String getOu() {
		return ou;
	}
	public void setOu(String ou) {
		this.ou = ou;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getTokenBaseDn() {
		return "ou=" + this.ou + "," + this.baseDN;
	}
	
	public String getLdapServerUrl() {
		if (this.ssl.booleanValue())
			return "ldaps://" + this.url + ":" + this.port;
		else
			return "ldap://" + this.url + ":" + this.port;
	}
}
