package com.axa.api.test.get;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.regex.Pattern;

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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import com.axa.api.configuration.yml.LdapConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.configuration.yml.SoapConfig;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.model.response.api.Status;
import com.axa.api.model.response.api.Token;
import com.axa.api.model.response.api.TokenObject;
import com.axa.api.model.enumeration.ChannelEnum;
import com.axa.api.model.input.TokenInput;
import com.axa.api.model.input.TokenValidation;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;
import com.unboundid.util.Base64;
import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class Get {
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	SchemaListConfig SchemaListConfig;
	
	@Autowired
	LdapConfig ldapConfig;
	
	@Autowired
	SoapConfig soapConfig;
	
	private InMemoryDirectoryServer LDAPServer;
	private StubServer SOAPServer;
	
	@Before
	public void start() {
		startStubLDAP();
		try {
			startStubSOAP();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private void startStubSOAP() throws SAXException, IOException, ParserConfigurationException{
		
		SOAPServer = new StubServer(new Integer(soapConfig.getPort())).run();
		
		/** SOAP OK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "</destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.ok(),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Infinite</providerName>" +
										"<status>SEND_SUCCESSFUL</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
		/** SOAP additionalAttributes NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"<additionalAttributes" + "(?!(/|>OTP=[0-9]{6}</additionalAttributes|>subject=AXA OTP Generation</additionalAttributes)).*" + ">" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "</destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
	
		/** SOAP destination NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
										"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)"  +
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(?!(phone_test|mail_test)<).*" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
		/** SOAP message NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
										"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(?!(Use the code [0-9]{6} for verification|OTP)<).*" + "/message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
		/** SOAP messageType NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(?!(phone_test|mail_test)<).*" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(?!(EMAIL|SMS|VOICE)<).*" + "/messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(sender_test|phone_test)" + "</sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
		
		/** SOAP sender NOK */
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(soapConfig.getLogin() + ":" + soapConfig.getPassword())),
				Condition.withHeader("Content-Type", "text/xml; charset=utf-8"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + soapConfig.getApplication() + "</application>" + 
											"<destination>" + "(?!(phone_test|mail_test)<).*" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(EMAIL|SMS|VOICE)" + "/messageType>" + 
											"<organization>" + soapConfig.getOrganization() + "</organization>" + 
											"<sender>" + "(?!sender_test|phone_test<).*" + "/sender>" + 
										"</name>" + 
									"</ser:messages>" + 
								"</SOAP-ENV:Body>" + 
							"</SOAP-ENV:Envelope>")))
		.then(	Action.status(org.glassfish.grizzly.http.util.HttpStatus.BAD_REQUEST_400),
				Action.contentType("text/xml"),
				Action.stringContent(
						"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
							"<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"/>" +
							"<soap:Body>" +
								"<ns2:messagesResponse xmlns:ns2=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
									"<return>" +
										"<providerName>Message Service</providerName>" +
										"<status>SEND_UNSUCCESSFUL_BAD_REQUEST</status>" +
									"</return>" +
								"</ns2:messagesResponse>" +
							"</soap:Body>" +
						"</soap:Envelope>"));
	}
	
	
	@After
	public void stop() {
		LDAPServer.shutDown(true);
		SOAPServer.stop();
	}
	
	/*
	 *  Get status of existing hidden token
	 *  expected OK 200
	 */
	@Test
	public void should_200_status_token_1() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(),6);
		
		ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
	
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(), tok.getChannel().toString());
		assertEquals(resp2.getBody().getSchema(), tok.getSchema());
		assertEquals(resp2.getBody().getMail(), tok.getMail());
		assertEquals(resp2.getBody().getPhone(), tok.getPhone());
		assertNull(resp2.getBody().getCode());
	}
	
	/*
	 *  Get status of existing locked hidden token
	 *  expected OK 200
	 */
	@Test
	public void should_200_status_token_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(),6);
			
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");
		
		ResponseEntity<Status> resp2;
		try {
			for(int i = 0 ; i < SchemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { } 
		
		ResponseEntity<TokenObject> resp3 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
	
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody());
		assertEquals(resp3.getBody().getUserIdentifier(), tok.getUserIdentifier());
		assertEquals(resp3.getBody().getChannel(), tok.getChannel().toString());
		assertEquals(resp3.getBody().getSchema(), tok.getSchema());
		assertEquals(resp3.getBody().getMail(), tok.getMail());
		assertEquals(resp3.getBody().getPhone(), tok.getPhone());
		assertNull(resp3.getBody().getCode());
	}
	
	/*
	 *  Get status of existing expired hidden token
	 *  expected OK 200
	 */
	@Test
	public void should_200_status_token_3() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(),6);
			
		try { Thread.sleep(SchemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxValidityTime()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
	
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(), tok.getChannel().toString());
		assertEquals(resp2.getBody().getSchema(), tok.getSchema());
		assertEquals(resp2.getBody().getMail(), tok.getMail());
		assertEquals(resp2.getBody().getPhone(), tok.getPhone());
		assertNull(resp2.getBody().getCode());
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
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok1, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(),6);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok2, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok3, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok4, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok1.getUserIdentifier() + "&channel=none&schema=" + tok1.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok1.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(), "none");
		assertEquals(resp2.getBody().getSchema(), tok1.getSchema());
		assertNull(resp2.getBody().getMail());
		assertNull(resp2.getBody().getPhone());
		assertNull(resp2.getBody().getCode());
		
		resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok2.getUserIdentifier() + "&channel=mail&schema=" + tok2.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok2.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(), "mail");
		assertEquals(resp2.getBody().getSchema(), tok2.getSchema());
		assertEquals(resp2.getBody().getMail(), "mail_test");
		assertNull(resp2.getBody().getPhone());
		assertNull(resp2.getBody().getCode());
		
		resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok3.getUserIdentifier() + "&channel=sms&schema=" + tok3.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok3.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(),"sms");
		assertEquals(resp2.getBody().getSchema(), tok3.getSchema());
		assertNull(resp2.getBody().getMail());
		assertEquals(resp2.getBody().getPhone(), "phone_test");
		assertNull(resp2.getBody().getCode());
		
		resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok4.getUserIdentifier() + "&channel=voice&schema=" + tok4.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok4.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(), "voice");
		assertEquals(resp2.getBody().getSchema(), tok4.getSchema());
		assertNull(resp2.getBody().getMail());
		assertEquals(resp2.getBody().getPhone(), "phone_test");
		assertNull(resp2.getBody().getCode());
	}
	
	/*
	 *  Get status of existing token without one of the three required parameters
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_status_token() {
		
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
		
		try{
			ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try{
			ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try{
			ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Get status of existing token with wrong userIdentifier parameter
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_status_token_1() {
		
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
		
		try{
			ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + "fake_uid" + "&channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
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
		
		try{
			ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + "fake_channel" + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
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
		
		try{
			ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel() + "&schema=" + "fake_schema", HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
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
	
		try{
			ResponseEntity<TokenObject> resp = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=" + tok.getChannel() + "&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}

}
