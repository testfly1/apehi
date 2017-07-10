package com.axa.api.model.dao;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.axa.api.configuration.yml.OpenAMConfig;
import com.axa.api.model.response.openam.AdminTokenResponse;
import com.axa.api.model.response.openam.AuthenticationResponse;
import com.axa.api.model.response.openam.RetrieveInfoResult;
import com.axa.api.model.response.openam.RetrieveMaxTime;
import com.axa.api.model.response.openam.TokenCache;

/**
 * Model : Data Access Object for exchanges with OpenAM
 * 
 */
@Repository
public class OpenAMDAO {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private OpenAMConfig openAMConfig;
	
	/**
	 * Assertion of the identity of a user and its presence in the database
	 * @param String login
	 * @param String password
	 * @param String scope
	 * @return boolean
	 */
	public Boolean authenticate(String login, String password, String scope) {
	
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		
		headers.add("Content-Type","application/json");
		headers.add("X-OpenAM-Username", login);
		headers.add("X-OpenAM-Password", password);
		try {
			
			/*
			 * Request : 
			 * 
			 * $ curl \
			 * --request POST \
			 * --header "X-OpenAM-Username: login" \
			 * --header "X-OpenAM-Password: password" \
			 * --header "Content-Type: application/json" \
			 * openAMConfig.getUserAuthenticationUrl(scope)
			 * {}
			 */
			
			AuthenticationResponse authenticationResponse = restTemplate.postForObject(openAMConfig.getUserAuthenticationUrl(scope), new HttpEntity<String>(headers), AuthenticationResponse.class);
			
			return (authenticationResponse != null
					&& !authenticationResponse.getMessage().isEmpty()
					&& authenticationResponse.getMessage().equals("Authentication Successful"));
		} catch (HttpClientErrorException e) {
			// Here maybe should switch client error exception to catch 500 and 400
			return false;
		}
	}
	
	/**
	 * Recovery of a new Admin Token needed to retrieve user informations
	 * @return adminTokenResponse
	 */
	public AdminTokenResponse getNewAdminToken(){
		
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		
		headers.add("Content-Type","application/json");
		headers.add("X-OpenAM-Username", openAMConfig.getAdminUser());
		headers.add("X-OpenAM-Password", openAMConfig.getAdminPwd());
		
		/*
		 * Request : 
		 * 
		 * $ curl \
		 * --request POST \
		 * --header "X-OpenAM-Username: openAMConfig.getAdminUser()" \
		 * --header "X-OpenAM-Password: openAMConfig.getAdminPwd()" \
		 * --header "Content-Type: application/json" \
		 * openAMConfig.getAdminAuthenticationUrl(scope)
		 * {}
		 */
	
		return restTemplate.postForObject(openAMConfig.getAdminAuthenticationUrl(), new HttpEntity<String>(headers), AdminTokenResponse.class);
	}
	
	/**
	 * Recovery of a new admin token maximum validity time and return the session expiration date
	 * @return sessionExpiration
	 */
	public Long getAdminTokenExpirationDate(TokenCache tokenCache){
		
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		
		headers.add("Content-Type","application/json");
		headers.add(openAMConfig.getCookieName(), tokenCache.getTokenId());
		

		/*
		 * Request : 
		 * 
		 * $ curl \
		 * --request POST \
		 * --header "openAMConfig.getCookieName(): tokenCache.getTokenId()" \
		 * --header "Content-Type: application/json" \
		 * getSessionExpirationUrl(tokenCache.getTokenId())
		 * {}
		 */
		
		return new Date().getTime() / 1000L + restTemplate.postForObject(openAMConfig.getSessionExpirationUrl(tokenCache.getTokenId()), new HttpEntity<String>(headers), RetrieveMaxTime.class).getMaxtime();
	}
	
	/**
	 * Recovery of the user informations request
	 * @param String login
	 * @param String scope
	 * @param AdminTokenResponse adminTokenResponse
	 * @return retrieveInfoResult
	 */
	public RetrieveInfoResult getUserInfoResults(String login, String scope, AdminTokenResponse adminTokenResponse){
		
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		
		headers.add("Content-Type","application/json");
		headers.add(openAMConfig.getCookieName(), adminTokenResponse.getTokenId());
		
		/*
		 * Request : 
		 * 
		 * $ curl \
		 * --request GET \
		 * --header "openAMConfig.getCookieName(): tokenCache.getTokenId()" \
		 * --header "Content-Type: application/json" \
		 * getUserAttributesUrl(login,scope)
		 * {}
		 */
		
		return restTemplate.exchange(openAMConfig.getUserAttributesUrl(login,scope), HttpMethod.GET, new HttpEntity<String>(headers), RetrieveInfoResult.class).getBody();
	}
}