package com.axa.api.configuration;

import com.axa.api.configuration.yml.LdapConfig;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;

public class StubLDAPConfig {

	private LdapConfig ldapConfig;
	private InMemoryDirectoryServer LDAPServer;
	
	public StubLDAPConfig(LdapConfig ldapConfig){
		this.ldapConfig = ldapConfig;
	}
	
	public StubLDAPConfig start(){
		stubMethod();
		return this;
	}
	
	public void stop(){
		LDAPServer.shutDown(true);
	}
	
	private static Entry getBaseDomain() throws LDIFException {
		return new Entry("dn: dc=example,dc=com",
        		"objectClass: top",
        		"objectClass: domain",
        		"dc: example");
	}
	
	private static Entry getTokensOu() throws LDIFException {
		return new Entry("dn: ou=tokens,dc=example,dc=com",
        		"objectClass: organizationalUnit",
        		"objectClass: top",
        		"ou: tokens",
        		"description: Tokens OU");
	}
	
	private void stubMethod(){
		InMemoryDirectoryServerConfig config;

		try {
	        config = new InMemoryDirectoryServerConfig(ldapConfig.getBaseDN());
	        config.addAdditionalBindCredentials(ldapConfig.getUserName(), ldapConfig.getPassword());
	        config.setSchema(null);
	        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", Integer.valueOf(ldapConfig.getPort())));
	        
	        LDAPServer = new InMemoryDirectoryServer(config);
	        LDAPServer.add(getBaseDomain());
	        LDAPServer.add(getTokensOu());

	        LDAPServer.startListening();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
