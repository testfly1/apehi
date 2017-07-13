package com.axa.api.test.get;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import com.axa.api.configuration.StubLDAPConfig;
import com.axa.api.configuration.StubSOAPConfig;
import com.axa.api.configuration.yml.LdapConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.configuration.yml.SoapConfig;
import com.axa.api.exception.InternalServerErrorException;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.model.response.api.Status;
import com.axa.api.model.response.api.Token;
import com.axa.api.model.response.api.TokenObject;
import com.axa.api.model.enumeration.ChannelEnum;
import com.axa.api.model.input.TokenInput;
import com.axa.api.model.input.TokenValidation;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class Get {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private SchemaListConfig schemaListConfig;
	
	@Autowired
	private LdapConfig ldapConfig;
	
	@Autowired
	private SoapConfig soapConfig;
	
	private StubLDAPConfig LDAPServer;
	private StubSOAPConfig SOAPServer;
	
	@Before
	public void start() throws InternalServerErrorException{
		LDAPServer = new StubLDAPConfig(ldapConfig).start();
		try {
			SOAPServer = new StubSOAPConfig(soapConfig).start();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new InternalServerErrorException("SOAP STUB Error");
		}
	}
	
	@After
	public void stop(){
		LDAPServer.stop();
		SOAPServer.stop();
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
	
	public ResponseEntity<TokenObject> getToken(TokenInput tok){
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		
		ResponseEntity<TokenObject> resp = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody());
		assertEquals(resp.getBody().getUserIdentifier(), tok.getUserIdentifier());
		assertEquals(resp.getBody().getChannel(), tok.getChannel().toString());
		assertEquals(resp.getBody().getSchema(), tok.getSchema());
		assertEquals(resp.getBody().getMail(), tok.getMail());
		assertEquals(resp.getBody().getPhone(), tok.getPhone());
		assertNull(resp.getBody().getCode());
		
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
	
	/*
	 *  Get status of existing hidden token
	 *  expected OK 200
	 */
	@Test
	public void should_200_status_token_1() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		createToken(tok);
		
		getToken(tok);
	}
	
	/*
	 *  Get status of existing locked hidden token
	 *  expected OK 200
	 */
	@Test
	public void should_200_status_token_2() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		createToken(tok);
		
		lockToken(tok);
		
		assertEquals(getToken(tok).getBody().getStatus(), "locked");
	}
	
	/*
	 *  Get status of existing expired hidden token
	 *  expected OK 200
	 */
	@Test
	public void should_200_status_token_3() {
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		createToken(tok);
			
		try { Thread.sleep(schemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxValidityTime()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		getToken(tok);
	}
	
	/*
	 *  Get status of 4 existing tokens with 4 different hidden tokens
	 *  expected OK 200
	 */
	@Test
	public void should_200_status_token_4() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok1 = new TokenInput();
	    tok1.setUserIdentifier("login_test_1");
	    tok1.setChannel(ChannelEnum.none);
	    tok1.setSchema("schema_test-noscope");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setUserIdentifier("login_test_1");
	    tok2.setChannel(ChannelEnum.mail);
	    tok2.setMail("mail_test");
	    tok2.setSchema("schema_test-noscope");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setUserIdentifier("login_test_1");
	    tok3.setChannel(ChannelEnum.sms);
	    tok3.setPhone("phone_test");
	    tok3.setSchema("schema_test-noscope");
	    
	    TokenInput tok4 = new TokenInput();
	    tok4.setUserIdentifier("login_test_1");
	    tok4.setChannel(ChannelEnum.voice);
	    tok4.setPhone("phone_test");
	    tok4.setSchema("schema_test-noscope");
	    
	    createToken(tok1);
	    
	    createToken(tok2);
	    
	    createToken(tok3);
	    
	    createToken(tok4);
		
		getToken(tok1);
		
		getToken(tok2);
		
		getToken(tok3);
		
		getToken(tok4);
	}
		
	/*
	 *  Get status of existing token with wrong userIdentifier parameter
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_status_token_1() {
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    createToken(tok);

	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + "fake_uid" + "&channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
	
	/*
	 *  Get status of existing token with wrong schema parameter
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_status_token_2() {
			    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    createToken(tok);
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + "fake_channel" + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}

	/*
	 *  Get status of existing token with wrong channel parameter
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_status_token_3() {
		
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    createToken(tok);
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel() + "&schema=" + "fake_schema", HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
	
	/*
	 *  Get status of non-existing token
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_status_token_4() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	
		try {
			restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}

	/*
	 *  Get status of existing token without one of the three required parameters
	 *  expected INTERNAL_SERVER_ERROR 500
	 */
	@Test
	public void should_500_status_token() {
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		createToken(tok);
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens?channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpServerErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpServerErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpServerErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
