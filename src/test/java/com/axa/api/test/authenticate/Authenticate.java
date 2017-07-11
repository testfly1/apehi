package com.axa.api.test.authenticate;

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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.axa.api.configuration.yml.OpenAMConfig;
import com.axa.api.model.response.api.User;
import com.axa.api.model.input.UserInput;
import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.semantics.Action;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class Authenticate {
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	OpenAMConfig openAMConfig;
	
	private StubServer OpenAMServer;
	
	@Before
	public void start(){
		startStubOpenAM();
	}
	
	private void startStubOpenAM(){
		
		OpenAMServer = new StubServer(Integer.parseInt(openAMConfig.getPort())).run();
		
		/*
		 * AuthenticateUser
		 */
		
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
		
		/*
		 * GetAdminToken
		 */
		
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
		
		
		/*
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

		/*
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
		
		/*
		 * Session expired
		 */
		
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
	
	@After
	public void stop(){
		OpenAMServer.stop();
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
			restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput, headers), User.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput2, headers), User.class);
		} catch (HttpClientErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput3, headers), User.class);
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
			restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput, headers), User.class);
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
			restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput, headers), User.class);
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
			restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, new HttpEntity<>(userInput, headers), User.class);
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
	    	restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, he, User.class);
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
	    	restTemplate.exchange("http://localhost:8080/authenticate", HttpMethod.POST, he, User.class);
	    } catch (HttpServerErrorException ex) {
			assertEquals(ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
