package com.axa.api.test.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import com.axa.api.configuration.yml.LdapConfig;
import com.axa.api.configuration.yml.OpenAMConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.configuration.yml.SoapConfig;
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
import com.unboundid.util.Base64;
import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class Generate {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	SchemaListConfig schemaListConfig;
	
	@Autowired
	LdapConfig ldapConfig;
	
	@Autowired
	OpenAMConfig openAMConfig;
	
	@Autowired
	SoapConfig soapConfig;
	
	private InMemoryDirectoryServer LDAPServer;
	private StubServer OpenAMServer;
	private StubServer SOAPServer;
	
	@Before
	public void start(){
		startStubLDAP();
		startStubOpenAM();
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
	
	private void startStubOpenAM(){
		
		OpenAMServer = new StubServer(new Integer(openAMConfig.getPort())).run();
		
		/** AuthenticateUser */
		
		StubHttp.whenHttp(OpenAMServer)
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
		
		StubHttp.whenHttp(OpenAMServer)
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
		
		StubHttp.whenHttp(OpenAMServer)
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
		
		StubHttp.whenHttp(OpenAMServer)
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
		
		StubHttp.whenHttp(OpenAMServer)
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
		
		/** GetAdminToken */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/authenticate"),
				Condition.parameter("authIndexType", "service"),
				Condition.parameter("authIndexValue", openAMConfig.getAdminServiceChain()),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader("X-OpenAM-Username", openAMConfig.getAdminUser()),
				Condition.withHeader("X-OpenAM-Password", openAMConfig.getAdminPwd()))		
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"tokenId\": \"token_test\",\n \"successUrl\": \"/openam/console\"\n}"));
		
		
		/**
		 * RetrieveUserInformations
		 * Scope: scope_test_1
		 */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_1\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_2\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_3\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    },\n    {\n      \"username\": \"uid_test_2\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 2,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_4\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        null\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        null\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"fake_uid\" and scope eq \"scope_test_1\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		/**
		 * RetrieveUserInformations
		 * Scope: scope_test_2
		 */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_1\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_2\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_3\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    },\n    {\n      \"username\": \"uid_test_2\",\n      \"mail\": [\n        \"mail_test\"\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        \"phone_test\"\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_3\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 2,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));

		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"login_test_4\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [\n    {\n      \"username\": \"uid_test\",\n      \"mail\": [\n        null\n      ],\n      \"givenName\": [\n        \"givenName_test\"\n      ],\n      \"cn\": [\n        \"uid_test\"\n      ],\n      \"b2CSRmobile\": [\n        null\n       ],\n      \"b2CSRlogin\": [\n        \"login_test_1\"\n      ],\n      \"sn\": [\n        \"sn_test\"\n      ]\n    }\n  ],\n  \"resultCount\": 1,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.get("/" + openAMConfig.getDeploymentPath() + "/json/users"),
				Condition.parameter("_queryFilter", "b2CSRlogin eq \"fake_uid\" and scope eq \"scope_test_2\""),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n  \"result\": [],\n  \"resultCount\": 0,  \"pagedResultsCookie\": null,\n  \"totalPagedResultsPolicy\": \"NONE\",\n  \"totalPagedResults\": -1,\n  \"remainingPagedResults\": -1\n}"));
		
		/** Session expired */
		
		StubHttp.whenHttp(OpenAMServer)
		.match(	Condition.post("/" + openAMConfig.getDeploymentPath() + "/json/sessions"),
				Condition.parameter("_action", "getTimeLeft"),
				Condition.parameter("tokenId", "token_test"),
				Condition.withHeader("Content-Type", "application/json"),
				Condition.withHeader(openAMConfig.getCookieName(), "token_test"))
		.then(	Action.ok(),
				Action.contentType("application/json;charset=UTF-8"),
				Action.stringContent("{\n \"maxtime\": 7500\n}"));
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
	public void stop(){
		LDAPServer.shutDown(true);
		OpenAMServer.stop();
		SOAPServer.stop();
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
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok1, headers), Token.class);
	
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
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
	    
		ResponseEntity<Token> resp = restTemplate.exchange("http://localhost:8080/tokens", HttpMethod.POST, new HttpEntity<>(tok1, headers), Token.class);
		
		assertEquals(resp.getStatusCode(), HttpStatus.OK);
		assertNotNull(resp.getBody().getToken());
		assertEquals(resp.getBody().getToken().length(), 6);
		
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
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tv.getSchema()).getChannelConfig(tv.getChannel().toString()).getMaxFailedAttempt() ; i++) {
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
			for(int i = 0 ; i < schemaListConfig.getSchemaItemConfig(tok.getSchema()).getChannelConfig(tok.getChannel().toString()).getMaxSuccessfulAttempt() ; i++) {
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
