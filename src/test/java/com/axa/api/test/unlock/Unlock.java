package com.axa.api.test.unlock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.axa.api.configuration.yml.LdapConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.model.response.api.Status;
import com.axa.api.model.response.api.Token;
import com.axa.api.model.enumeration.ChannelEnum;
import com.axa.api.model.input.TokenInput;
import com.axa.api.model.input.TokenValidation;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class Unlock {

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	SchemaListConfig schemaListConfig;
	
	@Autowired
	LdapConfig ldapConfig;
	
	private InMemoryDirectoryServer LDAPServer;
	
	@Before
	public void start() {
		startStubLDAP();
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
	
	private void startStubLDAP(){
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
	
	
	@After
	public void stop() {
		LDAPServer.shutDown(true);
	}

	/*
	 *  Unlock locked token
	 *  expected OK 200
	 */
	@Test
	public void should_200_unlock_token_1() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");

		try {
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
	
		ResponseEntity<Status> resp3 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
	}
	
	/*
	 *  Unlock not locked token
	 *  expected OK 200
	 */
	@Test
	public void should_200_unlock_token_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		ResponseEntity<Status> resp2 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertEquals(resp2.getBody().getCode(), HttpStatus.OK.value());
	}
	
	/*
	 *  Unlock locked expired token
	 *  expected OK 200
	 */
	@Test
	public void should_200_unlock_token_3() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");

		try {
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try {
					restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
	
		try { Thread.sleep(schemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxValidityTime()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
		} catch(HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
		}
	}

	/*
	 *  Unlock locked token missing one of the three required inputs
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_unlock_token() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		
		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");

		try {
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try {
					restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		TokenInput tok2 = new TokenInput();
		tok2.setChannel(ChannelEnum.none);
	    tok2.setSchema("schema_test-noscope");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setUserIdentifier("uid_test");
	    tok3.setSchema("schema_test-noscope");
	    
	    TokenInput tok4 = new TokenInput();
		tok4.setUserIdentifier("uid_test");
		tok4.setChannel(ChannelEnum.none);
	    
	    try {
			restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok2, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    try {
			restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok3, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    try {
			restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok4, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Unlock locked token with wrong userIdentifier
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_unlock_token_1() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());

		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");

		try {
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		tok.setUserIdentifier("fake_uid");

		try {
			restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}

	/*
	 *  Unlock locked token with wrong channel
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_unlock_token_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());

		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");
	
		try {
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try {
					restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		tok.setChannel(ChannelEnum.mail);
				
		try {
			restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
	
	/*
	 *  Unlock locked token with wrong schema
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_unlock_token_3() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		
		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");

		try {
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		tok.setSchema("fake_schema");
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
	
	/*
	 *  Unlock non-existing token
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_unlock_token_4() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		HttpEntity<TokenInput> he = new HttpEntity<>(tok, headers);
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, he, Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
}
