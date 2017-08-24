package com.axa.api.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

import javax.xml.soap.SOAPException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.axa.api.configuration.ApiLogger;
import com.axa.api.configuration.yml.ChannelConfig;
import com.axa.api.configuration.yml.SchemaItemConfig;
import com.axa.api.configuration.yml.SchemaListConfig;
import com.axa.api.exception.InternalServerErrorException;
import com.axa.api.exception.InvalidSchemaException;
import com.axa.api.exception.InvalidTokenException;
import com.axa.api.exception.LockedTokenException;
import com.axa.api.exception.MissingInformationChannelException;
import com.axa.api.exception.MultiResultException;
import com.axa.api.exception.NoUserResultException;
import com.axa.api.model.logging.EventLog;
import com.axa.api.model.dao.TokenDAO;
import com.axa.api.model.enumeration.ApiEvent;
import com.axa.api.model.enumeration.ChannelEnum;
import com.axa.api.model.input.MinimalTokenInput;
import com.axa.api.model.input.TokenInput;
import com.axa.api.model.input.TokenValidation;
import com.axa.api.model.logging.MinimalTokenInputLogging;
import com.axa.api.model.response.api.NoBodyResponse;
import com.axa.api.model.response.api.Status;
import com.axa.api.model.response.api.Token;
import com.axa.api.model.response.api.TokenObject;
import com.axa.api.model.response.api.User;
import com.google.common.hash.Hashing;

/**
 * Service : Actions about the token (generation, recovery, deletion, validation, unlocking)
 *
 */
@Service
public class TokenService {
	
	@Autowired
	private SchemaListConfig schemaListConfig;
	
	@Autowired
	private TokenDAO tokenDAO;
	
	@Autowired
	private OpenAMService openAMService;
	
    @Autowired
    private SoapService soapService;
	
    @Autowired
    private SecureRandom secureRandom;
    
    /**
     * Extraction of informations from MinimalTokenInput to strings to send to the getToken method
     * @param MinimalTokenInput minimalTokenInput
     * @return tokenObject
     */
    private TokenObject tryRetrieveToken(MinimalTokenInput minimalTokenInput) {
		return getToken(minimalTokenInput.getUserIdentifier(), minimalTokenInput.getChannel().toString(), minimalTokenInput.getSchema());
    }
    
