package com.axa.api.test.validate;

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
public class Validate {

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
	
public ResponseEntity<Token> createToken(TokenInput tok){
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
	    if(tok.getChannel()==ChannelEnum.none){
	    	assertEquals(resp.getStatusCode(), HttpStatus.OK);
	    	assertNotNull(resp.getBody().getToken());
	    	assertEquals(resp.getBody().getToken().length(), 6);
	    } else {
	    	assertEquals(resp.getStatusCode(), HttpStatus.OK);
			assertNull(resp.getBody().getToken());
			assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
			assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
	    }
		
		return resp;
	}

	private void lockToken(TokenInput tok){
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		tv.setCode("wrong code");
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		
		try {
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
	}
	
	private void validateToken(TokenInput tok, String token){
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		tv.setCode(createToken(tok).getBody().getToken());
			
		ResponseEntity<Status> resp = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getCode());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp.getBody().getMessage());
		assertEquals(resp.getBody().getMessage(),"Valid");
	}
	
	/*
	 *  Validate existing token
	 *  expected OK 200
	 */
	@Test
	public void should_200_validate_token_1() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    String token = createToken(tok).getBody().getToken();
		
		validateToken(tok, token);
	}
	
	/*
	 *  Validate existing should-not-be-locked token
	 *  expected OK 200
	 */
	@Test
	public void should_200_validate_token_2() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

	    String token = createToken(tok).getBody().getToken();
		
		lockToken(tok);

		try { Thread.sleep(schemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getLockDuration()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		validateToken(tok, token);
	}
	
	/*
	 *  Validate existing token missing one of the four required inputs
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_validate_token() {
	    
	    TokenInput tok1 = new TokenInput();
	    tok1.setUserIdentifier("uid_test");
	    tok1.setChannel(ChannelEnum.none);
	    tok1.setSchema("schema_test-noscope");
	    
	    String token = createToken(tok1).getBody().getToken();
		
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
			validateToken(tok1, null);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			validateToken(tok2, token);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			validateToken(tok3, token);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			validateToken(tok4, token);	
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}		
	}
	
	/*
	 *  Validate existing token with wrong channel
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_validate_token_2() {
		
		TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    String token = createToken(tok).getBody().getToken();
	    
	    TokenInput tok_test = new TokenInput();
	    tok_test.setUserIdentifier("uid_test");
	    tok_test.setChannel(ChannelEnum.sms);
	    tok_test.setSchema("schema_test-noscope");
			
		try {
			validateToken(tok_test, token);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Validate existing token with wrong schema
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_validate_token_3() {
		
		TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    String token = createToken(tok).getBody().getToken();
	    
	    TokenInput tok_test = new TokenInput();
	    tok_test.setUserIdentifier("uid_test");
	    tok_test.setChannel(ChannelEnum.none);
	    tok_test.setSchema("fake_schema");
			
		try {
			validateToken(tok_test, token);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Validate existing token with wrong code
	 *  expected UNAUTHORIZED 401
	 */
	@Test
	public void should_401_validate_token() {
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

		createToken(tok);
						
		try {
			validateToken(tok, "wrong token");
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
		}	
	}
	
	/*
	 *  Validate existing locked token
	 *  expected FORBIDDEN 403
	 */
	@Test
	public void should_403_validate_token() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    String token = createToken(tok).getBody().getToken();
			
		lockToken(tok);
		
		try {
			validateToken(tok, token);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}	
	}

	/*
	 *  Validate existing token with wrong userIdentifier
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_validate_token_1() {
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    String token = createToken(tok).getBody().getToken();
	    
	    TokenInput tok_test = new TokenInput();
	    tok_test.setUserIdentifier("fake_uid");
	    tok_test.setChannel(ChannelEnum.none);
	    tok_test.setSchema("schema_test-noscope");
			
		try {
			validateToken(tok_test, token);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}	
	}
	
	/*
	 *  Validate existing token maxSuccessfulTime + 1
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_validate_token_2() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    String token = createToken(tok).getBody().getToken();

		try {
			for (int i = 0; i < schemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxSuccessfulAttempt(); i++)
				validateToken(tok, token);
		} catch (RestClientException | InvalidSchemaException e) { }
			
		try {
			validateToken(tok, token);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}	
	}
	
	/*
	 *  Validate non-existing token
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_validate_token_3() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	   
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
		
	    String token = "000000";
			
		try {
			validateToken(tok, token);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
}
