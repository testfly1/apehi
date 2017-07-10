package com.axa.api.configuration.yml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Component containing SSL connections informations
 *
 */
@Configuration
@ConfigurationProperties(prefix = "ssl")
public class SSLConfig {

	private Boolean bypassCertificates;

	public Boolean getBypassCertificates() {
		return bypassCertificates;
	}

	public void setBypassCertificates(Boolean bypassCertificates) {
		this.bypassCertificates = bypassCertificates;
	}
}
