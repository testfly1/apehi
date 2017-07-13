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

import com.axa.api.configuration.StubOpenAMConfig;
import com.axa.api.configuration.yml.OpenAMConfig;
import com.axa.api.model.response.api.User;
import com.axa.api.model.input.UserInput;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class AuthenticateTest {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private OpenAMConfig openAMConfig;
	
	private StubOpenAMConfig OpenAMServer;
	
	@Before
	public void start(){
		OpenAMServer = new StubOpenAMConfig(openAMConfig).start();
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
