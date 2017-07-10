package com.axa.api.test.delete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import com.axa.api.model.response.api.NoBodyResponse;
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
public class Delete {

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
	 *  Delete existing token
	 *  expected OK 200
	 */
	@Test
	public void should_200_delete_token_1() {
		
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
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.NO_CONTENT);
		assertNull(resp.getBody());
	}
	
	/*
	 *  Delete existing locked token
	 *  expected OK 200
	 */
	@Test
	public void should_200_delete_token_2() {
		
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
		if(resp.getBody().getToken().equals("000000")) tv.setCode("000000");
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
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.NO_CONTENT);
		assertNull(resp.getBody());
	}
	
	/*
	 *  Delete existing expired token
	 *  expected OK 200
	 */
	@Test
	public void should_200_delete_token_3() {
		
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
		
		try { Thread.sleep(schemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxValidityTime()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.NO_CONTENT);
		assertNull(resp.getBody());
	}
	
	/*
	 *  Delete existing token missing one of the three required inputs
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_delete_token() {
		
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
		
		TokenInput tok2 = new TokenInput();
		tok2.setChannel(ChannelEnum.none);
		tok2.setSchema("schema_test-noscope");
		
		TokenInput tok3 = new TokenInput();
		tok.setUserIdentifier("uid_test");
		tok2.setSchema("schema_test-noscope");
		
		TokenInput tok4 = new TokenInput();
		tok.setUserIdentifier("uid_test");
		tok2.setChannel(ChannelEnum.none);

		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok2,headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok3,headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok4,headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Delete existing token with wrong userIdentifier
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_delete_token_1() {
		
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
		
		tok.setUserIdentifier("fake_uid");
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok,headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
	
	/*
	 *  Delete existing token with wrong channel
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_delete_token_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		HttpEntity<TokenInput> he = new HttpEntity<>(tok, headers);

		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, he, Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setChannel(ChannelEnum.sms);
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok,headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
	
	/*
	 *  Delete existing token with wrong schema
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_delete_token_3() {
		
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
		
		tok.setSchema("fake_schema");
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok,headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
	
	/*
	 *  Delete non-existing token
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_delete_token_4() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	 
		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok, headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
}
