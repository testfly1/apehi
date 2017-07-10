package com.axa.api.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.catalina.connector.Response;
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

import com.axa.api.configuration.yml.LdapConfig;
import com.axa.api.configuration.yml.OpenAMConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.configuration.yml.SoapConfig;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.model.enumeration.ChannelEnum;
import com.axa.api.model.input.TokenInput;
import com.axa.api.model.input.TokenValidation;
import com.axa.api.model.input.UserInput;
import com.axa.api.model.response.api.NoBodyResponse;
import com.axa.api.model.response.api.Status;
import com.axa.api.model.response.api.Token;
import com.axa.api.model.response.api.TokenObject;
import com.axa.api.model.response.api.User;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;
import com.unboundid.util.Base64;
import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.semantics.Function;
import com.xebialabs.restito.server.StubServer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class LdapTests {
	
	@Autowired
	OpenAMConfig openAMConfig;
	
	@Autowired
	private LdapConfig lc;
	
	@Autowired
	private SoapConfig sc;
	
	@Autowired
	private SchemaListConfig slc;
	
	@Autowired
	private RestTemplate restTemplate;
    
	private InMemoryDirectoryServer server;
	
	private StubServer OAMServer;
	private StubServer SOAPServer;
	
	private Entry getBaseDomain() throws LDIFException {
		return new Entry("dn: dc=example,dc=com",
        		"objectClass: top",
        		"objectClass: domain",
        		"dc: example");
	}
	
	private Entry getTokensOu() throws LDIFException {
		return new Entry("dn: ou=tokens,dc=example,dc=com",
        		"objectClass: organizationalUnit",
        		"objectClass: top",
        		"ou: tokens",
        		"description: Tokens OU");
	}
	
	public void start(){
		InMemoryDirectoryServerConfig config;

		try {
	        config = new InMemoryDirectoryServerConfig(lc.getBaseDN());
	        config.addAdditionalBindCredentials(lc.getUserName(), lc.getPassword());
	        config.setSchema(null);
	        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", Integer.valueOf(lc.getPort())));
	        
	        server = new InMemoryDirectoryServer(config);
	        server.add(getBaseDomain());
	        server.add(getTokensOu());

	        server.startListening();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
	
	public void startStubOpenAM(){
		
		OAMServer = new StubServer(new Integer(openAMConfig.getPort())).run();
		
		/*
		 * AuthenticateUser
		 */
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "login_test_1"),
				Condition.withHeader("X-OpenAM-Password", "Password1"))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"message\": \"Authentication Successful\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "login_test_2"),
				Condition.withHeader("X-OpenAM-Password", "Password1"))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"message\": \"Authentication Successful\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "login_test_3"),
				Condition.withHeader("X-OpenAM-Password", "Password1"))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"message\": \"Authentication Successful\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "fake_uid"),
				Condition.withHeader("X-OpenAM-Password", "Password1"))		
		.then(	Action.unauthorized(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"code\": 401,\n \"reason\": \"Unauthorized\",\n \"message\": \"Authentication Failed\"\n}"));
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "module"),
				Condition.parameter("authIndexValue", "scope_test_1"),
				Condition.parameter("noSession", "true"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", "login_test_1"),
				Condition.withHeader("X-OpenAM-Password", "fake_password"))		
		.then(	Action.unauthorized(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"code\": 401,\n \"reason\": \"Unauthorized\",\n \"message\": \"Authentication Failed\"\n}"));
		
		/*
		 * GetAdminToken
		 */
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "service"),
				Condition.parameter("authIndexValue", openAMConfig.getAdminServiceChain()),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", openAMConfig.getAdminUser()),
				Condition.withHeader("X-OpenAM-Password", openAMConfig.getAdminPwd()))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"tokenId\": \"token_test\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		
		/*
		 * RetrieveUserInformations
		 * Scope: scope_test_1
		 */
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_1\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_2\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_3\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    },\n    {\n      \"username\": \"uid_test_2\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 2,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_4\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        null\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        null\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"fake_uid\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		/*
		 * RetrieveUserInformations
		 * Scope: scope_test_2
		 */
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_1\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_2\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_3\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    },\n    {\n      \"username\": \"uid_test_2\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 2,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_4\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        null\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        null\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"fake_uid\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		/*
		 * Session expired
		 */
		
		StubHttp.whenHttp(OAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/sessions"),
				Condition.parameter("_action", "getTimeLeft"),
				Condition.parameter("tokenId", "token_test"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"maxtime\": 7500\n}"));
	}
	
	public void startStubSOAP() throws SAXException, IOException, ParserConfigurationException{
		
		SOAPServer = new StubServer(new Integer(8443)).run();
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(sc.getLogin() + ":" + sc.getPassword())),
				Condition.withHeader("Content-Type", "text/xml"),
				Condition.withHeader("Cache-Control", "no-cache"),
				Condition.withHeader("Pragma", "no-cache"),
				Condition.withHeader("SOAPAction", ""),
				Condition.withPostBody(),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + sc.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "</destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(MAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + sc.getOrganization() + "</organization>" + 
											"<sender>" + "sender_test" + "</sender>" + 
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
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(sc.getLogin() + ":" + sc.getPassword())),
				Condition.withHeader("Content-Type", "text/xml"),
				Condition.withHeader("Cache-Control", "no-cache"),
				Condition.withHeader("Pragma", "no-cache"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"<additionalAttributes" + "(?!(/|>OTP=[0-9]{6}</additionalAttributes|>subject=AXA OTP Generation</additionalAttributes)).*" + ">" + 
											"<application>" + sc.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "</destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(MAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + sc.getOrganization() + "</organization>" + 
											"<sender>" + "sender_test" + "</sender>" + 
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
	
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(sc.getLogin() + ":" + sc.getPassword())),
				Condition.withHeader("Content-Type", "text/xml"),
				Condition.withHeader("Cache-Control", "no-cache"),
				Condition.withHeader("Pragma", "no-cache"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
										"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)"  +
											"<application>" + sc.getApplication() + "</application>" + 
											"<destination>" + "(?!(phone_test|mail_test)<).*" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(MAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + sc.getOrganization() + "</organization>" + 
											"<sender>" + "sender_test" + "</sender>" + 
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
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(sc.getLogin() + ":" + sc.getPassword())),
				Condition.withHeader("Content-Type", "text/xml"),
				Condition.withHeader("Cache-Control", "no-cache"),
				Condition.withHeader("Pragma", "no-cache"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
										"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + sc.getApplication() + "</application>" + 
											"<destination>" + "(phone_test|mail_test)" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(?!(Use the code [0-9]{6} for verification|OTP)<).*" + "/message>" + 
											"<messageType>" + "(MAIL|SMS|VOICE)" + "</messageType>" + 
											"<organization>" + sc.getOrganization() + "</organization>" + 
											"<sender>" + "sender_test" + "</sender>" + 
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
		
		StubHttp.whenHttp(SOAPServer)
		.match(	Condition.post("/ATS_MessageService"),
				Condition.withHeader("Authorization", "Basic " + Base64.encode(sc.getLogin() + ":" + sc.getPassword())),
				Condition.withHeader("Content-Type", "text/xml"),
				Condition.withHeader("Cache-Control", "no-cache"),
				Condition.withHeader("Pragma", "no-cache"),
				Condition.withPostBodyContaining( Pattern.compile(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.soap.ws.messageservice.iam.axa.com/\">" +
								"<SOAP-ENV:Header/>" + 
								"<SOAP-ENV:Body>" + 
									"<ser:messages>" + 
										"<name>" + 
											"(<additionalAttributes/>|<additionalAttributes>OTP=[0-9]{6}</additionalAttributes>|<additionalAttributes>subject=AXA OTP Generation</additionalAttributes>)" + 
											"<application>" + sc.getApplication() + "</application>" + 
											"<destination>" + "(?!(phone_test|mail_test)<).*" + "/destination>" +
											"<language>" + "[a-z]{2}" + "</language>" + 
											"<message>" + "(Use the code [0-9]{6} for verification|OTP)" + "</message>" + 
											"<messageType>" + "(?!(MAIL|SMS|VOICE)<).*" + "/messageType>" + 
											"<organization>" + sc.getOrganization() + "</organization>" + 
											"<sender>" + "sender_test" + "</sender>" + 
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
	
	
	public void stop() {
	    server.shutDown(true);
	    OAMServer.stop();
	    //SOAPServer.stop();
	}
	
	@Before
	public void before() throws SAXException, IOException, ParserConfigurationException {
		start();
		startStubOpenAM();
		//startStubSOAP();
	}
	
	@After
	public void after() {
		stop();
	}	
	
	/*
	 *  Authenticate existing user
	 *  expected OK 200 
	 */
	@Test
	public void should_200_authenticate_token() {
		
		UserInput userInput = new UserInput();
	    userInput.setLogin("login_test_1");
	    userInput.setPassword("Password1");
	    userInput.setSchema("schema_test_1");
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");    
	
	    HttpEntity<UserInput> he = new HttpEntity<>(userInput, headers);
	   
	    ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, he, User.class);
	    
	    assertEquals(resp.getStatusCode(),HttpStatus.OK);
	    assertNotNull(resp.getBody());
	    assertEquals(resp.getBody().getUid(), "uid_test");
	    assertEquals(resp.getBody().getLogin(), "login_test_1");
	    assertEquals(resp.getBody().getMail(), "mail_test");
	    assertEquals(resp.getBody().getGivenName(), "givenName_test");
	    assertEquals(resp.getBody().getPhoneNumber(), "phone_test");
	    assertEquals(resp.getBody().getSn(), "sn_test");
	}
	
	/*
	 *  Authenticate existing user missing one of the three required inputs
	 *  expected BAD_REQUEST 400 
	 */
	@Test
	public void should_400_authenticate_token_1() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    UserInput userInput = new UserInput();
	    userInput.setPassword("Password1");
	    userInput.setSchema("schema_test_1");
	    
	    UserInput userInput2 = new UserInput();
	    userInput2.setLogin("login_test_1");
	    userInput2.setSchema("schema_test_1");
	    
	    UserInput userInput3 = new UserInput();
	    userInput3.setLogin("login_test_1");
	    userInput3.setPassword("Password1");
	      
	    
		try {
			ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput, headers), User.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput2, headers), User.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput3, headers), User.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Authenticate existing user with wrong schema
	 *  expected BAD_REQUEST 400 
	 */
	@Test
	public void should_400_authenticate_token_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    UserInput userInput = new UserInput();
	    userInput.setLogin("login_test_1");
	    userInput.setPassword("Password1");
	    userInput.setSchema("fake_schema");
	      
		try {
			ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput, headers), User.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/*
	 *  Authenticate existing user with wrong login
	 *  expected UNAUTHORIZED 401
	 */
	@Test
	public void should_401_authenticate_token_1() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    UserInput userInput = new UserInput();
	    userInput.setLogin("fake_uid");
	    userInput.setPassword("Password1");
	    userInput.setSchema("schema_test_1");
	    
		try {
			ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput, headers), User.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
		}
	}

	/*
	 *  Authenticate existing user with wrong password
	 *  expected UNAUTHORIZED 401 
	 */
	@Test
	public void should_401_authenticate_token_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    UserInput userInput = new UserInput();
	    userInput.setLogin("login_test_1");
	    userInput.setPassword("fake_password");
	    userInput.setSchema("schema_test_1");
	    
		try {
			ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput, headers), User.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
		}
	}

	/*
	 *  Authenticate existing user but no user result
	 *  expected INTERNAL_SERVER_ERROR 500
	 */
	@Test
	public void should_500_authenticate_token_1() {
		
		UserInput userInput = new UserInput();
	    userInput.setLogin("login_test_2");
	    userInput.setPassword("Password1");
	    userInput.setSchema("schema_test_1");
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");    
	
	    HttpEntity<UserInput> he = new HttpEntity<>(userInput, headers);
	   
	    try {
	    	ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, he, User.class);
	    } catch (HttpServerErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/*
	 *  Authenticate existing user but multiple user results
	 *  expected INTERNAL_SERVER_ERROR 500
	 */
	@Test
	public void should_500_authenticate_token_2() {
		
		UserInput userInput = new UserInput();
	    userInput.setLogin("login_test_3");
	    userInput.setPassword("Password1");
	    userInput.setSchema("schema_test_1");
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");    
	
	    HttpEntity<UserInput> he = new HttpEntity<>(userInput, headers);
	   
	    try {
	    	ResponseEntity<User> resp = restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, he, User.class);
	    } catch (HttpServerErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/*
	 *  Generate token for non-existing user
	 *  expected OK 200 
	 */
	@Test
	public void should_200_generate_token_unregistered_1() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    tok.setUserIdentifier("uid_test");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
	}
	
	/*
	 *  Generate token for non-existing user and overwrite the existing one
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_unregistered_2() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    tok.setUserIdentifier("uid_test");
	    tok.setMail("mail_test");
		
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		String code1 = resp.getBody().getToken();
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertNotEquals(resp.getBody().getToken(), code1);
	}
	
	/*
	 *  Generate 4 different tokens for non-existing user
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_unregistered_3() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    tok.setUserIdentifier("uid_test");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
	
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setChannel(ChannelEnum.mail);
		tok.setMail("mail_test");
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setMail(null);
		tok.setPhone("test_phone");
		tok.setChannel(ChannelEnum.sms);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
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
	}
	
	/*
	 *  Generate token for existing user
	 *  expected OK 200 
	 */
	@Test
	public void should_200_generate_token_registered_1() {
			
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		   
		TokenInput tok = new TokenInput();
		tok.setChannel(ChannelEnum.none);
		tok.setSchema("schema_test_1");
		tok.setUserIdentifier("login_test_1");
		    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
	}
		
	/*
	 *  Generate token for existing user and overwrite the existing one
	 *  expected OK 200
	*/
	@Test
	public void should_200_generate_token_registered_2() {
			
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		    
		TokenInput tok = new TokenInput();
		tok.setChannel(ChannelEnum.none);
		tok.setSchema("schema_test_1");
		tok.setUserIdentifier("login_test_1");

			
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		String code1 = resp.getBody().getToken();
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
			
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());		
		assertNotEquals(resp.getBody().getToken(), code1);
	}
	
	/*
	 *  Generate 4 different tokens for existing user
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_3() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("login_test_1");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_1");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setChannel(ChannelEnum.mail);
		tok.setMail("mail_test");
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setMail(null);
		tok.setPhone("test_phone");
		tok.setChannel(ChannelEnum.sms);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setChannel(ChannelEnum.voice);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		String code4 = resp.getBody().getToken();
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
	}
	
	/*
	 *  Generate 4 different tokens for existing user without channel informations inputs
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_4() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("login_test_1");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_1");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setChannel(ChannelEnum.mail);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setChannel(ChannelEnum.sms);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
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
	}
	
	/*
	 *  Generate 4 different tokens for existing user with channel information inputs
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_5() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("login_test_4");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_1");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setChannel(ChannelEnum.mail);
		tok.setMail("mail_test");
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setMail(null);
		tok.setPhone("test_phone");
		tok.setChannel(ChannelEnum.sms);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
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
	}
	
	/*
	 *  Generate 8 different tokens for existing user without channel informations inputs on two scopes
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_6() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("login_test_1");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_1");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		String code1 = resp.getBody().getToken();
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setChannel(ChannelEnum.mail);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setChannel(ChannelEnum.sms);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
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
		
		tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_2");
	    
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		String code2 = resp.getBody().getToken();
		assertNotEquals(code1, code2);
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setChannel(ChannelEnum.mail);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setChannel(ChannelEnum.sms);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
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
	}
	
	/*
	 *  Generate 8 different tokens for existing user without channel informations inputs
	 *  expected OK 200
	 */
	@Test
	public void should_200_generate_token_registered_7() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("login_test_4");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_1");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		String code1 = resp.getBody().getToken();
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setMail("mail_test");
		tok.setChannel(ChannelEnum.mail);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setMail(null);
		tok.setPhone("phone_test");
		tok.setChannel(ChannelEnum.sms);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
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
		tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test_2");
	    
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		String code2 = resp.getBody().getToken();
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertNotEquals(code1, code2);
		assertEquals(resp.getBody().getToken().length(), 6);
		
		tok.setMail("mail_test");
		tok.setChannel(ChannelEnum.mail);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setMail(null);
		tok.setPhone("phone_test");
		tok.setChannel(ChannelEnum.sms);
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		
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
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
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
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>("{\"userIdentifier\": \"uid_test\", \"channel\" : \"123456\", \"schema\" : \"schema_test-noscope\"}", headers), Token.class);
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
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok2, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok3, headers), Token.class);
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
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
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
	    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.mail);
	    tok.setSchema("schema_test-noscope");
	    tok.setUserIdentifier("uid_test");

	    try {
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    tok.setChannel(ChannelEnum.sms);
	    
	    try {
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    tok.setChannel(ChannelEnum.voice);
	    
	    try {
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
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
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
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
		    
	    TokenInput tok = new TokenInput();
	    tok.setChannel(ChannelEnum.mail);
	    tok.setSchema("schema_test_1");
	    tok.setUserIdentifier("login_test_4");

	    try {
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    tok.setChannel(ChannelEnum.sms);
	    
	    try {
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    tok.setChannel(ChannelEnum.voice);
	    
	    try {
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
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
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");

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
		
		ResponseEntity<Status> resp2;
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
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
			
		ResponseEntity<Status> resp2;
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxSuccessfulAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
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
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
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
			ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
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
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
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
			
		try { Thread.sleep(slc.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxValidityTime()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
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
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setChannel(ChannelEnum.none);
	    tok.setSchema("schema_test-noscope");
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(),6);
		
		tok.setMail("mail_test");
		tok.setChannel(ChannelEnum.mail);
	    
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		tok.setMail(null);
		tok.setPhone("phone_test");
		tok.setChannel(ChannelEnum.sms);
	    
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
			
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
		
		ResponseEntity<TokenObject> resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=none&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(), "none");
		assertEquals(resp2.getBody().getSchema(), tok.getSchema());
		assertNull(resp2.getBody().getMail());
		assertNull(resp2.getBody().getPhone());
		assertNull(resp2.getBody().getCode());
		
		resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=mail&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(), "mail");
		assertEquals(resp2.getBody().getSchema(), tok.getSchema());
		assertEquals(resp2.getBody().getMail(), "mail_test");
		assertNull(resp2.getBody().getPhone());
		assertNull(resp2.getBody().getCode());
		
		resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=sms&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(),"sms");
		assertEquals(resp2.getBody().getSchema(), tok.getSchema());
		assertNull(resp2.getBody().getMail());
		assertEquals(resp2.getBody().getPhone(), "phone_test");
		assertNull(resp2.getBody().getCode());
		
		resp2 = restTemplate.exchange("http://localhost:8080/tokens?userIdentifier=" + tok.getUserIdentifier() + "&channel=voice&schema=" + tok.getSchema(), HttpMethod.GET, new HttpEntity<>(headers), TokenObject.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody());
		assertEquals(resp2.getBody().getUserIdentifier(), tok.getUserIdentifier());
		assertEquals(resp2.getBody().getChannel(), "voice");
		assertEquals(resp2.getBody().getSchema(), tok.getSchema());
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
		
		ResponseEntity<Status> resp2;	
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
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
		
		try { Thread.sleep(slc.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxValidityTime()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
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
		
		ResponseEntity<NoBodyResponse> resp2;
		
		try {
			resp2 = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok2,headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			resp2 = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok3,headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			resp2 = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok4,headers), NoBodyResponse.class);
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
		
		try{
			ResponseEntity<NoBodyResponse> resp2 = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok,headers), NoBodyResponse.class);
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
		
		try{
			ResponseEntity<NoBodyResponse> resp2 = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok,headers), NoBodyResponse.class);
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
		
		try{
			ResponseEntity<NoBodyResponse> resp2 = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok,headers), NoBodyResponse.class);
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
	 
		try{
			ResponseEntity<NoBodyResponse> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.DELETE, new HttpEntity<>(tok, headers), NoBodyResponse.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
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
			
		ResponseEntity<Status> resp2;
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
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
			
		ResponseEntity<Status> resp2;
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
	
		try { Thread.sleep(slc.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxValidityTime()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		try {
			ResponseEntity<Status> resp3 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
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
		
		ResponseEntity<Status> resp2;	
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
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
		
	    ResponseEntity<Status> resp3;
	    
	    try {
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok2, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    try {
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok3, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
	    
	    try {
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok4, headers), Status.class);
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
			
		ResponseEntity<Status> resp2;	
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		tok.setUserIdentifier("fake_uid");

		try {
			ResponseEntity<Status> resp3 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
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
			
		ResponseEntity<Status> resp2;	
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		tok.setChannel(ChannelEnum.mail);
				
		try {
			ResponseEntity<Status> resp3 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
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
			
		ResponseEntity<Status> resp2;	
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		tok.setSchema("fake_schema");
		
		try {
			ResponseEntity<Status> resp3 = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, new HttpEntity<>(tok, headers), Status.class);
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
			ResponseEntity<Status> resp = restTemplate.exchange("http://localhost:8080/tokens/unlock", HttpMethod.POST, he, Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}
	}

	/*
	 *  Validate existing token
	 *  expected OK 200
	 */
	@Test
	public void should_200_validate_token_1() {
		
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
		tv.setCode(resp.getBody().getToken());
		
			
		ResponseEntity<Status> resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
		
		assertEquals(resp2.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp2.getBody().getCode());
		assertEquals(resp2.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp2.getBody().getMessage());
		assertEquals(resp2.getBody().getMessage(),"Valid");
	}
	
	/*
	 *  Validate existing should-not-be-locked token
	 *  expected OK 200
	 */
	@Test
	public void should_200_validate_token_2() {
		
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
		tv.setSchema(tok.getSchema());
		if(resp.getBody().getToken().equals("000000")) tv.setCode("000001");
		else tv.setCode("000000");
		
		String code = resp.getBody().getToken();
		
		ResponseEntity<Status> resp2;
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt(); i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv,headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		try { Thread.sleep(slc.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getLockDuration()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		tv.setCode(code);
		
		ResponseEntity<Status> resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");
	}
	
	/*
	 *  Validate 4 existing should-not-be-locked token with different lockDuration & maxValidityTime
	 *  expected 4 OK 200, 4 FORBIDDEN 403 & 4 UNAUTHORIZED 401
	 */
	@Test
	public void tests_validate_token() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok1 = new TokenInput();
	    tok1.setUserIdentifier("uid_test");
	    tok1.setChannel(ChannelEnum.none);
	    tok1.setSchema("schema_test-noscope");
	    
	    TokenInput tok2 = new TokenInput();
	    tok2.setUserIdentifier("uid_test");
	    tok2.setChannel(ChannelEnum.mail);
	    tok2.setSchema("schema_test-noscope");
	    tok2.setMail("mail_test");
	    
	    TokenInput tok3 = new TokenInput();
	    tok3.setUserIdentifier("uid_test");
	    tok3.setChannel(ChannelEnum.sms);
	    tok3.setSchema("schema_test-noscope");
	    tok3.setPhone("phone_test");
	    
	    TokenInput tok4 = new TokenInput();
	    tok4.setUserIdentifier("uid_test");
	    tok4.setChannel(ChannelEnum.voice);
	    tok4.setSchema("schema_test-noscope");
	    tok4.setPhone("phone_test");
	    
	    ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok1, headers), Token.class);

		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
	    
		String code1 = resp.getBody().getToken();
		
	    TokenValidation tv1 = new TokenValidation();
		tv1.setUserIdentifier(tok1.getUserIdentifier());
		tv1.setChannel(tok1.getChannel());
		tv1.setSchema(tok1.getSchema());
		if(resp.getBody().getToken().equals("000000")) tv1.setCode("000001");
		else tv1.setCode("000000");
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok2, headers), Token.class);

		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		TokenValidation tv2 = new TokenValidation();
		tv2.setUserIdentifier(tok2.getUserIdentifier());
		tv2.setChannel(tok2.getChannel());
		tv2.setSchema(tok2.getSchema());
		tv2.setCode("000000");
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok3, headers), Token.class);

		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		TokenValidation tv3 = new TokenValidation();
		tv3.setUserIdentifier(tok3.getUserIdentifier());
		tv3.setChannel(tok3.getChannel());
		tv3.setSchema(tok3.getSchema());
		tv3.setCode("000000");
		
		resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok4, headers), Token.class);

		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getCode(), HttpStatus.OK.value());
		assertEquals(resp.getBody().getMessage(), "OTP generated & sent through desired channel");
		
		TokenValidation tv4 = new TokenValidation();
		tv4.setUserIdentifier(tok4.getUserIdentifier());
		tv4.setChannel(tok4.getChannel());
		tv4.setSchema(tok4.getSchema());
		tv4.setCode("000000");
		
		ResponseEntity<Status> resp2;
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv1.getSchema()).getChannelConfig(tv1.getChannel().toString()).getMaxFailedAttempt(); i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv1,headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv2.getSchema()).getChannelConfig(tv2.getChannel().toString()).getMaxFailedAttempt(); i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv2,headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv3.getSchema()).getChannelConfig(tv3.getChannel().toString()).getMaxFailedAttempt(); i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv3,headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv4.getSchema()).getChannelConfig(tv4.getChannel().toString()).getMaxFailedAttempt(); i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv4,headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
		
		
		ResponseEntity<Status> resp3;
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv1,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv2,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv3,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv4,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		
		try { Thread.sleep(slc.getSchemaItemConfig(tok4.getSchema()).getChannelConfig(tok4.getChannel().toString()).getLockDuration()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		tv1.setCode(code1);
		/*tv2.setCode(code2);
		tv3.setCode(code3);
		tv4.setCode(code4);
		
		resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv4, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");*/
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv1,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv2,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv3,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		
		try { Thread.sleep(slc.getSchemaItemConfig(tok3.getSchema()).getChannelConfig(tok3.getChannel().toString()).getLockDuration()*1000-slc.getSchemaItemConfig(tok4.getSchema()).getChannelConfig(tok4.getChannel().toString()).getLockDuration()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		/*resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv3, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");
		
		resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv4, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");*/
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv1,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv2,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		
		try { Thread.sleep(slc.getSchemaItemConfig(tok2.getSchema()).getChannelConfig(tok2.getChannel().toString()).getLockDuration()*1000-slc.getSchemaItemConfig(tok3.getSchema()).getChannelConfig(tok3.getChannel().toString()).getLockDuration()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		/*resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv2, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");
		
		resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv3, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");
		
		resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv4, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");*/
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv1,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.FORBIDDEN);
		}
		
		try { Thread.sleep(slc.getSchemaItemConfig(tok1.getSchema()).getChannelConfig(tok1.getChannel().toString()).getLockDuration()*1000-slc.getSchemaItemConfig(tok2.getSchema()).getChannelConfig(tok2.getChannel().toString()).getLockDuration()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv1, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");
		
		/*resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv2, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");
		
		resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv3, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");
		
		resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv4, headers), Status.class);
		
		assertEquals(resp3.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp3.getBody().getCode());
		assertEquals(resp3.getBody().getCode(), HttpStatus.OK.value());
		assertNotNull(resp3.getBody().getMessage());
		assertEquals(resp3.getBody().getMessage(),"Valid");*/
		
		try { Thread.sleep(slc.getSchemaItemConfig(tok1.getSchema()).getChannelConfig(tok1.getChannel().toString()).getMaxValidityTime()*1000-slc.getSchemaItemConfig(tok1.getSchema()).getChannelConfig(tok1.getChannel().toString()).getLockDuration()*1000); } catch (InterruptedException | InvalidSchemaException e) { }
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv1,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
		}
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv2,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
		}
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv3,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
		}
		
		try{
			resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv4,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
		}
	}
	
	/*
	 *  Validate existing token missing one of the four required inputs
	 *  expected BAD_REQUEST 400
	 */
	@Test
	public void should_400_validate_token() {
		
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
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		tv.setCode(resp.getBody().getToken());
			
		TokenValidation tv2 = new TokenValidation();
		tv2.setUserIdentifier(tok.getUserIdentifier());
		tv2.setSchema(tok.getSchema());
		tv2.setCode(resp.getBody().getToken());
		
		TokenValidation tv3 = new TokenValidation();
		tv3.setUserIdentifier(tok.getUserIdentifier());
		tv3.setChannel(tok.getChannel());
		tv3.setCode(resp.getBody().getToken());
		
		TokenValidation tv4 = new TokenValidation();
		tv4.setUserIdentifier(tok.getUserIdentifier());
		tv4.setChannel(tok.getChannel());
		tv4.setSchema(tok.getSchema());
		
		ResponseEntity<Status> resp2;
			
		try{
			resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try{
			resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv2, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try{
			resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv3, headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try{
			resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv4, headers), Status.class);
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

		if(resp.getBody().getToken().equals("000000")){
			tv.setCode("000001");
		} else tv.setCode("000000");
						
		try{
			ResponseEntity<Status> resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv,headers), Status.class);
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
		
		ResponseEntity<Status> resp2;
		try {
			for(int i = 0 ; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt(); i++) {
				try{
					resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv,headers), Status.class);
				} catch(HttpClientErrorException ex){
					assertEquals(ex.getStatusCode(), HttpStatus.UNAUTHORIZED);
				}
			}
		} catch (RestClientException | InvalidSchemaException e) { }
			
		tv.setCode(resp.getBody().getToken());
		
		try{
			ResponseEntity<Status> resp3 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv,headers), Status.class);
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
		tv.setUserIdentifier("fake_uid");
		tv.setChannel(tok.getChannel());
		tv.setSchema(tok.getSchema());
		tv.setCode(resp.getBody().getToken());
			
		try{
			ResponseEntity<Status> resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}	
	}
	
	/*
	 *  Validate existing token with wrong channel
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_validate_token_2() {
		
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
		tv.setChannel(ChannelEnum.mail);
		tv.setSchema(tok.getSchema());
		tv.setCode(resp.getBody().getToken());
			
		try{
			ResponseEntity<Status> resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv,headers), Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}	
	}
	
	/*
	 *  Validate existing token with wrong schema
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
	    
	    ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);

		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier(tok.getUserIdentifier());
		tv.setChannel(tok.getChannel());
		tv.setSchema("fake_schema");
		tv.setCode(resp.getBody().getToken());
			
		HttpEntity<TokenValidation> he2 = new HttpEntity<>(tv,headers);
			
		try{
			ResponseEntity<Status> resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, he2, Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}	
	}
	
	/*
	 *  Validate existing token maxSuccessfulTime + 1
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_validate_token_4() {
		
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
		tv.setCode(resp.getBody().getToken());
			
		HttpEntity<TokenValidation> he2 = new HttpEntity<>(tv,headers);
		
		ResponseEntity<Status> resp2;
		
		try {
			for(int i = 0; i < slc.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxSuccessfulAttempt(); i++)
				resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, he2, Status.class);
		} catch (RestClientException | InvalidSchemaException e) { }
			
		try{
			resp2 = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, he2, Status.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
		}	
	}
	
	/*
	 *  Validate non-existing token
	 *  expected NOT_FOUND 404
	 */
	@Test
	public void should_404_validate_token_5() {
		
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	   
		TokenValidation tv = new TokenValidation();
		tv.setUserIdentifier("uid_test");
		tv.setChannel(ChannelEnum.none);
		tv.setSchema("schema_test-noscope");
		tv.setCode("000000");
			
		try{
			ResponseEntity<Status> resp = restTemplate.exchange("http://localhost:8080/tokens/validate", HttpMethod.POST, new HttpEntity<>(tv,headers), Status.class);
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
		
		ResponseEntity<Token> resp;
		HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    
	    TokenInput tok = new TokenInput();
	    tok.setUserIdentifier("uid_test");
	    tok.setSchema("schema_test-noscope");

	    tok.setPhone("phone_fake");
	    tok.setChannel(ChannelEnum.sms);
	    
	    try{
	    	resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
	    } catch (HttpServerErrorException ex) {
	    	assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
		
		tok.setChannel(ChannelEnum.voice);
		try{
	    	resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
	    } catch (HttpServerErrorException ex) {
	    	assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
		
		tok.setPhone(null);
		tok.setMail("mail_fake");
		tok.setChannel(ChannelEnum.mail);
		
		try{
	    	resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok, headers), Token.class);
	    } catch (HttpServerErrorException ex) {
	    	assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}