    /**
     * Recovery of stored token informations
     * @param String userIdentifier
     * @param String channel
     * @param String schema
     * @return tokenObject
     */
    public TokenObject getToken(
    		String userIdentifier,
    		String channel,
    		String schema) {
    	TokenObject tokenObject = null;
    	
    	try {
    		tokenObject = tokenDAO.get(userIdentifier, channel, schema);
    		ApiLogger.log.info("{}", new EventLog("INFO", ApiEvent.GETTOKEN.getValue(), new MinimalTokenInputLogging(userIdentifier, channel, schema), HttpStatus.OK.value(), null));
    		return tokenObject;
    	} 
    	//token not found
    	catch (org.springframework.dao.EmptyResultDataAccessException e){
    		ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.GETTOKEN.getValue(), new MinimalTokenInputLogging(userIdentifier, channel, schema), HttpStatus.NOT_FOUND.value(), null));
    		throw e;
    	}
   	}
    
    /**
     * Generation of a new token
     * The token is stored in the LDAP
     * If existing and not-locked token exists, it is overwritten
     * The token is then sent to the user by the adequate channel (API or SOAP server)
     * @param TokenInput tokenInput
     * @return token
     * @throws InvalidSchemaException
     * @throws LockedTokenException
     * @throws MultiResultException
     * @throws InternalServerErrorException
     * @throws NoUserResultException
     * @throws MissingInformationChannelException
     */
	public Token generateToken(TokenInput tokenInput) throws InvalidSchemaException, LockedTokenException, MultiResultException, InternalServerErrorException, NoUserResultException, MissingInformationChannelException {
		
		String code = randomCode();
		
		//try to delete existing token
		try { 
			deleteToken(tokenInput, true);	
		} catch (EmptyResultDataAccessException ex) {
		//if token does not exist, do nothing
		}
		
		SchemaItemConfig schemaItemConfig = null;
		
		//get the configurations of the schema
		try {
			schemaItemConfig = schemaListConfig.getSchemaItemConfig(tokenInput.getSchema());
		} catch (InvalidSchemaException e) {
			ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.GENERATETOKEN.getValue(), tokenInput, HttpStatus.BAD_REQUEST.value(), null));
			throw e;
		}
		
		//change tokenInput informations if a corresponding user is retrieved
		refreshTokenInformations(tokenInput, schemaItemConfig);
		
		//create the token in the LDAP
		createToken(schemaItemConfig, tokenInput, code);
		
		//if channel is "none", print code in output token
		if (tokenInput.getChannel().equals(ChannelEnum.none)){
			ApiLogger.log.info("{}", new EventLog("INFO", ApiEvent.GENERATETOKEN.getValue(), tokenInput, HttpStatus.OK.value(), null));
			return new Token(code, 200, "OTP generated");
		}
		//else send code to the proper channel and hide code
		else { 
			try {
				if (!soapService.isBypassedSendingToken())
					soapService.generateSOAPRequest(tokenInput, code);
				ApiLogger.log.info("{}", new EventLog("INFO", ApiEvent.GENERATETOKEN.getValue(), tokenInput, HttpStatus.OK.value(), null));
				return new Token(null, 200 , "OTP generated & sent through desired channel");
			} catch (SOAPException e) {
				tokenDAO.delete(tryRetrieveToken(tokenInput));
				throw new InternalServerErrorException("Unable to connect with SOAP server");
			}
		}
	}
	
	/**
	 * Deletion of a token if it is not still locked
	 * @param MinimalTokenInput minimalTokenInput
	 * @param boolean lockValidation
	 * @return 
	 * @throws LockedTokenException
	 * @throws InvalidSchemaException
	 */
	public ResponseEntity<NoBodyResponse> deleteToken(MinimalTokenInput minimalTokenInput, boolean lockValidation) throws LockedTokenException, InvalidSchemaException {
		TokenObject tokenObject = tryRetrieveToken(minimalTokenInput);

		if (tokenObject != null) {
			// if lockValidation is false (lock must not be checked) then delete without checking for lock status
			if (lockValidation && isLocked(tokenObject)) {
				ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.DELETETOKEN.getValue(), minimalTokenInput, HttpStatus.FORBIDDEN.value(), null));
				throw new LockedTokenException();
			}
			// else delete
			tokenDAO.delete(tokenObject);
			ApiLogger.log.info("{}", new EventLog("INFO", ApiEvent.DELETETOKEN.getValue(), minimalTokenInput, HttpStatus.OK.value(), null));
		}
		
		return new ResponseEntity<NoBodyResponse>(new NoBodyResponse(), HttpStatus.NO_CONTENT);
	}
	
	/**
	 * Validation of a token if code correspond, token is valid and not locked
	 * @param TokenValidation tokenValidation
	 * @return status
	 * @throws LockedTokenException
	 * @throws InvalidTokenException
	 * @throws InvalidSchemaException
	 */
	public Status validateToken(TokenValidation tokenValidation) throws LockedTokenException, InvalidTokenException, InvalidSchemaException {
		TokenObject tokenObject = tryRetrieveToken(tokenValidation);
		Status status = null;
		
		if (tokenObject != null) {
			if (!isLocked(tokenObject)) {
				if (processValidation(tokenObject, tokenValidation.getCode())) {
					status = new Status(200, "Valid");
					ApiLogger.log.info("{}", new EventLog("INFO", ApiEvent.VALIDATETOKEN.getValue(), tokenValidation, HttpStatus.OK.value(), null));
					return status;
				} else {
					ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.VALIDATETOKEN.getValue(), tokenValidation, HttpStatus.UNAUTHORIZED.value(), null));
					throw new InvalidTokenException();
				}
			} else {
				ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.VALIDATETOKEN.getValue(), tokenValidation, HttpStatus.FORBIDDEN.value(), null));
				throw new LockedTokenException();
			}
		}
		// A token was found but is null case should not happen...
		return status;
	}
	
	/**
	 * Unlocking an existing locked token
	 * @param MinimalTokenInput minimalTokenInput
	 * @return status
	 * @throws InvalidSchemaException
	 */
	public Status unlockToken(MinimalTokenInput minimalTokenInput) throws InvalidSchemaException {

		TokenObject tokenObject = tryRetrieveToken(minimalTokenInput);
		
		if (tokenObject != null) {
			if (isLocked(tokenObject)) {
				if (tokenObject.getSuccessfulAttempt() >= 1)
					tokenObject.setStatus("validated");
				else
					tokenObject.setStatus("generated");
				tokenObject.setFailedAttempt(0);
				tokenObject.setLockedTime(null);
				tokenDAO.update(tokenObject);
				ApiLogger.log.info("{}", new EventLog("INFO", ApiEvent.UNLOCKTOKEN.getValue(), minimalTokenInput, HttpStatus.OK.value(), null));
			}
			else
				ApiLogger.log.info("{}", new EventLog("INFO", ApiEvent.UNLOCKTOKEN.getValue(), minimalTokenInput, HttpStatus.OK.value(), "Token not locked"));
		}
		else {
			ApiLogger.log.error("{}", new EventLog("ERROR", ApiEvent.UNLOCKTOKEN.getValue(), minimalTokenInput, HttpStatus.NOT_FOUND.value(), "Token could not be found"));
		}
		// Return unlocked in any case
		return new Status(200, "Token unlocked");
	}
	
	/**
	 * Modify the channel informations of the input from the corresponding user informations
	 * @param TokenInput oldTokenInput
	 * @return tokenInput
	 * @throws InvalidSchemaException
	 * @throws MultiResultException
	 * @throws InternalServerErrorException
	 * @throws NoUserResultException
	 */
	private TokenInput refreshTokenInformations(TokenInput oldTokenInput, SchemaItemConfig schemaItemConfig) throws InvalidSchemaException, MultiResultException, InternalServerErrorException, NoUserResultException{
		
		TokenInput tokenInput = oldTokenInput;
		
		User user = null;
		
		//here query openam to retrieve phone or email from input.userIdentity and sic.getScope
		if (schemaItemConfig.getScope() != null && !schemaItemConfig.getScope().isEmpty()) {
			user = openAMService.requestUserAttributes(tokenInput.getUserIdentifier(), schemaItemConfig.getScope());
		}

		//if user is retrieved from scope, its informations erase the inputs
		if (user != null) {
			if (user.getLogin() != null && !user.getLogin().isEmpty()) 
				tokenInput.setUserIdentifier(user.getLogin());
			if (user.getMail() != null && !user.getMail().isEmpty()) 
				tokenInput.setMail(user.getMail());
			if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) 
				tokenInput.setPhone(user.getPhoneNumber());
		}
		
		return tokenInput;
	}
	
	/**
	 * Creation of a new token
	 * From the informations stored in Active Directory if user exists
	 * From the informations sent to the API otherwise
	 * @param SchemaItemConfig schemaItemConfig
	 * @param TokenInput tokenInput
	 * @param Code code
	 * @throws InvalidSchemaException
	 * @throws MultiResultException
	 * @throws InternalServerErrorException
	 * @throws NoUserResultException
	 * @throws MissingInformationChannelException
	 */
	private void createToken(SchemaItemConfig schemaItemConfig, TokenInput tokenInput, String code) throws InvalidSchemaException, MultiResultException, InternalServerErrorException, NoUserResultException, MissingInformationChannelException{
		
		ChannelConfig channelConfig = schemaItemConfig.getChannelConfig(tokenInput.getChannel().toString());
		
		switch (tokenInput.getChannel()) {
			case none : 
				tokenDAO.create(new TokenObject(generateUidFromToken(tokenInput), tokenInput.getUserIdentifier(), tokenInput.getPhone(), tokenInput.getMail(), getCurrentDate(), hash(code, tokenInput.getUserIdentifier()), ChannelEnum.none.toString(), "generated", channelConfig.getMaxValidityTime(), 0, channelConfig.getMaxFailedAttempt(), 0, channelConfig.getMaxSuccessfulAttempt(), tokenInput.getSchema(), channelConfig.getLockDuration()));
				break;
			case voice :
				if (tokenInput.getPhone() != null && !tokenInput.getPhone().isEmpty())
					tokenDAO.create(new TokenObject(generateUidFromToken(tokenInput), tokenInput.getUserIdentifier(), tokenInput.getPhone(), tokenInput.getMail(), getCurrentDate(), hash(code, tokenInput.getUserIdentifier()), ChannelEnum.voice.toString(), "generated", channelConfig.getMaxValidityTime(), 0, channelConfig.getMaxFailedAttempt(), 0, channelConfig.getMaxSuccessfulAttempt(), tokenInput.getSchema(), channelConfig.getLockDuration()));
				else
					throw new MissingInformationChannelException();
				break;
			case sms : 
				if (tokenInput.getPhone() != null && !tokenInput.getPhone().isEmpty())
					tokenDAO.create(new TokenObject(generateUidFromToken(tokenInput), tokenInput.getUserIdentifier(), tokenInput.getPhone(), tokenInput.getMail(), getCurrentDate(), hash(code, tokenInput.getUserIdentifier()), ChannelEnum.sms.toString(), "generated", channelConfig.getMaxValidityTime(), 0, channelConfig.getMaxFailedAttempt(), 0, channelConfig.getMaxSuccessfulAttempt(), tokenInput.getSchema(), channelConfig.getLockDuration()));
				else 
					throw new MissingInformationChannelException();
				break;
			case mail :
				if (tokenInput.getMail() != null && !tokenInput.getMail().isEmpty())
					tokenDAO.create(new TokenObject(generateUidFromToken(tokenInput), tokenInput.getUserIdentifier(), tokenInput.getPhone(), tokenInput.getMail(), getCurrentDate(), hash(code, tokenInput.getUserIdentifier()), ChannelEnum.mail.toString(), "generated", channelConfig.getMaxValidityTime(), 0, channelConfig.getMaxFailedAttempt(), 0, channelConfig.getMaxSuccessfulAttempt(), tokenInput.getSchema(), channelConfig.getLockDuration()));	
				else 
					throw new MissingInformationChannelException();
				break;
			default : 
				break; //should not happen 
		}
	}
	
	/**
	 * Validation of a valid token
	 * if code corresponds to token : token is deleted if successfulAttempt + 1 >= maxSuccessfulAttempt, token is validated otherwise (return true)
	 * if code doesn't correspond to token : token is locked if failedAttempt + 1 >= maxFailedAttempt, token status is failed otherwise (return false)
	 * @param TokenObject tokenObject
	 * @param String code
	 * @return boolean
	 */
	private boolean processValidation(TokenObject tokenObject, String code) {
		
		///Code considered invalid with one failedAttempt even if right
		if (isValid(hash(code, tokenObject.getUserIdentifier()), tokenObject.getCode(), tokenObject.getGenerationTime(), tokenObject.getValidityTime())) {
			// Code is OK return OK
			// special case, -1 = infinite successful attempt, do not delete
			if ((tokenObject.getMaxSuccessfulAttempt() != -1) && (tokenObject.getSuccessfulAttempt() + 1 >= tokenObject.getMaxSuccessfulAttempt())) {
				// delete token because consumed
				tokenDAO.delete(tokenObject);
			} else {
				tokenObject.setSuccessfulAttempt(tokenObject.getSuccessfulAttempt() + 1);
				tokenObject.setStatus("validated");
				tokenObject.setFailedAttempt(0);
				tokenDAO.update(tokenObject);
			}
			return true;
		} else {
			// Code is not OK
			// special case, -1 = infinite failure attempt, do not lock
			if ((tokenObject.getMaxFailedAttempt() != -1) && (tokenObject.getFailedAttempt() + 1 >= tokenObject.getMaxFailedAttempt())) {
				// lock token because too many failure
				tokenObject.setFailedAttempt(tokenObject.getMaxFailedAttempt());
				tokenObject.setStatus("locked");
				tokenObject.setLockedTime(getCurrentDate().toString());
				tokenDAO.update(tokenObject);
			} else {
				tokenObject.setFailedAttempt(tokenObject.getFailedAttempt() + 1);
				tokenObject.setStatus("failed");
				tokenDAO.update(tokenObject);
			}
			return false;
		}
	}
	
	/**
	 * Assertion of validity of a token
	 * If code and storedCode are the same and storedCode is still valid, return true
	 * Otherwise, return false
	 * @param String code
	 * @param String storedCode
	 * @param Long storedDate
	 * @param Integer storedValidity
	 * @return boolean
	 */
	private boolean isValid(String code, String storedCode, Long storedDate, Integer storedValidity) {
		if (code != null 
				&& storedCode != null 
				&& storedCode.equals(code)) {
			// Code is OK
			if (storedDate != null && storedValidity != null) {
				if (storedValidity == -1) {
					// special case if validity is -1, then infinite duration
					return true;
				}
				if (getCurrentDate() <= (storedDate + Long.valueOf(storedValidity))) {
					// Date is in the window, before end of validity
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Assertion of locked token status
	 * If token is locked and its lockDuration is not overrun, return true
	 * Otherwise return false
	 * @param TokenObject tokenObject
	 * @return Boolean b
	 * @throws InvalidSchemaException
	 */
	private boolean isLocked(TokenObject tokenObject) throws InvalidSchemaException {
		// Check if the status is locked first
		if (tokenObject.getStatus() != null && tokenObject.getStatus().equals("locked")) {
			// In case it is locked, is it still locked (current time is > to lockedtime + duration)
			if (tokenObject.getLockedTime() != null 
					&& !tokenObject.getLockedTime().isEmpty() 
					&& tokenObject.getLockDuration() != null) {
				if ((tokenObject.getLockDuration().equals(-1)) || (getCurrentDate() < (Long.parseLong(tokenObject.getLockedTime()) + Long.valueOf(tokenObject.getLockDuration())))) { //Special case lockDuration infinite
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Recovery the current date
	 * @return currentDate
	 */
	private Long getCurrentDate() {
    	return new Date().getTime() / 1000L;
    }
	
	/**
	 * Hashing the code to store in the LDAP and the code received before comparison to the one stored
	 * @param String code
	 * @param String userIdentifier
	 * @return hashCode
	 */
	private String hash(String code, String userIdentifier){
		return Hashing.sha256().hashString(code.concat(userIdentifier), StandardCharsets.UTF_8).toString();	
	}
	
	/**
	 * Generation of a random 6 digit OTP
	 * @return randomCode
	 */
	private String randomCode() {       
		return String.format("%06d", secureRandom.nextInt(1000000));
	}
	
	/**
	 * Concatenation of userIdentifier + channel + schema
	 * @param TokenInput object
	 * @return String
	 */
	private String generateUidFromToken(TokenInput tok) {       
		return java.util.UUID.nameUUIDFromBytes((tok.getUserIdentifier() + tok.getChannel().toString() + tok.getSchema()).getBytes()).toString();
	}
}