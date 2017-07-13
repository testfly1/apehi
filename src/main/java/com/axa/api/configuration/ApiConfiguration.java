package com.axa.api.configuration;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.client.RestTemplate;

import com.axa.api.configuration.yml.LdapConfig;
import com.axa.api.configuration.yml.SSLConfig;
import com.axa.api.model.logging.EventLog;

/**
 * Initialization of the components' values and context of the API
 *
 */
@Configuration
public class ApiConfiguration {

	@Autowired
	private LdapConfig ldapConfig;

	@Autowired
	private SSLConfig sslConfig;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
    public SecureRandom secureRandom() {
       try {
         return SecureRandom.getInstance("SHA1PRNG");
       } catch(Throwable e){     
          ApiLogger.log.error("Error initializing SecureRandom for OTP generation.");	
          return null;
       }                           
    }
	
	@Bean
	public LdapContextSource contextSource() {
		LdapContextSource context = new LdapContextSource();
		context.setUrl(ldapConfig.getLdapServerUrl());
		context.setBase(ldapConfig.getTokenBaseDn());
		context.setUserDn(ldapConfig.getUserName());
		context.setPassword(ldapConfig.getPassword());
		context.afterPropertiesSet();
		return context;
	}
	
	@Bean
	public LdapTemplate ldapTemplate() {
		return new LdapTemplate(contextSource());
	}
	
	/**
	 * Toleration of self signed SSL
	 * @return sslContext
	 */
	@Bean
	public SSLContext trustSelfSignedSSL() {
		if (sslConfig.getBypassCertificates()) {
			try {
				SSLContext context = SSLContext.getInstance("TLS");
				X509TrustManager tm = new X509TrustManager( ) {
				
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					}
					
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					}
				};
				context.init(null, new TrustManager[]{ tm }, null);
				SSLContext.setDefault(context);
			}
			catch (Exception e) {
				ApiLogger.log.error("Error initializing TrustManager for SSL bypass.");	
			}
		}
		return null;
	}
}
