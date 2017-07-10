package com.axa.api.exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.axa.api.configuration.ApiLogger;
import com.axa.api.model.logging.EventLog;
import com.axa.api.model.response.api.Error;
import com.axa.api.model.enumeration.ApiEvent;

/**
 * Generation of the messages to return when an exception is thrown and generation of log messages for errors
 *
 */
@ControllerAdvice
public class ExceptionControllerAdvice {
	
	/*
	 * Does not work because InputStream is closed (already read by Spring ?)
	 
	 private Object getInputBody(HttpServletRequest hsr) {
	 
		ObjectMapper mapper = new ObjectMapper();
		try {
			switch (hsr.getRequestURI()) {
				case "/tokens":
					if (hsr.getMethod().equals("GET"))
						return new MinimalTokenInputLogging(hsr.getParameter("userIdentifier"), hsr.getParameter("channel"), hsr.getParameter("schema"));
				default:
					String str = mapper.readValue(hsr.getInputStream(), String.class);
					JSONObject jo = new JSONObject(str);
					if (jo.has("password"))
						jo.remove("password");
					return jo;
			}
		} catch (JsonParseException e) {
			return null;
		} catch (JsonMappingException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (JSONException e) {
			return null;
		}
	} */
	
	/**
	 * Generation of an ErrorLog
	 * @param status
	 * @param msg
	 */
	private void generateErrorLog(HttpStatus status, Object msg) {
		ServletRequestAttributes servlet = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = servlet.getRequest();
		
		ApiLogger.log.error("{}", new EventLog("ERROR", getCurrentCalledUri(request), null, status.value(), msg));	
	}
	
	/**
	 * Recovery of action asked by the user
	 * @param hsr
	 * @return currentCalledUri
	 */
	private String getCurrentCalledUri(HttpServletRequest hsr) {
		switch (hsr.getRequestURI()) {
			case "/authenticate":
				return ApiEvent.AUTHENTICATE.getValue();
			case "/tokens":
				if (hsr.getMethod().equals("GET"))
					return ApiEvent.GETTOKEN.getValue();
				if (hsr.getMethod().equals("DELETE"))
					return ApiEvent.DELETETOKEN.getValue();
				if (hsr.getMethod().equals("POST"))
					return ApiEvent.GENERATETOKEN.getValue();
				break;
			case "/tokens/validate":
				return ApiEvent.VALIDATETOKEN.getValue();
			case "/tokens/unlock":
				return ApiEvent.UNLOCKTOKEN.getValue();
		}
		return null;
	}
	
	/*
	 * 
	 */
	@ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
	public ResponseEntity<Error> handleModelValidation(org.springframework.web.bind.MethodArgumentNotValidException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.BAD_REQUEST.value());
		
		List<ObjectError> errors = ex.getBindingResult().getAllErrors();
		
		String[] array = new String[errors.size()];
		int index = 0;
		for (ObjectError item : errors) {
		  array[index] = item.getDefaultMessage();
		  index++;
		}

		msg.setMessage(array);
		
		generateErrorLog(HttpStatus.BAD_REQUEST, msg);
	
		return new ResponseEntity<Error>(msg, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(org.springframework.dao.EmptyResultDataAccessException.class)
	public ResponseEntity<Error> handleLdapFindOne(org.springframework.dao.EmptyResultDataAccessException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.NOT_FOUND.value());
		msg.setMessage(new String[] { "token could not be found" });
		
		//generateErrorLog(HttpStatus.BAD_REQUEST, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(LockedTokenException.class)
	public ResponseEntity<Error> handleLockedTokens(LockedTokenException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.FORBIDDEN.value());
		msg.setMessage(new String[] { "token is locked" });
		
		//generateErrorLog(HttpStatus.FORBIDDEN, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(InvalidAuthenticationException.class)
	public ResponseEntity<Error> handleInvalidAuthentication(InvalidAuthenticationException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.UNAUTHORIZED.value());
		msg.setMessage(new String[] { "authentication failed" });
		
		//generateErrorLog(HttpStatus.UNAUTHORIZED, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<Error> handleInvalidTokens(InvalidTokenException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.UNAUTHORIZED.value());
		msg.setMessage(new String[] { "code is invalid" });
		
		//generateErrorLog(HttpStatus.UNAUTHORIZED, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(InvalidSchemaException.class)
	public ResponseEntity<Error> handleInvalidSchema(InvalidSchemaException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.BAD_REQUEST.value());
		msg.setMessage(new String[] { ex.getMessage() });
		
		//generateErrorLog(HttpStatus.BAD_REQUEST, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Error> handleWrongEnum(HttpMessageNotReadableException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.BAD_REQUEST.value());
		msg.setMessage(new String[] { "channel does not exist (none/voice/sms/mail)" });
		
		//generateErrorLog(HttpStatus.BAD_REQUEST, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(InternalServerErrorException.class)
	public ResponseEntity<Error> handleServerError(InternalServerErrorException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		msg.setMessage(new String[] { ex.getMessage() });
		
		//generateErrorLog(HttpStatus.INTERNAL_SERVER_ERROR, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(NoUserResultException.class)
	public ResponseEntity<Error> handleUserNotFound(NoUserResultException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.NOT_FOUND.value());
		msg.setMessage(new String[] { "user could not be found" });
		
		//generateErrorLog(HttpStatus.NOT_FOUND, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(MissingInformationChannelException.class)
	public ResponseEntity<Error> handleMissingInformationChannel(MissingInformationChannelException ex) {
		Error msg = new Error();
		msg.setCode(HttpStatus.BAD_REQUEST.value());
		msg.setMessage(new String[] { "channel information could not be found (phone number for channel sms/voice or mail for channel mail" });
		
		//generateErrorLog(HttpStatus.BAD_REQUEST, msg);
		
		return new ResponseEntity<Error>(msg, HttpStatus.BAD_REQUEST);
	}

	/* To uncomment before production
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorMessage> handleLdapFindOne(Exception ex) {
		ErrorMessage msg = new ErrorMessage();
		msg.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		msg.setMessage("Internal server error");
		
		generateErrorLog(HttpStatus.INTERNAL_SERVER_ERROR, msg);
		
		return new ResponseEntity<ErrorMessage>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
	}*/
}