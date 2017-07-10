package com.axa.api.model.dao;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.springframework.stereotype.Repository;

import com.axa.api.exception.InvalidSchemaException;

/**
 * Model : Data Access Object for exchanges with SOAP server
 * 
 */
@Repository
public class SoapDAO {
	
	/**
	 * Send the SOAP request created to the SOAP server
	 * @param SOAPMessage request
	 * @param String url
	 * @throws SOAPException
	 * @throws InvalidSchemaException
	 */
	public void sendSoapRequest(SOAPMessage request, String url) throws SOAPException, InvalidSchemaException {
				
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory.createConnection();
		
		/*
		 * Request : 
		 * 
		 * $ curl \
		 * --request POST \
		 * --header "Authorization: Basic Base64.encode(soapConfig.getLogin():soapConfig.getPassword())" \
		 * --header "Content-Type: text/xml" \
		 * openAMConfig.getUserAuthenticationUrl(scope)
		 * {<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://services.soap.ws.messageservice.iam.axa.com/"><SOAP-ENV:Header/><SOAP-ENV:Body><ser:messages><name><additionalAttributes>additionalAttributes</additionalAttributes><application>application</application><destination>destination</destination><language>language</language><message>message</message><messageType>messageType</messageType><organization>organization</organization><sender>sender</sender></name></ser:messages></SOAP-ENV:Body></SOAP-ENV:Envelope>}
		 */
		
		soapConnection.call(request, url);
	}
}
