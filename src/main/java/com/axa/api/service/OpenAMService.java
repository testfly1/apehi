package com.axa.api.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.axa.api.configuration.ApiLogger;
import com.axa.api.configuration.yml.SchemaItemConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.exception.InternalServerErrorException;
import com.axa.api.exception.InvalidAuthenticationException;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.exception.MultiResultException;
import com.axa.api.exception.NoUserResultException;
import com.axa.api.model.logging.EventLog;
import com.axa.api.model.response.api.User;
import com.axa.api.model.response.openam.AdminTokenResponse;
import com.axa.api.model.response.openam.RetrieveInfoResult;
import com.axa.api.model.response.openam.TokenCache;
import com.axa.api.model.input.UserInput;
import com.axa.api.model.dao.OpenAMDAO;
import com.axa.api.model.enumeration.ApiEvent;

/**
 * Service : Authentication and recovery of the user informations in the Active Directory
 *
 */
@Service
public class OpenAMService {

	@Autowired
	private OpenAMDAO openAMDAO;
	
	@Autowired
    private SchemaListConfig schemaListConfig;
	
	TokenCache tokenCache = new TokenCache();
	
	/**
	 * Assertion of the identity of the user by checking the its presence in the Active Directory
	 * Return the user informations if its login and password are correct and the user is found
	 * @param UserInput userInput
	 * @return user
	 * @throws MultiResultException
	 * @throws InvalidAuthenticationException
	 * @throws InternalServerErrorException
	 * @throws InvalidSchemaException
	 * @throws NoUserResultException
	 */
	public User authenticate(UserInput userInput) throws MultiResultException, InvalidAuthenticationException, InternalServerErrorException, InvalidSchemaException, NoUserResultException {

		SchemaItemConfig schemaItemConfig = schemaListConfig.getSchemaItemConfig(userInput.getSchema());
		
		//noscope do not work since scope is empty
		if (schemaItemConfig.getScope() != null && !schemaItemConfig.getScope().isEmpty()) {
			if (openAMDAO.authenticate(userInput.getLogin(), userInput.getPassword(), schemaItemConfig.getScope())) {
				try {
					User user = requestUserAttributes(userInput.getLogin(), schemaItemConfig.getScope());
					ApiLogger.log.info("{}", new EventLog("INFO", ApiEvent.AUTHENTICATE.getValue(), userInput.getWithoutPassword(), HttpStatus.OK.value(), null));
					return user;
				} 
				//there should be only one user found
				catch (MultiResultException ex) {
					ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.AUTHENTICATE.getValue(), userInput.getWithoutPassword(), HttpStatus.NOT_FOUND.value(), ex.getMessage()));
					throw new InternalServerErrorException("multiple users found in the same scope");
				} 
				//there should be at least one user found
				catch (NoUserResultException ex) {
					ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.AUTHENTICATE.getValue(), userInput.getWithoutPassword(), HttpStatus.NOT_FOUND.value(), null));
					throw new InternalServerErrorException("user not found");
				} 
				//unexpected error
				catch (InternalServerErrorException ex) {
					ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.AUTHENTICATE.getValue(), userInput.getWithoutPassword(), HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
					throw ex;
				}
			} 
			//login and password do not correspond
			else {
				ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.AUTHENTICATE.getValue(), userInput.getWithoutPassword(), HttpStatus.UNAUTHORIZED.value(), null));
				throw new InvalidAuthenticationException();
			}
		} else {
			ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.AUTHENTICATE.getValue(), userInput.getWithoutPassword(), HttpStatus.BAD_REQUEST.value(), "schema or scope invalid or not configured"));
			throw new InvalidSchemaException("schema or scope invalid or not configured");
		}	
	}
	
	/**
	 * Recover user attributes from database
	 * @param login
	 * @param scope
	 * @return
	 * @throws MultiResultException
	 * @throws NoUserResultException
	 * @throws InternalServerErrorException
	 */
	public User requestUserAttributes(String login, String scope) throws MultiResultException, NoUserResultException, InternalServerErrorException{
		
		AdminTokenResponse adminTokenResponse = new AdminTokenResponse();

		//If an admin token is still valid, do not request a new one
		if (tokenCache != null 
				&& tokenCache.getTokenId() != null 
				&& !tokenCache.getTokenId().isEmpty() 
				&& tokenCache.getSessionExpirate() > new Date().getTime() / 1000L) {
			adminTokenResponse.setTokenId(tokenCache.getTokenId());
		} else {
			adminTokenResponse = openAMDAO.getNewAdminToken();
			
			tokenCache.setTokenId(adminTokenResponse.getTokenId());
			tokenCache.setSessionExpirate(openAMDAO.getAdminTokenExpirationDate(tokenCache));
		}
		
		if (adminTokenResponse != null 
				&& adminTokenResponse.getTokenId() != null 
				&& !adminTokenResponse.getTokenId().isEmpty()) {
		
			RetrieveInfoResult userInfoResult = openAMDAO.getUserInfoResults(login, scope, adminTokenResponse);
			//There should be one and only one result for one user
			if (userInfoResult.getResultCount() == 1)
				return new User(userInfoResult.getResult().get(0));
			else if (userInfoResult.getResultCount() > 1)
				throw new MultiResultException();
			else 
				throw new NoUserResultException();			
			
		} else
			// Only execute this code if there was an error with admin authentication, which is an internal server error
			throw new InternalServerErrorException("Internal server error");
	}
}