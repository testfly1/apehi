package com.axa.api.test.unlock;

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
import com.axa.api.configuration.StubLDAPConfig;
import com.axa.api.configuration.yml.LdapConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.model.response.api.Status;
import com.axa.api.model.response.api.Token;
import com.axa.api.model.enumeration.ChannelEnum;
import com.axa.api.model.input.TokenInput;
import com.axa.api.model.input.TokenValidation;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class UnlockTest {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private SchemaListConfig schemaListConfig;
	
	@Autowired
	private LdapConfig ldapConfig;
	
	private StubLDAPConfig LDAPServer;
	
	@Before
	public void start(){
		LDAPServer = new StubLDAPConfig(ldapConfig).start();
	}
	
	@After
	public void stop(){
		LDAPServer.stop();
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
	
	private void unlockToken(TokenInput tok){
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		
		ResponseEntity<Status> resp = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
	}
	
	/*
	 *  Unlock locked token
	 *  expected OK 200
	 */
	@Test
	public void should_200_unlock_token_1() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

		createToken(tok);
		
		lockToken(tok);
	
		unlockToken(tok);
	}
	
	/*
	 *  Unlock not locked token
	 *  expected OK 200
	 */
	@Test
	public void should_200_unlock_token_2() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    createToken(tok);
		
		unlockToken(tok);
	}
	
	/*
	 *  Unlock locked expired token
	 *  expected OK 200
	 */
	@Test
	public void should_200_unlock_token_3() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

		createToken(tok);
		
		lockToken(tok);
	
		try { Thread.sleep(schemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxValidityTime()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		unlockToken(tok);
	}

	/*
	 *  Unlock locked token missing one of the three required inputs
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_unlock_token() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		createToken(tok);
		
		lockToken(tok);
		
		TokenInput tok2 = new TokenInput();
		tok2.setChannel(ChannelEnum.none);
	    tok2.setSchema("schema_test-noscope");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setUserIdentifier("uid_test");
	    tok3.setSchema("schema_test-noscope");
	    
	    TokenInput tok4 = new TokenInput();
		tok4.setUserIdentifier("uid_test");
		tok4.setChannel(ChannelEnum.none);
	    
		try{
			unlockToken(tok2);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try{
			unlockToken(tok3);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try{
			unlockToken(tok4);
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
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    createToken(tok);
		
		lockToken(tok);
		
		tok.setUserIdentifier("fake_uid");

		try {
			unlockToken(tok);
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
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    createToken(tok);
		
		lockToken(tok);
		
		tok.setChannel(ChannelEnum.mail);
				
		try {
			unlockToken(tok);
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
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    createToken(tok);
	    
	    lockToken(tok);
		
		tok.setSchema("fake_schema");
		
		try {
			unlockToken(tok);
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
			    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		try {
			unlockToken(tok);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
}
