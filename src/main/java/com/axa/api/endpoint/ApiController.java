package com.axa.api.endpoint;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axa.api.exception.InternalServerErrorException;
import com.axa.api.exception.InvalidAuthenticationException;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.exception.InvalidTokenException;
import com.axa.api.exception.LockedTokenException;
import com.axa.api.exception.MissingInformationChannelException;
import com.axa.api.exception.MultiResultException;
import com.axa.api.exception.NoUserResultException;
import com.axa.api.model.input.MinimalTokenInput;
import com.axa.api.model.input.TokenInput;
import com.axa.api.model.input.TokenValidation;
import com.axa.api.model.input.UserInput;
import com.axa.api.model.response.api.NoBodyResponse;
import com.axa.api.model.response.api.Status;
import com.axa.api.model.response.api.Token;
import com.axa.api.model.response.api.TokenObject;
import com.axa.api.model.response.api.User;
import com.axa.api.service.OpenAMService;
import com.axa.api.service.TokenService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller : According to the HTTP request received, it is forwarded and controlled by the Service Layer
 *
 */
@RestController
@Api
public class ApiController {
	
	@Autowired
	private OpenAMService amService;
	@Autowired
	private TokenService tokenService;
	
	/*
	 * Request : 
	 * 
	 * $ curl \
	 * --request POST \
	 * --header "Content-Type: application/json" \
	 * URL + /authenticate
	 * {	"login":login,	"password":password,	"schema" :schema	}
	 */
	/**
	 * Authentication of the user
	 * @param UserInput userInput
	 * @return user
	 * @throws MultiResultException
	 * @throws InvalidAuthenticationException
	 * @throws InternalServerErrorException
	 * @throws InvalidSchemaException
	 * @throws NoUserResultException
	 */
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Authenticate a user", 
    			  notes = "Authenticate a user using its login/password on a specific schema.\n\n"
    			  		+ "- \"schema\" input corresponds to a usecase or profile. It itself is linked to a scope which is a referential or subset of a referential, which must be declared and configured on backend side.\n\n"
    					+ "e.g : \"myAxaCh\" schema is linked to the scope \"AXA-CH-B2C\" in the b2c repository\n\n"
    			  		+ "User attributes (see response model) are returned if successful\n\n"
    					+ "Possible return codes : a 404 if the user is not found, a 401 if authentication failed, a 400 if input parameters are invalid or a 200 is authentication is successful",
    			  response = User.class,
    			  tags = { "Users" })
    public User authenticate(
    		@RequestBody @Valid UserInput userInput
    		) throws MultiResultException, InvalidAuthenticationException, InternalServerErrorException, InvalidSchemaException, NoUserResultException {
    	return amService.authenticate(userInput);
    }
	
	/*
	 * Request : 
	 * 
	 * $ curl \
	 * --request POST \
	 * --header "Content-Type: application/json" \
	 * URL + /tokens
	 * {  "userIdentifier": userIdentifier,	"channel": channel,	"schema": schema	}
	 */
	/**
	 * Generation of a Token
	 * @param TokenInput tokenInput
	 * @return token
	 * @throws InvalidSchemaException
	 * @throws LockedTokenException
	 * @throws MultiResultException
	 * @throws InternalServerErrorException
	 * @throws NoUserResultException
	 * @throws MissingInformationChannelException
	 */
    @RequestMapping(value = "/tokens", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Generate a token", 
    			  notes = "Generate a 6-numeric token for a single userIdentifier/channel/schema.\n\n"
    					+ "- \"userIdentifier\" corresponds to a unique identifier which can be a login (from b2c repository) or an opaque ID\n\n"
    					+ "- \"channel\" corresponds to the different communication channel to send the token (none, mail, voice, sms)\n\n"
    					+ "- \"schema\" corresponds to a usecase or profile. Each schema has a configurable scope, maxValidityTime, maxSuccessAttempt, maxFailedAttempt and lockTime value for each channel.\n\n"
    					+ "If the schema has a declared scope in its configuration, then the service will try to lookup the userIdentifier in the referential to pull the phoneNumber or email value\n\n"
    					+ "If the schema has no declared scope (e.g \"myAxaCh-noscope\"), then the service will use the input phone or email\n\n"
    					+ "Schemas must be declared and configured before service startup to be usable\n\n"
    			  		+ "Possible return codes : a 404 if the token is not found, a 401 if token is locked, a 400 if input parameters are invalid or a 200/201 if token is successfully created",
    			  response = Token.class,
    			  tags = { "Tokens" })
    public Token generate(
    		@RequestBody @Valid TokenInput tokenInput
    		) throws InvalidSchemaException, LockedTokenException, MultiResultException, InternalServerErrorException, NoUserResultException, MissingInformationChannelException {       	
    	return tokenService.generateToken(tokenInput);
    }

	/*
	 * Request : 
	 * 
	 * $ curl \
	 * --request GET \
	 * --header "Content-Type: application/json" \
	 * URL + /tokens?userIdentifier=userIdentifier&channel=channel&schema=schema
	 * {}
	 */
    /**
     * Recovery of token status
     * @param String userIdentifier
     * @param String channel
     * @param String schema
     * @return tokenObject
     * @throws EmptyResultDataAccessException
     */
    @RequestMapping(value = "/tokens", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "Display token status",
    			  notes = "Retrieve a token an display its status\n\n"
    					  + "Possible return codes : a 404 if the token is not found, a 400 if input parameters are invalid or a 200 if the token is found",
    			  tags = { "Tokens" })
    public TokenObject retrieve(
    		@RequestParam(value = "userIdentifier", required = true) String userIdentifier,
    		@RequestParam(value = "channel", required = true) String channel,
    		@RequestParam(value = "schema", required = true) String schema
    		) throws EmptyResultDataAccessException {	
    	return tokenService.getToken(userIdentifier, channel, schema);
    }
    
    /*
	 * Request : 
	 * 
	 * $ curl \
	 * --request DELETE \
	 * --header "Content-Type: application/json" \
	 * URL + /tokens
	 * {  "userIdentifier": userIdentifier,	"channel": channel,	"schema": schema	}
	 */
    /**
     * Deletion of a token
     * @param MinimalTokenInput minimalTokenInput
     * @return 
     * @throws LockedTokenException
     * @throws InvalidSchemaException
     */
    @RequestMapping(value = "/tokens", method = RequestMethod.DELETE, consumes = "application/json")
    @ApiOperation(value = "Delete a token",
    			  notes = "Delete a token (wipe from repository)\n\n"
    					  + "The endpoint bypasses the lockout feature.\n\n"
    					  + "Possible return codes : a 404 if the token is not found, a 400 if input parameters are invalid or a 204 if the token is deleted",
    			  response = NoBodyResponse.class,
    			  tags = { "Tokens" })
    public ResponseEntity<NoBodyResponse> delete(
    		@RequestBody @Valid MinimalTokenInput minimalTokenInput
    		) throws LockedTokenException, InvalidSchemaException {    	
    	return tokenService.deleteToken(minimalTokenInput, false);
    }
    
    /*
	 * Request : 
	 * 
	 * $ curl \
	 * --request POST \
	 * --header "Content-Type: application/json" \
	 * URL + /tokens/unlock
	 * {  "userIdentifier": userIdentifier,	"channel": channel,	"schema": schema	}
	 */
    /**
     * Unlocking of a token
     * @param MinimalTokenInput minimalTokenInput
     * @return status
     * @throws InvalidSchemaException
     */
    @RequestMapping(value = "/tokens/unlock", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Unlock a token",
    		      notes = "Unlock a token for a specific userIdentifier/channel/schema\n\n"
    		    		  + "Possible return codes : a 404 if the token is not found, a 400 if input parameters are invalid or a 200 if the token is unlocked",
    		      response = Status.class,
    		      tags = { "Tokens" })
    public Status unlock(
    		@RequestBody @Valid MinimalTokenInput minimalTokenInput
    		) throws InvalidSchemaException {	
    	return tokenService.unlockToken(minimalTokenInput);
    }
    
    /*
	 * Request : 
	 * 
	 * $ curl \
	 * --request POST \
	 * --header "Content-Type: application/json" \
	 * URL + /tokens/unlock
	 * {  "userIdentifier": userIdentifier,	"channel": channel,	"schema": schema,	"code": code	}
	 */
    /**
     * Validation of a token
     * @param TokenValidation tokenValidation
     * @return status
     * @throws LockedTokenException
     * @throws InvalidTokenException
     * @throws InvalidSchemaException
     */
    @RequestMapping(value = "/tokens/validate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Validate a token", 
    			  notes = "Validate a token for a specific userIdentifier/channel/schema\n\n"
    					  + "Possible return codes : a 404 if the token is not found, a 401 if token is locked, a 403 if the code is not valid, a 400 if input parameters are invalid or a 200 if it is valid",
    			  response = Status.class,
    			  tags = { "Tokens" })
    public Status validate(
    		@RequestBody @Valid TokenValidation tokenValidation
    		) throws LockedTokenException, InvalidTokenException, InvalidSchemaException {
    	return tokenService.validateToken(tokenValidation);
    }
}