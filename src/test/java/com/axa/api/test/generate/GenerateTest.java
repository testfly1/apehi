package com.axa.api.test.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
import com.axa.api.configuration.StubOpenAMConfig;
import com.axa.api.configuration.StubSOAPConfig;
import com.axa.api.configuration.yml.LdapConfig;
import com.axa.api.configuration.yml.OpenAMConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.configuration.yml.SoapConfig;
import com.axa.api.exception.InternalServerErrorException;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.model.response.api.Status;
import com.axa.api.model.response.api.Token;
import com.axa.api.model.enumeration.ChannelEnum;
import com.axa.api.model.input.TokenInput;
import com.axa.api.model.input.TokenValidation;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class GenerateTest {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private SchemaListConfig schemaListConfig;
	
	@Autowired
	private LdapConfig ldapConfig;
	
	@Autowired
	private SoapConfig soapConfig;
	
	@Autowired
	private OpenAMConfig openAMConfig;
	
	private StubLDAPConfig LDAPServer;
	private StubOpenAMConfig OpenAMServer;
	private StubSOAPConfig SOAPServer;
	
	@Before
	public void start() throws InternalServerErrorException{
		LDAPServer = new StubLDAPConfig(ldapConfig).start();
		OpenAMServer = new StubOpenAMConfig(openAMConfig).start();
		try {
			SOAPServer = new StubSOAPConfig(soapConfig).start();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new InternalServerErrorException("SOAP STUB Error");
		}
	}
	
	@After
	public void stop(){
		LDAPServer.stop();
		OpenAMServer.stop();
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
	 *  Generate token for non-existing user
	 *  expected OK 200 
	 */
	@Test
	public void should_200_generate_token_unregistered_1() {
		
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    tok.setUserIdentifier("uid_test");
	    
	    createToken(tok);
	}
	
	/*
	 *  Generate token for non-existing user and overwrite the existing one
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_unregistered_2() {
	    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    tok.setUserIdentifier("uid_test");
	    tok.setMail("mail_test");
		
		
		String code1 = createToken(tok).getBody().getToken();
		
		String code2 = createToken(tok).getBody().getToken();
		
		assertNotEquals(code1, code2);
	}
	
	/*
	 *  Generate 4 different tokens for non-existing user
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_unregistered_3() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok1 = new TokenInput();
	    tok1.setChannel(ChannelEnum.none);
	    tok1.setSchema("schema_test-noscope");
	    tok1.setUserIdentifier("uid_test");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setChannel(ChannelEnum.mail);
	    tok2.setMail("mail_test");
	    tok2.setSchema("schema_test-noscope");
	    tok2.setUserIdentifier("uid_test");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setChannel(ChannelEnum.sms);
	    tok3.setPhone("phone_test");
	    tok3.setSchema("schema_test-noscope");
	    tok3.setUserIdentifier("uid_test");
	    
	    TokenInput tok4 = new TokenInput();
	    tok4.setChannel(ChannelEnum.voice);
	    tok4.setPhone("phone_test");
	    tok4.setSchema("schema_test-noscope");
	    tok4.setUserIdentifier("uid_test");
	    
		createToken(tok1);
		createToken(tok2);
		createToken(tok3);
		createToken(tok4);
		
	}
	
	/*
	 *  Generate token for existing user
	 *  expected OK 200 
	 */
	@Test
	public void should_200_generate_token_registered_1() {
			
		TokenInput tok = new TokenInput();
		tok.setChannel(ChannelEnum.none);
		tok.setSchema("schema_test_1");
		tok.setUserIdentifier("login_test_1");
		    
		createToken(tok);
	}
		
	/*
	 *  Generate token for existing user and overwrite the existing one
	 *  expected OK 200
	*/
	@Test
	public void should_200_generate_token_registered_2() {
		    
		TokenInput tok = new TokenInput();
		tok.setChannel(ChannelEnum.none);
		tok.setSchema("schema_test_1");
		tok.setUserIdentifier("login_test_1");

			
		String code1 = createToken(tok).getBody().getToken();
		
		String code2 = createToken(tok).getBody().getToken();
		
		assertNotEquals(code1, code2);
	}
	
	/*
	 *  Generate 4 different tokens for existing user
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_3() {
		
	    TokenInput tok1 = new TokenInput();
	    tok1.setUserIdentifier("login_test_1");
	    tok1.setChannel(ChannelEnum.none);
	    tok1.setSchema("schema_test_1");
	    
	    TokenInput tok2 = new TokenInput();
		tok2.setUserIdentifier("login_test_1");
		tok2.setChannel(ChannelEnum.mail);
		tok2.setMail("mail_test");
		tok2.setSchema("schema_test_1");
		
		TokenInput tok3 = new TokenInput();
		tok3.setUserIdentifier("login_test_1");
		tok3.setChannel(ChannelEnum.sms);
		tok3.setPhone("phone_test");
		tok3.setSchema("schema_test_1");
		
		TokenInput tok4 = new TokenInput();
		tok4.setUserIdentifier("login_test_1");
		tok4.setChannel(ChannelEnum.voice);
		tok4.setPhone("phone_test");
		tok4.setSchema("schema_test_1");
		
		createToken(tok1);
		
		createToken(tok2);
		
		createToken(tok3);
		
		createToken(tok4);
	}
	
	/*
	 *  Generate 4 different tokens for existing user without channel informations inputs
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_4() {
		
	    TokenInput tok1 = new TokenInput();
	    tok1.setUserIdentifier("login_test_1");
	    tok1.setChannel(ChannelEnum.none);
	    tok1.setSchema("schema_test_1");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setUserIdentifier("login_test_1");
	    tok2.setChannel(ChannelEnum.mail);
	    tok2.setSchema("schema_test_1");
		
	    TokenInput tok3 = new TokenInput();
	    tok3.setUserIdentifier("login_test_1");
	    tok3.setChannel(ChannelEnum.sms);
	    tok3.setSchema("schema_test_1");
	    
	    TokenInput tok4 = new TokenInput();
	    tok4.setUserIdentifier("login_test_1");
	    tok4.setChannel(ChannelEnum.voice);
	    tok4.setSchema("schema_test_1");
	    
	    createToken(tok1);
	    
	    createToken(tok2);
	    
	    createToken(tok3);
	    
	    createToken(tok4);
	}
	
	/*
	 *  Generate 4 different tokens for existing user with channel information inputs
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_5() {
	    
	    TokenInput tok1 = new TokenInput();
	    tok1.setUserIdentifier("login_test_4");
	    tok1.setChannel(ChannelEnum.none);
	    tok1.setSchema("schema_test_1");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setUserIdentifier("login_test_4");
	    tok2.setChannel(ChannelEnum.mail);
	    tok2.setMail("mail_test");
	    tok2.setSchema("schema_test_1");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setUserIdentifier("login_test_4");
	    tok3.setChannel(ChannelEnum.sms);
	    tok3.setPhone("phone_test");
	    tok3.setSchema("schema_test_1");
	    
	    TokenInput tok4 = new TokenInput();
	    tok4.setUserIdentifier("login_test_4");
	    tok4.setChannel(ChannelEnum.voice);
	    tok4.setPhone("phone_test");
	    tok4.setSchema("schema_test_1");
	    
	    createToken(tok1);
	    
	    createToken(tok2);
	    
	    createToken(tok3);
	    
	    createToken(tok4);
	}
	
	/*
	 *  Generate 8 different tokens for existing user without channel informations inputs on two scopes
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_6() {
		
		TokenInput tok1 = new TokenInput();
	    tok1.setUserIdentifier("login_test_1");
	    tok1.setChannel(ChannelEnum.none);
	    tok1.setSchema("schema_test_1");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setUserIdentifier("login_test_1");
	    tok2.setChannel(ChannelEnum.mail);
	    tok2.setSchema("schema_test_1");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setUserIdentifier("login_test_1");
	    tok3.setChannel(ChannelEnum.sms);
	    tok3.setSchema("schema_test_1");
	    
	    TokenInput tok4 = new TokenInput();
	    tok4.setUserIdentifier("login_test_1");
	    tok4.setChannel(ChannelEnum.voice);
	    tok4.setSchema("schema_test_1");
	    
	    TokenInput tok5 = new TokenInput();
	    tok5.setUserIdentifier("login_test_1");
	    tok5.setChannel(ChannelEnum.none);
	    tok5.setSchema("schema_test_2");
	    
	    TokenInput tok6 = new TokenInput();
	    tok6.setUserIdentifier("login_test_1");
	    tok6.setChannel(ChannelEnum.mail);
	    tok6.setSchema("schema_test_2");
	    
	    TokenInput tok7 = new TokenInput();
	    tok7.setUserIdentifier("login_test_1");
	    tok7.setChannel(ChannelEnum.sms);
	    tok7.setSchema("schema_test_2");
	    
	    TokenInput tok8 = new TokenInput();
	    tok8.setUserIdentifier("login_test_1");
	    tok8.setChannel(ChannelEnum.voice);
	    tok8.setSchema("schema_test_2");
	    
	    createToken(tok1);
	    
	    createToken(tok2);
	    
	    createToken(tok3);
	    
	    createToken(tok4);
	    
	    createToken(tok5);
	    
	    createToken(tok6);
	    
	    createToken(tok7);
	    
	    createToken(tok8);
	}
	
	
	/*
	 *  Generate token for non-existing schema
	 *  expected BAD_REQUEST 400 
	 */
	@Test
	public void should_400_generate_token_1() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("fake_schema");
	    tok.setUserIdentifier("uid_test");

		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Generate token for unknown channel
	 *  expected BAD_REQUEST 400 
	 */
	@Test
	public void should_400_generate_token_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");

		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>("{\"userIdentifier\": \"uid_test\", \"channel\" : \"123456\", \"schema\" : \"schema_test-noscope\"}", headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Generate token missing one of the three required inputs
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_generate_token_3() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setUserIdentifier("uid_test");
	    tok2.setSchema("schema_test-noscope");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setUserIdentifier("uid_test");
	    tok3.setChannel(ChannelEnum.none);

		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok2, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok3, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Generate token for non-existing user but with all parameters
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_generate_token_4() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    tok.setUserIdentifier("uid_test");
	    tok.setMail("mail_test");
	    tok.setPhone("phone_test");

		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Generate token missing channel attributes from non-existing user
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_generate_token_unregistered() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok1 = new TokenInput();
	    tok1.setChannel(ChannelEnum.mail);
	    tok1.setSchema("schema_test-noscope");
	    tok1.setUserIdentifier("uid_test");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setChannel(ChannelEnum.sms);
	    tok2.setSchema("schema_test-noscope");
	    tok2.setUserIdentifier("uid_test");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setChannel(ChannelEnum.voice);
	    tok3.setSchema("schema_test-noscope");
	    tok3.setUserIdentifier("uid_test");

	    try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok1, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok2, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok3, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Generate token for existing user but with all parameters
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_generate_token_registered_1() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_1");
	    tok.setUserIdentifier("login_test_1");
	    tok.setMail("mail_test");
	    tok.setPhone("phone_test");

		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Generate token for existing user without channel informations (input & user)
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_generate_token_registered_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		    
	    TokenInput tok1 = new TokenInput();
	    tok1.setChannel(ChannelEnum.mail);
	    tok1.setSchema("schema_test_1");
	    tok1.setUserIdentifier("login_test_4");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setChannel(ChannelEnum.sms);
	    tok2.setSchema("schema_test_1");
	    tok2.setUserIdentifier("login_test_4");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setChannel(ChannelEnum.voice);
	    tok3.setSchema("schema_test_1");
	    tok3.setUserIdentifier("login_test_4");

	    try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok1, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok2, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok3, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}

	/*
	 *  Generate token when locked token exists for non-existing user
	 *  expected FORBIDDEN 403
	 */
	@Test
	public void should_403_generate_token_unregistered() {
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

		createToken(tok);
		
		lockToken(tok);		
		
		try {
			createToken(tok);
			} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
	}
	
	/*
	 *  Generate token when locked token exists for existing user
	 *  expected FORBIDDEN 403
	 */
	@Test
	public void should_403_generate_token_registered() {
			
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
		    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("login_test_1");
		tok.setChannel(ChannelEnum.none);
		tok.setSchema("schema_test_1");

		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody());
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
			
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
			
		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");

		try {
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxSuccessfulAttempt() ; i++) {
				try{
					restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){ 
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}	
			}
		} catch (RestClientException | InvalidSchemaException e) { }		
			
		try {
			resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
	}
	
	/*
	 *  Generate token with missing user
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_generate_token_unregistered() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("fake_uid");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}
	
	/*
	 *  Generate token with wrong existing user
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_generate_token_registered() {
			
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("fake_uid");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_1");

		try {
			restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}

	
	/*
	 *  Generate and send 3 OTP to SOAP Server
	 *  expected OK 200 
	 */
	@Test
	public void should_200_soap() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setSchema("schema_test-noscope");

	    tok.setPhone("phone_test");
	    tok.setChannel(ChannelEnum.sms);
	    ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
	    assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setChannel(ChannelEnum.voice);
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setPhone(null);
		tok.setMail("mail_test");
		tok.setChannel(ChannelEnum.mail);
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
	}
	
	/*
	 *  Generate and send 3 OTP to SOAP Server with Internal Error returned (channel informations)
	 *  expected 500 INTERNAL_SERVER_ERROR
	 */
	@Test
	public void should_500_soap() {
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setSchema("schema_test-noscope");

	    tok.setPhone("phone_fake");
	    tok.setChannel(ChannelEnum.sms);
	    
	    try{
	    	restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
	    } catch (HttpServerErrorException ex) {
	    	assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
		
		tok.setChannel(ChannelEnum.voice);
		try{
	    	restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
	    } catch (HttpServerErrorException ex) {
	    	assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
		
		tok.setPhone(null);
		tok.setMail("mail_fake");
		tok.setChannel(ChannelEnum.mail);
		
		try{
	    	restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
	    } catch (HttpServerErrorException ex) {
	    	assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}
